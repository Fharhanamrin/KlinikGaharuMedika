package com.release.klinikgaharumedika.view.panel;

import com.release.klinikgaharumedika.controller.KunjunganController;
import com.release.klinikgaharumedika.model.Kunjungan;
import com.release.klinikgaharumedika.model.PageResult;
import com.release.klinikgaharumedika.view.DashboardForm;
import java.awt.Color;
import java.awt.Component;
import java.beans.Beans;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class KunjunganPanel extends javax.swing.JPanel {

    private static final Color COLOR_SUCCESS = new Color(28, 112, 77);
    private static final Color COLOR_WARNING = new Color(171, 116, 34);
    private static final Color COLOR_MUTED = new Color(108, 117, 125);
    private static final Color COLOR_DANGER = new Color(208, 70, 55);
    private static final int PAGE_SIZE = 10;
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID"));
    private static final int COL_NO_KUNJUNGAN = 0;
    private static final int COL_NO_ANTRIAN = 1;
    private static final int COL_TANGGAL = 2;
    private static final int COL_PASIEN = 3;
    private static final int COL_DOKTER = 4;
    private static final int COL_JENIS = 5;
    private static final int COL_STATUS = 6;
    private static final int COL_BAYAR = 7;
    private static final int COL_AKSI = 8;

    private final KunjunganController controller;

    private int currentPage = 1;
    private int totalPages = 1;
    private int totalItems = 0;
    private String searchKeyword = "";
    private List<Kunjungan> currentData;

    public KunjunganPanel(boolean listMode) {
        this();
    }

    public KunjunganPanel() {
        initComponents();
        configureActionButtons();
        setupSearch();
        FormUiStyle.applyFormStyle(this);
        if (Beans.isDesignTime()) {
            controller = null;
            return;
        }
        controller = new KunjunganController();
        configureTable();
        loadData();
    }

    private void configureActionButtons() {
        stylePrimaryButton(btnDaftarkanPasien);
    }

    private void stylePrimaryButton(JButton button) {
        FormUiStyle.stylePrimaryButton(button);
    }

    private void configureTable() {
        configureTableBase();
        configureColumnWidths();
        configureColumnRenderers();
    }

    private void configureTableBase() {
        tblData.setDefaultEditor(Object.class, null);
        tblData.setRowHeight(32);
        tblData.setShowGrid(true);
        tblData.setGridColor(new Color(237, 236, 232));
        tblData.getTableHeader().setReorderingAllowed(false);
        tblData.getTableHeader().setBackground(new Color(243, 240, 233));
        tblData.setSelectionBackground(new Color(220, 235, 225));
    }

    private void configureColumnWidths() {
        setColumnWidth(COL_NO_KUNJUNGAN, 120);
        setColumnWidth(COL_NO_ANTRIAN, 90);
        setColumnWidth(COL_TANGGAL, 110);
        setColumnWidth(COL_PASIEN, 150);
        setColumnWidth(COL_DOKTER, 150);
        setColumnWidth(COL_JENIS, 90);
        setColumnWidth(COL_STATUS, 90);
        setColumnWidth(COL_BAYAR, 90);
        setColumnWidth(COL_AKSI, 110);
    }

    private void configureColumnRenderers() {
        DefaultTableCellRenderer padded = new PaddedRenderer();
        setColumnRenderer(COL_NO_KUNJUNGAN, padded);
        setColumnRenderer(COL_NO_ANTRIAN, padded);
        setColumnRenderer(COL_TANGGAL, padded);
        setColumnRenderer(COL_PASIEN, padded);
        setColumnRenderer(COL_DOKTER, padded);
        setColumnRenderer(COL_JENIS, padded);
        setColumnRenderer(COL_STATUS, new StatusKunjunganRenderer());
        setColumnRenderer(COL_BAYAR, new StatusBayarRenderer());
        setColumnRenderer(COL_AKSI, new AksiCellRenderer());
    }

    private void setColumnWidth(int columnIndex, int width) {
        if (!hasColumn(columnIndex)) {
            return;
        }
        tblData.getColumnModel().getColumn(columnIndex).setPreferredWidth(width);
    }

    private void setColumnRenderer(int columnIndex, DefaultTableCellRenderer renderer) {
        if (!hasColumn(columnIndex)) {
            return;
        }
        tblData.getColumnModel().getColumn(columnIndex).setCellRenderer(renderer);
    }

    private boolean hasColumn(int columnIndex) {
        return columnIndex >= 0 && columnIndex < tblData.getColumnModel().getColumnCount();
    }

    private void setupSearch() {
        txtSearch.putClientProperty(
                "JTextField.placeholderText",
                "Contoh: KJ260401001 / A001 / Ahmad Fauzi"
        );
    }

    private void performSearch() {
        searchKeyword = txtSearch.getText().trim();
        currentPage = 1;
        loadData();
    }

    private void loadData() {
        new SwingWorker<PageResult<Kunjungan>, Void>() {
            @Override
            protected PageResult<Kunjungan> doInBackground() throws Exception {
                return controller.loadPage(currentPage, PAGE_SIZE, searchKeyword);
            }

            @Override
            protected void done() {
                try {
                    applyData(get());
                } catch (Exception ex) {
                    showLoadError(ex);
                }
            }
        }.execute();
    }

    private void showLoadError(Exception ex) {
        lblSubtitle.setText("Gagal memuat data.");
        JOptionPane.showMessageDialog(
                this,
                "Gagal memuat data kunjungan.\n" + ex.getMessage(),
                "Gagal",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void applyData(PageResult<Kunjungan> result) {
        currentData = result.getItems();
        totalItems = result.getTotalItems();
        totalPages = result.getTotalPages();

        DefaultTableModel model = (DefaultTableModel) tblData.getModel();
        model.setRowCount(0);
        for (Kunjungan kunjungan : currentData) {
            model.addRow(toTableRow(kunjungan));
        }

        lblSubtitle.setText(totalItems + " kunjungan tercatat");
        int from = totalItems == 0 ? 0 : (currentPage - 1) * PAGE_SIZE + 1;
        int to = Math.min(currentPage * PAGE_SIZE, totalItems);
        lblPageInfo.setText("Menampilkan " + from + "-" + to + " dari " + totalItems + " data");
        btnPrevPage.setEnabled(currentPage > 1);
        btnNextPage.setEnabled(currentPage < totalPages);
    }

    private Object[] toTableRow(Kunjungan kunjungan) {
        return new Object[]{
            safeValue(kunjungan.getNoKunjungan()),
            safeValue(kunjungan.getNoAntrian()),
            kunjungan.getTanggalKunjungan() != null ? kunjungan.getTanggalKunjungan().format(DATE_FMT) : "-",
            safeValue(kunjungan.getNamaPasien()),
            formatNamaDokter(kunjungan.getNamaDokter()),
            capitalizeOrDash(kunjungan.getJenisKunjungan()),
            capitalizeOrDash(kunjungan.getStatus()),
            formatBayar(kunjungan.getStatusBayar()),
            "Edit | Hapus"
        };
    }

    private String safeValue(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String formatNamaDokter(String namaDokter) {
        if (namaDokter == null || namaDokter.isBlank()) {
            return "-";
        }
        return namaDokter.startsWith("dr.") ? namaDokter : "dr. " + namaDokter;
    }

    private String formatBayar(String status) {
        if (status == null || status.isBlank()) {
            return "-";
        }
        return capitalizeOrDash(status);
    }

    private void handleRowAction(int rowIndex, int clickX) {
        if (currentData == null || rowIndex < 0 || rowIndex >= currentData.size()) {
            return;
        }

        Kunjungan kunjungan = currentData.get(rowIndex);
        if (clickX < getAksiCellMidpoint()) {
            openEditInfo(kunjungan);
        } else {
            deleteKunjungan(kunjungan);
        }
    }

    private int getAksiCellMidpoint() {
        return tblData.getColumnModel().getColumn(COL_AKSI).getWidth() / 2;
    }

    private void openEditInfo(Kunjungan kunjungan) {
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof DashboardForm dashboardForm) {
            dashboardForm.showKunjunganEditPage(kunjungan);
            return;
        }
        JOptionPane.showMessageDialog(this, "Dashboard utama tidak ditemukan.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteKunjungan(Kunjungan kunjungan) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Hapus data kunjungan " + safeValue(kunjungan.getNoKunjungan()) + "?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean berhasil = controller.hapus(kunjungan.getId());
            if (berhasil) {
                if (currentData != null && currentData.size() == 1 && currentPage > 1) {
                    currentPage--;
                }
                loadData();
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Gagal menghapus kunjungan.",
                    "Gagal",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Gagal menghapus kunjungan.\n" + ex.getMessage(),
                    "Gagal",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String capitalizeOrDash(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelMain = new javax.swing.JPanel();
        panelTopBar = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblSubtitle = new javax.swing.JLabel();
        btnDaftarkanPasien = new javax.swing.JButton();
        panelContent = new javax.swing.JPanel();
        panelContentHeader = new javax.swing.JPanel();
        lblContentTitle = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblData = new javax.swing.JTable();
        panelPagination = new javax.swing.JPanel();
        lblPageInfo = new javax.swing.JLabel();
        btnPrevPage = new javax.swing.JButton();
        btnNextPage = new javax.swing.JButton();

        panelMain.setBackground(new java.awt.Color(243, 240, 233));

        panelTopBar.setBackground(new java.awt.Color(255, 255, 255));
        panelTopBar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 218, 208)));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTitle.setText("Data Kunjungan");

        lblSubtitle.setForeground(new java.awt.Color(120, 120, 120));
        lblSubtitle.setText("Memuat...");

        btnDaftarkanPasien.setBackground(new java.awt.Color(28, 112, 77));
        btnDaftarkanPasien.setForeground(new java.awt.Color(255, 255, 255));
        btnDaftarkanPasien.setText("+ Daftarkan Pasien");
        btnDaftarkanPasien.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDaftarkanPasienActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelTopBarLayout = new javax.swing.GroupLayout(panelTopBar);
        panelTopBar.setLayout(panelTopBarLayout);
        panelTopBarLayout.setHorizontalGroup(
            panelTopBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTopBarLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(panelTopBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitle)
                    .addComponent(lblSubtitle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnDaftarkanPasien)
                .addGap(14, 14, 14))
        );
        panelTopBarLayout.setVerticalGroup(
            panelTopBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTopBarLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(panelTopBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelTopBarLayout.createSequentialGroup()
                        .addComponent(lblTitle)
                        .addGap(0, 2, 2)
                        .addComponent(lblSubtitle))
                    .addComponent(btnDaftarkanPasien, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );

        panelContent.setBackground(new java.awt.Color(255, 255, 255));
        panelContent.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 218, 208)));

        panelContentHeader.setBackground(new java.awt.Color(255, 255, 255));

        lblContentTitle.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblContentTitle.setText("Data Kunjungan");

        txtSearch.setToolTipText("Cari...");
        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelContentHeaderLayout = new javax.swing.GroupLayout(panelContentHeader);
        panelContentHeader.setLayout(panelContentHeaderLayout);
        panelContentHeaderLayout.setHorizontalGroup(
            panelContentHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelContentHeaderLayout.createSequentialGroup()
                .addComponent(lblContentTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelContentHeaderLayout.setVerticalGroup(
            panelContentHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(lblContentTitle)
            .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        tblData.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "NO. KUNJUNGAN", "ANTRIAN", "TGL. KUNJUNGAN", "PASIEN", "DOKTER", "JENIS", "STATUS", "BAYAR", "AKSI"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblData.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblData.setRowHeight(25);
        tblData.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblData.setShowHorizontalLines(true);
        tblData.setShowVerticalLines(true);
        tblData.getTableHeader().setReorderingAllowed(false);
        tblData.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblDataMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblData);

        panelPagination.setBackground(new java.awt.Color(255, 255, 255));

        lblPageInfo.setForeground(new java.awt.Color(120, 120, 120));
        lblPageInfo.setText("Menampilkan 0 data");

        btnPrevPage.setText("< Sebelumnya");
        btnPrevPage.setEnabled(false);
        btnPrevPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevPageActionPerformed(evt);
            }
        });

        btnNextPage.setText("Selanjutnya >");
        btnNextPage.setEnabled(false);
        btnNextPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextPageActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelPaginationLayout = new javax.swing.GroupLayout(panelPagination);
        panelPagination.setLayout(panelPaginationLayout);
        panelPaginationLayout.setHorizontalGroup(
            panelPaginationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPaginationLayout.createSequentialGroup()
                .addComponent(lblPageInfo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 228, Short.MAX_VALUE)
                .addComponent(btnPrevPage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNextPage))
        );
        panelPaginationLayout.setVerticalGroup(
            panelPaginationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(lblPageInfo)
            .addComponent(btnPrevPage)
            .addComponent(btnNextPage)
        );

        javax.swing.GroupLayout panelContentLayout = new javax.swing.GroupLayout(panelContent);
        panelContent.setLayout(panelContentLayout);
        panelContentLayout.setHorizontalGroup(
            panelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelContentLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(panelContentHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelPagination, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        panelContentLayout.setVerticalGroup(
            panelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelContentLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(panelContentHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
                .addGap(8, 8, 8)
                .addComponent(panelPagination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(panelTopBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(16, 16, 16))
            .addGroup(panelMainLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(panelContent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(16, 16, 16))
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(panelTopBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(panelContent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

    private void btnDaftarkanPasienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDaftarkanPasienActionPerformed
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof DashboardForm dashboardForm) {
            dashboardForm.showKunjunganCreatePage();
            return;
        }
        JOptionPane.showMessageDialog(this, "Dashboard utama tidak ditemukan.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_btnDaftarkanPasienActionPerformed

    private void btnPrevPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevPageActionPerformed
        if (currentPage > 1) {
            currentPage--;
            loadData();
        }
    }//GEN-LAST:event_btnPrevPageActionPerformed

    private void btnNextPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextPageActionPerformed
        if (currentPage < totalPages) {
            currentPage++;
            loadData();
        }
    }//GEN-LAST:event_btnNextPageActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        performSearch();
    }//GEN-LAST:event_txtSearchActionPerformed

    private void tblDataMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblDataMouseClicked
        int row = tblData.rowAtPoint(evt.getPoint());
        int column = tblData.columnAtPoint(evt.getPoint());
        if (row < 0 || column != COL_AKSI) {
            return;
        }
        java.awt.Rectangle cellRect = tblData.getCellRect(row, column, true);
        int clickX = evt.getX() - cellRect.x;
        handleRowAction(row, clickX);
    }//GEN-LAST:event_tblDataMouseClicked

    private static class PaddedRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                javax.swing.JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return this;
        }
    }

    private class StatusKunjunganRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                javax.swing.JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setHorizontalAlignment(SwingConstants.CENTER);
            String text = value != null ? value.toString().toLowerCase() : "";
            switch (text) {
                case "selesai":
                    setForeground(COLOR_SUCCESS);
                    break;
                case "periksa":
                    setForeground(COLOR_WARNING);
                    break;
                default:
                    setForeground(COLOR_MUTED);
                    break;
            }
            setFont(getFont().deriveFont(java.awt.Font.BOLD));
            return this;
        }
    }

    private class StatusBayarRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                javax.swing.JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setHorizontalAlignment(SwingConstants.CENTER);
            String text = value != null ? value.toString().toLowerCase() : "";
            switch (text) {
                case "lunas":
                    setForeground(COLOR_SUCCESS);
                    break;
                case "pending":
                    setForeground(COLOR_WARNING);
                    break;
                default:
                    setForeground(COLOR_DANGER);
                    break;
            }
            setFont(getFont().deriveFont(java.awt.Font.BOLD));
            return this;
        }
    }

    private class AksiCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                javax.swing.JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setHorizontalAlignment(SwingConstants.CENTER);
            if (!isSelected) {
                setForeground(COLOR_DANGER);
            }
            return this;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDaftarkanPasien;
    private javax.swing.JButton btnNextPage;
    private javax.swing.JButton btnPrevPage;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblContentTitle;
    private javax.swing.JLabel lblPageInfo;
    private javax.swing.JLabel lblSubtitle;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel panelContent;
    private javax.swing.JPanel panelContentHeader;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelPagination;
    private javax.swing.JPanel panelTopBar;
    private javax.swing.JTable tblData;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
