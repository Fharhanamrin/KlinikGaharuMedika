package com.release.klinikgaharumedika.view.panel;

import com.release.klinikgaharumedika.controller.ObatController;
import com.release.klinikgaharumedika.model.Obat;
import com.release.klinikgaharumedika.view.DashboardForm;
import java.beans.Beans;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicButtonUI;

public class ObatFormPanel extends javax.swing.JPanel {

    private final ObatController controller;
    private final Obat editingObat;

    public ObatFormPanel() {
        this(null);
    }

    public ObatFormPanel(Obat editingObat) {
        this.editingObat = editingObat;
        initComponents();
        if (Beans.isDesignTime()) {
            controller = null;
            configureForm();
            return;
        }
        controller = new ObatController();
        configureForm();
    }

    private void configureForm() {
        txtNamaObat.putClientProperty("JTextField.placeholderText", "Contoh: Paracetamol 500 mg");
        txtKandungan.putClientProperty("JTextField.placeholderText", "Contoh: 500 mg");
        txtKodeObat.putClientProperty("JTextField.placeholderText", "Contoh: OB001 atau kosongkan otomatis");
        txtLokasiRak.putClientProperty("JTextField.placeholderText", "Contoh: A1");
        txtHargaBeli.putClientProperty("JTextField.placeholderText", "Contoh: 8500");
        txtHargaJual.putClientProperty("JTextField.placeholderText", "Contoh: 14500");
        txtStokAwal.putClientProperty("JTextField.placeholderText", "Contoh: 98");
        txtStokMinimum.putClientProperty("JTextField.placeholderText", "Contoh: 10");
        txtSupplier.putClientProperty("JTextField.placeholderText", "Contoh: PT Sehat Sentosa");
        txtKeterangan.putClientProperty("JTextArea.placeholderText", "Contoh: Simpan di suhu ruang");
        txtKeterangan.setLineWrap(true);
        txtKeterangan.setWrapStyleWord(true);

        FormUiStyle.dateSpinner(spnKadaluarsa);

        stylePrimaryButton(btnSimpanData);
        applyMode();
        FormUiStyle.applyFormStyle(this);
    }

    private void applyMode() {
        if (editingObat == null) {
            lblFormTitle.setText("Tambah Obat");
            btnSimpanData.setText("Simpan Data");
            cmbJenis.setSelectedIndex(0);
            cmbSatuan.setSelectedIndex(0);
            return;
        }

        lblFormTitle.setText("Edit Obat");
        btnSimpanData.setText("Simpan Perubahan");
        txtNamaObat.setText(editingObat.getNamaObat());
        txtKandungan.setText(editingObat.getKandunganDosis());
        txtKodeObat.setText(editingObat.getKodeObat());
        txtKodeObat.setEnabled(false);
        txtLokasiRak.setText(editingObat.getLokasiRak());
        txtHargaBeli.setText(safeText(editingObat.getHargaBeli()));
        txtHargaJual.setText(safeText(editingObat.getHargaJual()));
        txtStokAwal.setText(String.valueOf(editingObat.getStokSaatIni()));
        txtStokMinimum.setText(String.valueOf(editingObat.getStokMinimum()));
        txtSupplier.setText(editingObat.getSupplier());
        txtKeterangan.setText(editingObat.getKeterangan());
        selectCombo(cmbJenis, editingObat.getJenis());
        selectCombo(cmbSatuan, editingObat.getSatuan());
        if (editingObat.getKadaluarsa() != null) {
            Date expiryDate = Date.from(editingObat.getKadaluarsa().atStartOfDay(ZoneId.systemDefault()).toInstant());
            spnKadaluarsa.setValue(expiryDate);
        }
    }

    private void stylePrimaryButton(JButton button) {
        FormUiStyle.stylePrimaryButton(button);
    }

    private String validateFormInput() {
        if (txtNamaObat.getText().trim().isEmpty()) {
            return "Nama obat wajib diisi.";
        }
        if (cmbJenis.getSelectedIndex() <= 0) {
            return "Jenis / kategori wajib dipilih.";
        }
        if (cmbSatuan.getSelectedIndex() <= 0) {
            return "Satuan wajib dipilih.";
        }
        try {
            if (parseBigDecimal(txtHargaBeli.getText()).compareTo(BigDecimal.ZERO) < 0) {
                return "Harga beli tidak boleh negatif.";
            }
            if (parseBigDecimal(txtHargaJual.getText()).compareTo(BigDecimal.ZERO) < 0) {
                return "Harga jual tidak boleh negatif.";
            }
        } catch (NumberFormatException ex) {
            return "Harga beli dan harga jual harus berupa angka.";
        }
        try {
            if (Integer.parseInt(txtStokAwal.getText().trim()) < 0) {
                return "Stok awal tidak boleh negatif.";
            }
            if (Integer.parseInt(txtStokMinimum.getText().trim()) < 0) {
                return "Stok minimum tidak boleh negatif.";
            }
        } catch (NumberFormatException ex) {
            return "Stok awal dan stok minimum harus berupa angka.";
        }
        return null;
    }

    private Obat buildObat() throws Exception {
        Obat obat = new Obat();
        if (editingObat != null) {
            obat.setId(editingObat.getId());
            obat.setKodeObat(editingObat.getKodeObat());
        } else {
            String kode = blankToNull(txtKodeObat.getText());
            obat.setKodeObat(kode != null ? kode : controller.generateKodeObat());
        }
        obat.setNamaObat(txtNamaObat.getText().trim());
        obat.setJenis(cmbJenis.getSelectedItem().toString());
        obat.setSatuan(cmbSatuan.getSelectedItem().toString());
        obat.setKandunganDosis(blankToNull(txtKandungan.getText()));
        obat.setHargaBeli(parseBigDecimal(txtHargaBeli.getText()));
        obat.setHargaJual(parseBigDecimal(txtHargaJual.getText()));
        obat.setStokSaatIni(Integer.parseInt(txtStokAwal.getText().trim()));
        obat.setStokMinimum(Integer.parseInt(txtStokMinimum.getText().trim()));
        obat.setSupplier(blankToNull(txtSupplier.getText()));
        obat.setLokasiRak(blankToNull(txtLokasiRak.getText()));
        obat.setKeterangan(blankToNull(txtKeterangan.getText()));
        Date expiryDate = (Date) spnKadaluarsa.getValue();
        if (expiryDate != null) {
            LocalDate kadaluarsa = Instant.ofEpochMilli(expiryDate.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            obat.setKadaluarsa(kadaluarsa);
        }
        return obat;
    }

    private void saveObat() {
        String validationMessage = validateFormInput();
        if (validationMessage != null) {
            JOptionPane.showMessageDialog(this, validationMessage, "Validasi Form", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Obat obat = buildObat();
            boolean berhasil = isEditMode()
                    ? controller.edit(obat)
                    : controller.tambah(obat);

            if (berhasil) {
                JOptionPane.showMessageDialog(
                        this,
                        isEditMode() ? "Data obat berhasil diperbarui." : "Data obat berhasil ditambahkan.",
                        "Sukses",
                        JOptionPane.INFORMATION_MESSAGE
                );
                navigateToObatList();
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    isEditMode() ? "Gagal memperbarui obat." : "Gagal menambahkan obat.",
                    "Gagal",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    (isEditMode() ? "Gagal memperbarui obat.\n" : "Gagal menambahkan obat.\n") + ex.getMessage(),
                    "Gagal",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void navigateToObatList() {
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof DashboardForm dashboardForm) {
            dashboardForm.showObatPage();
        }
    }

    private boolean isEditMode() {
        return editingObat != null;
    }

    private void selectCombo(javax.swing.JComboBox<String> combo, String value) {
        if (value == null || value.isBlank()) {
            combo.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            String item = combo.getItemAt(i);
            if (item != null && item.equalsIgnoreCase(value)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        String normalized = value == null ? "" : value.replace(".", "").replace(",", "").replace("Rp", "").trim();
        return new BigDecimal(normalized);
    }

    private String safeText(BigDecimal value) {
        return value != null ? value.toPlainString() : "";
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelMain = new javax.swing.JPanel();
        panelActionBar = new javax.swing.JPanel();
        btnBatalHeader = new javax.swing.JButton();
        panelFormCard = new javax.swing.JPanel();
        panelFormHeader = new javax.swing.JPanel();
        lblFormTitle = new javax.swing.JLabel();
        lblFormNote = new javax.swing.JLabel();
        panelBody = new javax.swing.JPanel();
        panelRow1 = new javax.swing.JPanel();
        panelFieldNama = new javax.swing.JPanel();
        lblNamaObat = new javax.swing.JLabel();
        txtNamaObat = new javax.swing.JTextField();
        panelFieldJenis = new javax.swing.JPanel();
        lblJenis = new javax.swing.JLabel();
        cmbJenis = new javax.swing.JComboBox<>();
        panelRow2 = new javax.swing.JPanel();
        panelFieldSatuan = new javax.swing.JPanel();
        lblSatuan = new javax.swing.JLabel();
        cmbSatuan = new javax.swing.JComboBox<>();
        panelFieldKandungan = new javax.swing.JPanel();
        lblKandungan = new javax.swing.JLabel();
        txtKandungan = new javax.swing.JTextField();
        panelFieldKode = new javax.swing.JPanel();
        lblKodeObat = new javax.swing.JLabel();
        txtKodeObat = new javax.swing.JTextField();
        panelFieldLokasi = new javax.swing.JPanel();
        lblLokasiRak = new javax.swing.JLabel();
        txtLokasiRak = new javax.swing.JTextField();
        panelRow3 = new javax.swing.JPanel();
        panelFieldHargaBeli = new javax.swing.JPanel();
        lblHargaBeli = new javax.swing.JLabel();
        txtHargaBeli = new javax.swing.JTextField();
        panelFieldHargaJual = new javax.swing.JPanel();
        lblHargaJual = new javax.swing.JLabel();
        txtHargaJual = new javax.swing.JTextField();
        panelFieldStokAwal = new javax.swing.JPanel();
        lblStokAwal = new javax.swing.JLabel();
        txtStokAwal = new javax.swing.JTextField();
        panelFieldStokMinimum = new javax.swing.JPanel();
        lblStokMinimum = new javax.swing.JLabel();
        txtStokMinimum = new javax.swing.JTextField();
        panelRow4 = new javax.swing.JPanel();
        panelFieldKadaluarsa = new javax.swing.JPanel();
        lblKadaluarsa = new javax.swing.JLabel();
        spnKadaluarsa = new javax.swing.JSpinner();
        panelFieldSupplier = new javax.swing.JPanel();
        lblSupplier = new javax.swing.JLabel();
        txtSupplier = new javax.swing.JTextField();
        panelRow5 = new javax.swing.JPanel();
        panelFieldKeterangan = new javax.swing.JPanel();
        lblKeterangan = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtKeterangan = new javax.swing.JTextArea();
        panelActionsBottom = new javax.swing.JPanel();
        btnBatal = new javax.swing.JButton();
        btnSimpanData = new javax.swing.JButton();

        panelMain.setBackground(new java.awt.Color(243, 240, 233));

        panelActionBar.setBackground(new java.awt.Color(243, 240, 233));

        btnBatalHeader.setForeground(new java.awt.Color(90, 90, 90));
        btnBatalHeader.setText("Batal");
        btnBatalHeader.setFocusPainted(false);
        btnBatalHeader.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBatalHeaderActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelActionBarLayout = new javax.swing.GroupLayout(panelActionBar);
        panelActionBar.setLayout(panelActionBarLayout);
        panelActionBarLayout.setHorizontalGroup(
            panelActionBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelActionBarLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnBatalHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelActionBarLayout.setVerticalGroup(
            panelActionBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnBatalHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        panelFormCard.setBackground(new java.awt.Color(255, 255, 255));
        panelFormCard.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 218, 208)));

        panelFormHeader.setBackground(new java.awt.Color(255, 255, 255));

        lblFormTitle.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblFormTitle.setText("Tambah Obat");

        lblFormNote.setForeground(new java.awt.Color(198, 95, 95));
        lblFormNote.setText("* Wajib diisi");

        javax.swing.GroupLayout panelFormHeaderLayout = new javax.swing.GroupLayout(panelFormHeader);
        panelFormHeader.setLayout(panelFormHeaderLayout);
        panelFormHeaderLayout.setHorizontalGroup(
            panelFormHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormHeaderLayout.createSequentialGroup()
                .addComponent(lblFormTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblFormNote))
        );
        panelFormHeaderLayout.setVerticalGroup(
            panelFormHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblFormTitle)
                .addComponent(lblFormNote))
        );

        panelBody.setBackground(new java.awt.Color(255, 255, 255));

        panelRow1.setBackground(new java.awt.Color(255, 255, 255));

        panelFieldNama.setBackground(new java.awt.Color(255, 255, 255));

        lblNamaObat.setText("Nama obat *");

        javax.swing.GroupLayout panelFieldNamaLayout = new javax.swing.GroupLayout(panelFieldNama);
        panelFieldNama.setLayout(panelFieldNamaLayout);
        panelFieldNamaLayout.setHorizontalGroup(
            panelFieldNamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblNamaObat)
            .addComponent(txtNamaObat)
        );
        panelFieldNamaLayout.setVerticalGroup(
            panelFieldNamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldNamaLayout.createSequentialGroup()
                .addComponent(lblNamaObat)
                .addGap(6, 6, 6)
                .addComponent(txtNamaObat, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelFieldJenis.setBackground(new java.awt.Color(255, 255, 255));

        lblJenis.setText("Jenis / Kategori *");

        cmbJenis.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih...", "Tablet", "Kapsul", "Sirup", "Sachet", "Cairan", "Salep", "Injeksi", "Vitamin" }));

        javax.swing.GroupLayout panelFieldJenisLayout = new javax.swing.GroupLayout(panelFieldJenis);
        panelFieldJenis.setLayout(panelFieldJenisLayout);
        panelFieldJenisLayout.setHorizontalGroup(
            panelFieldJenisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblJenis)
            .addComponent(cmbJenis, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelFieldJenisLayout.setVerticalGroup(
            panelFieldJenisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldJenisLayout.createSequentialGroup()
                .addComponent(lblJenis)
                .addGap(6, 6, 6)
                .addComponent(cmbJenis, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout panelRow1Layout = new javax.swing.GroupLayout(panelRow1);
        panelRow1.setLayout(panelRow1Layout);
        panelRow1Layout.setHorizontalGroup(
            panelRow1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRow1Layout.createSequentialGroup()
                .addComponent(panelFieldNama, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(14, 14, 14)
                .addComponent(panelFieldJenis, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelRow1Layout.setVerticalGroup(
            panelRow1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelFieldNama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelFieldJenis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        panelRow2.setBackground(new java.awt.Color(255, 255, 255));

        panelFieldSatuan.setBackground(new java.awt.Color(255, 255, 255));

        lblSatuan.setText("Satuan *");

        cmbSatuan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih...", "Strip", "Botol", "Box", "Vial", "Tube", "Sachet", "Ampul" }));

        javax.swing.GroupLayout panelFieldSatuanLayout = new javax.swing.GroupLayout(panelFieldSatuan);
        panelFieldSatuan.setLayout(panelFieldSatuanLayout);
        panelFieldSatuanLayout.setHorizontalGroup(
            panelFieldSatuanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblSatuan)
            .addComponent(cmbSatuan, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelFieldSatuanLayout.setVerticalGroup(
            panelFieldSatuanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldSatuanLayout.createSequentialGroup()
                .addComponent(lblSatuan)
                .addGap(6, 6, 6)
                .addComponent(cmbSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelFieldKandungan.setBackground(new java.awt.Color(255, 255, 255));

        lblKandungan.setText("Kandungan / dosis");

        javax.swing.GroupLayout panelFieldKandunganLayout = new javax.swing.GroupLayout(panelFieldKandungan);
        panelFieldKandungan.setLayout(panelFieldKandunganLayout);
        panelFieldKandunganLayout.setHorizontalGroup(
            panelFieldKandunganLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblKandungan)
            .addComponent(txtKandungan)
        );
        panelFieldKandunganLayout.setVerticalGroup(
            panelFieldKandunganLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldKandunganLayout.createSequentialGroup()
                .addComponent(lblKandungan)
                .addGap(6, 6, 6)
                .addComponent(txtKandungan, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelFieldKode.setBackground(new java.awt.Color(255, 255, 255));

        lblKodeObat.setText("Kode obat");

        javax.swing.GroupLayout panelFieldKodeLayout = new javax.swing.GroupLayout(panelFieldKode);
        panelFieldKode.setLayout(panelFieldKodeLayout);
        panelFieldKodeLayout.setHorizontalGroup(
            panelFieldKodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblKodeObat)
            .addComponent(txtKodeObat)
        );
        panelFieldKodeLayout.setVerticalGroup(
            panelFieldKodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldKodeLayout.createSequentialGroup()
                .addComponent(lblKodeObat)
                .addGap(6, 6, 6)
                .addComponent(txtKodeObat, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelFieldLokasi.setBackground(new java.awt.Color(255, 255, 255));

        lblLokasiRak.setText("Lokasi rak");

        javax.swing.GroupLayout panelFieldLokasiLayout = new javax.swing.GroupLayout(panelFieldLokasi);
        panelFieldLokasi.setLayout(panelFieldLokasiLayout);
        panelFieldLokasiLayout.setHorizontalGroup(
            panelFieldLokasiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblLokasiRak)
            .addComponent(txtLokasiRak)
        );
        panelFieldLokasiLayout.setVerticalGroup(
            panelFieldLokasiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldLokasiLayout.createSequentialGroup()
                .addComponent(lblLokasiRak)
                .addGap(6, 6, 6)
                .addComponent(txtLokasiRak, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout panelRow2Layout = new javax.swing.GroupLayout(panelRow2);
        panelRow2.setLayout(panelRow2Layout);
        panelRow2Layout.setHorizontalGroup(
            panelRow2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRow2Layout.createSequentialGroup()
                .addComponent(panelFieldSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(panelFieldKandungan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(14, 14, 14)
                .addComponent(panelFieldKode, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(panelFieldLokasi, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelRow2Layout.setVerticalGroup(
            panelRow2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelFieldSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelFieldKandungan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelFieldKode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelFieldLokasi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        panelRow3.setBackground(new java.awt.Color(255, 255, 255));

        panelFieldHargaBeli.setBackground(new java.awt.Color(255, 255, 255));

        lblHargaBeli.setText("Harga beli (Rp) *");

        javax.swing.GroupLayout panelFieldHargaBeliLayout = new javax.swing.GroupLayout(panelFieldHargaBeli);
        panelFieldHargaBeli.setLayout(panelFieldHargaBeliLayout);
        panelFieldHargaBeliLayout.setHorizontalGroup(
            panelFieldHargaBeliLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblHargaBeli)
            .addComponent(txtHargaBeli)
        );
        panelFieldHargaBeliLayout.setVerticalGroup(
            panelFieldHargaBeliLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldHargaBeliLayout.createSequentialGroup()
                .addComponent(lblHargaBeli)
                .addGap(6, 6, 6)
                .addComponent(txtHargaBeli, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelFieldHargaJual.setBackground(new java.awt.Color(255, 255, 255));

        lblHargaJual.setText("Harga jual (Rp) *");

        javax.swing.GroupLayout panelFieldHargaJualLayout = new javax.swing.GroupLayout(panelFieldHargaJual);
        panelFieldHargaJual.setLayout(panelFieldHargaJualLayout);
        panelFieldHargaJualLayout.setHorizontalGroup(
            panelFieldHargaJualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblHargaJual)
            .addComponent(txtHargaJual)
        );
        panelFieldHargaJualLayout.setVerticalGroup(
            panelFieldHargaJualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldHargaJualLayout.createSequentialGroup()
                .addComponent(lblHargaJual)
                .addGap(6, 6, 6)
                .addComponent(txtHargaJual, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelFieldStokAwal.setBackground(new java.awt.Color(255, 255, 255));

        lblStokAwal.setText("Stok awal *");

        javax.swing.GroupLayout panelFieldStokAwalLayout = new javax.swing.GroupLayout(panelFieldStokAwal);
        panelFieldStokAwal.setLayout(panelFieldStokAwalLayout);
        panelFieldStokAwalLayout.setHorizontalGroup(
            panelFieldStokAwalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblStokAwal)
            .addComponent(txtStokAwal)
        );
        panelFieldStokAwalLayout.setVerticalGroup(
            panelFieldStokAwalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldStokAwalLayout.createSequentialGroup()
                .addComponent(lblStokAwal)
                .addGap(6, 6, 6)
                .addComponent(txtStokAwal, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelFieldStokMinimum.setBackground(new java.awt.Color(255, 255, 255));

        lblStokMinimum.setText("Stok minimum *");

        javax.swing.GroupLayout panelFieldStokMinimumLayout = new javax.swing.GroupLayout(panelFieldStokMinimum);
        panelFieldStokMinimum.setLayout(panelFieldStokMinimumLayout);
        panelFieldStokMinimumLayout.setHorizontalGroup(
            panelFieldStokMinimumLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblStokMinimum)
            .addComponent(txtStokMinimum)
        );
        panelFieldStokMinimumLayout.setVerticalGroup(
            panelFieldStokMinimumLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldStokMinimumLayout.createSequentialGroup()
                .addComponent(lblStokMinimum)
                .addGap(6, 6, 6)
                .addComponent(txtStokMinimum, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout panelRow3Layout = new javax.swing.GroupLayout(panelRow3);
        panelRow3.setLayout(panelRow3Layout);
        panelRow3Layout.setHorizontalGroup(
            panelRow3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRow3Layout.createSequentialGroup()
                .addComponent(panelFieldHargaBeli, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(14, 14, 14)
                .addComponent(panelFieldHargaJual, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(14, 14, 14)
                .addComponent(panelFieldStokAwal, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(panelFieldStokMinimum, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelRow3Layout.setVerticalGroup(
            panelRow3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelFieldHargaBeli, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelFieldHargaJual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelFieldStokAwal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelFieldStokMinimum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        panelRow4.setBackground(new java.awt.Color(255, 255, 255));

        panelFieldKadaluarsa.setBackground(new java.awt.Color(255, 255, 255));

        lblKadaluarsa.setText("Tanggal kadaluarsa");

        javax.swing.GroupLayout panelFieldKadaluarsaLayout = new javax.swing.GroupLayout(panelFieldKadaluarsa);
        panelFieldKadaluarsa.setLayout(panelFieldKadaluarsaLayout);
        panelFieldKadaluarsaLayout.setHorizontalGroup(
            panelFieldKadaluarsaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblKadaluarsa)
            .addComponent(spnKadaluarsa)
        );
        panelFieldKadaluarsaLayout.setVerticalGroup(
            panelFieldKadaluarsaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldKadaluarsaLayout.createSequentialGroup()
                .addComponent(lblKadaluarsa)
                .addGap(6, 6, 6)
                .addComponent(spnKadaluarsa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelFieldSupplier.setBackground(new java.awt.Color(255, 255, 255));

        lblSupplier.setText("Supplier / Distributor");

        javax.swing.GroupLayout panelFieldSupplierLayout = new javax.swing.GroupLayout(panelFieldSupplier);
        panelFieldSupplier.setLayout(panelFieldSupplierLayout);
        panelFieldSupplierLayout.setHorizontalGroup(
            panelFieldSupplierLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblSupplier)
            .addComponent(txtSupplier)
        );
        panelFieldSupplierLayout.setVerticalGroup(
            panelFieldSupplierLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldSupplierLayout.createSequentialGroup()
                .addComponent(lblSupplier)
                .addGap(6, 6, 6)
                .addComponent(txtSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout panelRow4Layout = new javax.swing.GroupLayout(panelRow4);
        panelRow4.setLayout(panelRow4Layout);
        panelRow4Layout.setHorizontalGroup(
            panelRow4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRow4Layout.createSequentialGroup()
                .addComponent(panelFieldKadaluarsa, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(panelFieldSupplier, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelRow4Layout.setVerticalGroup(
            panelRow4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelFieldKadaluarsa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelFieldSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        panelRow5.setBackground(new java.awt.Color(255, 255, 255));

        panelFieldKeterangan.setBackground(new java.awt.Color(255, 255, 255));

        lblKeterangan.setText("Keterangan tambahan");

        txtKeterangan.setColumns(20);
        txtKeterangan.setRows(5);
        jScrollPane1.setViewportView(txtKeterangan);

        javax.swing.GroupLayout panelFieldKeteranganLayout = new javax.swing.GroupLayout(panelFieldKeterangan);
        panelFieldKeterangan.setLayout(panelFieldKeteranganLayout);
        panelFieldKeteranganLayout.setHorizontalGroup(
            panelFieldKeteranganLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblKeterangan)
            .addComponent(jScrollPane1)
        );
        panelFieldKeteranganLayout.setVerticalGroup(
            panelFieldKeteranganLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldKeteranganLayout.createSequentialGroup()
                .addComponent(lblKeterangan)
                .addGap(6, 6, 6)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout panelRow5Layout = new javax.swing.GroupLayout(panelRow5);
        panelRow5.setLayout(panelRow5Layout);
        panelRow5Layout.setHorizontalGroup(
            panelRow5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelFieldKeterangan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelRow5Layout.setVerticalGroup(
            panelRow5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelFieldKeterangan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout panelBodyLayout = new javax.swing.GroupLayout(panelBody);
        panelBody.setLayout(panelBodyLayout);
        panelBodyLayout.setHorizontalGroup(
            panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelRow1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelRow2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelRow3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelRow4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelRow5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelBodyLayout.setVerticalGroup(
            panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBodyLayout.createSequentialGroup()
                .addComponent(panelRow1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(panelRow2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(panelRow3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(panelRow4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(panelRow5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelActionsBottom.setBackground(new java.awt.Color(255, 255, 255));

        btnBatal.setText("Batal");
        btnBatal.setFocusPainted(false);
        btnBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBatalActionPerformed(evt);
            }
        });

        btnSimpanData.setBackground(new java.awt.Color(28, 112, 77));
        btnSimpanData.setForeground(new java.awt.Color(255, 255, 255));
        btnSimpanData.setText("Simpan Data");
        btnSimpanData.setFocusPainted(false);
        btnSimpanData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanDataActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelActionsBottomLayout = new javax.swing.GroupLayout(panelActionsBottom);
        panelActionsBottom.setLayout(panelActionsBottomLayout);
        panelActionsBottomLayout.setHorizontalGroup(
            panelActionsBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelActionsBottomLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnBatal, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnSimpanData, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelActionsBottomLayout.setVerticalGroup(
            panelActionsBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelActionsBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btnBatal, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btnSimpanData, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout panelFormCardLayout = new javax.swing.GroupLayout(panelFormCard);
        panelFormCard.setLayout(panelFormCardLayout);
        panelFormCardLayout.setHorizontalGroup(
            panelFormCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormCardLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelFormCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelFormHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelBody, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelActionsBottom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        panelFormCardLayout.setVerticalGroup(
            panelFormCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormCardLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(panelFormHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(panelBody, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(panelActionsBottom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelActionBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelFormCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(panelActionBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(panelFormCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnBatalHeaderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBatalHeaderActionPerformed
        navigateToObatList();
    }//GEN-LAST:event_btnBatalHeaderActionPerformed

    private void btnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBatalActionPerformed
        navigateToObatList();
    }//GEN-LAST:event_btnBatalActionPerformed

    private void btnSimpanDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanDataActionPerformed
        saveObat();
    }//GEN-LAST:event_btnSimpanDataActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBatal;
    private javax.swing.JButton btnBatalHeader;
    private javax.swing.JButton btnSimpanData;
    private javax.swing.JComboBox<String> cmbJenis;
    private javax.swing.JComboBox<String> cmbSatuan;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblFormNote;
    private javax.swing.JLabel lblFormTitle;
    private javax.swing.JLabel lblHargaBeli;
    private javax.swing.JLabel lblHargaJual;
    private javax.swing.JLabel lblKadaluarsa;
    private javax.swing.JLabel lblKandungan;
    private javax.swing.JLabel lblKeterangan;
    private javax.swing.JLabel lblJenis;
    private javax.swing.JLabel lblKodeObat;
    private javax.swing.JLabel lblLokasiRak;
    private javax.swing.JLabel lblNamaObat;
    private javax.swing.JLabel lblSatuan;
    private javax.swing.JLabel lblStokAwal;
    private javax.swing.JLabel lblStokMinimum;
    private javax.swing.JLabel lblSupplier;
    private javax.swing.JPanel panelActionBar;
    private javax.swing.JPanel panelActionsBottom;
    private javax.swing.JPanel panelBody;
    private javax.swing.JPanel panelFieldHargaBeli;
    private javax.swing.JPanel panelFieldHargaJual;
    private javax.swing.JPanel panelFieldJenis;
    private javax.swing.JPanel panelFieldKadaluarsa;
    private javax.swing.JPanel panelFieldKandungan;
    private javax.swing.JPanel panelFieldKeterangan;
    private javax.swing.JPanel panelFieldKode;
    private javax.swing.JPanel panelFieldLokasi;
    private javax.swing.JPanel panelFieldNama;
    private javax.swing.JPanel panelFieldSatuan;
    private javax.swing.JPanel panelFieldStokAwal;
    private javax.swing.JPanel panelFieldStokMinimum;
    private javax.swing.JPanel panelFieldSupplier;
    private javax.swing.JPanel panelFormCard;
    private javax.swing.JPanel panelFormHeader;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelRow1;
    private javax.swing.JPanel panelRow2;
    private javax.swing.JPanel panelRow3;
    private javax.swing.JPanel panelRow4;
    private javax.swing.JPanel panelRow5;
    private javax.swing.JSpinner spnKadaluarsa;
    private javax.swing.JTextField txtHargaBeli;
    private javax.swing.JTextField txtHargaJual;
    private javax.swing.JTextField txtKandungan;
    private javax.swing.JTextArea txtKeterangan;
    private javax.swing.JTextField txtKodeObat;
    private javax.swing.JTextField txtLokasiRak;
    private javax.swing.JTextField txtNamaObat;
    private javax.swing.JTextField txtStokAwal;
    private javax.swing.JTextField txtStokMinimum;
    private javax.swing.JTextField txtSupplier;
    // End of variables declaration//GEN-END:variables
}
