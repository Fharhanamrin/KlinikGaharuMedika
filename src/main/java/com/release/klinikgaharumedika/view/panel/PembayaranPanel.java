package com.release.klinikgaharumedika.view.panel;

import com.release.klinikgaharumedika.controller.PembayaranController;
import com.release.klinikgaharumedika.model.PageResult;
import com.release.klinikgaharumedika.model.Pembayaran;
import java.awt.Color;
import java.awt.Component;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class PembayaranPanel extends javax.swing.JPanel {

    private static final Color COLOR_SUCCESS = new Color(28, 112, 77);
    private static final Color COLOR_MUTED   = new Color(108, 117, 125);
    private static final Color COLOR_DANGER  = new Color(208, 70, 55);

    private static final int PAGE_SIZE = 10;
    private static final int COL_NO_INVOICE = 0;
    private static final int COL_PASIEN = 1;
    private static final int COL_TANGGAL_BAYAR = 2;
    private static final int COL_TOTAL_TAGIHAN = 3;
    private static final int COL_METODE_BAYAR = 4;
    private static final int COL_STATUS = 5;
    private static final int COL_AKSI = 6;

    private int currentPage = 1;
    private int totalPages  = 1;
    private int totalItems  = 0;

    private PembayaranController controller;
    private String keyword = "";
    private LocalDate filterDate = null;

    private static final NumberFormat CURRENCY = NumberFormat.getInstance(new Locale("id", "ID"));

    public PembayaranPanel(boolean listMode) { this(); }

    public PembayaranPanel() {
        initComponents();
        setupTable();
        setupSearch();
        setupSpinner();
        FormUiStyle.applyFormStyle(this);
        if (java.beans.Beans.isDesignTime()) {
            return;
        }
        controller = new PembayaranController();
        setupListeners();
        loadStats();
        loadPage();
    }

    private void setupSpinner() {
        FormUiStyle.dateSpinner(spnFilterTgl);
    }

    private void setupTable() {
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
        tblData.getTableHeader().setForeground(new Color(118, 118, 118));
        tblData.getTableHeader().setFont(new java.awt.Font("Segoe UI", 1, 10));
        tblData.setSelectionBackground(new Color(220, 235, 225));
        tblData.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblData.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private void configureColumnWidths() {
        setColumnWidth(COL_NO_INVOICE, 140);
        setColumnWidth(COL_PASIEN, 180);
        setColumnWidth(COL_TANGGAL_BAYAR, 105);
        setColumnWidth(COL_TOTAL_TAGIHAN, 140);
        setColumnWidth(COL_METODE_BAYAR, 95);
        setColumnWidth(COL_STATUS, 90);
        setColumnWidth(COL_AKSI, 95);
    }

    private void configureColumnRenderers() {
        setColumnRenderer(COL_NO_INVOICE, new PaddedRenderer(SwingConstants.LEFT));
        setColumnRenderer(COL_PASIEN, new PaddedRenderer(SwingConstants.LEFT));
        setColumnRenderer(COL_TANGGAL_BAYAR, new PaddedRenderer(SwingConstants.CENTER));
        setColumnRenderer(COL_TOTAL_TAGIHAN, new PaddedRenderer(SwingConstants.RIGHT));
        setColumnRenderer(COL_METODE_BAYAR, new PaddedRenderer(SwingConstants.CENTER));
        setColumnRenderer(COL_STATUS, new StatusPembayaranRenderer());
        setColumnRenderer(COL_AKSI, new AksiPembayaranRenderer());
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
        txtSearch.putClientProperty("JTextField.placeholderText", "Contoh: INV260401001 / Ahmad Fauzi");
    }

    private void setupListeners() {
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { onSearch(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { onSearch(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { onSearch(); }
        });

        btnPrevPage.addActionListener(e -> {
            if (currentPage > 1) { currentPage--; loadPage(); }
        });
        btnNextPage.addActionListener(e -> {
            if (currentPage < totalPages) { currentPage++; loadPage(); }
        });
        btnResetFilter.addActionListener(e -> {
            filterDate = null;
            currentPage = 1;
            loadStats();
            loadPage();
        });
        btnExport.addActionListener(e ->
            javax.swing.JOptionPane.showMessageDialog(this, "Fitur Export Excel segera hadir.", "Info",
                javax.swing.JOptionPane.INFORMATION_MESSAGE));

        spnFilterTgl.addChangeListener(e -> {
            Date d = (Date) spnFilterTgl.getValue();
            filterDate = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            currentPage = 1;
            loadStats();
            loadPage();
        });
    }

    private void onSearch() {
        keyword = txtSearch.getText().trim();
        currentPage = 1;
        loadPage();
    }

    private void loadStats() {
        final LocalDate date = filterDate;
        new SwingWorker<int[], Void>() {
            int transaksi; BigDecimal pendapatan; int belum;
            @Override
            protected int[] doInBackground() throws Exception {
                transaksi  = controller.countTransaksi(date);
                pendapatan = controller.sumPendapatan(date);
                belum      = controller.countBelumBayar(date);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    lblTransaksiVal.setText(String.valueOf(transaksi));
                    lblPendapatanVal.setText("Rp " + CURRENCY.format(pendapatan));
                    lblBelumBayarVal.setText(String.valueOf(belum));
                } catch (Exception ex) { /* silent */ }
            }
        }.execute();
    }

    private void loadPage() {
        final int page = currentPage;
        final String kw = keyword;
        final LocalDate date = filterDate;
        new SwingWorker<PageResult<Pembayaran>, Void>() {
            @Override
            protected PageResult<Pembayaran> doInBackground() throws Exception {
                return controller.loadPage(page, PAGE_SIZE, kw, date);
            }
            @Override
            protected void done() {
                try {
                    PageResult<Pembayaran> result = get();
                    populateTable(result);
                    totalItems = result.getTotalItems();
                    totalPages = result.getTotalPages();
                    updatePagination();
                    long aktif = result.getItems().stream()
                            .filter(p -> "lunas".equalsIgnoreCase(p.getStatus())).count();
                    lblSubtitle.setText(totalItems + " transaksi, " + aktif + " lunas");
                } catch (Exception ex) {
                    lblSubtitle.setText("Gagal memuat data");
                }
            }
        }.execute();
    }

    private void populateTable(PageResult<Pembayaran> result) {
        DefaultTableModel model = (DefaultTableModel) tblData.getModel();
        model.setRowCount(0);
        List<Pembayaran> list = result.getItems();
        for (Pembayaran p : list) {
            model.addRow(new Object[]{
                p.getNoInvoice(),
                p.getNamaPasien(),
                p.getTanggalBayar() != null ? p.getTanggalBayar().toString() : "-",
                "Rp " + CURRENCY.format(p.getTotalTagihan() != null ? p.getTotalTagihan() : BigDecimal.ZERO),
                p.getMetodeBayarDisplay(),
                p.getStatus(),
                "lunas".equalsIgnoreCase(p.getStatus()) ? "Struk" : "Bayar"
            });
        }
    }

    private void updatePagination() {
        int from = totalItems == 0 ? 0 : (currentPage - 1) * PAGE_SIZE + 1;
        int to   = Math.min(currentPage * PAGE_SIZE, totalItems);
        lblPageInfo.setText("Menampilkan " + from + "-" + to + " dari " + totalItems + " data");
        btnPrevPage.setEnabled(currentPage > 1);
        btnNextPage.setEnabled(currentPage < totalPages);
    }

    private static class StatusPembayaranRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            if (!isSelected) {
                String val = value == null ? "" : value.toString().toLowerCase();
                switch (val) {
                    case "lunas"   -> setForeground(COLOR_SUCCESS);
                    case "pending" -> setForeground(COLOR_MUTED);
                    default        -> setForeground(COLOR_DANGER);
                }
            }
            return this;
        }
    }

    private static class PaddedRenderer extends DefaultTableCellRenderer {

        private final int horizontalAlignment;

        private PaddedRenderer(int horizontalAlignment) {
            this.horizontalAlignment = horizontalAlignment;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(horizontalAlignment);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return this;
        }
    }

    private static class AksiPembayaranRenderer extends PaddedRenderer {

        private AksiPembayaranRenderer() {
            super(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                String action = value == null ? "" : value.toString().toLowerCase();
                setForeground("bayar".equals(action) ? COLOR_SUCCESS : COLOR_MUTED);
            }
            return component;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelMain = new javax.swing.JPanel();
        panelTopBar = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblSubtitle = new javax.swing.JLabel();
        lblFilterTgl = new javax.swing.JLabel();
        spnFilterTgl = new javax.swing.JSpinner();
        btnResetFilter = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        panelStats = new javax.swing.JPanel();
        cardTransaksi = new javax.swing.JPanel();
        lblTransaksiVal = new javax.swing.JLabel();
        lblTransaksiTitle = new javax.swing.JLabel();
        cardPendapatan = new javax.swing.JPanel();
        lblPendapatanVal = new javax.swing.JLabel();
        lblPendapatanTitle = new javax.swing.JLabel();
        cardBelumBayar = new javax.swing.JPanel();
        lblBelumBayarVal = new javax.swing.JLabel();
        lblBelumBayarTitle = new javax.swing.JLabel();
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
        lblTitle.setText("Data Pembayaran");

        lblSubtitle.setForeground(new java.awt.Color(120, 120, 120));
        lblSubtitle.setText("Memuat...");

        lblFilterTgl.setText("Tanggal:");

        btnResetFilter.setText("Semua");

        btnExport.setText("Export Excel");

        javax.swing.GroupLayout panelTopBarLayout = new javax.swing.GroupLayout(panelTopBar);
        panelTopBar.setLayout(panelTopBarLayout);
        panelTopBarLayout.setHorizontalGroup(
            panelTopBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTopBarLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(panelTopBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitle)
                    .addComponent(lblSubtitle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 100, Short.MAX_VALUE)
                .addComponent(lblFilterTgl)
                .addGap(6, 6, 6)
                .addComponent(spnFilterTgl, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(btnResetFilter)
                .addGap(8, 8, 8)
                .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addGroup(panelTopBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblFilterTgl)
                        .addComponent(spnFilterTgl, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnResetFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(12, 12, 12))
        );

        panelStats.setBackground(new java.awt.Color(243, 240, 233));

        cardTransaksi.setBackground(new java.awt.Color(255, 255, 255));
        cardTransaksi.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 218, 208)));

        lblTransaksiVal.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTransaksiVal.setText("0");

        lblTransaksiTitle.setForeground(new java.awt.Color(120, 120, 120));
        lblTransaksiTitle.setText("Total Transaksi");

        javax.swing.GroupLayout cardTransaksiLayout = new javax.swing.GroupLayout(cardTransaksi);
        cardTransaksi.setLayout(cardTransaksiLayout);
        cardTransaksiLayout.setHorizontalGroup(
            cardTransaksiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardTransaksiLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(cardTransaksiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTransaksiVal)
                    .addComponent(lblTransaksiTitle))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        cardTransaksiLayout.setVerticalGroup(
            cardTransaksiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardTransaksiLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblTransaksiVal)
                .addGap(4, 4, 4)
                .addComponent(lblTransaksiTitle)
                .addGap(14, 14, 14))
        );

        cardPendapatan.setBackground(new java.awt.Color(255, 255, 255));
        cardPendapatan.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 218, 208)));

        lblPendapatanVal.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblPendapatanVal.setForeground(new java.awt.Color(28, 112, 77));
        lblPendapatanVal.setText("Rp 0");

        lblPendapatanTitle.setForeground(new java.awt.Color(120, 120, 120));
        lblPendapatanTitle.setText("Total Pendapatan");

        javax.swing.GroupLayout cardPendapatanLayout = new javax.swing.GroupLayout(cardPendapatan);
        cardPendapatan.setLayout(cardPendapatanLayout);
        cardPendapatanLayout.setHorizontalGroup(
            cardPendapatanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPendapatanLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(cardPendapatanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPendapatanVal)
                    .addComponent(lblPendapatanTitle))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        cardPendapatanLayout.setVerticalGroup(
            cardPendapatanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPendapatanLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblPendapatanVal)
                .addGap(4, 4, 4)
                .addComponent(lblPendapatanTitle)
                .addGap(14, 14, 14))
        );

        cardBelumBayar.setBackground(new java.awt.Color(255, 255, 255));
        cardBelumBayar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 218, 208)));

        lblBelumBayarVal.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblBelumBayarVal.setForeground(new java.awt.Color(208, 70, 55));
        lblBelumBayarVal.setText("0");

        lblBelumBayarTitle.setForeground(new java.awt.Color(120, 120, 120));
        lblBelumBayarTitle.setText("Belum Dibayar");

        javax.swing.GroupLayout cardBelumBayarLayout = new javax.swing.GroupLayout(cardBelumBayar);
        cardBelumBayar.setLayout(cardBelumBayarLayout);
        cardBelumBayarLayout.setHorizontalGroup(
            cardBelumBayarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardBelumBayarLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(cardBelumBayarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBelumBayarVal)
                    .addComponent(lblBelumBayarTitle))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        cardBelumBayarLayout.setVerticalGroup(
            cardBelumBayarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardBelumBayarLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblBelumBayarVal)
                .addGap(4, 4, 4)
                .addComponent(lblBelumBayarTitle)
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout panelStatsLayout = new javax.swing.GroupLayout(panelStats);
        panelStats.setLayout(panelStatsLayout);
        panelStatsLayout.setHorizontalGroup(
            panelStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelStatsLayout.createSequentialGroup()
                .addComponent(cardTransaksi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addComponent(cardPendapatan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addComponent(cardBelumBayar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelStatsLayout.setVerticalGroup(
            panelStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(cardTransaksi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(cardPendapatan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(cardBelumBayar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        panelContent.setBackground(new java.awt.Color(255, 255, 255));
        panelContent.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 218, 208)));

        panelContentHeader.setBackground(new java.awt.Color(255, 255, 255));

        lblContentTitle.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblContentTitle.setText("Riwayat Pembayaran");

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
            panelContentHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(lblContentTitle)
            .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        tblData.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "NO. INVOICE", "PASIEN", "TGL. BAYAR", "TOTAL TAGIHAN", "METODE", "STATUS", "AKSI"
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
        jScrollPane1.setViewportView(tblData);

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
                .addComponent(jScrollPane1)
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
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelStats, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelContent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(panelTopBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(panelStats, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnNextPage;
    private javax.swing.JButton btnPrevPage;
    private javax.swing.JButton btnResetFilter;
    private javax.swing.JPanel cardBelumBayar;
    private javax.swing.JPanel cardPendapatan;
    private javax.swing.JPanel cardTransaksi;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblBelumBayarTitle;
    private javax.swing.JLabel lblBelumBayarVal;
    private javax.swing.JLabel lblContentTitle;
    private javax.swing.JLabel lblFilterTgl;
    private javax.swing.JLabel lblPageInfo;
    private javax.swing.JLabel lblPendapatanTitle;
    private javax.swing.JLabel lblPendapatanVal;
    private javax.swing.JLabel lblSubtitle;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTransaksiTitle;
    private javax.swing.JLabel lblTransaksiVal;
    private javax.swing.JPanel panelContent;
    private javax.swing.JPanel panelContentHeader;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelPagination;
    private javax.swing.JPanel panelStats;
    private javax.swing.JPanel panelTopBar;
    private javax.swing.JSpinner spnFilterTgl;
    private javax.swing.JTable tblData;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
