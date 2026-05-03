package com.release.klinikgaharumedika.view.panel;

import com.release.klinikgaharumedika.controller.DokterController;
import com.release.klinikgaharumedika.model.Dokter;
import com.release.klinikgaharumedika.model.JadwalDokter;
import com.release.klinikgaharumedika.view.DashboardForm;
import java.beans.Beans;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableModel;

public class DokterFormPanel extends javax.swing.JPanel {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final NumberFormat CURRENCY =
            NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"));

    static {
        CURRENCY.setMinimumFractionDigits(0);
        CURRENCY.setMaximumFractionDigits(0);
    }

    private final DokterController controller;
    private final Dokter editingDokter;

    public DokterFormPanel() {
        this(null);
    }

    public DokterFormPanel(Dokter editingDokter) {
        this.editingDokter = editingDokter;
        initComponents();
        if (Beans.isDesignTime()) {
            controller = null;
            configureForm();
            return;
        }
        controller = new DokterController();
        configureForm();
    }

    private void configureForm() {
        txtNamaLengkap.putClientProperty("JTextField.placeholderText", "Contoh: dr. Andi Saputra");
        txtNoStr.putClientProperty("JTextField.placeholderText", "Contoh: STR-2026-001");
        txtNoSip.putClientProperty("JTextField.placeholderText", "Contoh: SIP-2026-001");
        txtNoHp.putClientProperty("JTextField.placeholderText", "Contoh: 081222220001");
        txtTarifKonsultasi.putClientProperty("JTextField.placeholderText", "Contoh: 150000");
        txtKuotaJadwal.putClientProperty("JTextField.placeholderText", "Contoh: 20");
        configureTarifKonsultasiFormatter();

        FormUiStyle.timeSpinner(spnJamMulai);
        FormUiStyle.timeSpinner(spnJamSelesai);
        spnJamMulai.setValue(toDate(LocalTime.of(8, 0)));
        spnJamSelesai.setValue(toDate(LocalTime.of(14, 0)));

        tblJadwal.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Hari", "Jam Mulai", "Jam Selesai", "Kuota"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        tblJadwal.setRowHeight(28);
        tblJadwal.getTableHeader().setReorderingAllowed(false);

        stylePrimaryButton(btnSimpanData);
        stylePrimaryButton(btnTambahJadwal);
        applyMode();
        FormUiStyle.applyFormStyle(this);
    }

    private void applyMode() {
        if (editingDokter == null) {
            lblFormTitle.setText("Tambah Dokter");
            btnSimpanData.setText("Simpan Data");
            cmbStatus.setSelectedIndex(0);
            cmbSpesialisasi.setSelectedIndex(0);
            cmbPoliUnit.setSelectedIndex(0);
            cmbHariJadwal.setSelectedIndex(0);
            return;
        }

        lblFormTitle.setText("Edit Dokter");
        btnSimpanData.setText("Simpan Perubahan");
        txtNamaLengkap.setText(editingDokter.getNama());
        txtNoStr.setText(editingDokter.getNoStr());
        txtNoSip.setText(editingDokter.getNoSip());
        txtNoHp.setText(editingDokter.getNoHp());
        txtTarifKonsultasi.setText(formatRupiah(editingDokter.getTarifKonsultasi()));
        selectCombo(cmbSpesialisasi, editingDokter.getSpesialisasi());
        selectCombo(cmbPoliUnit, editingDokter.getPoliUnit());
        selectCombo(cmbStatus, capitalize(editingDokter.getStatus()));
        loadJadwal(editingDokter.getJadwalPraktik());
    }

    private void configureTarifKonsultasiFormatter() {
        txtTarifKonsultasi.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                showRawTarifKonsultasi();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                formatTarifKonsultasiField();
            }
        });
    }

    private void showRawTarifKonsultasi() {
        String text = txtTarifKonsultasi.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        try {
            txtTarifKonsultasi.setText(parseBigDecimal(text).toPlainString());
        } catch (NumberFormatException ex) {
            // Biarkan validasi form menampilkan pesan error yang tepat.
        }
    }

    private void formatTarifKonsultasiField() {
        String text = txtTarifKonsultasi.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        try {
            txtTarifKonsultasi.setText(formatRupiah(parseBigDecimal(text)));
        } catch (NumberFormatException ex) {
            // Biarkan input invalid tetap terlihat agar bisa dikoreksi user.
        }
    }

    private void loadJadwal(List<JadwalDokter> jadwalList) {
        DefaultTableModel model = (DefaultTableModel) tblJadwal.getModel();
        model.setRowCount(0);
        if (jadwalList == null) {
            return;
        }
        for (JadwalDokter jadwal : jadwalList) {
            model.addRow(new Object[]{
                jadwal.getHari(),
                formatTime(jadwal.getJamMulai()),
                formatTime(jadwal.getJamSelesai()),
                jadwal.getKuotaPasien()
            });
        }
    }

    private void stylePrimaryButton(JButton button) {
        FormUiStyle.stylePrimaryButton(button);
    }

    private void addJadwal() {
        String validationMessage = validateJadwalInput();
        if (validationMessage != null) {
            JOptionPane.showMessageDialog(this, validationMessage, "Validasi Jadwal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalTime jamMulai = toLocalTime((Date) spnJamMulai.getValue());
        LocalTime jamSelesai = toLocalTime((Date) spnJamSelesai.getValue());

        DefaultTableModel model = (DefaultTableModel) tblJadwal.getModel();
        model.addRow(new Object[]{
            cmbHariJadwal.getSelectedItem().toString(),
            formatTime(jamMulai),
            formatTime(jamSelesai),
            Integer.parseInt(txtKuotaJadwal.getText().trim())
        });
        cmbHariJadwal.setSelectedIndex(0);
        spnJamMulai.setValue(toDate(LocalTime.of(8, 0)));
        spnJamSelesai.setValue(toDate(LocalTime.of(14, 0)));
        txtKuotaJadwal.setText("");
    }

    private String validateJadwalInput() {
        if (cmbHariJadwal.getSelectedIndex() <= 0) {
            return "Hari praktik wajib dipilih.";
        }
        if (txtKuotaJadwal.getText().trim().isEmpty()) {
            return "Kuota jadwal wajib diisi.";
        }
        try {
            int kuota = Integer.parseInt(txtKuotaJadwal.getText().trim());
            if (kuota <= 0) {
                return "Kuota harus lebih besar dari 0.";
            }
        } catch (NumberFormatException ex) {
            return "Kuota jadwal harus berupa angka.";
        }

        LocalTime jamMulai = toLocalTime((Date) spnJamMulai.getValue());
        LocalTime jamSelesai = toLocalTime((Date) spnJamSelesai.getValue());
        if (!jamMulai.isBefore(jamSelesai)) {
            return "Jam selesai harus lebih besar dari jam mulai.";
        }
        return null;
    }

    private void removeSelectedJadwal() {
        int selectedRow = tblJadwal.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih jadwal yang ingin dihapus.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        ((DefaultTableModel) tblJadwal.getModel()).removeRow(selectedRow);
    }

    private String validateFormInput() {
        if (txtNamaLengkap.getText().trim().isEmpty()) {
            return "Nama dokter wajib diisi.";
        }
        if (cmbSpesialisasi.getSelectedIndex() <= 0) {
            return "Spesialisasi wajib dipilih.";
        }
        if (txtNoStr.getText().trim().isEmpty()) {
            return "No. STR wajib diisi.";
        }
        if (txtNoSip.getText().trim().isEmpty()) {
            return "No. SIP wajib diisi.";
        }
        if (txtNoHp.getText().trim().isEmpty()) {
            return "No. HP wajib diisi.";
        }
        if (cmbPoliUnit.getSelectedIndex() <= 0) {
            return "Poli / Unit wajib dipilih.";
        }
        if (txtTarifKonsultasi.getText().trim().isEmpty()) {
            return "Tarif konsultasi wajib diisi.";
        }
        try {
            BigDecimal tarif = parseBigDecimal(txtTarifKonsultasi.getText());
            if (tarif.compareTo(BigDecimal.ZERO) < 0) {
                return "Tarif konsultasi tidak boleh negatif.";
            }
        } catch (NumberFormatException ex) {
            return "Tarif konsultasi harus berupa angka.";
        }
        if (cmbStatus.getSelectedIndex() <= 0) {
            return "Status wajib dipilih.";
        }
        if (tblJadwal.getRowCount() == 0) {
            return "Minimal satu jadwal praktik wajib ditambahkan.";
        }
        return null;
    }

    private Dokter buildDokter() {
        Dokter dokter = new Dokter();
        if (editingDokter != null) {
            dokter.setId(editingDokter.getId());
            dokter.setKodeDokter(editingDokter.getKodeDokter());
        }
        dokter.setNama(txtNamaLengkap.getText().trim());
        dokter.setSpesialisasi(cmbSpesialisasi.getSelectedItem().toString());
        dokter.setPoliUnit(cmbPoliUnit.getSelectedItem().toString());
        dokter.setNoStr(txtNoStr.getText().trim());
        dokter.setNoSip(txtNoSip.getText().trim());
        dokter.setNoHp(txtNoHp.getText().trim());
        dokter.setTarifKonsultasi(parseBigDecimal(txtTarifKonsultasi.getText()));
        dokter.setStatus(cmbStatus.getSelectedItem().toString().toLowerCase());
        dokter.setJadwalPraktik(buildJadwalList());
        return dokter;
    }

    private List<JadwalDokter> buildJadwalList() {
        List<JadwalDokter> list = new ArrayList<>();
        DefaultTableModel model = (DefaultTableModel) tblJadwal.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            JadwalDokter jadwal = new JadwalDokter();
            jadwal.setHari(model.getValueAt(i, 0).toString());
            jadwal.setJamMulai(LocalTime.parse(model.getValueAt(i, 1).toString(), TIME_FORMAT));
            jadwal.setJamSelesai(LocalTime.parse(model.getValueAt(i, 2).toString(), TIME_FORMAT));
            jadwal.setKuotaPasien(Integer.parseInt(model.getValueAt(i, 3).toString()));
            list.add(jadwal);
        }
        return list;
    }

    private void saveDokter() {
        String validationMessage = validateFormInput();
        if (validationMessage != null) {
            JOptionPane.showMessageDialog(this, validationMessage, "Validasi Form", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Dokter dokter = buildDokter();
            if (!isEditMode()) {
                dokter.setKodeDokter(controller.generateKodeDokter());
            }

            boolean berhasil = isEditMode()
                    ? controller.edit(dokter)
                    : controller.tambah(dokter);

            if (berhasil) {
                JOptionPane.showMessageDialog(
                        this,
                        isEditMode() ? "Data dokter berhasil diperbarui." : "Data dokter berhasil ditambahkan.",
                        "Sukses",
                        JOptionPane.INFORMATION_MESSAGE
                );
                navigateToDokterList();
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    isEditMode() ? "Gagal memperbarui dokter." : "Gagal menambahkan dokter.",
                    "Gagal",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    (isEditMode() ? "Gagal memperbarui dokter.\n" : "Gagal menambahkan dokter.\n") + ex.getMessage(),
                    "Gagal",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void navigateToDokterList() {
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof DashboardForm dashboardForm) {
            dashboardForm.showDokterPage();
        }
    }

    private boolean isEditMode() {
        return editingDokter != null;
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
        String normalized = normalizeCurrencyText(value);
        if (!normalized.matches("-?\\d+")) {
            throw new NumberFormatException("Invalid currency value: " + value);
        }
        return new BigDecimal(normalized);
    }

    private String normalizeCurrencyText(String value) {
        return value == null
                ? ""
                : value.replaceAll("(?i)rp", "")
                        .replace(".", "")
                        .replace(",", "")
                        .replace(" ", "")
                        .trim();
    }

    private String formatRupiah(BigDecimal value) {
        return value != null ? "Rp " + CURRENCY.format(value) : "";
    }

    private LocalTime toLocalTime(Date value) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(value);
        return LocalTime.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

    private Date toDate(LocalTime time) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, time.getHour());
        calendar.set(Calendar.MINUTE, time.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMAT) : "";
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
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
        lblNamaLengkap = new javax.swing.JLabel();
        txtNamaLengkap = new javax.swing.JTextField();
        panelFieldSpesialisasi = new javax.swing.JPanel();
        lblSpesialisasi = new javax.swing.JLabel();
        cmbSpesialisasi = new javax.swing.JComboBox<>();
        panelRow2 = new javax.swing.JPanel();
        panelFieldNoStr = new javax.swing.JPanel();
        lblNoStr = new javax.swing.JLabel();
        txtNoStr = new javax.swing.JTextField();
        panelFieldNoSip = new javax.swing.JPanel();
        lblNoSip = new javax.swing.JLabel();
        txtNoSip = new javax.swing.JTextField();
        panelFieldNoHp = new javax.swing.JPanel();
        lblNoHp = new javax.swing.JLabel();
        txtNoHp = new javax.swing.JTextField();
        panelRow3 = new javax.swing.JPanel();
        panelFieldPoli = new javax.swing.JPanel();
        lblPoliUnit = new javax.swing.JLabel();
        cmbPoliUnit = new javax.swing.JComboBox<>();
        panelFieldTarif = new javax.swing.JPanel();
        lblTarifKonsultasi = new javax.swing.JLabel();
        txtTarifKonsultasi = new javax.swing.JTextField();
        panelFieldStatus = new javax.swing.JPanel();
        lblStatus = new javax.swing.JLabel();
        cmbStatus = new javax.swing.JComboBox<>();
        panelJadwal = new javax.swing.JPanel();
        lblJadwalTitle = new javax.swing.JLabel();
        panelJadwalInput = new javax.swing.JPanel();
        panelFieldHari = new javax.swing.JPanel();
        lblHari = new javax.swing.JLabel();
        cmbHariJadwal = new javax.swing.JComboBox<>();
        panelFieldJamMulai = new javax.swing.JPanel();
        lblJamMulai = new javax.swing.JLabel();
        spnJamMulai = new javax.swing.JSpinner();
        panelFieldJamSelesai = new javax.swing.JPanel();
        lblJamSelesai = new javax.swing.JLabel();
        spnJamSelesai = new javax.swing.JSpinner();
        panelFieldKuota = new javax.swing.JPanel();
        lblKuota = new javax.swing.JLabel();
        txtKuotaJadwal = new javax.swing.JTextField();
        panelJadwalButtons = new javax.swing.JPanel();
        btnTambahJadwal = new javax.swing.JButton();
        btnHapusJadwal = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblJadwal = new javax.swing.JTable();
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
        lblFormTitle.setText("Tambah Dokter");

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

        lblNamaLengkap.setText("Nama lengkap + gelar *");

        javax.swing.GroupLayout panelFieldNamaLayout = new javax.swing.GroupLayout(panelFieldNama);
        panelFieldNama.setLayout(panelFieldNamaLayout);
        panelFieldNamaLayout.setHorizontalGroup(
            panelFieldNamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblNamaLengkap)
            .addComponent(txtNamaLengkap)
        );
        panelFieldNamaLayout.setVerticalGroup(
            panelFieldNamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldNamaLayout.createSequentialGroup()
                .addComponent(lblNamaLengkap)
                .addGap(6, 6, 6)
                .addComponent(txtNamaLengkap, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelFieldSpesialisasi.setBackground(new java.awt.Color(255, 255, 255));

        lblSpesialisasi.setText("Spesialisasi *");

        cmbSpesialisasi.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih...", "Umum", "Penyakit Dalam", "Anak", "Jantung", "Saraf", "Kulit", "THT", "Mata", "Gigi", "Paru" }));

        javax.swing.GroupLayout panelFieldSpesialisasiLayout = new javax.swing.GroupLayout(panelFieldSpesialisasi);
        panelFieldSpesialisasi.setLayout(panelFieldSpesialisasiLayout);
        panelFieldSpesialisasiLayout.setHorizontalGroup(
            panelFieldSpesialisasiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblSpesialisasi)
            .addComponent(cmbSpesialisasi, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelFieldSpesialisasiLayout.setVerticalGroup(
            panelFieldSpesialisasiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldSpesialisasiLayout.createSequentialGroup()
                .addComponent(lblSpesialisasi)
                .addGap(6, 6, 6)
                .addComponent(cmbSpesialisasi, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout panelRow1Layout = new javax.swing.GroupLayout(panelRow1);
        panelRow1.setLayout(panelRow1Layout);
        panelRow1Layout.setHorizontalGroup(
            panelRow1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRow1Layout.createSequentialGroup()
                .addComponent(panelFieldNama, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(14, 14, 14)
                .addComponent(panelFieldSpesialisasi, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelRow1Layout.setVerticalGroup(
            panelRow1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelFieldNama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelFieldSpesialisasi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        panelRow2.setBackground(new java.awt.Color(255, 255, 255));

        panelFieldNoStr.setBackground(new java.awt.Color(255, 255, 255));

        lblNoStr.setText("No. STR *");

        javax.swing.GroupLayout panelFieldNoStrLayout = new javax.swing.GroupLayout(panelFieldNoStr);
        panelFieldNoStr.setLayout(panelFieldNoStrLayout);
        panelFieldNoStrLayout.setHorizontalGroup(
            panelFieldNoStrLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblNoStr)
            .addComponent(txtNoStr)
        );
        panelFieldNoStrLayout.setVerticalGroup(
            panelFieldNoStrLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldNoStrLayout.createSequentialGroup()
                .addComponent(lblNoStr)
                .addGap(6, 6, 6)
                .addComponent(txtNoStr, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelFieldNoSip.setBackground(new java.awt.Color(255, 255, 255));

        lblNoSip.setText("No. SIP *");

        javax.swing.GroupLayout panelFieldNoSipLayout = new javax.swing.GroupLayout(panelFieldNoSip);
        panelFieldNoSip.setLayout(panelFieldNoSipLayout);
        panelFieldNoSipLayout.setHorizontalGroup(
            panelFieldNoSipLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblNoSip)
            .addComponent(txtNoSip)
        );
        panelFieldNoSipLayout.setVerticalGroup(
            panelFieldNoSipLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldNoSipLayout.createSequentialGroup()
                .addComponent(lblNoSip)
                .addGap(6, 6, 6)
                .addComponent(txtNoSip, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelFieldNoHp.setBackground(new java.awt.Color(255, 255, 255));

        lblNoHp.setText("No. HP *");

        javax.swing.GroupLayout panelFieldNoHpLayout = new javax.swing.GroupLayout(panelFieldNoHp);
        panelFieldNoHp.setLayout(panelFieldNoHpLayout);
        panelFieldNoHpLayout.setHorizontalGroup(
            panelFieldNoHpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblNoHp)
            .addComponent(txtNoHp)
        );
        panelFieldNoHpLayout.setVerticalGroup(
            panelFieldNoHpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldNoHpLayout.createSequentialGroup()
                .addComponent(lblNoHp)
                .addGap(6, 6, 6)
                .addComponent(txtNoHp, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout panelRow2Layout = new javax.swing.GroupLayout(panelRow2);
        panelRow2.setLayout(panelRow2Layout);
        panelRow2Layout.setHorizontalGroup(
            panelRow2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRow2Layout.createSequentialGroup()
                .addComponent(panelFieldNoStr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(14, 14, 14)
                .addComponent(panelFieldNoSip, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(14, 14, 14)
                .addComponent(panelFieldNoHp, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelRow2Layout.setVerticalGroup(
            panelRow2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelFieldNoStr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelFieldNoSip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelFieldNoHp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        panelRow3.setBackground(new java.awt.Color(255, 255, 255));

        panelFieldPoli.setBackground(new java.awt.Color(255, 255, 255));

        lblPoliUnit.setText("Poli / Unit *");

        cmbPoliUnit.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih...", "Poli Umum", "Poli Penyakit Dalam", "Poli Anak", "Poli Jantung", "Poli Saraf", "Poli Kulit", "Poli THT", "Poli Mata", "Poli Gigi", "Poli Paru" }));

        javax.swing.GroupLayout panelFieldPoliLayout = new javax.swing.GroupLayout(panelFieldPoli);
        panelFieldPoli.setLayout(panelFieldPoliLayout);
        panelFieldPoliLayout.setHorizontalGroup(
            panelFieldPoliLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblPoliUnit)
            .addComponent(cmbPoliUnit, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelFieldPoliLayout.setVerticalGroup(
            panelFieldPoliLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldPoliLayout.createSequentialGroup()
                .addComponent(lblPoliUnit)
                .addGap(6, 6, 6)
                .addComponent(cmbPoliUnit, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelFieldTarif.setBackground(new java.awt.Color(255, 255, 255));

        lblTarifKonsultasi.setText("Tarif konsultasi (Rp) *");

        javax.swing.GroupLayout panelFieldTarifLayout = new javax.swing.GroupLayout(panelFieldTarif);
        panelFieldTarif.setLayout(panelFieldTarifLayout);
        panelFieldTarifLayout.setHorizontalGroup(
            panelFieldTarifLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTarifKonsultasi)
            .addComponent(txtTarifKonsultasi)
        );
        panelFieldTarifLayout.setVerticalGroup(
            panelFieldTarifLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldTarifLayout.createSequentialGroup()
                .addComponent(lblTarifKonsultasi)
                .addGap(6, 6, 6)
                .addComponent(txtTarifKonsultasi, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelFieldStatus.setBackground(new java.awt.Color(255, 255, 255));

        lblStatus.setText("Status *");

        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih...", "Aktif", "Nonaktif" }));

        javax.swing.GroupLayout panelFieldStatusLayout = new javax.swing.GroupLayout(panelFieldStatus);
        panelFieldStatus.setLayout(panelFieldStatusLayout);
        panelFieldStatusLayout.setHorizontalGroup(
            panelFieldStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblStatus)
            .addComponent(cmbStatus, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelFieldStatusLayout.setVerticalGroup(
            panelFieldStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldStatusLayout.createSequentialGroup()
                .addComponent(lblStatus)
                .addGap(6, 6, 6)
                .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout panelRow3Layout = new javax.swing.GroupLayout(panelRow3);
        panelRow3.setLayout(panelRow3Layout);
        panelRow3Layout.setHorizontalGroup(
            panelRow3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRow3Layout.createSequentialGroup()
                .addComponent(panelFieldPoli, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(14, 14, 14)
                .addComponent(panelFieldTarif, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(panelFieldStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelRow3Layout.setVerticalGroup(
            panelRow3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelFieldPoli, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelFieldTarif, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelFieldStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        panelJadwal.setBackground(new java.awt.Color(255, 255, 255));
        panelJadwal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(232, 228, 220)));

        lblJadwalTitle.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblJadwalTitle.setText("Jadwal praktik");

        panelJadwalInput.setBackground(new java.awt.Color(255, 255, 255));

        panelFieldHari.setBackground(new java.awt.Color(255, 255, 255));

        lblHari.setText("Hari");

        cmbHariJadwal.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih...", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu" }));

        javax.swing.GroupLayout panelFieldHariLayout = new javax.swing.GroupLayout(panelFieldHari);
        panelFieldHari.setLayout(panelFieldHariLayout);
        panelFieldHariLayout.setHorizontalGroup(
            panelFieldHariLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblHari)
            .addComponent(cmbHariJadwal, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelFieldHariLayout.setVerticalGroup(
            panelFieldHariLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldHariLayout.createSequentialGroup()
                .addComponent(lblHari)
                .addGap(6, 6, 6)
                .addComponent(cmbHariJadwal, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelFieldJamMulai.setBackground(new java.awt.Color(255, 255, 255));

        lblJamMulai.setText("Jam mulai");

        javax.swing.GroupLayout panelFieldJamMulaiLayout = new javax.swing.GroupLayout(panelFieldJamMulai);
        panelFieldJamMulai.setLayout(panelFieldJamMulaiLayout);
        panelFieldJamMulaiLayout.setHorizontalGroup(
            panelFieldJamMulaiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblJamMulai)
            .addComponent(spnJamMulai)
        );
        panelFieldJamMulaiLayout.setVerticalGroup(
            panelFieldJamMulaiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldJamMulaiLayout.createSequentialGroup()
                .addComponent(lblJamMulai)
                .addGap(6, 6, 6)
                .addComponent(spnJamMulai, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelFieldJamSelesai.setBackground(new java.awt.Color(255, 255, 255));

        lblJamSelesai.setText("Jam selesai");

        javax.swing.GroupLayout panelFieldJamSelesaiLayout = new javax.swing.GroupLayout(panelFieldJamSelesai);
        panelFieldJamSelesai.setLayout(panelFieldJamSelesaiLayout);
        panelFieldJamSelesaiLayout.setHorizontalGroup(
            panelFieldJamSelesaiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblJamSelesai)
            .addComponent(spnJamSelesai)
        );
        panelFieldJamSelesaiLayout.setVerticalGroup(
            panelFieldJamSelesaiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldJamSelesaiLayout.createSequentialGroup()
                .addComponent(lblJamSelesai)
                .addGap(6, 6, 6)
                .addComponent(spnJamSelesai, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelFieldKuota.setBackground(new java.awt.Color(255, 255, 255));

        lblKuota.setText("Kuota");

        javax.swing.GroupLayout panelFieldKuotaLayout = new javax.swing.GroupLayout(panelFieldKuota);
        panelFieldKuota.setLayout(panelFieldKuotaLayout);
        panelFieldKuotaLayout.setHorizontalGroup(
            panelFieldKuotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblKuota)
            .addComponent(txtKuotaJadwal)
        );
        panelFieldKuotaLayout.setVerticalGroup(
            panelFieldKuotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldKuotaLayout.createSequentialGroup()
                .addComponent(lblKuota)
                .addGap(6, 6, 6)
                .addComponent(txtKuotaJadwal, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelJadwalButtons.setBackground(new java.awt.Color(255, 255, 255));

        btnTambahJadwal.setBackground(new java.awt.Color(28, 112, 77));
        btnTambahJadwal.setForeground(new java.awt.Color(255, 255, 255));
        btnTambahJadwal.setText("+ Tambah jadwal");
        btnTambahJadwal.setFocusPainted(false);
        btnTambahJadwal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahJadwalActionPerformed(evt);
            }
        });

        btnHapusJadwal.setText("Hapus terpilih");
        btnHapusJadwal.setFocusPainted(false);
        btnHapusJadwal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusJadwalActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelJadwalButtonsLayout = new javax.swing.GroupLayout(panelJadwalButtons);
        panelJadwalButtons.setLayout(panelJadwalButtonsLayout);
        panelJadwalButtonsLayout.setHorizontalGroup(
            panelJadwalButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnTambahJadwal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnHapusJadwal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelJadwalButtonsLayout.setVerticalGroup(
            panelJadwalButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelJadwalButtonsLayout.createSequentialGroup()
                .addComponent(btnTambahJadwal, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnHapusJadwal, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout panelJadwalInputLayout = new javax.swing.GroupLayout(panelJadwalInput);
        panelJadwalInput.setLayout(panelJadwalInputLayout);
        panelJadwalInputLayout.setHorizontalGroup(
            panelJadwalInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelJadwalInputLayout.createSequentialGroup()
                .addComponent(panelFieldHari, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(panelFieldJamMulai, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(panelFieldJamSelesai, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(panelFieldKuota, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(panelJadwalButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelJadwalInputLayout.setVerticalGroup(
            panelJadwalInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelFieldHari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelFieldJamMulai, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelFieldJamSelesai, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelFieldKuota, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelJadwalButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        tblJadwal.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Hari", "Jam Mulai", "Jam Selesai", "Kuota"
            }
        ));
        jScrollPane1.setViewportView(tblJadwal);

        javax.swing.GroupLayout panelJadwalLayout = new javax.swing.GroupLayout(panelJadwal);
        panelJadwal.setLayout(panelJadwalLayout);
        panelJadwalLayout.setHorizontalGroup(
            panelJadwalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelJadwalLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(panelJadwalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblJadwalTitle)
                    .addComponent(panelJadwalInput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addGap(14, 14, 14))
        );
        panelJadwalLayout.setVerticalGroup(
            panelJadwalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelJadwalLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblJadwalTitle)
                .addGap(12, 12, 12)
                .addComponent(panelJadwalInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout panelBodyLayout = new javax.swing.GroupLayout(panelBody);
        panelBody.setLayout(panelBodyLayout);
        panelBodyLayout.setHorizontalGroup(
            panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelRow1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelRow2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelRow3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelJadwal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelBodyLayout.setVerticalGroup(
            panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBodyLayout.createSequentialGroup()
                .addComponent(panelRow1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(panelRow2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(panelRow3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(panelJadwal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
        navigateToDokterList();
    }//GEN-LAST:event_btnBatalHeaderActionPerformed

    private void btnTambahJadwalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahJadwalActionPerformed
        addJadwal();
    }//GEN-LAST:event_btnTambahJadwalActionPerformed

    private void btnHapusJadwalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusJadwalActionPerformed
        removeSelectedJadwal();
    }//GEN-LAST:event_btnHapusJadwalActionPerformed

    private void btnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBatalActionPerformed
        navigateToDokterList();
    }//GEN-LAST:event_btnBatalActionPerformed

    private void btnSimpanDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanDataActionPerformed
        saveDokter();
    }//GEN-LAST:event_btnSimpanDataActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBatal;
    private javax.swing.JButton btnBatalHeader;
    private javax.swing.JButton btnHapusJadwal;
    private javax.swing.JButton btnSimpanData;
    private javax.swing.JButton btnTambahJadwal;
    private javax.swing.JComboBox<String> cmbHariJadwal;
    private javax.swing.JComboBox<String> cmbPoliUnit;
    private javax.swing.JComboBox<String> cmbSpesialisasi;
    private javax.swing.JComboBox<String> cmbStatus;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblFormNote;
    private javax.swing.JLabel lblFormTitle;
    private javax.swing.JLabel lblHari;
    private javax.swing.JLabel lblJamMulai;
    private javax.swing.JLabel lblJamSelesai;
    private javax.swing.JLabel lblJadwalTitle;
    private javax.swing.JLabel lblKuota;
    private javax.swing.JLabel lblNamaLengkap;
    private javax.swing.JLabel lblNoHp;
    private javax.swing.JLabel lblNoSip;
    private javax.swing.JLabel lblNoStr;
    private javax.swing.JLabel lblPoliUnit;
    private javax.swing.JLabel lblSpesialisasi;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblTarifKonsultasi;
    private javax.swing.JPanel panelActionBar;
    private javax.swing.JPanel panelActionsBottom;
    private javax.swing.JPanel panelBody;
    private javax.swing.JPanel panelFieldHari;
    private javax.swing.JPanel panelFieldJamMulai;
    private javax.swing.JPanel panelFieldJamSelesai;
    private javax.swing.JPanel panelFieldKuota;
    private javax.swing.JPanel panelFieldNama;
    private javax.swing.JPanel panelFieldNoHp;
    private javax.swing.JPanel panelFieldNoSip;
    private javax.swing.JPanel panelFieldNoStr;
    private javax.swing.JPanel panelFieldPoli;
    private javax.swing.JPanel panelFieldSpesialisasi;
    private javax.swing.JPanel panelFieldStatus;
    private javax.swing.JPanel panelFieldTarif;
    private javax.swing.JPanel panelFormCard;
    private javax.swing.JPanel panelFormHeader;
    private javax.swing.JPanel panelJadwal;
    private javax.swing.JPanel panelJadwalButtons;
    private javax.swing.JPanel panelJadwalInput;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelRow1;
    private javax.swing.JPanel panelRow2;
    private javax.swing.JPanel panelRow3;
    private javax.swing.JSpinner spnJamMulai;
    private javax.swing.JSpinner spnJamSelesai;
    private javax.swing.JTable tblJadwal;
    private javax.swing.JTextField txtKuotaJadwal;
    private javax.swing.JTextField txtNamaLengkap;
    private javax.swing.JTextField txtNoHp;
    private javax.swing.JTextField txtNoSip;
    private javax.swing.JTextField txtNoStr;
    private javax.swing.JTextField txtTarifKonsultasi;
    // End of variables declaration//GEN-END:variables
}
