package com.release.klinikgaharumedika.view.panel;

import com.release.klinikgaharumedika.controller.PerawatController;
import com.release.klinikgaharumedika.model.PageResult;
import com.release.klinikgaharumedika.model.Perawat;
import com.release.klinikgaharumedika.view.DashboardForm;
import java.awt.Color;
import java.awt.Component;
import java.beans.Beans;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.plaf.basic.BasicButtonUI;

public class PerawatPanel extends javax.swing.JPanel {

    private static final Color COLOR_SUCCESS = new Color(28, 112, 77);
    private static final Color COLOR_WARNING = new Color(171, 116, 34);
    private static final Color COLOR_MUTED   = new Color(108, 117, 125);
    private static final Color COLOR_DANGER  = new Color(208, 70, 55);
    private static final int PAGE_SIZE = 10;
    private static final int COL_NAMA = 0;
    private static final int COL_NO_SIPP = 1;
    private static final int COL_SHIFT = 2;
    private static final int COL_POLI_TUGAS = 3;
    private static final int COL_NO_HP = 4;
    private static final int COL_STATUS = 5;
    private static final int COL_AKSI = 6;

    private final PerawatController controller;

    private int currentPage = 1;
    private int totalPages  = 1;
    private int totalItems  = 0;
    private String searchKeyword = "";
    private List<Perawat> currentData;

    public PerawatPanel() {
        initComponents();
        configureActionButtons();
        setupSearch();
        FormUiStyle.applyFormStyle(this);
        if (Beans.isDesignTime()) {
            controller = null;
            return;
        }
        controller = new PerawatController();
        configureTable();
        loadData();
    }

    private void configureActionButtons() {
        stylePrimaryButton(btnTambahPerawat);
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
        setColumnWidth(COL_NAMA, 170);
        setColumnWidth(COL_NO_SIPP, 100);
        setColumnWidth(COL_SHIFT, 100);
        setColumnWidth(COL_POLI_TUGAS, 120);
        setColumnWidth(COL_NO_HP, 110);
        setColumnWidth(COL_STATUS, 70);
        setColumnWidth(COL_AKSI, 110);
    }

    private void configureColumnRenderers() {
        DefaultTableCellRenderer padded = new PaddedRenderer();
        setColumnRenderer(COL_NAMA, padded);
        setColumnRenderer(COL_NO_SIPP, padded);
        setColumnRenderer(COL_SHIFT, padded);
        setColumnRenderer(COL_POLI_TUGAS, padded);
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
        txtSearch.putClientProperty("JTextField.placeholderText", "Contoh: Nur Aisyah / SIPP-PR-001 / Poli Umum");
    }

    private void performSearch() {
        searchKeyword = txtSearch.getText().trim();
        currentPage = 1;
        loadData();
    }

    private void loadData() {
        try {
            PageResult<Perawat> result = controller.loadPage(currentPage, PAGE_SIZE, searchKeyword);
            applyData(result);
        } catch (Exception ex) {
            lblSubtitle.setText("Gagal memuat data.");
            JOptionPane.showMessageDialog(this, "Gagal memuat data perawat.\n" + ex.getMessage(), "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyData(PageResult<Perawat> result) {
        currentData = result.getItems();
        totalItems  = result.getTotalItems();
        totalPages  = result.getTotalPages();

        DefaultTableModel model = (DefaultTableModel) tblData.getModel();
        model.setRowCount(0);
        for (Perawat p : currentData) {
            model.addRow(toTableRow(p));
        }

        lblSubtitle.setText(totalItems + " perawat terdaftar");
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

    private Object[] toTableRow(Perawat perawat) {
        return new Object[]{
            perawat.getNama(),
            perawat.getNoSipp(),
            perawat.getShiftDisplay(),
            perawat.getPoliTugas(),
            maskHp(perawat.getNoHp()),
            perawat.getStatus() != null ? capitalize(perawat.getStatus()) : "-",
            "Edit | Hapus"
        };
    }

    private void handleRowAction(int rowIndex, int clickX) {
        if (currentData == null || rowIndex < 0 || rowIndex >= currentData.size()) {
            return;
        }

        Perawat perawat = currentData.get(rowIndex);
        if (clickX < getAksiCellMidpoint()) {
            openEditPage(perawat);
        } else {
            deletePerawat(perawat);
        }
    }

    private int getAksiCellMidpoint() {
        int columnWidth = tblData.getColumnModel().getColumn(COL_AKSI).getWidth();
        return columnWidth / 2;
    }

    private void openEditPage(Perawat perawat) {
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof DashboardForm dashboardForm) {
            dashboardForm.showPerawatEditPage(perawat);
            return;
        }
        JOptionPane.showMessageDialog(this, "Dashboard utama tidak ditemukan.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deletePerawat(Perawat perawat) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Hapus data perawat " + perawat.getNama() + "?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean berhasil = controller.hapus(perawat.getId());
            if (berhasil) {
                if (currentData != null && currentData.size() == 1 && currentPage > 1) {
                    currentPage--;
                }
                loadData();
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Gagal menghapus perawat.",
                    "Gagal",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Gagal menghapus perawat.\n" + ex.getMessage(),
                    "Gagal",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelMain = new javax.swing.JPanel();
        panelTopBar = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblSubtitle = new javax.swing.JLabel();
        btnTambahPerawat = new javax.swing.JButton();
        panelContent = new javax.swing.JPanel();
        panelContentHeader = new javax.swing.JPanel();
        lblContentTitle = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        panelPagination = new javax.swing.JPanel();
        lblPageInfo = new javax.swing.JLabel();
        btnPrevPage = new javax.swing.JButton();
        btnNextPage = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblData = new javax.swing.JTable();

        panelMain.setBackground(new java.awt.Color(243, 240, 233));

        panelTopBar.setBackground(new java.awt.Color(255, 255, 255));
        panelTopBar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 218, 208)));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTitle.setText("Data Perawat");

        lblSubtitle.setForeground(new java.awt.Color(120, 120, 120));
        lblSubtitle.setText("Memuat...");

        btnTambahPerawat.setBackground(new java.awt.Color(28, 112, 77));
        btnTambahPerawat.setForeground(new java.awt.Color(255, 255, 255));
        btnTambahPerawat.setText("+ Tambah Perawat");
        btnTambahPerawat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahPerawatActionPerformed(evt);
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
                .addComponent(btnTambahPerawat)
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
                    .addComponent(btnTambahPerawat, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );

        panelContent.setBackground(new java.awt.Color(255, 255, 255));
        panelContent.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 218, 208)));

        panelContentHeader.setBackground(new java.awt.Color(255, 255, 255));

        lblContentTitle.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblContentTitle.setText("Data Perawat");

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

        tblData.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "NAMA", "NO. SIPP", "SHIFT", "POLI TUGAS", "NO. HP", "STATUS", "AKSI"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
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

    private void btnTambahPerawatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahPerawatActionPerformed
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof DashboardForm dashboardForm) {
            dashboardForm.showPerawatCreatePage();
            return;
        }
        JOptionPane.showMessageDialog(this, "Dashboard utama tidak ditemukan.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_btnTambahPerawatActionPerformed

    private void btnPrevPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevPageActionPerformed
        if (currentPage > 1) { currentPage--; loadData(); }
    }//GEN-LAST:event_btnPrevPageActionPerformed

    private void btnNextPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextPageActionPerformed
        if (currentPage < totalPages) { currentPage++; loadData(); }
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
                case "aktif":   setForeground(COLOR_SUCCESS); break;
                case "cuti":    setForeground(COLOR_WARNING); break;
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
    private javax.swing.JButton btnTambahPerawat;
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
