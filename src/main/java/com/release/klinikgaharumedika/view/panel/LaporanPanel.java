package com.release.klinikgaharumedika.view.panel;

import com.release.klinikgaharumedika.service.report.PendapatanReportService;
import com.release.klinikgaharumedika.service.report.TableReportService;
import com.release.klinikgaharumedika.service.report.TableReportService.ReportType;
import java.beans.Beans;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.sf.jasperreports.engine.JasperPrint;

public class LaporanPanel extends javax.swing.JPanel {

    private LocalDate tanggalDari;
    private LocalDate tanggalSampai;
    private PendapatanReportService pendapatanReportService;
    private TableReportService tableReportService;

    public LaporanPanel(boolean dailyReportMode) { this(); }

    public LaporanPanel() {
        initComponents();
        setupSpinners();
        FormUiStyle.applyFormStyle(this);
        if (!Beans.isDesignTime()) {
            pendapatanReportService = new PendapatanReportService();
            tableReportService = new TableReportService();
            setupListeners();
        }
    }

    private void setupSpinners() {
        FormUiStyle.dateSpinner(spnDari);
        FormUiStyle.dateSpinner(spnSampai);

        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        spnDari.setValue(Date.from(firstOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        spnSampai.setValue(Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        tanggalDari   = firstOfMonth;
        tanggalSampai = today;
    }

    private void setupListeners() {
        btnTerapkan.addActionListener(e -> applyPeriod());

        btnPasienPreview.addActionListener(e     -> previewTableReport(ReportType.PASIEN));
        btnPasienPdf.addActionListener(e         -> exportTableReport(ReportType.PASIEN));

        btnPemeriksaanPreview.addActionListener(e -> previewTableReport(ReportType.PEMERIKSAAN));
        btnPemeriksaanPdf.addActionListener(e     -> exportTableReport(ReportType.PEMERIKSAAN));

        btnObatPreview.addActionListener(e       -> previewTableReport(ReportType.OBAT));
        btnObatPdf.addActionListener(e           -> exportTableReport(ReportType.OBAT));

        btnPendapatanPreview.addActionListener(e -> previewPendapatanReport());
        btnPendapatanPdf.addActionListener(e     -> exportPendapatanReport());
    }

    private void applyPeriod() {
        if (!syncSelectedPeriod()) {
            return;
        }
        JOptionPane.showMessageDialog(this,
            "Periode diterapkan: " + tanggalDari + " s.d. " + tanggalSampai,
            "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void previewPendapatanReport() {
        if (!syncSelectedPeriod()) {
            return;
        }

        setReportButtonsEnabled(false);
        new SwingWorker<JasperPrint, Void>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return pendapatanReportService.buildReport(tanggalDari, tanggalSampai);
            }

            @Override
            protected void done() {
                setReportButtonsEnabled(true);
                try {
                    pendapatanReportService.showPreview(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("Preview laporan pendapatan dibatalkan.", ex);
                } catch (ExecutionException ex) {
                    showError("Gagal menampilkan preview laporan pendapatan.", ex.getCause());
                }
            }
        }.execute();
    }

    private void exportPendapatanReport() {
        if (!syncSelectedPeriod()) {
            return;
        }

        File selectedFile = choosePdfTargetFile(
                "Simpan Laporan Pendapatan PDF",
                pendapatanReportService.buildDefaultFileName(tanggalDari, tanggalSampai)
        );
        if (selectedFile == null) {
            return;
        }

        setReportButtonsEnabled(false);
        new SwingWorker<Path, Void>() {
            @Override
            protected Path doInBackground() throws Exception {
                JasperPrint report = pendapatanReportService.buildReport(tanggalDari, tanggalSampai);
                return pendapatanReportService.exportPdf(report, selectedFile.toPath());
            }

            @Override
            protected void done() {
                setReportButtonsEnabled(true);
                try {
                    Path exportedFile = get();
                    JOptionPane.showMessageDialog(LaporanPanel.this,
                            "PDF berhasil disimpan di:\n" + exportedFile.toAbsolutePath(),
                            "Sukses",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("Export PDF laporan pendapatan dibatalkan.", ex);
                } catch (ExecutionException ex) {
                    showError("Gagal export PDF laporan pendapatan.", ex.getCause());
                }
            }
        }.execute();
    }

    private void previewTableReport(ReportType type) {
        if (!syncSelectedPeriod()) {
            return;
        }

        setReportButtonsEnabled(false);
        new SwingWorker<JasperPrint, Void>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return tableReportService.buildReport(type, tanggalDari, tanggalSampai);
            }

            @Override
            protected void done() {
                setReportButtonsEnabled(true);
                try {
                    tableReportService.showPreview(type, get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("Preview " + type.title() + " dibatalkan.", ex);
                } catch (ExecutionException ex) {
                    showError("Gagal menampilkan preview " + type.title() + ".", ex.getCause());
                }
            }
        }.execute();
    }

    private void exportTableReport(ReportType type) {
        if (!syncSelectedPeriod()) {
            return;
        }

        File selectedFile = choosePdfTargetFile(
                "Simpan " + type.title() + " PDF",
                tableReportService.buildDefaultFileName(type, tanggalDari, tanggalSampai)
        );
        if (selectedFile == null) {
            return;
        }

        setReportButtonsEnabled(false);
        new SwingWorker<Path, Void>() {
            @Override
            protected Path doInBackground() throws Exception {
                JasperPrint report = tableReportService.buildReport(type, tanggalDari, tanggalSampai);
                return tableReportService.exportPdf(report, selectedFile.toPath());
            }

            @Override
            protected void done() {
                setReportButtonsEnabled(true);
                try {
                    Path exportedFile = get();
                    JOptionPane.showMessageDialog(LaporanPanel.this,
                            "PDF berhasil disimpan di:\n" + exportedFile.toAbsolutePath(),
                            "Sukses",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("Export PDF " + type.title() + " dibatalkan.", ex);
                } catch (ExecutionException ex) {
                    showError("Gagal export PDF " + type.title() + ".", ex.getCause());
                }
            }
        }.execute();
    }

    private boolean syncSelectedPeriod() {
        Date dari   = (Date) spnDari.getValue();
        Date sampai = (Date) spnSampai.getValue();
        tanggalDari   = dari.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        tanggalSampai = sampai.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if (tanggalDari.isAfter(tanggalSampai)) {
            JOptionPane.showMessageDialog(this,
                    "Tanggal awal tidak boleh lebih besar dari tanggal akhir.",
                    "Periode tidak valid",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private File choosePdfTargetFile(String dialogTitle, String defaultFileName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(dialogTitle);
        chooser.setFileFilter(new FileNameExtensionFilter("PDF (*.pdf)", "pdf"));
        chooser.setSelectedFile(new File(defaultFileName));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File selectedFile = chooser.getSelectedFile();
        if (!selectedFile.getName().toLowerCase().endsWith(".pdf")) {
            selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".pdf");
        }

        if (selectedFile.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(
                    this,
                    "File sudah ada. Timpa file tersebut?",
                    "Konfirmasi",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (overwrite != JOptionPane.YES_OPTION) {
                return null;
            }
        }

        return selectedFile;
    }

    private void setReportButtonsEnabled(boolean enabled) {
        btnPasienPreview.setEnabled(enabled);
        btnPasienPdf.setEnabled(enabled);
        btnPemeriksaanPreview.setEnabled(enabled);
        btnPemeriksaanPdf.setEnabled(enabled);
        btnObatPreview.setEnabled(enabled);
        btnObatPdf.setEnabled(enabled);
        btnPendapatanPreview.setEnabled(enabled);
        btnPendapatanPdf.setEnabled(enabled);
    }

    private void showError(String title, Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause != null && rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        String detail = rootCause != null && rootCause.getMessage() != null
                ? rootCause.getMessage()
                : "Periksa koneksi database dan template report.";
        JOptionPane.showMessageDialog(this,
                title + "\n" + detail,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelMain = new javax.swing.JPanel();
        panelTopBar = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblSubtitle = new javax.swing.JLabel();
        panelPeriod = new javax.swing.JPanel();
        lblPeriodTitle = new javax.swing.JLabel();
        lblDari = new javax.swing.JLabel();
        spnDari = new javax.swing.JSpinner();
        lblSampai = new javax.swing.JLabel();
        spnSampai = new javax.swing.JSpinner();
        btnTerapkan = new javax.swing.JButton();
        panelGrid = new javax.swing.JPanel();
        panelCardTop = new javax.swing.JPanel();
        cardPasien = new javax.swing.JPanel();
        lblCardPasienTitle = new javax.swing.JLabel();
        lblCardPasienDesc = new javax.swing.JLabel();
        btnPasienPreview = new javax.swing.JButton();
        btnPasienPdf = new javax.swing.JButton();
        cardPemeriksaan = new javax.swing.JPanel();
        lblCardPemeriksaanTitle = new javax.swing.JLabel();
        lblCardPemeriksaanDesc = new javax.swing.JLabel();
        btnPemeriksaanPreview = new javax.swing.JButton();
        btnPemeriksaanPdf = new javax.swing.JButton();
        panelCardBottom = new javax.swing.JPanel();
        cardObat = new javax.swing.JPanel();
        lblCardObatTitle = new javax.swing.JLabel();
        lblCardObatDesc = new javax.swing.JLabel();
        btnObatPreview = new javax.swing.JButton();
        btnObatPdf = new javax.swing.JButton();
        cardPendapatan = new javax.swing.JPanel();
        lblCardPendapatanTitle = new javax.swing.JLabel();
        lblCardPendapatanDesc = new javax.swing.JLabel();
        btnPendapatanPreview = new javax.swing.JButton();
        btnPendapatanPdf = new javax.swing.JButton();

        // TOP BAR
        panelTopBar.setBackground(java.awt.Color.WHITE);
        panelTopBar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 218, 208)));
        lblTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        lblTitle.setText("Laporan");
        lblSubtitle.setForeground(new java.awt.Color(120, 120, 120));
        lblSubtitle.setText("Cetak laporan PDF klinik");
        javax.swing.GroupLayout panelTopBarLayout = new javax.swing.GroupLayout(panelTopBar);
        panelTopBar.setLayout(panelTopBarLayout);
        panelTopBarLayout.setHorizontalGroup(
            panelTopBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTopBarLayout.createSequentialGroup()
                .addGap(18)
                .addGroup(panelTopBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitle)
                    .addComponent(lblSubtitle))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelTopBarLayout.setVerticalGroup(
            panelTopBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTopBarLayout.createSequentialGroup()
                .addGap(12)
                .addComponent(lblTitle)
                .addGap(2)
                .addComponent(lblSubtitle)
                .addGap(12))
        );

        // PERIOD FILTER
        panelPeriod.setBackground(java.awt.Color.WHITE);
        panelPeriod.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 218, 208)));
        lblPeriodTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        lblPeriodTitle.setText("Periode Laporan:");
        lblDari.setText("Dari:");
        lblSampai.setText("Sampai:");
        btnTerapkan.setBackground(new java.awt.Color(28, 112, 77));
        btnTerapkan.setForeground(java.awt.Color.WHITE);
        btnTerapkan.setText("Terapkan");
        javax.swing.GroupLayout panelPeriodLayout = new javax.swing.GroupLayout(panelPeriod);
        panelPeriod.setLayout(panelPeriodLayout);
        panelPeriodLayout.setHorizontalGroup(
            panelPeriodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPeriodLayout.createSequentialGroup()
                .addGap(16)
                .addComponent(lblPeriodTitle)
                .addGap(16)
                .addComponent(lblDari)
                .addGap(6)
                .addComponent(spnDari, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10)
                .addComponent(lblSampai)
                .addGap(6)
                .addComponent(spnSampai, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10)
                .addComponent(btnTerapkan)
                .addContainerGap(100, Short.MAX_VALUE))
        );
        panelPeriodLayout.setVerticalGroup(
            panelPeriodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(lblPeriodTitle)
            .addComponent(lblDari)
            .addComponent(spnDari, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(lblSampai)
            .addComponent(spnSampai, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnTerapkan, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        // Helper: build one report card
        buildCard(cardPasien, lblCardPasienTitle, "Laporan Data Pasien",
                  lblCardPasienDesc, "Rekap data seluruh pasien terdaftar",
                  btnPasienPreview, btnPasienPdf);
        buildCard(cardPemeriksaan, lblCardPemeriksaanTitle, "Laporan Pemeriksaan",
                  lblCardPemeriksaanDesc, "Rekap kunjungan dan diagnosa pasien",
                  btnPemeriksaanPreview, btnPemeriksaanPdf);
        buildCard(cardObat, lblCardObatTitle, "Laporan Penjualan Obat",
                  lblCardObatDesc, "Rekap obat terjual dan stok",
                  btnObatPreview, btnObatPdf);
        buildCard(cardPendapatan, lblCardPendapatanTitle, "Laporan Pendapatan",
                  lblCardPendapatanDesc, "Rekap total pendapatan klinik",
                  btnPendapatanPreview, btnPendapatanPdf);

        // TOP ROW
        panelCardTop.setBackground(new java.awt.Color(243, 240, 233));
        javax.swing.GroupLayout panelCardTopLayout = new javax.swing.GroupLayout(panelCardTop);
        panelCardTop.setLayout(panelCardTopLayout);
        panelCardTopLayout.setHorizontalGroup(
            panelCardTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardTopLayout.createSequentialGroup()
                .addComponent(cardPasien, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10)
                .addComponent(cardPemeriksaan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelCardTopLayout.setVerticalGroup(
            panelCardTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(cardPasien, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cardPemeriksaan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        // BOTTOM ROW
        panelCardBottom.setBackground(new java.awt.Color(243, 240, 233));
        javax.swing.GroupLayout panelCardBottomLayout = new javax.swing.GroupLayout(panelCardBottom);
        panelCardBottom.setLayout(panelCardBottomLayout);
        panelCardBottomLayout.setHorizontalGroup(
            panelCardBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCardBottomLayout.createSequentialGroup()
                .addComponent(cardObat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10)
                .addComponent(cardPendapatan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelCardBottomLayout.setVerticalGroup(
            panelCardBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(cardObat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cardPendapatan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        // GRID
        panelGrid.setBackground(new java.awt.Color(243, 240, 233));
        javax.swing.GroupLayout panelGridLayout = new javax.swing.GroupLayout(panelGrid);
        panelGrid.setLayout(panelGridLayout);
        panelGridLayout.setHorizontalGroup(
            panelGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelCardTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelCardBottom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelGridLayout.setVerticalGroup(
            panelGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGridLayout.createSequentialGroup()
                .addComponent(panelCardTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10)
                .addComponent(panelCardBottom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        // MAIN
        panelMain.setBackground(new java.awt.Color(243, 240, 233));
        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelTopBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addGap(16)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelPeriod, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelGrid, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16))
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addComponent(panelTopBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14)
                .addComponent(panelPeriod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14)
                .addComponent(panelGrid, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(16))
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

    /** Build a report card panel with title, description, and PDF actions. */
    private void buildCard(javax.swing.JPanel card,
                           javax.swing.JLabel titleLbl, String titleText,
                           javax.swing.JLabel descLbl, String descText,
                           javax.swing.JButton btnPreview,
                           javax.swing.JButton btnPdf) {
        card.setBackground(java.awt.Color.WHITE);
        card.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 218, 208)));

        titleLbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        titleLbl.setText(titleText);

        descLbl.setForeground(new java.awt.Color(120, 120, 120));
        descLbl.setText(descText);

        btnPreview.setText("Preview");
        btnPdf.setText("PDF");

        javax.swing.GroupLayout gl = new javax.swing.GroupLayout(card);
        card.setLayout(gl);
        gl.setHorizontalGroup(
            gl.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gl.createSequentialGroup()
                .addGap(16)
                .addGroup(gl.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(titleLbl)
                    .addComponent(descLbl)
                    .addGroup(gl.createSequentialGroup()
                        .addComponent(btnPreview)
                        .addGap(6)
                        .addComponent(btnPdf)))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        gl.setVerticalGroup(
            gl.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gl.createSequentialGroup()
                .addGap(16)
                .addComponent(titleLbl)
                .addGap(4)
                .addComponent(descLbl)
                .addGap(12)
                .addGroup(gl.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnPreview, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPdf, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16))
        );
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelTopBar;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblSubtitle;
    private javax.swing.JPanel panelPeriod;
    private javax.swing.JLabel lblPeriodTitle;
    private javax.swing.JLabel lblDari;
    private javax.swing.JSpinner spnDari;
    private javax.swing.JLabel lblSampai;
    private javax.swing.JSpinner spnSampai;
    private javax.swing.JButton btnTerapkan;
    private javax.swing.JPanel panelGrid;
    private javax.swing.JPanel panelCardTop;
    private javax.swing.JPanel cardPasien;
    private javax.swing.JLabel lblCardPasienTitle;
    private javax.swing.JLabel lblCardPasienDesc;
    private javax.swing.JButton btnPasienPreview;
    private javax.swing.JButton btnPasienPdf;
    private javax.swing.JPanel cardPemeriksaan;
    private javax.swing.JLabel lblCardPemeriksaanTitle;
    private javax.swing.JLabel lblCardPemeriksaanDesc;
    private javax.swing.JButton btnPemeriksaanPreview;
    private javax.swing.JButton btnPemeriksaanPdf;
    private javax.swing.JPanel panelCardBottom;
    private javax.swing.JPanel cardObat;
    private javax.swing.JLabel lblCardObatTitle;
    private javax.swing.JLabel lblCardObatDesc;
    private javax.swing.JButton btnObatPreview;
    private javax.swing.JButton btnObatPdf;
    private javax.swing.JPanel cardPendapatan;
    private javax.swing.JLabel lblCardPendapatanTitle;
    private javax.swing.JLabel lblCardPendapatanDesc;
    private javax.swing.JButton btnPendapatanPreview;
    private javax.swing.JButton btnPendapatanPdf;
    // End of variables declaration//GEN-END:variables
}
