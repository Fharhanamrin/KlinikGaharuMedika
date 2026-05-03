package com.release.klinikgaharumedika.view.panel;

import com.release.klinikgaharumedika.controller.PerawatController;
import com.release.klinikgaharumedika.model.Perawat;
import com.release.klinikgaharumedika.view.DashboardForm;
import java.beans.Beans;
import java.text.ParseException;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicButtonUI;

public class PerawatFormPanel extends javax.swing.JPanel {

    private final PerawatController controller;
    private final Perawat editingPerawat;

    public PerawatFormPanel() {
        this(null);
    }

    public PerawatFormPanel(Perawat editingPerawat) {
        this.editingPerawat = editingPerawat;
        initComponents();
        if (Beans.isDesignTime()) {
            controller = null;
            configureForm();
            return;
        }
        controller = new PerawatController();
        configureForm();
    }

    private void configureForm() {
        txtNamaLengkap.putClientProperty("JTextField.placeholderText", "Contoh: Nur Aisyah");
        txtNoSipp.putClientProperty("JTextField.placeholderText", "Contoh: SIPP-PR-001");
        txtNoHp.putClientProperty("JTextField.placeholderText", "Contoh: 081233330001");

        FormUiStyle.dateSpinner(spnTanggalMulaiKerja);
        cmbShiftKerja.setSelectedIndex(0);
        cmbPoliTugas.setSelectedIndex(0);
        cmbStatus.setSelectedIndex(0);
        applyMode();
        stylePrimaryButton(btnSimpanData);
        FormUiStyle.applyFormStyle(this);
    }

    private void applyMode() {
        if (editingPerawat == null) {
            lblFormTitle.setText("Tambah Perawat");
            btnSimpanData.setText("Simpan Data");
            return;
        }

        lblFormTitle.setText("Edit Perawat");
        btnSimpanData.setText("Simpan Perubahan");
        txtNamaLengkap.setText(editingPerawat.getNama());
        txtNoSipp.setText(editingPerawat.getNoSipp());
        txtNoHp.setText(editingPerawat.getNoHp());
        selectComboItem(cmbShiftKerja, capitalize(editingPerawat.getShift()));
        selectComboItem(cmbPoliTugas, editingPerawat.getPoliTugas());
        selectComboItem(cmbStatus, capitalize(editingPerawat.getStatus()));
    }

    private void selectComboItem(javax.swing.JComboBox<String> comboBox, String value) {
        if (value == null || value.isBlank()) {
            comboBox.setSelectedIndex(0);
            return;
        }
        comboBox.setSelectedItem(value);
    }

    private void stylePrimaryButton(JButton button) {
        FormUiStyle.stylePrimaryButton(button);
    }

    private String validateFormInput() {
        if (txtNamaLengkap.getText().trim().isEmpty()) {
            return "Nama lengkap wajib diisi.";
        }
        if (txtNoSipp.getText().trim().isEmpty()) {
            return "No. SIPP wajib diisi.";
        }
        if (txtNoHp.getText().trim().isEmpty()) {
            return "No. HP wajib diisi.";
        }
        if (cmbShiftKerja.getSelectedIndex() <= 0) {
            return "Shift kerja wajib dipilih.";
        }
        if (cmbPoliTugas.getSelectedIndex() <= 0) {
            return "Poli / Unit tugas wajib dipilih.";
        }
        if (cmbStatus.getSelectedIndex() <= 0) {
            return "Status wajib dipilih.";
        }
        try {
            spnTanggalMulaiKerja.commitEdit();
        } catch (ParseException ex) {
            return "Tanggal mulai kerja belum valid.";
        }
        return null;
    }

    private Perawat buildPerawat(String kodePerawat) {
        Perawat perawat = new Perawat();
        if (editingPerawat != null) {
            perawat.setId(editingPerawat.getId());
        }
        perawat.setKodePerawat(kodePerawat);
        perawat.setNama(txtNamaLengkap.getText().trim());
        perawat.setNoSipp(txtNoSipp.getText().trim());
        perawat.setNoHp(txtNoHp.getText().trim());
        perawat.setShift(cmbShiftKerja.getSelectedItem().toString().toLowerCase());
        perawat.setPoliTugas(cmbPoliTugas.getSelectedItem().toString());
        perawat.setStatus(cmbStatus.getSelectedItem().toString().toLowerCase());
        return perawat;
    }

    private void navigateToPerawatList() {
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof DashboardForm dashboardForm) {
            dashboardForm.showPerawatPage();
        }
    }

    private boolean isEditMode() {
        return editingPerawat != null;
    }

    private void savePerawat() {
        String validationMessage = validateFormInput();
        if (validationMessage != null) {
            JOptionPane.showMessageDialog(this, validationMessage, "Validasi Form", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String kodePerawat = isEditMode()
                    ? editingPerawat.getKodePerawat()
                    : controller.generateKodePerawat();
            Perawat perawat = buildPerawat(kodePerawat);

            boolean berhasil = isEditMode()
                    ? controller.edit(perawat)
                    : controller.tambah(perawat);

            if (berhasil) {
                navigateToPerawatList();
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    isEditMode() ? "Gagal memperbarui perawat." : "Gagal menambahkan perawat.",
                    "Gagal",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    (isEditMode() ? "Gagal memperbarui perawat.\n" : "Gagal menambahkan perawat.\n") + ex.getMessage(),
                    "Gagal",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

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
        panelFieldNoSipp = new javax.swing.JPanel();
        lblNoSipp = new javax.swing.JLabel();
        txtNoSipp = new javax.swing.JTextField();
        panelRow2 = new javax.swing.JPanel();
        panelFieldNoHp = new javax.swing.JPanel();
        lblNoHp = new javax.swing.JLabel();
        txtNoHp = new javax.swing.JTextField();
        panelFieldShift = new javax.swing.JPanel();
        lblShiftKerja = new javax.swing.JLabel();
        cmbShiftKerja = new javax.swing.JComboBox<>();
        panelFieldPoli = new javax.swing.JPanel();
        lblPoliTugas = new javax.swing.JLabel();
        cmbPoliTugas = new javax.swing.JComboBox<>();
        panelRow3 = new javax.swing.JPanel();
        panelFieldTanggal = new javax.swing.JPanel();
        lblTanggalMulaiKerja = new javax.swing.JLabel();
        spnTanggalMulaiKerja = new javax.swing.JSpinner();
        panelFieldStatus = new javax.swing.JPanel();
        lblStatus = new javax.swing.JLabel();
        cmbStatus = new javax.swing.JComboBox<>();
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
                .addComponent(btnBatalHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        panelActionBarLayout.setVerticalGroup(
            panelActionBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelActionBarLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(btnBatalHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        panelFormCard.setBackground(new java.awt.Color(255, 255, 255));
        panelFormCard.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 218, 208)));

        panelFormHeader.setBackground(new java.awt.Color(255, 255, 255));

        lblFormTitle.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblFormTitle.setText("Tambah Perawat");

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

        panelFieldNoSipp.setBackground(new java.awt.Color(255, 255, 255));

        lblNoSipp.setText("No. SIPP *");

        javax.swing.GroupLayout panelFieldNoSippLayout = new javax.swing.GroupLayout(panelFieldNoSipp);
        panelFieldNoSipp.setLayout(panelFieldNoSippLayout);
        panelFieldNoSippLayout.setHorizontalGroup(
            panelFieldNoSippLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblNoSipp)
            .addComponent(txtNoSipp)
        );
        panelFieldNoSippLayout.setVerticalGroup(
            panelFieldNoSippLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldNoSippLayout.createSequentialGroup()
                .addComponent(lblNoSipp)
                .addGap(6, 6, 6)
                .addComponent(txtNoSipp, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout panelRow1Layout = new javax.swing.GroupLayout(panelRow1);
        panelRow1.setLayout(panelRow1Layout);
        panelRow1Layout.setHorizontalGroup(
            panelRow1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRow1Layout.createSequentialGroup()
                .addComponent(panelFieldNama, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(14, 14, 14)
                .addComponent(panelFieldNoSipp, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelRow1Layout.setVerticalGroup(
            panelRow1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelFieldNama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelFieldNoSipp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        panelRow2.setBackground(new java.awt.Color(255, 255, 255));

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

        panelFieldShift.setBackground(new java.awt.Color(255, 255, 255));

        lblShiftKerja.setText("Shift kerja *");

        cmbShiftKerja.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih...", "Pagi", "Sore", "Malam" }));

        javax.swing.GroupLayout panelFieldShiftLayout = new javax.swing.GroupLayout(panelFieldShift);
        panelFieldShift.setLayout(panelFieldShiftLayout);
        panelFieldShiftLayout.setHorizontalGroup(
            panelFieldShiftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblShiftKerja)
            .addComponent(cmbShiftKerja, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelFieldShiftLayout.setVerticalGroup(
            panelFieldShiftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldShiftLayout.createSequentialGroup()
                .addComponent(lblShiftKerja)
                .addGap(6, 6, 6)
                .addComponent(cmbShiftKerja, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelFieldPoli.setBackground(new java.awt.Color(255, 255, 255));

        lblPoliTugas.setText("Poli / Unit tugas *");

        cmbPoliTugas.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih...", "Poli Umum", "Poli Penyakit Dalam", "Poli Anak", "Poli Jantung", "Poli Saraf", "Poli Kulit", "UGD", "Rawat Jalan" }));

        javax.swing.GroupLayout panelFieldPoliLayout = new javax.swing.GroupLayout(panelFieldPoli);
        panelFieldPoli.setLayout(panelFieldPoliLayout);
        panelFieldPoliLayout.setHorizontalGroup(
            panelFieldPoliLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblPoliTugas)
            .addComponent(cmbPoliTugas, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelFieldPoliLayout.setVerticalGroup(
            panelFieldPoliLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldPoliLayout.createSequentialGroup()
                .addComponent(lblPoliTugas)
                .addGap(6, 6, 6)
                .addComponent(cmbPoliTugas, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout panelRow2Layout = new javax.swing.GroupLayout(panelRow2);
        panelRow2.setLayout(panelRow2Layout);
        panelRow2Layout.setHorizontalGroup(
            panelRow2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRow2Layout.createSequentialGroup()
                .addComponent(panelFieldNoHp, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(panelFieldShift, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(panelFieldPoli, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelRow2Layout.setVerticalGroup(
            panelRow2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelFieldNoHp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelFieldShift, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelFieldPoli, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        panelRow3.setBackground(new java.awt.Color(255, 255, 255));

        panelFieldTanggal.setBackground(new java.awt.Color(255, 255, 255));

        lblTanggalMulaiKerja.setText("Tanggal mulai kerja *");

        javax.swing.GroupLayout panelFieldTanggalLayout = new javax.swing.GroupLayout(panelFieldTanggal);
        panelFieldTanggal.setLayout(panelFieldTanggalLayout);
        panelFieldTanggalLayout.setHorizontalGroup(
            panelFieldTanggalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTanggalMulaiKerja)
            .addComponent(spnTanggalMulaiKerja)
        );
        panelFieldTanggalLayout.setVerticalGroup(
            panelFieldTanggalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFieldTanggalLayout.createSequentialGroup()
                .addComponent(lblTanggalMulaiKerja)
                .addGap(6, 6, 6)
                .addComponent(spnTanggalMulaiKerja, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelFieldStatus.setBackground(new java.awt.Color(255, 255, 255));

        lblStatus.setText("Status *");

        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih...", "Aktif", "Nonaktif", "Cuti" }));

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
                .addComponent(panelFieldTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(panelFieldStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        panelRow3Layout.setVerticalGroup(
            panelRow3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelFieldTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelFieldStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout panelBodyLayout = new javax.swing.GroupLayout(panelBody);
        panelBody.setLayout(panelBodyLayout);
        panelBodyLayout.setHorizontalGroup(
            panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelRow1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelRow2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelRow3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelBodyLayout.setVerticalGroup(
            panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBodyLayout.createSequentialGroup()
                .addComponent(panelRow1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(panelRow2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(panelRow3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
        btnSimpanData.setOpaque(true);
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
                .addComponent(btnSimpanData, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelActionsBottomLayout.setVerticalGroup(
            panelActionsBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelActionsBottomLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(panelActionsBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBatal, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSimpanData, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
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
                .addGap(0, 0, 0)
                .addComponent(panelActionBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(panelFormCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
        navigateToPerawatList();
    }//GEN-LAST:event_btnBatalHeaderActionPerformed

    private void btnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBatalActionPerformed
        navigateToPerawatList();
    }//GEN-LAST:event_btnBatalActionPerformed

    private void btnSimpanDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanDataActionPerformed
        savePerawat();
    }//GEN-LAST:event_btnSimpanDataActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBatal;
    private javax.swing.JButton btnBatalHeader;
    private javax.swing.JButton btnSimpanData;
    private javax.swing.JComboBox<String> cmbPoliTugas;
    private javax.swing.JComboBox<String> cmbShiftKerja;
    private javax.swing.JComboBox<String> cmbStatus;
    private javax.swing.JLabel lblFormNote;
    private javax.swing.JLabel lblFormTitle;
    private javax.swing.JLabel lblNamaLengkap;
    private javax.swing.JLabel lblNoHp;
    private javax.swing.JLabel lblNoSipp;
    private javax.swing.JLabel lblPoliTugas;
    private javax.swing.JLabel lblShiftKerja;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblTanggalMulaiKerja;
    private javax.swing.JPanel panelActionBar;
    private javax.swing.JPanel panelActionsBottom;
    private javax.swing.JPanel panelBody;
    private javax.swing.JPanel panelFieldNama;
    private javax.swing.JPanel panelFieldNoHp;
    private javax.swing.JPanel panelFieldNoSipp;
    private javax.swing.JPanel panelFieldPoli;
    private javax.swing.JPanel panelFieldShift;
    private javax.swing.JPanel panelFieldStatus;
    private javax.swing.JPanel panelFieldTanggal;
    private javax.swing.JPanel panelFormCard;
    private javax.swing.JPanel panelFormHeader;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelRow1;
    private javax.swing.JPanel panelRow2;
    private javax.swing.JPanel panelRow3;
    private javax.swing.JSpinner spnTanggalMulaiKerja;
    private javax.swing.JTextField txtNamaLengkap;
    private javax.swing.JTextField txtNoHp;
    private javax.swing.JTextField txtNoSipp;
    // End of variables declaration//GEN-END:variables
}
