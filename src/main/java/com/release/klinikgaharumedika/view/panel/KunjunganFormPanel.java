package com.release.klinikgaharumedika.view.panel;

import com.release.klinikgaharumedika.controller.KunjunganController;
import com.release.klinikgaharumedika.controller.PembayaranController;
import com.release.klinikgaharumedika.model.Dokter;
import com.release.klinikgaharumedika.model.Kunjungan;
import com.release.klinikgaharumedika.model.Obat;
import com.release.klinikgaharumedika.model.Pasien;
import com.release.klinikgaharumedika.model.Pembayaran;
import com.release.klinikgaharumedika.model.ResepObatItem;
import com.release.klinikgaharumedika.model.User;
import com.release.klinikgaharumedika.state.SessionManager;
import com.release.klinikgaharumedika.view.DashboardForm;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.beans.Beans;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;

public class KunjunganFormPanel extends javax.swing.JPanel {

    private static final Color COLOR_SUCCESS = new Color(28, 112, 77);
    private static final Color COLOR_SUCCESS_SOFT = new Color(235, 247, 241);
    private static final Color COLOR_MUTED = new Color(120, 120, 120);
    private static final Color COLOR_TEXT = new Color(34, 34, 34);
    private static final Color COLOR_LABEL = new Color(36, 36, 36);
    private static final Color COLOR_REQUIRED = new Color(198, 78, 78);
    private static final Color COLOR_DANGER = new Color(208, 70, 55);
    private static final Color COLOR_BORDER = new Color(222, 218, 208);
    private static final Color COLOR_BG = new Color(243, 240, 233);
    private static final Color COLOR_INPUT_BG = new Color(255, 255, 255);
    private static final int FIELD_HEIGHT = 40;
    private static final DateTimeFormatter UI_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String STEP_PENDAFTARAN = "pendaftaran";
    private static final String STEP_PEMERIKSAAN = "pemeriksaan";
    private static final String STEP_RESEP = "resep";
    private static final String MARKER_TINDAKAN = "[TINDAKAN]";
    private static final String MARKER_RENCANA_KONTROL = "[RENCANA_KONTROL]";

    private final KunjunganController controller;
    private final Integer editingKunjunganId;

    private final List<Dokter> availableDokter = new ArrayList<>();
    private final List<Obat> availableObat = new ArrayList<>();
    private Kunjungan loadedKunjungan;
    private LocalDate initialRencanaKontrol;
    private int currentStep = 0;
    private boolean suppressDoctorSync = false;

    public KunjunganFormPanel() {
        this(null);
    }

    public KunjunganFormPanel(Kunjungan editingKunjungan) {
        this.editingKunjunganId = editingKunjungan != null ? editingKunjungan.getId() : null;
        initComponents();
        if (Beans.isDesignTime()) {
            controller = null;
            installDesignPreview();
            return;
        }
        controller = new KunjunganController();
        configureForm();
        loadReferenceData();
    }

    private void configureForm() {
        styleActionButtons();
        configurePlaceholders();

        txtKeluhanUtama.setLineWrap(true);
        txtKeluhanUtama.setWrapStyleWord(true);
        txtCatatanDokter.setLineWrap(true);
        txtCatatanDokter.setWrapStyleWord(true);
        txtCatatanResep.setLineWrap(true);
        txtCatatanResep.setWrapStyleWord(true);

        FormUiStyle.dateSpinner(spnTanggalPeriksa);
        FormUiStyle.dateSpinner(spnRencanaKontrol);

        cmbJenisKunjungan.setModel(new DefaultComboBoxModel<>(new String[] { "Pilih...", "Baru", "Kontrol" }));
        cmbTindakan.setModel(new DefaultComboBoxModel<>(new String[] { "Pilih...", "Observasi", "Terapi obat", "Rawat jalan", "Rujuk" }));

        panelResepItems.setLayout(new BoxLayout(panelResepItems, BoxLayout.Y_AXIS));
        applyProductionFormStyles();
        ((CardLayout) panelStepHost.getLayout()).show(panelStepHost, STEP_PENDAFTARAN);

        configureComboRenderers();
        updateHeaderMode();
        updateStepIndicator();
        updateActionButtons();
        setInteractiveState(false);
    }

    private void installDesignPreview() {
        styleActionButtons();
        configurePlaceholders();
        panelResepItems.setLayout(new BoxLayout(panelResepItems, BoxLayout.Y_AXIS));
        cmbPasien.setModel(new DefaultComboBoxModel<>(new Object[] { "Pilih pasien...", "RM-0001 - Ahmad Fauzi", "RM-0002 - Sari Wulandari" }));
        cmbJenisKunjungan.setModel(new DefaultComboBoxModel<>(new String[] { "Pilih...", "Baru", "Kontrol" }));
        cmbPoli.setModel(new DefaultComboBoxModel<>(new String[] { "Pilih...", "Poli Umum", "Poli Anak" }));
        cmbDokter.setModel(new DefaultComboBoxModel<>(new Object[] { "Pilih dokter...", "dr. Andi Saputra", "dr. Ratna Permata" }));
        cmbTindakan.setModel(new DefaultComboBoxModel<>(new String[] { "Pilih...", "Observasi", "Terapi obat" }));
        FormUiStyle.dateSpinner(spnTanggalPeriksa);
        FormUiStyle.dateSpinner(spnRencanaKontrol);
        panelResepItems.removeAll();
        panelResepItems.add(new JLabel("Preview item resep akan muncul saat runtime."));
        applyProductionFormStyles();
        ((CardLayout) panelStepHost.getLayout()).show(panelStepHost, STEP_PENDAFTARAN);
        updateHeaderMode();
        updateStepIndicator();
        updateActionButtons();
    }

    private void configurePlaceholders() {
        txtKeluhanUtama.putClientProperty("JTextArea.placeholderText", "Contoh: Demam dan batuk sejak 2 hari");
        txtCatatanDokter.putClientProperty("JTextArea.placeholderText", "Contoh: Istirahat cukup, minum air putih, kontrol jika tidak membaik.");
        txtCatatanResep.putClientProperty("JTextArea.placeholderText", "Contoh: Antipiretik utama");
        txtDiagnosa.putClientProperty("JTextField.placeholderText", "Contoh: Influenza");
        txtKodeIcd10.putClientProperty("JTextField.placeholderText", "Contoh: J11");
        txtTekananDarah.putClientProperty("JTextField.placeholderText", "Contoh: 120/80");
        txtNadi.putClientProperty("JTextField.placeholderText", "Contoh: 88");
        txtSuhu.putClientProperty("JTextField.placeholderText", "Contoh: 38.2");
        txtSaturasi.putClientProperty("JTextField.placeholderText", "Contoh: 97");
        txtBeratBadan.putClientProperty("JTextField.placeholderText", "Contoh: 70.0");
        txtTinggiBadan.putClientProperty("JTextField.placeholderText", "Contoh: 170.0");
    }

    private void styleActionButtons() {
        stylePrimaryButton(btnNext);
        stylePrimaryButton(btnSimpanKunjungan);
        styleGhostButton(btnBatalHeader);
        styleGhostButton(btnBack);
        styleGhostButton(btnSimpanDraft);
        styleGhostButton(btnTambahPasien);
        styleGhostButton(btnTambahObat);
    }

    private void configureComboRenderers() {
        cmbPasien.setRenderer(createComboRenderer(value -> {
            if (value instanceof Pasien pasien) {
                return safe(pasien.getNoRm()) + " - " + safe(pasien.getNama());
            }
            return "Pilih pasien...";
        }));
        cmbDokter.setRenderer(createComboRenderer(value -> {
            if (value instanceof Dokter dokter) {
                return dokter.getNama() + " - " + resolvePoliDokter(dokter);
            }
            return "Pilih dokter...";
        }));
    }

    private ListCellRenderer<Object> createComboRenderer(java.util.function.Function<Object, String> labelProvider) {
        return (list, value, index, isSelected, cellHasFocus) -> {
            javax.swing.DefaultListCellRenderer renderer = new javax.swing.DefaultListCellRenderer();
            Component component = renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (component instanceof JLabel label) {
                label.setText(labelProvider.apply(value));
                label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            }
            return component;
        };
    }

    private void loadReferenceData() {
        new SwingWorker<LookupData, Void>() {
            @Override
            protected LookupData doInBackground() throws Exception {
                LookupData lookupData = new LookupData();
                lookupData.pasien = controller.findAllPasien();
                lookupData.dokter = controller.findAllDokterAktif();
                lookupData.obat = controller.findAllObat();
                if (editingKunjunganId != null) {
                    lookupData.kunjungan = controller.findById(editingKunjunganId);
                    lookupData.resep = controller.findResepByKunjunganId(editingKunjunganId);
                }
                return lookupData;
            }

            @Override
            protected void done() {
                try {
                    applyLookupData(get());
                    setInteractiveState(true);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            KunjunganFormPanel.this,
                            "Gagal memuat form kunjungan.\n" + ex.getMessage(),
                            "Gagal",
                            JOptionPane.ERROR_MESSAGE
                    );
                    navigateToKunjunganList();
                }
            }
        }.execute();
    }

    private void applyLookupData(LookupData data) {
        loadedKunjungan = data.kunjungan;
        availableDokter.clear();
        availableDokter.addAll(data.dokter);
        availableDokter.sort(Comparator.comparing(Dokter::getNama, String.CASE_INSENSITIVE_ORDER));
        availableObat.clear();
        availableObat.addAll(data.obat);
        availableObat.sort(Comparator.comparing(Obat::getNamaObat, String.CASE_INSENSITIVE_ORDER));

        populatePasienCombo(data.pasien);
        populatePoliCombo(data.dokter);
        populateDokterCombo(null);

        panelResepItems.removeAll();
        if (data.resep != null && !data.resep.isEmpty()) {
            for (ResepObatItem item : data.resep) {
                addResepRow(item);
            }
            txtCatatanResep.setText(resolveSharedResepNote(data.resep));
        } else {
            addResepRow(null);
        }

        if (loadedKunjungan != null) {
            applyLoadedKunjungan(loadedKunjungan);
        } else {
            spnTanggalPeriksa.setValue(new Date());
            spnRencanaKontrol.setValue(new Date());
            initialRencanaKontrol = null;
        }

        updateHeaderMode();
        updateActionButtons();
        panelResepItems.revalidate();
        panelResepItems.repaint();
    }

    private void populatePasienCombo(List<Pasien> pasienList) {
        DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<>();
        model.addElement(null);
        pasienList.stream()
                .sorted(Comparator.comparing(Pasien::getNama, String.CASE_INSENSITIVE_ORDER))
                .forEach(model::addElement);
        cmbPasien.setModel(model);
        cmbPasien.setRenderer(createComboRenderer(value -> {
            if (value instanceof Pasien pasien) {
                return safe(pasien.getNoRm()) + " - " + safe(pasien.getNama());
            }
            return "Pilih pasien...";
        }));
    }

    private void populatePoliCombo(List<Dokter> dokterList) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("Pilih...");
        Set<String> poliSet = new LinkedHashSet<>();
        dokterList.stream()
                .map(this::resolvePoliDokter)
                .filter(value -> value != null && !value.isBlank())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(poliSet::add);
        poliSet.forEach(model::addElement);
        cmbPoli.setModel(model);
        cmbPoli.setSelectedIndex(0);
    }

    private void populateDokterCombo(String poliFilter) {
        suppressDoctorSync = true;
        try {
            Object previouslySelected = cmbDokter.getSelectedItem();
            Integer selectedId = previouslySelected instanceof Dokter dokter ? dokter.getId() : null;
            DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<>();
            model.addElement(null);
            availableDokter.stream()
                    .filter(dokter -> poliFilter == null || poliFilter.isBlank() || Objects.equals(resolvePoliDokter(dokter), poliFilter))
                    .sorted(Comparator.comparing(Dokter::getNama, String.CASE_INSENSITIVE_ORDER))
                    .forEach(model::addElement);
            cmbDokter.setModel(model);
            cmbDokter.setRenderer(createComboRenderer(value -> {
                if (value instanceof Dokter dokter) {
                    return dokter.getNama() + " - " + resolvePoliDokter(dokter);
                }
                return "Pilih dokter...";
            }));
            if (selectedId != null) {
                selectDokterById(selectedId);
            }
        } finally {
            suppressDoctorSync = false;
        }
    }

    private void applyLoadedKunjungan(Kunjungan kunjungan) {
        selectPasienById(kunjungan.getPasienId());
        selectComboValue(cmbJenisKunjungan, capitalize(kunjungan.getJenisKunjungan()));
        if (kunjungan.getTanggalKunjungan() != null) {
            spnTanggalPeriksa.setValue(toDate(kunjungan.getTanggalKunjungan()));
        }
        txtKeluhanUtama.setText(safe(kunjungan.getKeluhanUtama()));
        txtTekananDarah.setText(safe(kunjungan.getTekananDarah()));
        txtNadi.setText(formatInteger(kunjungan.getNadi()));
        txtSuhu.setText(formatDecimal(kunjungan.getSuhu()));
        txtSaturasi.setText(formatInteger(kunjungan.getSaturasiO2()));
        txtBeratBadan.setText(formatDecimal(kunjungan.getBeratBadan()));
        txtTinggiBadan.setText(formatDecimal(kunjungan.getTinggiBadan()));
        txtDiagnosa.setText(safe(kunjungan.getDiagnosa()));
        txtKodeIcd10.setText(safe(kunjungan.getKodeIcd10()));

        ParsedDoctorNotes parsedNotes = parseDoctorNotes(kunjungan.getCatatanDokter());
        txtCatatanDokter.setText(parsedNotes.catatanUtama());
        if (parsedNotes.tindakan() != null) {
            selectComboValue(cmbTindakan, parsedNotes.tindakan());
        }
        if (parsedNotes.rencanaKontrol() != null) {
            spnRencanaKontrol.setValue(toDate(parsedNotes.rencanaKontrol()));
        }
        initialRencanaKontrol = parsedNotes.rencanaKontrol();

        Dokter dokter = findDokterById(kunjungan.getDokterId());
        if (dokter != null) {
            String poli = resolvePoliDokter(dokter);
            selectComboValue(cmbPoli, poli);
            populateDokterCombo(poli);
            selectDokterById(kunjungan.getDokterId());
        }

        currentStep = determineStepIndex(kunjungan.getStatus());
        showCurrentStep();
    }

    private int determineStepIndex(String status) {
        if ("selesai".equalsIgnoreCase(status)) {
            return 2;
        }
        if ("periksa".equalsIgnoreCase(status)) {
            return 1;
        }
        return 0;
    }

    private void showCurrentStep() {
        CardLayout layout = (CardLayout) panelStepHost.getLayout();
        switch (currentStep) {
            case 1 -> layout.show(panelStepHost, STEP_PEMERIKSAAN);
            case 2 -> layout.show(panelStepHost, STEP_RESEP);
            default -> layout.show(panelStepHost, STEP_PENDAFTARAN);
        }
        updateStepIndicator();
        updateActionButtons();
    }

    private void updateHeaderMode() {
        if (editingKunjunganId == null) {
            lblFormTitle.setText("Pendaftaran & Pemeriksaan");
            lblFormSubtitle.setText("Input kunjungan pasien baru");
            btnSimpanKunjungan.setText("Simpan & Lanjut Bayar >");
            return;
        }
        lblFormTitle.setText("Edit Kunjungan");
        lblFormSubtitle.setText("Perbarui detail pendaftaran, pemeriksaan, dan resep");
        btnSimpanKunjungan.setText("Simpan & Lanjut Bayar >");
    }

    private void updateStepIndicator() {
        styleStepLabel(lblStep1, currentStep == 0, "1. Pendaftaran");
        styleStepLabel(lblStep2, currentStep == 1, "2. Pemeriksaan");
        styleStepLabel(lblStep3, currentStep == 2, "3. Resep Obat");
    }

    private void styleStepLabel(JLabel label, boolean active, String text) {
        label.setText(text);
        label.setOpaque(true);
        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label.setForeground(active ? COLOR_SUCCESS : COLOR_MUTED);
        label.setBackground(active ? COLOR_SUCCESS_SOFT : Color.WHITE);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(active ? COLOR_SUCCESS : COLOR_BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        label.setFont(label.getFont().deriveFont(active ? Font.BOLD : Font.PLAIN, 12f));
    }

    private void updateActionButtons() {
        btnBack.setVisible(currentStep > 0);
        btnNext.setVisible(currentStep < 2);
        btnSimpanDraft.setVisible(currentStep == 2);
        btnSimpanKunjungan.setVisible(currentStep == 2);
        btnBack.setText(currentStep == 1 ? "< Kembali" : "< Kembali");
        btnNext.setText(currentStep == 0 ? "Lanjut ke Pemeriksaan >" : "Lanjut ke Resep >");
        FormUiStyle.refreshButtonSize(btnBack);
        FormUiStyle.refreshButtonSize(btnNext);
        FormUiStyle.refreshButtonSize(btnSimpanDraft);
        FormUiStyle.refreshButtonSize(btnSimpanKunjungan);
        panelActions.revalidate();
        panelActions.repaint();
    }

    private void setInteractiveState(boolean enabled) {
        for (Component component : panelWizardCard.getComponents()) {
            component.setEnabled(enabled);
        }
        setComponentTreeEnabled(panelStepHost, enabled);
        btnBatalHeader.setEnabled(true);
    }

    private void setComponentTreeEnabled(Component component, boolean enabled) {
        component.setEnabled(enabled);
        if (component instanceof JPanel panel) {
            for (Component child : panel.getComponents()) {
                setComponentTreeEnabled(child, enabled);
            }
        } else if (component instanceof JScrollPane scrollPane) {
            Component view = scrollPane.getViewport().getView();
            if (view != null) {
                setComponentTreeEnabled(view, enabled);
            }
        }
    }

    private void stylePrimaryButton(JButton button) {
        FormUiStyle.stylePrimaryButton(button);
    }

    private void styleGhostButton(JButton button) {
        FormUiStyle.styleGhostButton(button);
    }

    private void applyProductionFormStyles() {
        styleRequiredLabel(lblPasien, "Pasien");
        styleRequiredLabel(lblJenisKunjungan, "Jenis kunjungan");
        styleRequiredLabel(lblPoli, "Poli");
        styleRequiredLabel(lblDokter, "Dokter");
        styleRequiredLabel(lblTanggalPeriksa, "Tanggal periksa");
        styleRequiredLabel(lblKeluhanUtama, "Keluhan utama");
        styleRequiredLabel(lblDiagnosa, "Diagnosa");

        styleFieldLabel(lblTekananDarah);
        styleFieldLabel(lblNadi);
        styleFieldLabel(lblSuhu);
        styleFieldLabel(lblSaturasi);
        styleFieldLabel(lblBeratBadan);
        styleFieldLabel(lblTinggiBadan);
        styleFieldLabel(lblKodeIcd10);
        styleFieldLabel(lblCatatanDokter);
        styleFieldLabel(lblTindakan);
        styleFieldLabel(lblRencanaKontrol);
        styleFieldLabel(lblCatatanResep);

        styleTextField(txtTekananDarah);
        styleTextField(txtNadi);
        styleTextField(txtSuhu);
        styleTextField(txtSaturasi);
        styleTextField(txtBeratBadan);
        styleTextField(txtTinggiBadan);
        styleTextField(txtDiagnosa);
        styleTextField(txtKodeIcd10);

        styleTextArea(txtKeluhanUtama, scrollKeluhan);
        styleTextArea(txtCatatanDokter, scrollCatatanDokter);
        styleTextArea(txtCatatanResep, scrollCatatanResep);

        styleComboBox(cmbPasien);
        styleComboBox(cmbJenisKunjungan);
        styleComboBox(cmbPoli);
        styleComboBox(cmbDokter);
        styleComboBox(cmbTindakan);

        styleSpinner(spnTanggalPeriksa);
        styleSpinner(spnRencanaKontrol);
        styleScrollPane(scrollResep);

        lblFormTitle.setForeground(COLOR_TEXT);
        lblFormSubtitle.setForeground(COLOR_MUTED);
        lblResepTitle.setForeground(COLOR_TEXT);

        pinBoxRow(panelStep1Row1, 64);
        pinBoxRow(panelStep1Row2, 64);
        pinBoxRow(panelVitalRow1, 64);
        pinBoxRow(panelVitalRow2, 64);
        pinBoxRow(panelDiagnosisRow, 64);
        pinBoxRow(panelMetaPemeriksaan, 64);
        pinBoxRow(panelResepHeader, 36);
        pinBoxRow(panelStepNav, 40);

        pinBoxRow(panelFieldKeluhan, 150);
        pinBoxRow(panelFieldCatatanDokter, 170);
        pinBoxRow(panelFieldCatatanResep, 150);
        setComponentHeight(scrollResep, 132);

        setBoxAlignment(panelStep1Row1);
        setBoxAlignment(panelStep1Row2);
        setBoxAlignment(panelFieldKeluhan);
        setBoxAlignment(panelVitalRow1);
        setBoxAlignment(panelVitalRow2);
        setBoxAlignment(panelDiagnosisRow);
        setBoxAlignment(panelFieldCatatanDokter);
        setBoxAlignment(panelMetaPemeriksaan);
        setBoxAlignment(panelResepHeader);
        setBoxAlignment(scrollResep);
        setBoxAlignment(panelFieldCatatanResep);
    }

    private void styleRequiredLabel(JLabel label, String text) {
        styleFieldLabel(label);
        label.setText("<html>" + text + " <span style='color:" + toHtmlColor(COLOR_REQUIRED) + "'>*</span></html>");
    }

    private void styleFieldLabel(JLabel label) {
        label.setForeground(COLOR_LABEL);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
    }

    private void styleTextField(JTextField field) {
        FormUiStyle.styleTextField(field);
        setComponentHeight(field, FIELD_HEIGHT);
    }

    private void styleTextArea(JTextArea area, JScrollPane scrollPane) {
        FormUiStyle.styleTextArea(area);
        styleScrollPane(scrollPane);
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        FormUiStyle.styleComboBox(comboBox);
        setComponentHeight(comboBox, FIELD_HEIGHT);
    }

    private void styleSpinner(JSpinner spinner) {
        FormUiStyle.styleSpinner(spinner);
        setComponentHeight(spinner, FIELD_HEIGHT);
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        scrollPane.getViewport().setBackground(COLOR_INPUT_BG);
    }

    private Border createInputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        );
    }

    private void setComponentHeight(JComponent component, int height) {
        Dimension preferredSize = component.getPreferredSize();
        component.setPreferredSize(new Dimension(preferredSize.width, height));
        component.setMinimumSize(new Dimension(0, height));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    }

    private void pinBoxRow(JComponent component, int height) {
        Dimension preferredSize = component.getPreferredSize();
        component.setPreferredSize(new Dimension(preferredSize.width, height));
        component.setMinimumSize(new Dimension(0, height));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    }

    private void setBoxAlignment(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private String toHtmlColor(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private void navigateToKunjunganList() {
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof DashboardForm dashboardForm) {
            dashboardForm.showKunjunganPage();
        }
    }

    private void openPasienCreatePage() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Form kunjungan yang belum disimpan akan ditinggalkan. Lanjut ke form pasien baru?",
                "Buka Form Pasien",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof DashboardForm dashboardForm) {
            dashboardForm.showPasienCreatePage();
        }
    }

    private void moveToNextStep() {
        String validationMessage = validateCurrentStep();
        if (validationMessage != null) {
            JOptionPane.showMessageDialog(this, validationMessage, "Validasi Form", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentStep < 2) {
            currentStep++;
            showCurrentStep();
        }
    }

    private void moveToPreviousStep() {
        if (currentStep > 0) {
            currentStep--;
            showCurrentStep();
        }
    }

    private String validateCurrentStep() {
        return switch (currentStep) {
            case 0 -> validateRegistrationStep();
            case 1 -> validatePemeriksaanStep(false);
            case 2 -> validateCompletionStep(false);
            default -> null;
        };
    }

    private String validateRegistrationStep() {
        if (!(cmbPasien.getSelectedItem() instanceof Pasien)) {
            return "Pasien wajib dipilih.";
        }
        if (cmbJenisKunjungan.getSelectedIndex() <= 0) {
            return "Jenis kunjungan wajib dipilih.";
        }
        if (cmbPoli.getSelectedIndex() <= 0) {
            return "Poli wajib dipilih.";
        }
        if (!(cmbDokter.getSelectedItem() instanceof Dokter)) {
            return "Dokter wajib dipilih.";
        }
        if (txtKeluhanUtama.getText().trim().isEmpty()) {
            return "Keluhan utama wajib diisi.";
        }
        return null;
    }

    private String validatePemeriksaanStep(boolean allowDraft) {
        String registrationError = validateRegistrationStep();
        if (registrationError != null) {
            return registrationError;
        }
        try {
            parseOptionalDecimal(txtBeratBadan);
            parseOptionalDecimal(txtTinggiBadan);
            parseOptionalDecimal(txtSuhu);
            parseOptionalInteger(txtNadi);
            parseOptionalInteger(txtSaturasi);
        } catch (IllegalArgumentException ex) {
            return ex.getMessage();
        }
        if (!allowDraft && txtDiagnosa.getText().trim().isEmpty()) {
            return "Diagnosa wajib diisi sebelum lanjut ke resep.";
        }
        return null;
    }

    private String validateCompletionStep(boolean allowDraft) {
        String pemeriksaanError = validatePemeriksaanStep(allowDraft);
        if (pemeriksaanError != null) {
            return pemeriksaanError;
        }
        List<ResepObatItem> resepItems = collectResepItems();
        for (ResepObatItem item : resepItems) {
            if (item.getObatId() <= 0) {
                return "Setiap baris resep wajib memilih obat atau hapus baris kosong.";
            }
            if (item.getJumlah() <= 0) {
                return "Jumlah obat harus lebih dari 0.";
            }
            if (item.getAturanPakai() == null || item.getAturanPakai().isBlank()) {
                return "Aturan pakai wajib diisi untuk setiap obat.";
            }
        }
        return null;
    }

    private void saveKunjungan(boolean draftMode) {
        String validationMessage = draftMode
                ? validateCompletionStep(true)
                : validateCompletionStep(false);
        if (validationMessage != null) {
            JOptionPane.showMessageDialog(this, validationMessage, "Validasi Form", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Kunjungan kunjungan = buildKunjungan(draftMode);
            List<ResepObatItem> resepItems = collectResepItems();
            boolean berhasil = editingKunjunganId == null
                    ? controller.tambah(kunjungan, resepItems)
                    : controller.edit(kunjungan, resepItems);
            if (berhasil) {
                if (draftMode) {
                    JOptionPane.showMessageDialog(
                            this,
                            editingKunjunganId == null ? "Draft kunjungan berhasil disimpan." : "Draft kunjungan berhasil diperbarui.",
                            "Sukses",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    navigateToKunjunganList();
                    return;
                }
                navigateToPaymentForm(kunjungan);
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    editingKunjunganId == null ? "Gagal menyimpan kunjungan." : "Gagal memperbarui kunjungan.",
                    "Gagal",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    (editingKunjunganId == null ? "Gagal menyimpan kunjungan.\n" : "Gagal memperbarui kunjungan.\n") + ex.getMessage(),
                    "Gagal",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private Kunjungan buildKunjungan(boolean draftMode) {
        Kunjungan kunjungan = new Kunjungan();
        if (editingKunjunganId != null) {
            kunjungan.setId(editingKunjunganId);
            kunjungan.setNoAntrian(loadedKunjungan != null ? loadedKunjungan.getNoAntrian() : null);
            kunjungan.setNoKunjungan(loadedKunjungan != null ? loadedKunjungan.getNoKunjungan() : null);
            kunjungan.setPerawatId(loadedKunjungan != null ? loadedKunjungan.getPerawatId() : null);
        }

        Pasien pasien = getSelectedPasien();
        Dokter dokter = getSelectedDokter();
        kunjungan.setPasienId(pasien.getId());
        kunjungan.setDokterId(dokter.getId());
        kunjungan.setTanggalKunjungan(toLocalDate((Date) spnTanggalPeriksa.getValue()));
        kunjungan.setJenisKunjungan(cmbJenisKunjungan.getSelectedItem().toString().trim().toLowerCase());
        kunjungan.setKeluhanUtama(blankToNull(txtKeluhanUtama.getText()));
        kunjungan.setTekananDarah(blankToNull(txtTekananDarah.getText()));
        kunjungan.setNadi(parseOptionalInteger(txtNadi));
        kunjungan.setSuhu(parseOptionalDecimal(txtSuhu));
        kunjungan.setSaturasiO2(parseOptionalInteger(txtSaturasi));
        kunjungan.setBeratBadan(parseOptionalDecimal(txtBeratBadan));
        kunjungan.setTinggiBadan(parseOptionalDecimal(txtTinggiBadan));
        kunjungan.setDiagnosa(blankToNull(txtDiagnosa.getText()));
        kunjungan.setKodeIcd10(blankToNull(txtKodeIcd10.getText()));
        kunjungan.setCatatanDokter(buildDoctorNotes());
        kunjungan.setStatus(resolveStatus(draftMode));
        return kunjungan;
    }

    private void navigateToPaymentForm(Kunjungan kunjungan) throws Exception {
        PembayaranController pembayaranController = new PembayaranController();
        Pembayaran pembayaran = pembayaranController.createPendingForKunjungan(kunjungan.getId(), currentUserId());
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof DashboardForm dashboardForm) {
            dashboardForm.showPembayaranFormPage(pembayaran);
        }
    }

    private Integer currentUserId() {
        User user = SessionManager.getInstance().getCurrentUser();
        return user != null ? user.getId() : null;
    }

    private String resolveStatus(boolean draftMode) {
        if (!draftMode) {
            return "selesai";
        }
        if (!txtDiagnosa.getText().trim().isEmpty()) {
            return "selesai";
        }
        if (hasPemeriksaanData()) {
            return "periksa";
        }
        return "menunggu";
    }

    private boolean hasPemeriksaanData() {
        return !txtTekananDarah.getText().trim().isEmpty()
                || !txtNadi.getText().trim().isEmpty()
                || !txtSuhu.getText().trim().isEmpty()
                || !txtSaturasi.getText().trim().isEmpty()
                || !txtBeratBadan.getText().trim().isEmpty()
                || !txtTinggiBadan.getText().trim().isEmpty()
                || !txtCatatanDokter.getText().trim().isEmpty();
    }

    private String buildDoctorNotes() {
        List<String> lines = new ArrayList<>();
        String catatanUtama = blankToNull(txtCatatanDokter.getText());
        if (catatanUtama != null) {
            lines.add(catatanUtama);
        }
        if (cmbTindakan.getSelectedIndex() > 0) {
            lines.add(MARKER_TINDAKAN + " " + cmbTindakan.getSelectedItem());
        }
        Date plannedControlDate = (Date) spnRencanaKontrol.getValue();
        LocalDate selectedPlannedControl = plannedControlDate != null ? toLocalDate(plannedControlDate) : null;
        LocalDate tanggalPeriksa = toLocalDate((Date) spnTanggalPeriksa.getValue());
        if (selectedPlannedControl != null
                && (initialRencanaKontrol != null || !selectedPlannedControl.equals(tanggalPeriksa))) {
            lines.add(MARKER_RENCANA_KONTROL + " " + UI_DATE_FORMAT.format(selectedPlannedControl));
        }
        return lines.isEmpty() ? null : String.join("\n", lines);
    }

    private ParsedDoctorNotes parseDoctorNotes(String noteText) {
        if (noteText == null || noteText.isBlank()) {
            return new ParsedDoctorNotes("", null, null);
        }

        StringBuilder catatanUtama = new StringBuilder();
        String tindakan = null;
        LocalDate rencanaKontrol = null;

        for (String line : noteText.split("\\R")) {
            if (line.startsWith(MARKER_TINDAKAN + " ")) {
                tindakan = line.substring((MARKER_TINDAKAN + " ").length()).trim();
                continue;
            }
            if (line.startsWith(MARKER_RENCANA_KONTROL + " ")) {
                String dateText = line.substring((MARKER_RENCANA_KONTROL + " ").length()).trim();
                try {
                    rencanaKontrol = LocalDate.parse(dateText, UI_DATE_FORMAT);
                } catch (DateTimeParseException ignored) {
                    rencanaKontrol = null;
                }
                continue;
            }
            if (catatanUtama.length() > 0) {
                catatanUtama.append('\n');
            }
            catatanUtama.append(line);
        }

        return new ParsedDoctorNotes(catatanUtama.toString().trim(), tindakan, rencanaKontrol);
    }

    private List<ResepObatItem> collectResepItems() {
        List<ResepObatItem> items = new ArrayList<>();
        String sharedNote = blankToNull(txtCatatanResep.getText());
        for (Component component : panelResepItems.getComponents()) {
            if (component instanceof ResepRowPanel rowPanel) {
                ResepObatItem item = rowPanel.toItem();
                if (item == null) {
                    continue;
                }
                item.setKeterangan(sharedNote);
                items.add(item);
            }
        }
        return items;
    }

    private String resolveSharedResepNote(List<ResepObatItem> resepItems) {
        String note = null;
        for (ResepObatItem item : resepItems) {
            if (item.getKeterangan() == null || item.getKeterangan().isBlank()) {
                continue;
            }
            if (note == null) {
                note = item.getKeterangan();
                continue;
            }
            if (!note.equals(item.getKeterangan())) {
                return note;
            }
        }
        return note != null ? note : "";
    }

    private void addResepRow(ResepObatItem item) {
        ResepRowPanel rowPanel = new ResepRowPanel(item);
        panelResepItems.add(rowPanel);
        panelResepItems.add(Box.createVerticalStrut(10));
        panelResepItems.revalidate();
        panelResepItems.repaint();
    }

    private void removeResepRow(ResepRowPanel rowPanel) {
        List<Component> componentsToRemove = new ArrayList<>();
        Component[] components = panelResepItems.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] == rowPanel) {
                componentsToRemove.add(components[i]);
                if (i + 1 < components.length && components[i + 1] instanceof Box.Filler) {
                    componentsToRemove.add(components[i + 1]);
                }
                break;
            }
        }
        componentsToRemove.forEach(panelResepItems::remove);
        if (countResepRows() == 0) {
            addResepRow(null);
        }
        panelResepItems.revalidate();
        panelResepItems.repaint();
    }

    private int countResepRows() {
        int count = 0;
        for (Component component : panelResepItems.getComponents()) {
            if (component instanceof ResepRowPanel) {
                count++;
            }
        }
        return count;
    }

    private Pasien getSelectedPasien() {
        return (Pasien) cmbPasien.getSelectedItem();
    }

    private Dokter getSelectedDokter() {
        return (Dokter) cmbDokter.getSelectedItem();
    }

    private Dokter findDokterById(int dokterId) {
        return availableDokter.stream()
                .filter(dokter -> dokter.getId() == dokterId)
                .findFirst()
                .orElse(null);
    }

    private void selectPasienById(int pasienId) {
        for (int i = 0; i < cmbPasien.getItemCount(); i++) {
            Object value = cmbPasien.getItemAt(i);
            if (value instanceof Pasien pasien && pasien.getId() == pasienId) {
                cmbPasien.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectDokterById(int dokterId) {
        for (int i = 0; i < cmbDokter.getItemCount(); i++) {
            Object value = cmbDokter.getItemAt(i);
            if (value instanceof Dokter dokter && dokter.getId() == dokterId) {
                cmbDokter.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectComboValue(JComboBox<?> comboBox, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            Object item = comboBox.getItemAt(i);
            if (value.equalsIgnoreCase(String.valueOf(item))) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private String resolvePoliDokter(Dokter dokter) {
        if (dokter == null) {
            return null;
        }
        if (dokter.getPoliUnit() != null && !dokter.getPoliUnit().isBlank()) {
            return dokter.getPoliUnit().trim();
        }
        return dokter.getPoli();
    }

    private Integer parseOptionalInteger(JTextField field) {
        String value = blankToNull(field.getText());
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldLabel(field) + " harus berupa angka bulat.");
        }
    }

    private Double parseOptionalDecimal(JTextField field) {
        String value = blankToNull(field.getText());
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldLabel(field) + " harus berupa angka desimal yang valid.");
        }
    }

    private String fieldLabel(JTextField field) {
        if (field == txtBeratBadan) {
            return "Berat badan";
        }
        if (field == txtTinggiBadan) {
            return "Tinggi badan";
        }
        if (field == txtSuhu) {
            return "Suhu";
        }
        if (field == txtNadi) {
            return "Nadi";
        }
        if (field == txtSaturasi) {
            return "Saturasi";
        }
        return "Nilai";
    }

    private LocalDate toLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    private String formatInteger(Integer value) {
        return value != null ? String.valueOf(value) : "";
    }

    private String formatDecimal(Double value) {
        if (value == null) {
            return "";
        }
        if (Math.floor(value) == value) {
            return String.format("%.0f", value);
        }
        return String.format("%.1f", value);
    }

    private String capitalize(String value) {
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
        panelWizardCard = new javax.swing.JPanel();
        panelHeader = new javax.swing.JPanel();
        lblFormTitle = new javax.swing.JLabel();
        lblFormSubtitle = new javax.swing.JLabel();
        panelStepNav = new javax.swing.JPanel();
        lblStep1 = new javax.swing.JLabel();
        lblStep2 = new javax.swing.JLabel();
        lblStep3 = new javax.swing.JLabel();
        panelStepHost = new javax.swing.JPanel();
        panelStepPendaftaran = new javax.swing.JPanel();
        panelStep1Body = new javax.swing.JPanel();
        panelStep1Row1 = new javax.swing.JPanel();
        panelFieldPasien = new javax.swing.JPanel();
        lblPasien = new javax.swing.JLabel();
        panelPasienInput = new javax.swing.JPanel();
        cmbPasien = new javax.swing.JComboBox<>();
        btnTambahPasien = new javax.swing.JButton();
        panelFieldJenisKunjungan = new javax.swing.JPanel();
        lblJenisKunjungan = new javax.swing.JLabel();
        cmbJenisKunjungan = new javax.swing.JComboBox<>();
        panelStep1Row2 = new javax.swing.JPanel();
        panelFieldPoli = new javax.swing.JPanel();
        lblPoli = new javax.swing.JLabel();
        cmbPoli = new javax.swing.JComboBox<>();
        panelFieldDokter = new javax.swing.JPanel();
        lblDokter = new javax.swing.JLabel();
        cmbDokter = new javax.swing.JComboBox<>();
        panelFieldTanggal = new javax.swing.JPanel();
        lblTanggalPeriksa = new javax.swing.JLabel();
        spnTanggalPeriksa = new javax.swing.JSpinner();
        panelFieldKeluhan = new javax.swing.JPanel();
        lblKeluhanUtama = new javax.swing.JLabel();
        scrollKeluhan = new javax.swing.JScrollPane();
        txtKeluhanUtama = new javax.swing.JTextArea();
        panelStepPemeriksaan = new javax.swing.JPanel();
        panelStep2Body = new javax.swing.JPanel();
        panelVitalRow1 = new javax.swing.JPanel();
        panelFieldTekananDarah = new javax.swing.JPanel();
        lblTekananDarah = new javax.swing.JLabel();
        txtTekananDarah = new javax.swing.JTextField();
        panelFieldNadi = new javax.swing.JPanel();
        lblNadi = new javax.swing.JLabel();
        txtNadi = new javax.swing.JTextField();
        panelFieldSuhu = new javax.swing.JPanel();
        lblSuhu = new javax.swing.JLabel();
        txtSuhu = new javax.swing.JTextField();
        panelVitalRow2 = new javax.swing.JPanel();
        panelFieldSaturasi = new javax.swing.JPanel();
        lblSaturasi = new javax.swing.JLabel();
        txtSaturasi = new javax.swing.JTextField();
        panelFieldBerat = new javax.swing.JPanel();
        lblBeratBadan = new javax.swing.JLabel();
        txtBeratBadan = new javax.swing.JTextField();
        panelFieldTinggi = new javax.swing.JPanel();
        lblTinggiBadan = new javax.swing.JLabel();
        txtTinggiBadan = new javax.swing.JTextField();
        panelDiagnosisRow = new javax.swing.JPanel();
        panelFieldDiagnosa = new javax.swing.JPanel();
        lblDiagnosa = new javax.swing.JLabel();
        txtDiagnosa = new javax.swing.JTextField();
        panelFieldIcd = new javax.swing.JPanel();
        lblKodeIcd10 = new javax.swing.JLabel();
        txtKodeIcd10 = new javax.swing.JTextField();
        panelFieldCatatanDokter = new javax.swing.JPanel();
        lblCatatanDokter = new javax.swing.JLabel();
        scrollCatatanDokter = new javax.swing.JScrollPane();
        txtCatatanDokter = new javax.swing.JTextArea();
        panelMetaPemeriksaan = new javax.swing.JPanel();
        panelFieldTindakan = new javax.swing.JPanel();
        lblTindakan = new javax.swing.JLabel();
        cmbTindakan = new javax.swing.JComboBox<>();
        panelFieldKontrol = new javax.swing.JPanel();
        lblRencanaKontrol = new javax.swing.JLabel();
        spnRencanaKontrol = new javax.swing.JSpinner();
        panelStepResep = new javax.swing.JPanel();
        panelStep3Body = new javax.swing.JPanel();
        panelResepHeader = new javax.swing.JPanel();
        lblResepTitle = new javax.swing.JLabel();
        btnTambahObat = new javax.swing.JButton();
        scrollResep = new javax.swing.JScrollPane();
        panelResepItems = new javax.swing.JPanel();
        panelFieldCatatanResep = new javax.swing.JPanel();
        lblCatatanResep = new javax.swing.JLabel();
        scrollCatatanResep = new javax.swing.JScrollPane();
        txtCatatanResep = new javax.swing.JTextArea();
        panelActions = new javax.swing.JPanel();
        btnBack = new javax.swing.JButton();
        btnSimpanDraft = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnSimpanKunjungan = new javax.swing.JButton();

        panelMain.setBackground(new java.awt.Color(243, 240, 233));

        panelActionBar.setBackground(new java.awt.Color(243, 240, 233));

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
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnBatalHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelActionBarLayout.setVerticalGroup(
            panelActionBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnBatalHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        panelWizardCard.setBackground(new java.awt.Color(255, 255, 255));
        panelWizardCard.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 218, 208)));

        panelHeader.setBackground(new java.awt.Color(255, 255, 255));

        lblFormTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblFormTitle.setText("Pendaftaran & Pemeriksaan");

        lblFormSubtitle.setForeground(new java.awt.Color(120, 120, 120));
        lblFormSubtitle.setText("Input kunjungan pasien baru");

        javax.swing.GroupLayout panelHeaderLayout = new javax.swing.GroupLayout(panelHeader);
        panelHeader.setLayout(panelHeaderLayout);
        panelHeaderLayout.setHorizontalGroup(
            panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeaderLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFormTitle)
                    .addComponent(lblFormSubtitle))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        panelHeaderLayout.setVerticalGroup(
            panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeaderLayout.createSequentialGroup()
                .addComponent(lblFormTitle)
                .addGap(4, 4, 4)
                .addComponent(lblFormSubtitle))
        );

        panelStepNav.setBackground(new java.awt.Color(255, 255, 255));
        panelStepNav.setLayout(new java.awt.GridLayout(1, 3, 18, 0));

        lblStep1.setText("1. Pendaftaran");
        panelStepNav.add(lblStep1);

        lblStep2.setText("2. Pemeriksaan");
        panelStepNav.add(lblStep2);

        lblStep3.setText("3. Resep Obat");
        panelStepNav.add(lblStep3);

        panelStepHost.setBackground(new java.awt.Color(255, 255, 255));
        panelStepHost.setLayout(new java.awt.CardLayout());

        panelStepPendaftaran.setBackground(new java.awt.Color(255, 255, 255));

        panelStep1Body.setBackground(new java.awt.Color(255, 255, 255));
        panelStep1Body.setLayout(new javax.swing.BoxLayout(panelStep1Body, javax.swing.BoxLayout.Y_AXIS));

        panelStep1Row1.setBackground(new java.awt.Color(255, 255, 255));
        panelStep1Row1.setLayout(new java.awt.GridLayout(1, 2, 14, 0));

        panelFieldPasien.setBackground(new java.awt.Color(255, 255, 255));
        panelFieldPasien.setLayout(new java.awt.BorderLayout(0, 6));

        lblPasien.setText("Pasien *");
        panelFieldPasien.add(lblPasien, java.awt.BorderLayout.PAGE_START);

        panelPasienInput.setBackground(new java.awt.Color(255, 255, 255));
        panelPasienInput.setLayout(new java.awt.BorderLayout(8, 0));
        panelPasienInput.add(cmbPasien, java.awt.BorderLayout.CENTER);

        btnTambahPasien.setText("+ Baru");
        btnTambahPasien.setFocusPainted(false);
        btnTambahPasien.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahPasienActionPerformed(evt);
            }
        });
        panelPasienInput.add(btnTambahPasien, java.awt.BorderLayout.LINE_END);

        panelFieldPasien.add(panelPasienInput, java.awt.BorderLayout.CENTER);
        panelStep1Row1.add(panelFieldPasien);

        panelFieldJenisKunjungan.setBackground(new java.awt.Color(255, 255, 255));
        panelFieldJenisKunjungan.setLayout(new java.awt.BorderLayout(0, 6));

        lblJenisKunjungan.setText("Jenis kunjungan *");
        panelFieldJenisKunjungan.add(lblJenisKunjungan, java.awt.BorderLayout.PAGE_START);
        panelFieldJenisKunjungan.add(cmbJenisKunjungan, java.awt.BorderLayout.CENTER);

        panelStep1Row1.add(panelFieldJenisKunjungan);

        panelStep1Body.add(panelStep1Row1);
        panelStep1Body.add(Box.createVerticalStrut(12));

        panelStep1Row2.setBackground(new java.awt.Color(255, 255, 255));
        panelStep1Row2.setLayout(new java.awt.GridLayout(1, 3, 14, 0));

        panelFieldPoli.setBackground(new java.awt.Color(255, 255, 255));
        panelFieldPoli.setLayout(new java.awt.BorderLayout(0, 6));

        lblPoli.setText("Poli *");
        panelFieldPoli.add(lblPoli, java.awt.BorderLayout.PAGE_START);
        cmbPoli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbPoliActionPerformed(evt);
            }
        });
        panelFieldPoli.add(cmbPoli, java.awt.BorderLayout.CENTER);

        panelStep1Row2.add(panelFieldPoli);

        panelFieldDokter.setBackground(new java.awt.Color(255, 255, 255));
        panelFieldDokter.setLayout(new java.awt.BorderLayout(0, 6));

        lblDokter.setText("Dokter *");
        panelFieldDokter.add(lblDokter, java.awt.BorderLayout.PAGE_START);
        cmbDokter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbDokterActionPerformed(evt);
            }
        });
        panelFieldDokter.add(cmbDokter, java.awt.BorderLayout.CENTER);

        panelStep1Row2.add(panelFieldDokter);

        panelFieldTanggal.setBackground(new java.awt.Color(255, 255, 255));
        panelFieldTanggal.setLayout(new java.awt.BorderLayout(0, 6));

        lblTanggalPeriksa.setText("Tanggal periksa *");
        panelFieldTanggal.add(lblTanggalPeriksa, java.awt.BorderLayout.PAGE_START);
        panelFieldTanggal.add(spnTanggalPeriksa, java.awt.BorderLayout.CENTER);

        panelStep1Row2.add(panelFieldTanggal);

        panelStep1Body.add(panelStep1Row2);
        panelStep1Body.add(Box.createVerticalStrut(12));

        panelFieldKeluhan.setBackground(new java.awt.Color(255, 255, 255));
        panelFieldKeluhan.setLayout(new java.awt.BorderLayout(0, 6));

        lblKeluhanUtama.setText("Keluhan utama *");
        panelFieldKeluhan.add(lblKeluhanUtama, java.awt.BorderLayout.PAGE_START);

        txtKeluhanUtama.setColumns(20);
        txtKeluhanUtama.setRows(5);
        scrollKeluhan.setViewportView(txtKeluhanUtama);

        panelFieldKeluhan.add(scrollKeluhan, java.awt.BorderLayout.CENTER);

        panelStep1Body.add(panelFieldKeluhan);

        javax.swing.GroupLayout panelStepPendaftaranLayout = new javax.swing.GroupLayout(panelStepPendaftaran);
        panelStepPendaftaran.setLayout(panelStepPendaftaranLayout);
        panelStepPendaftaranLayout.setHorizontalGroup(
            panelStepPendaftaranLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelStep1Body, javax.swing.GroupLayout.DEFAULT_SIZE, 801, Short.MAX_VALUE)
        );
        panelStepPendaftaranLayout.setVerticalGroup(
            panelStepPendaftaranLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelStep1Body, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        panelStepHost.add(panelStepPendaftaran, "pendaftaran");

        panelStepPemeriksaan.setBackground(new java.awt.Color(255, 255, 255));

        panelStep2Body.setBackground(new java.awt.Color(255, 255, 255));
        panelStep2Body.setLayout(new javax.swing.BoxLayout(panelStep2Body, javax.swing.BoxLayout.Y_AXIS));

        panelVitalRow1.setBackground(new java.awt.Color(255, 255, 255));
        panelVitalRow1.setLayout(new java.awt.GridLayout(1, 3, 14, 0));

        panelFieldTekananDarah.setBackground(new java.awt.Color(255, 255, 255));
        panelFieldTekananDarah.setLayout(new java.awt.BorderLayout(0, 6));

        lblTekananDarah.setText("Tekanan darah");
        panelFieldTekananDarah.add(lblTekananDarah, java.awt.BorderLayout.PAGE_START);
        panelFieldTekananDarah.add(txtTekananDarah, java.awt.BorderLayout.CENTER);

        panelVitalRow1.add(panelFieldTekananDarah);

        panelFieldNadi.setBackground(new java.awt.Color(255, 255, 255));
        panelFieldNadi.setLayout(new java.awt.BorderLayout(0, 6));

        lblNadi.setText("Nadi");
        panelFieldNadi.add(lblNadi, java.awt.BorderLayout.PAGE_START);
        panelFieldNadi.add(txtNadi, java.awt.BorderLayout.CENTER);

        panelVitalRow1.add(panelFieldNadi);

        panelFieldSuhu.setBackground(new java.awt.Color(255, 255, 255));
        panelFieldSuhu.setLayout(new java.awt.BorderLayout(0, 6));

        lblSuhu.setText("Suhu");
        panelFieldSuhu.add(lblSuhu, java.awt.BorderLayout.PAGE_START);
        panelFieldSuhu.add(txtSuhu, java.awt.BorderLayout.CENTER);

        panelVitalRow1.add(panelFieldSuhu);

        panelStep2Body.add(panelVitalRow1);
        panelStep2Body.add(Box.createVerticalStrut(12));

        panelVitalRow2.setBackground(new java.awt.Color(255, 255, 255));
        panelVitalRow2.setLayout(new java.awt.GridLayout(1, 3, 14, 0));

        panelFieldSaturasi.setBackground(new java.awt.Color(255, 255, 255));
        panelFieldSaturasi.setLayout(new java.awt.BorderLayout(0, 6));

        lblSaturasi.setText("Saturasi O2");
        panelFieldSaturasi.add(lblSaturasi, java.awt.BorderLayout.PAGE_START);
        panelFieldSaturasi.add(txtSaturasi, java.awt.BorderLayout.CENTER);

        panelVitalRow2.add(panelFieldSaturasi);

        panelFieldBerat.setBackground(new java.awt.Color(255, 255, 255));
        panelFieldBerat.setLayout(new java.awt.BorderLayout(0, 6));

        lblBeratBadan.setText("Berat badan (kg)");
        panelFieldBerat.add(lblBeratBadan, java.awt.BorderLayout.PAGE_START);
        panelFieldBerat.add(txtBeratBadan, java.awt.BorderLayout.CENTER);

        panelVitalRow2.add(panelFieldBerat);

        panelFieldTinggi.setBackground(new java.awt.Color(255, 255, 255));
        panelFieldTinggi.setLayout(new java.awt.BorderLayout(0, 6));

        lblTinggiBadan.setText("Tinggi badan (cm)");
        panelFieldTinggi.add(lblTinggiBadan, java.awt.BorderLayout.PAGE_START);
        panelFieldTinggi.add(txtTinggiBadan, java.awt.BorderLayout.CENTER);

        panelVitalRow2.add(panelFieldTinggi);

        panelStep2Body.add(panelVitalRow2);
        panelStep2Body.add(Box.createVerticalStrut(12));

        panelDiagnosisRow.setBackground(new java.awt.Color(255, 255, 255));
        panelDiagnosisRow.setLayout(new java.awt.GridLayout(1, 2, 14, 0));

        panelFieldDiagnosa.setBackground(new java.awt.Color(255, 255, 255));
        panelFieldDiagnosa.setLayout(new java.awt.BorderLayout(0, 6));

        lblDiagnosa.setText("Diagnosa *");
        panelFieldDiagnosa.add(lblDiagnosa, java.awt.BorderLayout.PAGE_START);
        panelFieldDiagnosa.add(txtDiagnosa, java.awt.BorderLayout.CENTER);

        panelDiagnosisRow.add(panelFieldDiagnosa);

        panelFieldIcd.setBackground(new java.awt.Color(255, 255, 255));
        panelFieldIcd.setLayout(new java.awt.BorderLayout(0, 6));

        lblKodeIcd10.setText("Kode ICD-10");
        panelFieldIcd.add(lblKodeIcd10, java.awt.BorderLayout.PAGE_START);
        panelFieldIcd.add(txtKodeIcd10, java.awt.BorderLayout.CENTER);

        panelDiagnosisRow.add(panelFieldIcd);

        panelStep2Body.add(panelDiagnosisRow);
        panelStep2Body.add(Box.createVerticalStrut(12));

        panelFieldCatatanDokter.setBackground(new java.awt.Color(255, 255, 255));
        panelFieldCatatanDokter.setLayout(new java.awt.BorderLayout(0, 6));

        lblCatatanDokter.setText("Catatan dokter");
        panelFieldCatatanDokter.add(lblCatatanDokter, java.awt.BorderLayout.PAGE_START);

        txtCatatanDokter.setColumns(20);
        txtCatatanDokter.setRows(5);
        scrollCatatanDokter.setViewportView(txtCatatanDokter);

        panelFieldCatatanDokter.add(scrollCatatanDokter, java.awt.BorderLayout.CENTER);

        panelStep2Body.add(panelFieldCatatanDokter);
        panelStep2Body.add(Box.createVerticalStrut(12));

        panelMetaPemeriksaan.setBackground(new java.awt.Color(255, 255, 255));
        panelMetaPemeriksaan.setLayout(new java.awt.GridLayout(1, 2, 14, 0));

        panelFieldTindakan.setBackground(new java.awt.Color(255, 255, 255));
        panelFieldTindakan.setLayout(new java.awt.BorderLayout(0, 6));

        lblTindakan.setText("Tindakan");
        panelFieldTindakan.add(lblTindakan, java.awt.BorderLayout.PAGE_START);
        panelFieldTindakan.add(cmbTindakan, java.awt.BorderLayout.CENTER);

        panelMetaPemeriksaan.add(panelFieldTindakan);

        panelFieldKontrol.setBackground(new java.awt.Color(255, 255, 255));
        panelFieldKontrol.setLayout(new java.awt.BorderLayout(0, 6));

        lblRencanaKontrol.setText("Rencana kontrol");
        panelFieldKontrol.add(lblRencanaKontrol, java.awt.BorderLayout.PAGE_START);
        panelFieldKontrol.add(spnRencanaKontrol, java.awt.BorderLayout.CENTER);

        panelMetaPemeriksaan.add(panelFieldKontrol);

        panelStep2Body.add(panelMetaPemeriksaan);

        javax.swing.GroupLayout panelStepPemeriksaanLayout = new javax.swing.GroupLayout(panelStepPemeriksaan);
        panelStepPemeriksaan.setLayout(panelStepPemeriksaanLayout);
        panelStepPemeriksaanLayout.setHorizontalGroup(
            panelStepPemeriksaanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelStep2Body, javax.swing.GroupLayout.DEFAULT_SIZE, 801, Short.MAX_VALUE)
        );
        panelStepPemeriksaanLayout.setVerticalGroup(
            panelStepPemeriksaanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelStep2Body, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        panelStepHost.add(panelStepPemeriksaan, "pemeriksaan");

        panelStepResep.setBackground(new java.awt.Color(255, 255, 255));

        panelStep3Body.setBackground(new java.awt.Color(255, 255, 255));
        panelStep3Body.setLayout(new javax.swing.BoxLayout(panelStep3Body, javax.swing.BoxLayout.Y_AXIS));

        panelResepHeader.setBackground(new java.awt.Color(255, 255, 255));
        panelResepHeader.setLayout(new java.awt.BorderLayout());

        lblResepTitle.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblResepTitle.setText("Resep obat");
        panelResepHeader.add(lblResepTitle, java.awt.BorderLayout.LINE_START);

        btnTambahObat.setText("+ Tambah obat");
        btnTambahObat.setFocusPainted(false);
        btnTambahObat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahObatActionPerformed(evt);
            }
        });
        panelResepHeader.add(btnTambahObat, java.awt.BorderLayout.LINE_END);

        panelStep3Body.add(panelResepHeader);
        panelStep3Body.add(Box.createVerticalStrut(12));

        scrollResep.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 218, 208)));
        scrollResep.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        panelResepItems.setBackground(new java.awt.Color(255, 255, 255));
        scrollResep.setViewportView(panelResepItems);

        panelStep3Body.add(scrollResep);
        panelStep3Body.add(Box.createVerticalStrut(12));

        panelFieldCatatanResep.setBackground(new java.awt.Color(255, 255, 255));
        panelFieldCatatanResep.setLayout(new java.awt.BorderLayout(0, 6));

        lblCatatanResep.setText("Catatan resep / instruksi tambahan");
        panelFieldCatatanResep.add(lblCatatanResep, java.awt.BorderLayout.PAGE_START);

        txtCatatanResep.setColumns(20);
        txtCatatanResep.setRows(4);
        scrollCatatanResep.setViewportView(txtCatatanResep);

        panelFieldCatatanResep.add(scrollCatatanResep, java.awt.BorderLayout.CENTER);

        panelStep3Body.add(panelFieldCatatanResep);

        javax.swing.GroupLayout panelStepResepLayout = new javax.swing.GroupLayout(panelStepResep);
        panelStepResep.setLayout(panelStepResepLayout);
        panelStepResepLayout.setHorizontalGroup(
            panelStepResepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelStep3Body, javax.swing.GroupLayout.DEFAULT_SIZE, 801, Short.MAX_VALUE)
        );
        panelStepResepLayout.setVerticalGroup(
            panelStepResepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelStep3Body, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        panelStepHost.add(panelStepResep, "resep");

        panelActions.setBackground(new java.awt.Color(255, 255, 255));
        panelActions.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));

        btnBack.setText("< Kembali");
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });
        panelActions.add(btnBack);

        btnSimpanDraft.setText("Simpan Draft");
        btnSimpanDraft.setFocusPainted(false);
        btnSimpanDraft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanDraftActionPerformed(evt);
            }
        });
        panelActions.add(btnSimpanDraft);

        btnNext.setText("Lanjut >");
        btnNext.setFocusPainted(false);
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });
        panelActions.add(btnNext);

        btnSimpanKunjungan.setText("Simpan & Lanjut Bayar >");
        btnSimpanKunjungan.setFocusPainted(false);
        btnSimpanKunjungan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanKunjunganActionPerformed(evt);
            }
        });
        panelActions.add(btnSimpanKunjungan);

        javax.swing.GroupLayout panelWizardCardLayout = new javax.swing.GroupLayout(panelWizardCard);
        panelWizardCard.setLayout(panelWizardCardLayout);
        panelWizardCardLayout.setHorizontalGroup(
            panelWizardCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelWizardCardLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelWizardCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelStepNav, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelStepHost, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelActions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        panelWizardCardLayout.setVerticalGroup(
            panelWizardCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelWizardCardLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(panelHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(panelStepNav, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(panelStepHost, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(panelActions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(panelWizardCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(panelActionBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(panelWizardCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
        navigateToKunjunganList();
    }//GEN-LAST:event_btnBatalHeaderActionPerformed

    private void btnTambahPasienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahPasienActionPerformed
        openPasienCreatePage();
    }//GEN-LAST:event_btnTambahPasienActionPerformed

    private void cmbPoliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPoliActionPerformed
        Object selected = cmbPoli.getSelectedItem();
        String poli = selected != null && cmbPoli.getSelectedIndex() > 0 ? selected.toString() : null;
        populateDokterCombo(poli);
    }//GEN-LAST:event_cmbPoliActionPerformed

    private void cmbDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbDokterActionPerformed
        if (suppressDoctorSync) {
            return;
        }
        Object selected = cmbDokter.getSelectedItem();
        if (!(selected instanceof Dokter dokter)) {
            return;
        }
        String poli = resolvePoliDokter(dokter);
        if (!Objects.equals(cmbPoli.getSelectedItem(), poli)) {
            selectComboValue(cmbPoli, poli);
        }
    }//GEN-LAST:event_cmbDokterActionPerformed

    private void btnTambahObatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahObatActionPerformed
        addResepRow(null);
    }//GEN-LAST:event_btnTambahObatActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        moveToPreviousStep();
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        moveToNextStep();
    }//GEN-LAST:event_btnNextActionPerformed

    private void btnSimpanDraftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanDraftActionPerformed
        saveKunjungan(true);
    }//GEN-LAST:event_btnSimpanDraftActionPerformed

    private void btnSimpanKunjunganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanKunjunganActionPerformed
        saveKunjungan(false);
    }//GEN-LAST:event_btnSimpanKunjunganActionPerformed

    private class ResepRowPanel extends JPanel {

        private final JComboBox<Object> cmbObatRow = new JComboBox<>();
        private final JSpinner spnJumlah = new JSpinner();
        private final JLabel lblSatuanValue = new JLabel("-");
        private final JTextField txtAturanPakaiRow = new JTextField();
        private final JButton btnHapusRow = new JButton("Hapus");

        ResepRowPanel(ResepObatItem item) {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_BORDER),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            setLayout(new GridLayout(1, 5, 10, 0));

            cmbObatRow.setRenderer(createComboRenderer(value -> {
                if (value instanceof Obat obat) {
                    return obat.getNamaObat();
                }
                return "Pilih obat...";
            }));
            DefaultComboBoxModel<Object> obatModel = new DefaultComboBoxModel<>();
            obatModel.addElement(null);
            availableObat.forEach(obatModel::addElement);
            cmbObatRow.setModel(obatModel);
            cmbObatRow.addActionListener(e -> updateSatuanLabel());
            styleComboBox(cmbObatRow);

            spnJumlah.setModel(new javax.swing.SpinnerNumberModel(1, 1, 999, 1));
            styleSpinner(spnJumlah);
            txtAturanPakaiRow.putClientProperty("JTextField.placeholderText", "Contoh: 3x1 setelah makan");
            styleTextField(txtAturanPakaiRow);
            lblSatuanValue.setForeground(COLOR_TEXT);
            lblSatuanValue.setBorder(createInputBorder());
            setComponentHeight(lblSatuanValue, FIELD_HEIGHT);
            btnHapusRow.setFocusPainted(false);
            btnHapusRow.setForeground(COLOR_DANGER);
            styleGhostButton(btnHapusRow);
            btnHapusRow.setForeground(COLOR_DANGER);
            btnHapusRow.addActionListener(e -> removeResepRow(this));

            add(wrapRowField("Nama obat", cmbObatRow));
            add(wrapRowField("Jumlah", spnJumlah));
            add(wrapRowField("Satuan", lblSatuanValue));
            add(wrapRowField("Aturan pakai", txtAturanPakaiRow));
            add(wrapRowField("Aksi", btnHapusRow));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

            if (item != null) {
                selectObatById(item.getObatId());
                spnJumlah.setValue(Math.max(item.getJumlah(), 1));
                txtAturanPakaiRow.setText(safe(item.getAturanPakai()));
                lblSatuanValue.setText(safe(item.getSatuan()).isBlank() ? "-" : item.getSatuan());
            }
        }

        private JPanel wrapRowField(String labelText, JComponent input) {
            JPanel wrapper = new JPanel(new BorderLayout(0, 6));
            wrapper.setBackground(Color.WHITE);
            JLabel label = new JLabel(labelText);
            styleFieldLabel(label);
            wrapper.add(label, BorderLayout.PAGE_START);
            wrapper.add(input, BorderLayout.CENTER);
            return wrapper;
        }

        private void updateSatuanLabel() {
            Object selected = cmbObatRow.getSelectedItem();
            if (selected instanceof Obat obat) {
                lblSatuanValue.setText(safe(obat.getSatuan()).isBlank() ? "-" : obat.getSatuan());
                return;
            }
            lblSatuanValue.setText("-");
        }

        private void selectObatById(int obatId) {
            for (int i = 0; i < cmbObatRow.getItemCount(); i++) {
                Object value = cmbObatRow.getItemAt(i);
                if (value instanceof Obat obat && obat.getId() == obatId) {
                    cmbObatRow.setSelectedIndex(i);
                    return;
                }
            }
        }

        private ResepObatItem toItem() {
            Object selected = cmbObatRow.getSelectedItem();
            String aturanPakai = blankToNull(txtAturanPakaiRow.getText());
            int jumlah = ((Number) spnJumlah.getValue()).intValue();

            if (!(selected instanceof Obat obat)) {
                if (aturanPakai == null && jumlah == 1) {
                    return null;
                }
                ResepObatItem invalidItem = new ResepObatItem();
                invalidItem.setObatId(0);
                invalidItem.setJumlah(jumlah);
                invalidItem.setAturanPakai(aturanPakai);
                return invalidItem;
            }

            ResepObatItem item = new ResepObatItem();
            item.setObatId(obat.getId());
            item.setNamaObat(obat.getNamaObat());
            item.setSatuan(obat.getSatuan());
            item.setJumlah(jumlah);
            item.setAturanPakai(aturanPakai);
            return item;
        }
    }

    private static class LookupData {
        private List<Pasien> pasien = List.of();
        private List<Dokter> dokter = List.of();
        private List<Obat> obat = List.of();
        private Kunjungan kunjungan;
        private List<ResepObatItem> resep = List.of();
    }

    private record ParsedDoctorNotes(String catatanUtama, String tindakan, LocalDate rencanaKontrol) {
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnBatalHeader;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnSimpanDraft;
    private javax.swing.JButton btnSimpanKunjungan;
    private javax.swing.JButton btnTambahObat;
    private javax.swing.JButton btnTambahPasien;
    private javax.swing.JComboBox<Object> cmbDokter;
    private javax.swing.JComboBox<String> cmbJenisKunjungan;
    private javax.swing.JComboBox<Object> cmbPasien;
    private javax.swing.JComboBox<String> cmbPoli;
    private javax.swing.JComboBox<String> cmbTindakan;
    private javax.swing.JLabel lblBeratBadan;
    private javax.swing.JLabel lblCatatanDokter;
    private javax.swing.JLabel lblCatatanResep;
    private javax.swing.JLabel lblDiagnosa;
    private javax.swing.JLabel lblDokter;
    private javax.swing.JLabel lblFormSubtitle;
    private javax.swing.JLabel lblFormTitle;
    private javax.swing.JLabel lblJenisKunjungan;
    private javax.swing.JLabel lblKeluhanUtama;
    private javax.swing.JLabel lblKodeIcd10;
    private javax.swing.JLabel lblNadi;
    private javax.swing.JLabel lblPasien;
    private javax.swing.JLabel lblPoli;
    private javax.swing.JLabel lblRencanaKontrol;
    private javax.swing.JLabel lblResepTitle;
    private javax.swing.JLabel lblSaturasi;
    private javax.swing.JLabel lblStep1;
    private javax.swing.JLabel lblStep2;
    private javax.swing.JLabel lblStep3;
    private javax.swing.JLabel lblSuhu;
    private javax.swing.JLabel lblTanggalPeriksa;
    private javax.swing.JLabel lblTekananDarah;
    private javax.swing.JLabel lblTindakan;
    private javax.swing.JLabel lblTinggiBadan;
    private javax.swing.JPanel panelActionBar;
    private javax.swing.JPanel panelActions;
    private javax.swing.JPanel panelDiagnosisRow;
    private javax.swing.JPanel panelFieldBerat;
    private javax.swing.JPanel panelFieldCatatanDokter;
    private javax.swing.JPanel panelFieldCatatanResep;
    private javax.swing.JPanel panelFieldDiagnosa;
    private javax.swing.JPanel panelFieldDokter;
    private javax.swing.JPanel panelFieldIcd;
    private javax.swing.JPanel panelFieldJenisKunjungan;
    private javax.swing.JPanel panelFieldKeluhan;
    private javax.swing.JPanel panelFieldKontrol;
    private javax.swing.JPanel panelFieldNadi;
    private javax.swing.JPanel panelFieldPasien;
    private javax.swing.JPanel panelFieldPoli;
    private javax.swing.JPanel panelFieldSaturasi;
    private javax.swing.JPanel panelFieldSuhu;
    private javax.swing.JPanel panelFieldTanggal;
    private javax.swing.JPanel panelFieldTekananDarah;
    private javax.swing.JPanel panelFieldTindakan;
    private javax.swing.JPanel panelFieldTinggi;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelMetaPemeriksaan;
    private javax.swing.JPanel panelPasienInput;
    private javax.swing.JPanel panelResepHeader;
    private javax.swing.JPanel panelResepItems;
    private javax.swing.JScrollPane scrollCatatanDokter;
    private javax.swing.JScrollPane scrollCatatanResep;
    private javax.swing.JScrollPane scrollKeluhan;
    private javax.swing.JScrollPane scrollResep;
    private javax.swing.JSpinner spnRencanaKontrol;
    private javax.swing.JSpinner spnTanggalPeriksa;
    private javax.swing.JPanel panelStep1Body;
    private javax.swing.JPanel panelStep1Row1;
    private javax.swing.JPanel panelStep1Row2;
    private javax.swing.JPanel panelStep2Body;
    private javax.swing.JPanel panelStep3Body;
    private javax.swing.JPanel panelStepHost;
    private javax.swing.JPanel panelStepNav;
    private javax.swing.JPanel panelStepPendaftaran;
    private javax.swing.JPanel panelStepPemeriksaan;
    private javax.swing.JPanel panelStepResep;
    private javax.swing.JPanel panelVitalRow1;
    private javax.swing.JPanel panelVitalRow2;
    private javax.swing.JPanel panelWizardCard;
    private javax.swing.JTextArea txtCatatanDokter;
    private javax.swing.JTextArea txtCatatanResep;
    private javax.swing.JTextField txtDiagnosa;
    private javax.swing.JTextField txtBeratBadan;
    private javax.swing.JTextArea txtKeluhanUtama;
    private javax.swing.JTextField txtKodeIcd10;
    private javax.swing.JTextField txtNadi;
    private javax.swing.JTextField txtSaturasi;
    private javax.swing.JTextField txtSuhu;
    private javax.swing.JTextField txtTekananDarah;
    private javax.swing.JTextField txtTinggiBadan;
    // End of variables declaration//GEN-END:variables
}
