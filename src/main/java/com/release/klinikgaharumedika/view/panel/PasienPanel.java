package com.release.klinikgaharumedika.view.panel;

import com.release.klinikgaharumedika.controller.PasienController;
import com.release.klinikgaharumedika.model.PageResult;
import com.release.klinikgaharumedika.model.Pasien;
import com.release.klinikgaharumedika.view.DashboardForm;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.Beans;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class PasienPanel extends javax.swing.JPanel {

    private static final Color COLOR_SUCCESS = new Color(28, 112, 77);
    private static final Color COLOR_WARNING = new Color(171, 116, 34);
    private static final Color COLOR_MUTED   = new Color(108, 117, 125);
    private static final int PAGE_SIZE = 10;
    private static final int COL_RM = 0;
    private static final int COL_NAMA = 1;
    private static final int COL_NIK = 2;
    private static final int COL_TGL_LAHIR = 3;
    private static final int COL_JK = 4;
    private static final int COL_GOL_DARAH = 5;
    private static final int COL_NO_HP = 6;
    private static final int COL_STATUS = 7;
    private static final int COL_AKSI = 8;
    private static final Color COLOR_DANGER  = new Color(208, 70, 55);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PasienController controller;

    private int currentPage = 1;
    private int totalPages = 1;
    private int totalItems = 0;
    private String searchKeyword = "";
    private List<Pasien> currentData;

    public PasienPanel(boolean listMode) { this(); }

    public PasienPanel() {
        initComponents();
        configureActionButtons();
        setupSearch();
        FormUiStyle.applyFormStyle(this);
        if (Beans.isDesignTime()) {
            controller = null;
            return;
        }
        controller = new PasienController();
        configureTable();
        loadData();
    }

    private void configureActionButtons() {
        stylePrimaryButton(btnTambahPasien);
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
        setColumnWidth(COL_RM, 100);
        setColumnWidth(COL_NAMA, 170);
        setColumnWidth(COL_NIK, 130);
        setColumnWidth(COL_TGL_LAHIR, 100);
        setColumnWidth(COL_JK, 60);
        setColumnWidth(COL_GOL_DARAH, 80);
        setColumnWidth(COL_NO_HP, 120);
        setColumnWidth(COL_STATUS, 90);
        setColumnWidth(COL_AKSI, 110);
    }

    private void configureColumnRenderers() {
        DefaultTableCellRenderer padded = new PaddedRenderer();

        setColumnRenderer(COL_RM, padded);
        setColumnRenderer(COL_NAMA, padded);
        setColumnRenderer(COL_NIK, padded);
        setColumnRenderer(COL_TGL_LAHIR, padded);
        setColumnRenderer(COL_JK, padded);
        setColumnRenderer(COL_GOL_DARAH, padded);
        setColumnRenderer(COL_NO_HP, padded);
        setColumnRenderer(COL_STATUS, new StatusCellRenderer());
        setColumnRenderer(COL_AKSI, new AksiCellRenderer());
    }

    private void setColumnWidth(int columnIndex, int width) {
        if (!hasColumn(columnIndex)) return;
        tblData.getColumnModel().getColumn(columnIndex).setPreferredWidth(width);
    }

    private void setColumnRenderer(int columnIndex, DefaultTableCellRenderer renderer) {
        if (!hasColumn(columnIndex)) return;
        tblData.getColumnModel().getColumn(columnIndex).setCellRenderer(renderer);
    }

    private boolean hasColumn(int columnIndex) {
        return columnIndex >= 0 && columnIndex < tblData.getColumnModel().getColumnCount();
    }

    private void setupSearch() {
        txtSearch.putClientProperty("JTextField.placeholderText", "Contoh: Ahmad Fauzi / RM0000001 / 3175110201000001");
        txtSearch.addActionListener(evt -> performSearch());
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    performSearch();
                }
            }
        });
    }

    private void performSearch() {
        searchKeyword = txtSearch.getText().trim();
        currentPage = 1;
        loadData();
    }

    private void loadData() {
        new SwingWorker<PageResult<Pasien>, Void>() {
            @Override protected PageResult<Pasien> doInBackground() throws Exception {
                return controller.loadPage(currentPage, PAGE_SIZE, searchKeyword);
            }
            @Override protected void done() {
                try { applyData(get()); }
                catch (Exception ex) { lblSubtitle.setText("Gagal memuat data."); }
            }
        }.execute();
    }

    private void applyData(PageResult<Pasien> result) {
        currentData = result.getItems();
        totalItems  = result.getTotalItems();
        totalPages  = result.getTotalPages();

        DefaultTableModel model = (DefaultTableModel) tblData.getModel();
        model.setRowCount(0);
        for (Pasien p : currentData) {
            model.addRow(toTableRow(p));
        }

        lblSubtitle.setText(totalItems + " pasien terdaftar");
        int from = totalItems == 0 ? 0 : (currentPage - 1) * PAGE_SIZE + 1;
        int to   = Math.min(currentPage * PAGE_SIZE, totalItems);
        lblPageInfo.setText("Menampilkan " + from + "-" + to + " dari " + totalItems + " data");
        btnPrevPage.setEnabled(currentPage > 1);
        btnNextPage.setEnabled(currentPage < totalPages);
    }

    private String maskHp(String hp) {
        if (hp == null || hp.length() < 8) return hp != null ? hp : "-";
        return hp.substring(0, 8) + "...";
    }

    private String formatDate(LocalDate value) {
        return value == null ? "-" : value.format(DATE_FORMAT);
    }

    private Object[] toTableRow(Pasien pasien) {
        return new Object[]{
            pasien.getNoRm() != null ? pasien.getNoRm() : "-",
            pasien.getNama(),
            maskNik(pasien.getNik()),
            formatDate(pasien.getTanggalLahir()),
            pasien.getJenisKelaminDisplay(),
            pasien.getGolonganDarah() != null ? pasien.getGolonganDarah() : "-",
            maskHp(pasien.getNoHp()),
            pasien.getJenisPasien() != null ? capitalize(pasien.getJenisPasien()) : "-",
            "Edit | Hapus"
        };
    }

    private void handleRowAction(int rowIndex, int clickX) {
        if (currentData == null || rowIndex < 0 || rowIndex >= currentData.size()) {
            return;
        }
        Pasien pasien = currentData.get(rowIndex);
        if (clickX < getAksiCellMidpoint()) {
            openEditPage(pasien);
        } else {
            deletePasien(pasien);
        }
    }

    private int getAksiCellMidpoint() {
        int columnWidth = tblData.getColumnModel().getColumn(COL_AKSI).getWidth();
        return columnWidth / 2;
    }

    private void openEditPage(Pasien pasien) {
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof DashboardForm dashboardForm) {
            dashboardForm.showPasienEditPage(pasien);
            return;
        }
        JOptionPane.showMessageDialog(this, "Dashboard utama tidak ditemukan.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deletePasien(Pasien pasien) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Hapus data pasien " + pasien.getNama() + "?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            boolean berhasil = controller.hapus(pasien.getId());
            if (berhasil) {
                if (currentData != null && currentData.size() == 1 && currentPage > 1) {
                    currentPage--;
                }
                loadData();
                JOptionPane.showMessageDialog(this, "Data pasien berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "Data pasien gagal dihapus.", "Gagal", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus pasien.\n" + ex.getMessage(), "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String maskNik(String nik) {
        if (nik == null || nik.length() < 8) return nik != null ? nik : "-";
        return nik.substring(0, 4) + "xxxxxxxxxx";
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelMain = new javax.swing.JPanel();
        panelTopBar = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblSubtitle = new javax.swing.JLabel();
        btnTambahPasien = new javax.swing.JButton();
        panelContent = new javax.swing.JPanel();
        panelContentHeader = new javax.swing.JPanel();
        lblContentTitle = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        panelPagination = new javax.swing.JPanel();
        lblPageInfo = new javax.swing.JLabel();
        btnPrevPage = new javax.swing.JButton();
        btnNextPage = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblData = new javax.swing.JTable();

        panelMain.setBackground(new java.awt.Color(243, 240, 233));

        panelTopBar.setBackground(new java.awt.Color(255, 255, 255));
        panelTopBar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 218, 208)));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTitle.setText("Data pasien");

        lblSubtitle.setForeground(new java.awt.Color(120, 120, 120));
        lblSubtitle.setText("Memuat...");

        btnTambahPasien.setBackground(new java.awt.Color(28, 112, 77));
        btnTambahPasien.setForeground(new java.awt.Color(255, 255, 255));
        btnTambahPasien.setText("+ Tambah Pasien");
        btnTambahPasien.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahPasienActionPerformed(evt);
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
                .addComponent(btnTambahPasien)
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
                    .addComponent(btnTambahPasien, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );

        panelContent.setBackground(new java.awt.Color(255, 255, 255));
        panelContent.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 218, 208)));

        panelContentHeader.setBackground(new java.awt.Color(255, 255, 255));

        lblContentTitle.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblContentTitle.setText("Data pasien");

        txtSearch.setToolTipText("Cari...");

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
            panelContentHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelContentHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblContentTitle)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelPagination.setBackground(new java.awt.Color(255, 255, 255));

        lblPageInfo.setForeground(new java.awt.Color(120, 120, 120));
        lblPageInfo.setText("Menampilkan 0 data");

        btnPrevPage.setText("< Sebelumnya");
        btnPrevPage.setEnabled(false);

        btnNextPage.setText("Selanjutnya >");
        btnNextPage.setEnabled(false);

        javax.swing.GroupLayout panelPaginationLayout = new javax.swing.GroupLayout(panelPagination);
        panelPagination.setLayout(panelPaginationLayout);
        panelPaginationLayout.setHorizontalGroup(
            panelPaginationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPaginationLayout.createSequentialGroup()
                .addComponent(lblPageInfo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 455, Short.MAX_VALUE)
                .addComponent(btnPrevPage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNextPage))
        );
        panelPaginationLayout.setVerticalGroup(
            panelPaginationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPaginationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblPageInfo)
                .addComponent(btnPrevPage)
                .addComponent(btnNextPage))
        );

        tblData.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "No. RM", "Nama", "NIK", "TGL LAHIR", "JK", "Gol.Darah", "No. HP", "Status", "Aksi"
            }
        ));
        jScrollPane2.setViewportView(tblData);

        javax.swing.GroupLayout panelContentLayout = new javax.swing.GroupLayout(panelContent);
        panelContent.setLayout(panelContentLayout);
        panelContentLayout.setHorizontalGroup(
            panelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelContentLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 835, Short.MAX_VALUE)
                    .addComponent(panelContentHeader, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelPagination, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        panelContentLayout.setVerticalGroup(
            panelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelContentLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(panelContentHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
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
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelTopBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelContent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        JOptionPane.showMessageDialog(this, "Fitur Export Excel akan segera tersedia.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_btnExportActionPerformed

    private void btnTambahPasienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahPasienActionPerformed
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof DashboardForm dashboardForm) {
            dashboardForm.showPasienCreatePage();
            return;
        }
        JOptionPane.showMessageDialog(this, "Dashboard utama tidak ditemukan.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_btnTambahPasienActionPerformed

    private void btnPrevPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevPageActionPerformed
        if (currentPage > 1) { currentPage--; loadData(); }
    }//GEN-LAST:event_btnPrevPageActionPerformed

    private void btnNextPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextPageActionPerformed
        if (currentPage < totalPages) { currentPage++; loadData(); }
    }//GEN-LAST:event_btnNextPageActionPerformed

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

    // ---- Cell Renderers ----

    private static class PaddedRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return this;
        }
    }

    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setHorizontalAlignment(SwingConstants.CENTER);
            String v = value != null ? value.toString().toLowerCase() : "";
            switch (v) {
                case "bpjs":
                case "asuransi": setForeground(COLOR_SUCCESS); break;
                case "umum": setForeground(COLOR_WARNING); break;
                default:        setForeground(COLOR_MUTED);
            }
            setFont(getFont().deriveFont(java.awt.Font.BOLD));
            return this;
        }
    }

    private class AksiCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
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
    private javax.swing.JButton btnNextPage;
    private javax.swing.JButton btnPrevPage;
    private javax.swing.JButton btnTambahPasien;
    private javax.swing.JScrollPane jScrollPane2;
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
