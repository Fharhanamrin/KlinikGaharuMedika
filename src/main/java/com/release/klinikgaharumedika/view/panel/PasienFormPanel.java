package com.release.klinikgaharumedika.view.panel;

import com.release.klinikgaharumedika.controller.PasienController;
import com.release.klinikgaharumedika.model.Pasien;
import com.release.klinikgaharumedika.view.DashboardForm;
import java.awt.Color;
import java.beans.Beans;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicButtonUI;

public class PasienFormPanel extends javax.swing.JPanel {

    private static final Color PAGE_BACKGROUND = new Color(243, 240, 233);
    private static final Color PRIMARY_GREEN = new Color(28, 112, 77);
    private static final Color DISABLED_INPUT = new Color(245, 245, 245);
    private static final String UNKNOWN_BLOOD_TYPE = "Tidak Diketahui";
    private static final String[] GOLONGAN_DARAH_ITEMS = {
        UNKNOWN_BLOOD_TYPE, "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    };
    private static final String[] PROVINSI_ITEMS = {
        "Pilih...", "Aceh", "Sumatera Utara", "Sumatera Barat", "Riau",
        "Kepulauan Riau", "Jambi", "Sumatera Selatan", "Kepulauan Bangka Belitung",
        "Bengkulu", "Lampung", "DKI Jakarta", "Jawa Barat", "Banten",
        "Jawa Tengah", "DI Yogyakarta", "Jawa Timur", "Bali", "Nusa Tenggara Barat",
        "Nusa Tenggara Timur", "Kalimantan Barat", "Kalimantan Tengah",
        "Kalimantan Selatan", "Kalimantan Timur", "Kalimantan Utara",
        "Sulawesi Utara", "Gorontalo", "Sulawesi Tengah", "Sulawesi Barat",
        "Sulawesi Selatan", "Sulawesi Tenggara", "Maluku", "Maluku Utara",
        "Papua Barat", "Papua Barat Daya", "Papua", "Papua Selatan",
        "Papua Tengah", "Papua Pegunungan"
    };

    private final PasienController controller;
    private final Pasien editingPasien;

    public PasienFormPanel() {
        this(null);
    }

    public PasienFormPanel(Pasien editingPasien) {
        this.editingPasien = editingPasien;
        initComponents();
        if (Beans.isDesignTime()) {
            controller = null;
            initializeComboBoxModels();
            configureInputComponents();
            stylePrimaryButton(btnSimpanData);
            FormUiStyle.applyFormStyle(this);
            return;
        }
        controller = new PasienController();
        configureForm();
    }

    private void configureForm() {
        initializeComboBoxModels();
        configureInputComponents();
        bindComponentActions();
        applyMode();
        updateNomorPenjaminState();
        stylePrimaryButton(btnSimpanData);
        FormUiStyle.applyFormStyle(this);
    }

    private void configureInputComponents() {
        setBackground(PAGE_BACKGROUND);
        panelMain.setBackground(PAGE_BACKGROUND);
        panelActionBar.setBackground(PAGE_BACKGROUND);

        txtNamaLengkap.putClientProperty("JTextField.placeholderText", "Contoh: Ahmad Fauzi");
        txtNik.putClientProperty("JTextField.placeholderText", "Contoh: 3175110201000001");
        txtTempatLahir.putClientProperty("JTextField.placeholderText", "Contoh: Jakarta");
        txtKota.putClientProperty("JTextField.placeholderText", "Contoh: Jakarta Pusat");
        txtKodePos.putClientProperty("JTextField.placeholderText", "Contoh: 41001");
        txtNoHpPasien.putClientProperty("JTextField.placeholderText", "Contoh: 081211110001");
        txtKontakDarurat.putClientProperty("JTextField.placeholderText", "Contoh: Hendra Fauzi");
        txtNoHpDarurat.putClientProperty("JTextField.placeholderText", "Contoh: 081311110001");
        txtPekerjaan.putClientProperty("JTextField.placeholderText", "Contoh: Karyawan Swasta");
        txtBpjs.putClientProperty("JTextField.placeholderText", "Contoh: 0001456799003 / ASN-000005");
        txtAlergi.putClientProperty("JTextField.placeholderText", "Contoh: Penicillin");
        txtAlamat.putClientProperty("JTextArea.placeholderText", "Contoh: Jl Contoh Sehat No 1");
        txtRiwayatPenyakit.putClientProperty("JTextArea.placeholderText", "Contoh: Hipertensi");

        txtAlamat.setLineWrap(true);
        txtAlamat.setWrapStyleWord(true);
        txtAlamat.setRows(3);
        txtRiwayatPenyakit.setLineWrap(true);
        txtRiwayatPenyakit.setWrapStyleWord(true);
        txtRiwayatPenyakit.setRows(3);

        FormUiStyle.dateSpinner(spnTanggalLahir);

        jScrollPane2.setBorder(null);
        jScrollPane2.getViewport().setBackground(Color.WHITE);
        jScrollPane2.getVerticalScrollBar().setUnitIncrement(18);

        safeSetSelectedIndex(cmbJenisKelamin, 0);
        safeSetSelectedIndex(cmbGolDarah, 0);
        safeSetSelectedIndex(cmbProvinsi, 0);
        safeSetSelectedIndex(cmbHubunganDarurat, 0);
        safeSetSelectedIndex(cmbJenisPasien, 0);
    }

    private void initializeComboBoxModels() {
        cmbJenisKelamin.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {
            "Pilih...", "Laki-laki", "Perempuan"
        }));
        cmbGolDarah.setModel(new javax.swing.DefaultComboBoxModel<>(GOLONGAN_DARAH_ITEMS));
        cmbProvinsi.setModel(new javax.swing.DefaultComboBoxModel<>(PROVINSI_ITEMS));
        cmbHubunganDarurat.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {
            "Pilih...", "Orang Tua", "Suami/Istri", "Saudara", "Anak", "Lainnya"
        }));
        cmbJenisPasien.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {
            "Pilih...", "Umum", "BPJS", "Asuransi Swasta"
        }));
    }

    private void bindComponentActions() {
        cmbJenisPasien.addActionListener(evt -> updateNomorPenjaminState());
    }

    private void safeSetSelectedIndex(javax.swing.JComboBox<?> combo, int index) {
        if (combo.getItemCount() > index) {
            combo.setSelectedIndex(index);
        }
    }

    private void stylePrimaryButton(JButton button) {
        FormUiStyle.stylePrimaryButton(button);
    }

    private void navigateToPasienList() {
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof DashboardForm dashboardForm) {
            dashboardForm.showPasienPage();
        }
    }

    private void applyMode() {
        if (!isEditMode()) {
            lblFormTitle.setText("Tambah Pasien Baru");
            btnSimpanData.setText("Simpan");
            resetTanggalLahir();
            return;
        }

        lblFormTitle.setText("Edit Pasien");
        btnSimpanData.setText("Simpan Perubahan");
        txtNamaLengkap.setText(editingPasien.getNama());
        txtNik.setText(editingPasien.getNik());
        txtTempatLahir.setText(editingPasien.getTempatLahir());
        setTanggalLahir(editingPasien.getTanggalLahir());
        txtAlamat.setText(editingPasien.getAlamat());
        txtKota.setText(editingPasien.getKota());
        txtKodePos.setText(editingPasien.getKodePos());
        txtNoHpPasien.setText(editingPasien.getNoHp());
        txtKontakDarurat.setText(editingPasien.getNamaKontakDarurat());
        txtNoHpDarurat.setText(editingPasien.getNoHpDarurat());
        txtPekerjaan.setText(editingPasien.getPekerjaan());
        txtBpjs.setText(editingPasien.getNoBpjs());
        txtAlergi.setText(editingPasien.getAlergiObat());
        txtRiwayatPenyakit.setText(editingPasien.getRiwayatPenyakit());

        selectCombo(cmbGolDarah, normalizeGolonganDarah(editingPasien.getGolonganDarah()));
        selectCombo(cmbJenisPasien, toJenisPasienDisplay(editingPasien.getJenisPasien()));
        selectCombo(cmbProvinsi, editingPasien.getProvinsi());
        selectCombo(cmbHubunganDarurat, normalizeHubunganDarurat(editingPasien.getHubunganDarurat()));

        String jenisKelamin = editingPasien.getJenisKelamin();
        if ("L".equalsIgnoreCase(jenisKelamin)) {
            cmbJenisKelamin.setSelectedItem("Laki-laki");
        } else if ("P".equalsIgnoreCase(jenisKelamin)) {
            cmbJenisKelamin.setSelectedItem("Perempuan");
        }
    }

    private void selectCombo(javax.swing.JComboBox<String> combo, String value) {
        if (value == null || value.isBlank()) {
            safeSetSelectedIndex(combo, 0);
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            String item = combo.getItemAt(i);
            if (item != null && item.equalsIgnoreCase(value)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        safeSetSelectedIndex(combo, 0);
    }

    private boolean isEditMode() {
        return editingPasien != null;
    }

    private String validateFormInput() {
        if (txtNamaLengkap.getText().trim().isEmpty()) return "Nama lengkap wajib diisi.";
        if (!txtNik.getText().trim().matches("\\d{16}")) return "NIK wajib 16 digit angka.";
        if (txtTempatLahir.getText().trim().isEmpty()) return "Tempat lahir wajib diisi.";
        if (parseTanggalLahir() == null) return "Tanggal lahir wajib diisi dengan format dd/MM/yyyy.";
        if (cmbJenisKelamin.getSelectedIndex() <= 0) return "Jenis kelamin wajib dipilih.";
        if (txtAlamat.getText().trim().isEmpty()) return "Alamat lengkap wajib diisi.";
        if (txtKota.getText().trim().isEmpty()) return "Kota / Kabupaten wajib diisi.";
        if (cmbProvinsi.getSelectedIndex() <= 0) return "Provinsi wajib dipilih.";
        if (cmbJenisPasien.getSelectedIndex() <= 0) return "Jenis pasien wajib dipilih.";

        String noHpPasien = normalizePhoneNumber(txtNoHpPasien.getText());
        if (noHpPasien == null) return "No. HP pasien wajib diisi.";
        if (!isValidPhoneNumber(noHpPasien)) return "No. HP pasien harus diawali 08 dan terdiri dari 10 sampai 15 digit.";

        String noHpDarurat = normalizePhoneNumber(txtNoHpDarurat.getText());
        if (noHpDarurat != null && !isValidPhoneNumber(noHpDarurat)) {
            return "No. HP darurat harus diawali 08 dan terdiri dari 10 sampai 15 digit.";
        }
        if (noHpDarurat != null && txtKontakDarurat.getText().trim().isEmpty()) {
            return "Nama kontak darurat wajib diisi jika No. HP darurat diisi.";
        }
        if (noHpDarurat != null && cmbHubunganDarurat.getSelectedIndex() <= 0) {
            return "Hubungan kontak darurat wajib dipilih jika No. HP darurat diisi.";
        }
        if ("BPJS".equalsIgnoreCase(getSelectedText(cmbJenisPasien)) && txtBpjs.getText().trim().isEmpty()) {
            return "No. BPJS wajib diisi untuk pasien BPJS.";
        }
        return null;
    }

    private LocalDate parseTanggalLahir() {
        String value = getTanggalLahirEditor().getText().trim();
        if (value.isEmpty()) {
            return null;
        }
        try {
            spnTanggalLahir.commitEdit();
            Date birthDate = (Date) spnTanggalLahir.getValue();
            return birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (Exception ex) {
            return null;
        }
    }

    private Pasien buildPasien() {
        Pasien pasien = new Pasien();
        if (isEditMode()) {
            pasien.setId(editingPasien.getId());
            pasien.setNoRm(editingPasien.getNoRm());
            pasien.setKelurahan(editingPasien.getKelurahan());
            pasien.setKecamatan(editingPasien.getKecamatan());
        }

        pasien.setNama(txtNamaLengkap.getText().trim());
        pasien.setNik(txtNik.getText().trim());
        pasien.setTanggalLahir(parseTanggalLahir());
        pasien.setTempatLahir(blankToNull(txtTempatLahir.getText()));
        pasien.setJenisKelamin(cmbJenisKelamin.getSelectedIndex() == 1 ? "L" : "P");
        pasien.setGolonganDarah(normalizeGolonganDarah(getSelectedText(cmbGolDarah)));
        pasien.setAlamat(blankToNull(txtAlamat.getText()));
        pasien.setKota(blankToNull(txtKota.getText()));
        pasien.setProvinsi(cmbProvinsi.getSelectedIndex() <= 0 ? null : getSelectedText(cmbProvinsi));
        pasien.setKodePos(blankToNull(txtKodePos.getText()));
        pasien.setNoHp(normalizePhoneNumber(txtNoHpPasien.getText()));
        pasien.setNamaKontakDarurat(blankToNull(txtKontakDarurat.getText()));
        pasien.setNoHpDarurat(normalizePhoneNumber(txtNoHpDarurat.getText()));
        pasien.setHubunganDarurat(cmbHubunganDarurat.getSelectedIndex() <= 0 ? null : getSelectedText(cmbHubunganDarurat));
        pasien.setPekerjaan(blankToNull(txtPekerjaan.getText()));
        pasien.setJenisPasien(toJenisPasienValue(getSelectedText(cmbJenisPasien)));
        pasien.setNoBpjs(requiresNomorPenjamin(pasien.getJenisPasien()) ? blankToNull(txtBpjs.getText()) : null);
        pasien.setAlergiObat(blankToNull(txtAlergi.getText()));
        pasien.setRiwayatPenyakit(blankToNull(txtRiwayatPenyakit.getText()));
        return pasien;
    }

    private void savePasien() {
        String validationMessage = validateFormInput();
        if (validationMessage != null) {
            JOptionPane.showMessageDialog(this, validationMessage, "Validasi Form", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Pasien pasien = buildPasien();
            boolean berhasil = isEditMode() ? controller.edit(pasien) : controller.tambah(pasien);
            if (berhasil) {
                JOptionPane.showMessageDialog(
                        this,
                        isEditMode() ? "Data pasien berhasil diperbarui." : "Data pasien berhasil ditambahkan.",
                        "Sukses",
                        JOptionPane.INFORMATION_MESSAGE
                );
                navigateToPasienList();
                return;
            }
            JOptionPane.showMessageDialog(
                    this,
                    isEditMode() ? "Gagal memperbarui pasien." : "Gagal menambahkan pasien.",
                    "Gagal",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    (isEditMode() ? "Gagal memperbarui pasien.\n" : "Gagal menambahkan pasien.\n") + ex.getMessage(),
                    "Gagal",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizePhoneNumber(String value) {
        if (value == null) {
            return null;
        }
        String digits = value.replaceAll("\\D", "");
        return digits.isEmpty() ? null : digits;
    }

    private boolean isValidPhoneNumber(String value) {
        return value != null && value.matches("08\\d{8,13}");
    }

    private String getSelectedText(javax.swing.JComboBox<String> combo) {
        Object selected = combo.getSelectedItem();
        return selected != null ? selected.toString().trim() : "";
    }

    private String normalizeGolonganDarah(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN_BLOOD_TYPE;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace(" ", "");
        return switch (normalized) {
            case "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-" -> normalized;
            case "TIDAKDIKETAHUI" -> UNKNOWN_BLOOD_TYPE;
            default -> UNKNOWN_BLOOD_TYPE;
        };
    }

    private String normalizeHubunganDarurat(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if ("Orang tua".equalsIgnoreCase(value)) {
            return "Orang Tua";
        }
        if ("Pasangan".equalsIgnoreCase(value)) {
            return "Suami/Istri";
        }
        return value;
    }

    private String toJenisPasienDisplay(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if ("bpjs".equalsIgnoreCase(value)) {
            return "BPJS";
        }
        if ("asuransi".equalsIgnoreCase(value)) {
            return "Asuransi Swasta";
        }
        return "Umum";
    }

    private String toJenisPasienValue(String value) {
        if ("BPJS".equalsIgnoreCase(value)) {
            return "bpjs";
        }
        if (value != null && value.toLowerCase().contains("asuransi")) {
            return "asuransi";
        }
        return "umum";
    }

    private boolean requiresNomorPenjamin(String jenisPasien) {
        return "bpjs".equalsIgnoreCase(jenisPasien) || "asuransi".equalsIgnoreCase(jenisPasien);
    }

    private void updateNomorPenjaminState() {
        boolean enabled = requiresNomorPenjamin(toJenisPasienValue(getSelectedText(cmbJenisPasien)));
        txtBpjs.setEnabled(enabled);
        txtBpjs.setBackground(enabled ? Color.WHITE : DISABLED_INPUT);
        txtBpjs.putClientProperty(
                "JTextField.placeholderText",
                enabled ? "Contoh: 0001456799003 / ASN-000005" : "Tidak diperlukan untuk pasien umum"
        );
        if (!enabled) {
            txtBpjs.setText("");
        }
        FormUiStyle.styleTextField(txtBpjs);
    }

    private void setTanggalLahir(LocalDate tanggalLahir) {
        if (tanggalLahir == null) {
            resetTanggalLahir();
            return;
        }
        Date date = Date.from(tanggalLahir.atStartOfDay(ZoneId.systemDefault()).toInstant());
        spnTanggalLahir.setValue(date);
    }

    private void resetTanggalLahir() {
        spnTanggalLahir.setValue(new Date());
        getTanggalLahirEditor().setText("");
    }

    private javax.swing.JFormattedTextField getTanggalLahirEditor() {
        return ((JSpinner.DefaultEditor) spnTanggalLahir.getEditor()).getTextField();
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
        jScrollPane2 = new javax.swing.JScrollPane();
        panelBody = new javax.swing.JPanel();
        panelSectionIdentitas = new javax.swing.JPanel();
        lblSectionIdentitas = new javax.swing.JLabel();
        sepIdentitas = new javax.swing.JSeparator();
        jLabelNama = new javax.swing.JLabel();
        txtNamaLengkap = new javax.swing.JTextField();
        jLabelNik = new javax.swing.JLabel();
        txtNik = new javax.swing.JTextField();
        jLabelTempatLahir = new javax.swing.JLabel();
        txtTempatLahir = new javax.swing.JTextField();
        jLabelTanggalLahir = new javax.swing.JLabel();
        spnTanggalLahir = new javax.swing.JSpinner();
        jLabelJK = new javax.swing.JLabel();
        cmbJenisKelamin = new javax.swing.JComboBox();
        jLabelGolDarah = new javax.swing.JLabel();
        cmbGolDarah = new javax.swing.JComboBox();
        jLabelAlamat = new javax.swing.JLabel();
        jScrollAlamat = new javax.swing.JScrollPane();
        txtAlamat = new javax.swing.JTextArea();
        jLabelKota = new javax.swing.JLabel();
        txtKota = new javax.swing.JTextField();
        jLabelProvinsi = new javax.swing.JLabel();
        cmbProvinsi = new javax.swing.JComboBox();
        jLabelKodePos = new javax.swing.JLabel();
        txtKodePos = new javax.swing.JTextField();
        panelSectionKontak = new javax.swing.JPanel();
        lblSectionKontak = new javax.swing.JLabel();
        sepKontak = new javax.swing.JSeparator();
        jLabelNoHpPasien = new javax.swing.JLabel();
        txtNoHpPasien = new javax.swing.JTextField();
        jLabelNamaKontakDarurat = new javax.swing.JLabel();
        txtKontakDarurat = new javax.swing.JTextField();
        jLabelNoHpDarurat = new javax.swing.JLabel();
        txtNoHpDarurat = new javax.swing.JTextField();
        jLabelHubunganDarurat = new javax.swing.JLabel();
        cmbHubunganDarurat = new javax.swing.JComboBox();
        jLabelPekerjaan = new javax.swing.JLabel();
        txtPekerjaan = new javax.swing.JTextField();
        panelSectionMedis = new javax.swing.JPanel();
        lblSectionMedis = new javax.swing.JLabel();
        sepMedis = new javax.swing.JSeparator();
        jLabelJenisPasien = new javax.swing.JLabel();
        cmbJenisPasien = new javax.swing.JComboBox();
        jLabelBpjs = new javax.swing.JLabel();
        txtBpjs = new javax.swing.JTextField();
        jLabelAlergi = new javax.swing.JLabel();
        txtAlergi = new javax.swing.JTextField();
        jLabelRiwayat = new javax.swing.JLabel();
        jScrollRiwayat = new javax.swing.JScrollPane();
        txtRiwayatPenyakit = new javax.swing.JTextArea();
        panelActionsBottom = new javax.swing.JPanel();
        btnBatal = new javax.swing.JButton();
        btnSimpanData = new javax.swing.JButton();

        panelMain.setBackground(new java.awt.Color(243, 240, 233));

        panelActionBar.setBackground(new java.awt.Color(243, 240, 233));

        btnBatalHeader.setText("Batal");
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
        lblFormTitle.setText("Tambah Pasien Baru");

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
            panelFormHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(lblFormTitle)
            .addComponent(lblFormNote)
        );

        panelBody.setBackground(new java.awt.Color(255, 255, 255));

        panelSectionIdentitas.setBackground(new java.awt.Color(255, 255, 255));

        lblSectionIdentitas.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSectionIdentitas.setText("Identitas Diri");

        jLabelNama.setText("Nama lengkap *");

        jLabelNik.setText("NIK (16 digit) *");

        jLabelTempatLahir.setText("Tempat lahir *");

        jLabelTanggalLahir.setText("Tanggal lahir *");

        jLabelJK.setText("Jenis kelamin *");

        jLabelGolDarah.setText("Golongan darah");

        jLabelAlamat.setText("Alamat lengkap *");

        txtAlamat.setColumns(20);
        txtAlamat.setRows(3);
        jScrollAlamat.setViewportView(txtAlamat);

        jLabelKota.setText("Kota / Kabupaten *");

        jLabelProvinsi.setText("Provinsi *");

        jLabelKodePos.setText("Kode pos");

        javax.swing.GroupLayout panelSectionIdentitasLayout = new javax.swing.GroupLayout(panelSectionIdentitas);
        panelSectionIdentitas.setLayout(panelSectionIdentitasLayout);
        panelSectionIdentitasLayout.setHorizontalGroup(
            panelSectionIdentitasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblSectionIdentitas)
            .addComponent(sepIdentitas)
            .addComponent(jLabelNama)
            .addComponent(txtNamaLengkap)
            .addComponent(jLabelNik)
            .addComponent(txtNik)
            .addComponent(jLabelTempatLahir)
            .addComponent(txtTempatLahir)
            .addComponent(jLabelTanggalLahir)
            .addComponent(spnTanggalLahir)
            .addComponent(jLabelJK)
            .addComponent(cmbJenisKelamin, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabelGolDarah)
            .addComponent(cmbGolDarah, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabelAlamat)
            .addComponent(jScrollAlamat)
            .addComponent(jLabelKota)
            .addComponent(txtKota)
            .addComponent(jLabelProvinsi)
            .addComponent(cmbProvinsi, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabelKodePos)
            .addComponent(txtKodePos)
        );
        panelSectionIdentitasLayout.setVerticalGroup(
            panelSectionIdentitasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSectionIdentitasLayout.createSequentialGroup()
                .addComponent(lblSectionIdentitas)
                .addGap(6, 6, 6)
                .addComponent(sepIdentitas, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabelNama)
                .addGap(6, 6, 6)
                .addComponent(txtNamaLengkap, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabelNik)
                .addGap(6, 6, 6)
                .addComponent(txtNik, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabelTempatLahir)
                .addGap(6, 6, 6)
                .addComponent(txtTempatLahir, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabelTanggalLahir)
                .addGap(6, 6, 6)
                .addComponent(spnTanggalLahir, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabelJK)
                .addGap(6, 6, 6)
                .addComponent(cmbJenisKelamin, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabelGolDarah)
                .addGap(6, 6, 6)
                .addComponent(cmbGolDarah, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabelAlamat)
                .addGap(6, 6, 6)
                .addComponent(jScrollAlamat, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabelKota)
                .addGap(6, 6, 6)
                .addComponent(txtKota, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabelProvinsi)
                .addGap(6, 6, 6)
                .addComponent(cmbProvinsi, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabelKodePos)
                .addGap(6, 6, 6)
                .addComponent(txtKodePos, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelSectionKontak.setBackground(new java.awt.Color(255, 255, 255));

        lblSectionKontak.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSectionKontak.setText("Kontak");

        jLabelNoHpPasien.setText("No. HP pasien *");

        jLabelNamaKontakDarurat.setText("Nama kontak darurat");

        jLabelNoHpDarurat.setText("No. HP darurat");

        jLabelHubunganDarurat.setText("Hubungan kontak darurat");

        jLabelPekerjaan.setText("Pekerjaan");

        javax.swing.GroupLayout panelSectionKontakLayout = new javax.swing.GroupLayout(panelSectionKontak);
        panelSectionKontak.setLayout(panelSectionKontakLayout);
        panelSectionKontakLayout.setHorizontalGroup(
            panelSectionKontakLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblSectionKontak)
            .addComponent(sepKontak)
            .addComponent(jLabelNoHpPasien)
            .addComponent(txtNoHpPasien)
            .addComponent(jLabelNamaKontakDarurat)
            .addComponent(txtKontakDarurat)
            .addComponent(jLabelNoHpDarurat)
            .addComponent(txtNoHpDarurat)
            .addComponent(jLabelHubunganDarurat)
            .addComponent(cmbHubunganDarurat, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabelPekerjaan)
            .addComponent(txtPekerjaan)
        );
        panelSectionKontakLayout.setVerticalGroup(
            panelSectionKontakLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSectionKontakLayout.createSequentialGroup()
                .addComponent(lblSectionKontak)
                .addGap(6, 6, 6)
                .addComponent(sepKontak, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabelNoHpPasien)
                .addGap(6, 6, 6)
                .addComponent(txtNoHpPasien, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabelNamaKontakDarurat)
                .addGap(6, 6, 6)
                .addComponent(txtKontakDarurat, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabelNoHpDarurat)
                .addGap(6, 6, 6)
                .addComponent(txtNoHpDarurat, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabelHubunganDarurat)
                .addGap(6, 6, 6)
                .addComponent(cmbHubunganDarurat, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabelPekerjaan)
                .addGap(6, 6, 6)
                .addComponent(txtPekerjaan, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelSectionMedis.setBackground(new java.awt.Color(255, 255, 255));

        lblSectionMedis.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSectionMedis.setText("Medis & Asuransi");

        jLabelJenisPasien.setText("Jenis pasien *");

        jLabelBpjs.setText("No. BPJS / Asuransi");

        jLabelAlergi.setText("Alergi obat");

        jLabelRiwayat.setText("Riwayat penyakit");

        txtRiwayatPenyakit.setColumns(20);
        txtRiwayatPenyakit.setRows(3);
        jScrollRiwayat.setViewportView(txtRiwayatPenyakit);

        javax.swing.GroupLayout panelSectionMedisLayout = new javax.swing.GroupLayout(panelSectionMedis);
        panelSectionMedis.setLayout(panelSectionMedisLayout);
        panelSectionMedisLayout.setHorizontalGroup(
            panelSectionMedisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblSectionMedis)
            .addComponent(sepMedis)
            .addComponent(jLabelJenisPasien)
            .addComponent(cmbJenisPasien, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabelBpjs)
            .addComponent(txtBpjs)
            .addComponent(jLabelAlergi)
            .addComponent(txtAlergi)
            .addComponent(jLabelRiwayat)
            .addComponent(jScrollRiwayat)
        );
        panelSectionMedisLayout.setVerticalGroup(
            panelSectionMedisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSectionMedisLayout.createSequentialGroup()
                .addComponent(lblSectionMedis)
                .addGap(6, 6, 6)
                .addComponent(sepMedis, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabelJenisPasien)
                .addGap(6, 6, 6)
                .addComponent(cmbJenisPasien, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabelBpjs)
                .addGap(6, 6, 6)
                .addComponent(txtBpjs, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabelAlergi)
                .addGap(6, 6, 6)
                .addComponent(txtAlergi, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabelRiwayat)
                .addGap(6, 6, 6)
                .addComponent(jScrollRiwayat, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout panelBodyLayout = new javax.swing.GroupLayout(panelBody);
        panelBody.setLayout(panelBodyLayout);
        panelBodyLayout.setHorizontalGroup(
            panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelSectionIdentitas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelSectionKontak, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelSectionMedis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelBodyLayout.setVerticalGroup(
            panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBodyLayout.createSequentialGroup()
                .addComponent(panelSectionIdentitas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(panelSectionKontak, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(panelSectionMedis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jScrollPane2.setViewportView(panelBody);

        panelActionsBottom.setBackground(new java.awt.Color(255, 255, 255));

        btnBatal.setText("Batal");
        btnBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBatalActionPerformed(evt);
            }
        });

        btnSimpanData.setText("Simpan");
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
                .addContainerGap(625, Short.MAX_VALUE)
                .addComponent(btnBatal, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(btnSimpanData, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelActionsBottomLayout.setVerticalGroup(
            panelActionsBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(btnBatal, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnSimpanData, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout panelFormCardLayout = new javax.swing.GroupLayout(panelFormCard);
        panelFormCard.setLayout(panelFormCardLayout);
        panelFormCardLayout.setHorizontalGroup(
            panelFormCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormCardLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelFormCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelFormHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2)
                    .addComponent(panelActionsBottom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        panelFormCardLayout.setVerticalGroup(
            panelFormCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormCardLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(panelFormHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(jScrollPane2)
                .addGap(14, 14, 14)
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
                .addComponent(panelFormCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
        navigateToPasienList();
    }//GEN-LAST:event_btnBatalHeaderActionPerformed

    private void btnSimpanDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanDataActionPerformed
        savePasien();
    }//GEN-LAST:event_btnSimpanDataActionPerformed

    private void btnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBatalActionPerformed
        navigateToPasienList();
    }//GEN-LAST:event_btnBatalActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBatal;
    private javax.swing.JButton btnBatalHeader;
    private javax.swing.JButton btnSimpanData;
    private javax.swing.JComboBox cmbGolDarah;
    private javax.swing.JComboBox cmbHubunganDarurat;
    private javax.swing.JComboBox cmbJenisKelamin;
    private javax.swing.JComboBox cmbJenisPasien;
    private javax.swing.JComboBox cmbProvinsi;
    private javax.swing.JLabel jLabelAlamat;
    private javax.swing.JLabel jLabelAlergi;
    private javax.swing.JLabel jLabelBpjs;
    private javax.swing.JLabel jLabelGolDarah;
    private javax.swing.JLabel jLabelHubunganDarurat;
    private javax.swing.JLabel jLabelJK;
    private javax.swing.JLabel jLabelJenisPasien;
    private javax.swing.JLabel jLabelKodePos;
    private javax.swing.JLabel jLabelKota;
    private javax.swing.JLabel jLabelNama;
    private javax.swing.JLabel jLabelNamaKontakDarurat;
    private javax.swing.JLabel jLabelNik;
    private javax.swing.JLabel jLabelNoHpDarurat;
    private javax.swing.JLabel jLabelNoHpPasien;
    private javax.swing.JLabel jLabelPekerjaan;
    private javax.swing.JLabel jLabelProvinsi;
    private javax.swing.JLabel jLabelRiwayat;
    private javax.swing.JLabel jLabelTanggalLahir;
    private javax.swing.JLabel jLabelTempatLahir;
    private javax.swing.JScrollPane jScrollAlamat;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollRiwayat;
    private javax.swing.JLabel lblFormNote;
    private javax.swing.JLabel lblFormTitle;
    private javax.swing.JLabel lblSectionIdentitas;
    private javax.swing.JLabel lblSectionKontak;
    private javax.swing.JLabel lblSectionMedis;
    private javax.swing.JPanel panelActionBar;
    private javax.swing.JPanel panelActionsBottom;
    private javax.swing.JPanel panelBody;
    private javax.swing.JPanel panelFormCard;
    private javax.swing.JPanel panelFormHeader;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelSectionIdentitas;
    private javax.swing.JPanel panelSectionKontak;
    private javax.swing.JPanel panelSectionMedis;
    private javax.swing.JSeparator sepIdentitas;
    private javax.swing.JSeparator sepKontak;
    private javax.swing.JSeparator sepMedis;
    private javax.swing.JSpinner spnTanggalLahir;
    private javax.swing.JTextArea txtAlamat;
    private javax.swing.JTextField txtAlergi;
    private javax.swing.JTextField txtBpjs;
    private javax.swing.JTextField txtKodePos;
    private javax.swing.JTextField txtKontakDarurat;
    private javax.swing.JTextField txtKota;
    private javax.swing.JTextField txtNamaLengkap;
    private javax.swing.JTextField txtNik;
    private javax.swing.JTextField txtNoHpDarurat;
    private javax.swing.JTextField txtNoHpPasien;
    private javax.swing.JTextField txtPekerjaan;
    private javax.swing.JTextArea txtRiwayatPenyakit;
    private javax.swing.JTextField txtTempatLahir;
    // End of variables declaration//GEN-END:variables
}
