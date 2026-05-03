package com.release.klinikgaharumedika.view.panel;

import com.release.klinikgaharumedika.controller.KunjunganController;
import com.release.klinikgaharumedika.controller.PembayaranController;
import com.release.klinikgaharumedika.model.Kunjungan;
import com.release.klinikgaharumedika.model.Obat;
import com.release.klinikgaharumedika.model.Pembayaran;
import com.release.klinikgaharumedika.model.ResepObatItem;
import com.release.klinikgaharumedika.model.User;
import com.release.klinikgaharumedika.state.SessionManager;
import com.release.klinikgaharumedika.view.DashboardForm;
import java.awt.Color;
import java.awt.Component;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class PembayaranFormPanel extends javax.swing.JPanel {

    private static final NumberFormat CURRENCY = NumberFormat.getInstance(new Locale("id", "ID"));
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID"));
    private static final Color SUCCESS = new Color(28, 112, 77);
    private static final Color MUTED = new Color(120, 120, 120);

    private final int pembayaranId;
    private Pembayaran pembayaran;
    private PembayaranController pembayaranController;
    private KunjunganController kunjunganController;

    public PembayaranFormPanel() {
        this(null);
    }

    public PembayaranFormPanel(Pembayaran pembayaran) {
        this.pembayaran = pembayaran;
        this.pembayaranId = pembayaran != null ? pembayaran.getId() : 0;
        initComponents();
        configureForm();
        if (java.beans.Beans.isDesignTime()) {
            installDesignPreview();
            return;
        }
        pembayaranController = new PembayaranController();
        kunjunganController = new KunjunganController();
        loadPaymentDetail();
    }

    private void configureForm() {
        ButtonGroup metodeGroup = new ButtonGroup();
        metodeGroup.add(rdoTunai);
        metodeGroup.add(rdoTransfer);
        metodeGroup.add(rdoBpjs);
        rdoTunai.setSelected(true);

        tblTagihan.setDefaultEditor(Object.class, null);
        tblTagihan.setRowHeight(34);
        tblTagihan.setShowHorizontalLines(true);
        tblTagihan.setShowVerticalLines(false);
        tblTagihan.setGridColor(new Color(230, 226, 216));
        tblTagihan.getTableHeader().setReorderingAllowed(false);
        tblTagihan.getTableHeader().setBackground(new Color(243, 240, 233));
        tblTagihan.getTableHeader().setForeground(MUTED);
        tblTagihan.getTableHeader().setFont(new java.awt.Font("Segoe UI", 1, 10));
        tblTagihan.setSelectionBackground(new Color(220, 235, 225));
        tblTagihan.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        setColumnRenderer(0, new PaddedRenderer(javax.swing.SwingConstants.LEFT));
        setColumnRenderer(1, new PaddedRenderer(javax.swing.SwingConstants.CENTER));
        setColumnRenderer(2, new PaddedRenderer(javax.swing.SwingConstants.RIGHT));
        setColumnRenderer(3, new PaddedRenderer(javax.swing.SwingConstants.RIGHT));

        txtUangDiterima.putClientProperty("JTextField.placeholderText", "Contoh: 150000");
        txtKeterangan.putClientProperty("JTextField.placeholderText", "Opsional");
        FormUiStyle.applyFormStyle(this);
        FormUiStyle.stylePrimaryButton(btnKonfirmasi);
        FormUiStyle.styleGhostButton(btnRiwayat);
        FormUiStyle.styleGhostButton(btnKembali);
        FormUiStyle.styleGhostButton(btnCetakStruk);

        rdoTunai.addActionListener(e -> updatePaymentMethodState());
        rdoTransfer.addActionListener(e -> updatePaymentMethodState());
        rdoBpjs.addActionListener(e -> updatePaymentMethodState());
        txtUangDiterima.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateSummary(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateSummary(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateSummary(); }
        });
        btnKonfirmasi.addActionListener(e -> confirmPayment());
        btnKembali.addActionListener(e -> openPaymentList());
        btnRiwayat.addActionListener(e -> openPaymentList());
        btnCetakStruk.addActionListener(e -> JOptionPane.showMessageDialog(this, "Fitur cetak struk segera hadir.", "Info", JOptionPane.INFORMATION_MESSAGE));
        updatePaymentMethodState();
    }

    private void installDesignPreview() {
        lblInvoice.setText("Invoice #INV260426001");
        lblPatientTitle.setText("Detail tagihan - Ahmad Fauzi");
        lblDokterValue.setText("dr. Andi Saputra");
        lblDiagnosaValue.setText("Influenza (J11)");
        lblTanggalValue.setText("26 Apr 2026");
        lblNoAntrianValue.setText("A001");
        lblJenisPasienValue.setText("Umum");
        pembayaran = new Pembayaran();
        pembayaran.setTotalTagihan(new BigDecimal("128000"));
        pembayaran.setBiayaKonsultasi(new BigDecimal("90000"));
        pembayaran.setBiayaObat(new BigDecimal("38000"));
        populateTable(List.of());
        txtUangDiterima.setText("150000");
        updateSummary();
    }

    private void loadPaymentDetail() {
        setLoadingState(true);
        new SwingWorker<PaymentDetail, Void>() {
            @Override
            protected PaymentDetail doInBackground() throws Exception {
                Pembayaran loadedPayment = pembayaranId > 0 ? pembayaranController.findById(pembayaranId) : pembayaran;
                if (loadedPayment == null) {
                    throw new IllegalStateException("Data pembayaran tidak ditemukan.");
                }
                Kunjungan kunjungan = kunjunganController.findById(loadedPayment.getKunjunganId());
                List<ResepObatItem> resep = kunjunganController.findResepByKunjunganId(loadedPayment.getKunjunganId());
                Map<Integer, Obat> obatById = new HashMap<>();
                for (Obat obat : kunjunganController.findAllObat()) {
                    obatById.put(obat.getId(), obat);
                }
                return new PaymentDetail(loadedPayment, kunjungan, resep, obatById);
            }

            @Override
            protected void done() {
                try {
                    applyPaymentDetail(get());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PembayaranFormPanel.this,
                            "Gagal memuat form pembayaran.\n" + ex.getMessage(),
                            "Gagal",
                            JOptionPane.ERROR_MESSAGE);
                    openPaymentList();
                } finally {
                    setLoadingState(false);
                }
            }
        }.execute();
    }

    private void applyPaymentDetail(PaymentDetail detail) {
        pembayaran = detail.pembayaran();
        Kunjungan kunjungan = detail.kunjungan();
        lblInvoice.setText("Invoice #" + safe(pembayaran.getNoInvoice()));
        lblPatientTitle.setText("Detail tagihan - " + safe(pembayaran.getNamaPasien()));
        lblDokterValue.setText(kunjungan != null ? safe(kunjungan.getNamaDokter()) : "-");
        lblDiagnosaValue.setText(kunjungan != null ? safe(kunjungan.getDiagnosaDisplay()) : "-");
        lblTanggalValue.setText(kunjungan != null && kunjungan.getTanggalKunjungan() != null
                ? DATE_FORMAT.format(kunjungan.getTanggalKunjungan()) : "-");
        lblNoAntrianValue.setText(safe(pembayaran.getNoAntrian()));
        lblJenisPasienValue.setText(kunjungan != null ? capitalize(safe(kunjungan.getJenisKunjungan())) : "-");
        populateTable(detail.resep(), detail.obatById());
        applyExistingPaymentState();
        updateSummary();
    }

    private void populateTable(List<ResepObatItem> resepItems) {
        populateTable(resepItems, Map.of());
    }

    private void populateTable(List<ResepObatItem> resepItems, Map<Integer, Obat> obatById) {
        DefaultTableModel model = (DefaultTableModel) tblTagihan.getModel();
        model.setRowCount(0);
        BigDecimal konsultasi = paymentValue(Pembayaran::getBiayaKonsultasi);
        model.addRow(new Object[] {"Biaya konsultasi", "1", formatCurrency(konsultasi), formatCurrency(konsultasi)});
        if (resepItems != null) {
            for (ResepObatItem item : resepItems) {
                Obat obat = obatById.get(item.getObatId());
                BigDecimal harga = obat != null && obat.getHargaJual() != null ? obat.getHargaJual() : BigDecimal.ZERO;
                BigDecimal subtotal = harga.multiply(BigDecimal.valueOf(item.getJumlah()));
                String satuan = item.getSatuan() != null ? item.getSatuan() : (obat != null ? obat.getSatuan() : "");
                model.addRow(new Object[] {
                    safe(item.getNamaObat()),
                    item.getJumlah() + (satuan == null || satuan.isBlank() ? "" : " " + satuan),
                    formatCurrency(harga),
                    formatCurrency(subtotal)
                });
            }
        }
    }

    private void applyExistingPaymentState() {
        String metode = pembayaran.getMetodeBayar();
        if ("transfer".equalsIgnoreCase(metode)) {
            rdoTransfer.setSelected(true);
        } else if ("bpjs".equalsIgnoreCase(metode)) {
            rdoBpjs.setSelected(true);
        } else {
            rdoTunai.setSelected(true);
        }
        if (pembayaran.getUangDiterima() != null) {
            txtUangDiterima.setText(pembayaran.getUangDiterima().toPlainString());
        } else if ("transfer".equalsIgnoreCase(metode)) {
            txtUangDiterima.setText(totalTagihan().toPlainString());
        }
        boolean lunas = "lunas".equalsIgnoreCase(pembayaran.getStatus());
        btnKonfirmasi.setEnabled(!lunas);
        btnCetakStruk.setEnabled(lunas);
        updatePaymentMethodState();
    }

    private void updatePaymentMethodState() {
        BigDecimal total = totalTagihan();
        if (rdoTransfer.isSelected()) {
            txtUangDiterima.setText(total.toPlainString());
            txtUangDiterima.setEditable(false);
        } else if (rdoBpjs.isSelected()) {
            txtUangDiterima.setText("");
            txtUangDiterima.setEditable(false);
        } else {
            txtUangDiterima.setEditable(true);
        }
        FormUiStyle.styleTextField(txtUangDiterima);
        updateSummary();
    }

    private void updateSummary() {
        BigDecimal total = totalTagihan();
        BigDecimal diterima = currentUangDiterima(false);
        BigDecimal kembalian = diterima != null ? diterima.subtract(total) : BigDecimal.ZERO;
        if (kembalian.compareTo(BigDecimal.ZERO) < 0) {
            kembalian = BigDecimal.ZERO;
        }
        lblTotalTagihanValue.setText(formatCurrency(total));
        lblTotalBottomValue.setText(formatCurrency(total));
        lblKembalianValue.setText(formatCurrency(kembalian));
    }

    private void confirmPayment() {
        BigDecimal uangDiterima = currentUangDiterima(true);
        if (uangDiterima == null && rdoTunai.isSelected()) {
            return;
        }
        if (rdoTunai.isSelected() && uangDiterima.compareTo(totalTagihan()) < 0) {
            JOptionPane.showMessageDialog(this, "Uang diterima tidak boleh kurang dari total tagihan.", "Validasi Pembayaran", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean paid = pembayaranController.bayar(
                    pembayaran.getId(),
                    selectedMethod(),
                    rdoBpjs.isSelected() ? null : uangDiterima,
                    currentUserId()
            );
            if (!paid) {
                JOptionPane.showMessageDialog(this, "Gagal mengonfirmasi pembayaran.", "Gagal", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "Pembayaran berhasil dikonfirmasi.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            pembayaran = pembayaranController.findById(pembayaran.getId());
            applyExistingPaymentState();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengonfirmasi pembayaran.\n" + ex.getMessage(), "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BigDecimal currentUangDiterima(boolean showValidation) {
        if (rdoBpjs.isSelected()) {
            return null;
        }
        if (rdoTransfer.isSelected()) {
            return totalTagihan();
        }
        String raw = txtUangDiterima.getText().replace(".", "").replace(",", "").trim();
        if (raw.isEmpty()) {
            if (showValidation) {
                JOptionPane.showMessageDialog(this, "Uang diterima wajib diisi untuk pembayaran tunai.", "Validasi Pembayaran", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException ex) {
            if (showValidation) {
                JOptionPane.showMessageDialog(this, "Uang diterima harus berupa angka.", "Validasi Pembayaran", JOptionPane.WARNING_MESSAGE);
            }
            return null;
        }
    }

    private String selectedMethod() {
        if (rdoTransfer.isSelected()) {
            return "transfer";
        }
        if (rdoBpjs.isSelected()) {
            return "bpjs";
        }
        return "tunai";
    }

    private BigDecimal totalTagihan() {
        return paymentValue(Pembayaran::getTotalTagihan);
    }

    private BigDecimal paymentValue(java.util.function.Function<Pembayaran, BigDecimal> getter) {
        if (pembayaran == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal value = getter.apply(pembayaran);
        return value != null ? value : BigDecimal.ZERO;
    }

    private Integer currentUserId() {
        User user = SessionManager.getInstance().getCurrentUser();
        return user != null ? user.getId() : null;
    }

    private void setLoadingState(boolean loading) {
        boolean lunas = pembayaran != null && "lunas".equalsIgnoreCase(pembayaran.getStatus());
        btnKonfirmasi.setEnabled(!loading && !lunas);
        btnCetakStruk.setEnabled(!loading && lunas);
        btnKembali.setEnabled(!loading);
        btnRiwayat.setEnabled(!loading);
        if (loading) {
            lblInvoice.setText("Memuat invoice...");
        }
    }

    private void setColumnRenderer(int columnIndex, DefaultTableCellRenderer renderer) {
        tblTagihan.getColumnModel().getColumn(columnIndex).setCellRenderer(renderer);
    }

    private void openPaymentList() {
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof DashboardForm dashboardForm) {
            dashboardForm.showPembayaranPage();
        }
    }

    private String formatCurrency(BigDecimal value) {
        return "Rp " + CURRENCY.format(value != null ? value : BigDecimal.ZERO);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank() || "-".equals(value)) {
            return "-";
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    private static class PaddedRenderer extends DefaultTableCellRenderer {
        private final int alignment;

        private PaddedRenderer(int alignment) {
            this.alignment = alignment;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(alignment);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return this;
        }
    }

    private record PaymentDetail(Pembayaran pembayaran, Kunjungan kunjungan, List<ResepObatItem> resep, Map<Integer, Obat> obatById) {
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelRoot = new javax.swing.JPanel();
        panelHeader = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblInvoice = new javax.swing.JLabel();
        btnRiwayat = new javax.swing.JButton();
        panelBody = new javax.swing.JPanel();
        panelDetail = new javax.swing.JPanel();
        lblPatientTitle = new javax.swing.JLabel();
        separatorDetail = new javax.swing.JSeparator();
        panelInfoGrid = new javax.swing.JPanel();
        lblDokter = new javax.swing.JLabel();
        lblDiagnosa = new javax.swing.JLabel();
        lblTanggal = new javax.swing.JLabel();
        lblJenisPasien = new javax.swing.JLabel();
        lblNoAntrian = new javax.swing.JLabel();
        lblDokterValue = new javax.swing.JLabel();
        lblDiagnosaValue = new javax.swing.JLabel();
        lblTanggalValue = new javax.swing.JLabel();
        lblJenisPasienValue = new javax.swing.JLabel();
        lblNoAntrianValue = new javax.swing.JLabel();
        scrollTagihan = new javax.swing.JScrollPane();
        tblTagihan = new javax.swing.JTable();
        panelTotalBottom = new javax.swing.JPanel();
        lblTotalBottom = new javax.swing.JLabel();
        lblTotalBottomValue = new javax.swing.JLabel();
        panelProcess = new javax.swing.JPanel();
        lblProcessTitle = new javax.swing.JLabel();
        separatorProcess = new javax.swing.JSeparator();
        lblMetode = new javax.swing.JLabel();
        panelMethodGrid = new javax.swing.JPanel();
        rdoTunai = new javax.swing.JRadioButton();
        rdoTransfer = new javax.swing.JRadioButton();
        rdoBpjs = new javax.swing.JRadioButton();
        lblUangDiterima = new javax.swing.JLabel();
        txtUangDiterima = new javax.swing.JTextField();
        panelSummary = new javax.swing.JPanel();
        lblTotalTagihan = new javax.swing.JLabel();
        lblTotalTagihanValue = new javax.swing.JLabel();
        lblKembalian = new javax.swing.JLabel();
        lblKembalianValue = new javax.swing.JLabel();
        lblKeterangan = new javax.swing.JLabel();
        txtKeterangan = new javax.swing.JTextField();
        btnKonfirmasi = new javax.swing.JButton();
        btnCetakStruk = new javax.swing.JButton();
        btnKembali = new javax.swing.JButton();

        setBackground(new java.awt.Color(243, 240, 233));
        setPreferredSize(new java.awt.Dimension(1100, 640));

        panelRoot.setBackground(new java.awt.Color(243, 240, 233));
        panelRoot.setPreferredSize(new java.awt.Dimension(1100, 640));

        panelHeader.setBackground(new java.awt.Color(255, 255, 255));
        panelHeader.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 218, 208)));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitle.setText("Form Pembayaran");

        lblInvoice.setForeground(new java.awt.Color(120, 120, 120));
        lblInvoice.setText("Invoice");

        btnRiwayat.setText("Riwayat");

        javax.swing.GroupLayout panelHeaderLayout = new javax.swing.GroupLayout(panelHeader);
        panelHeader.setLayout(panelHeaderLayout);
        panelHeaderLayout.setHorizontalGroup(
            panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeaderLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitle)
                    .addComponent(lblInvoice))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 720, Short.MAX_VALUE)
                .addComponent(btnRiwayat, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );
        panelHeaderLayout.setVerticalGroup(
            panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeaderLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addGroup(panelHeaderLayout.createSequentialGroup()
                        .addComponent(lblTitle)
                        .addGap(2, 2, 2)
                        .addComponent(lblInvoice))
                    .addComponent(btnRiwayat, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18))
        );

        panelBody.setBackground(new java.awt.Color(243, 240, 233));

        panelDetail.setBackground(new java.awt.Color(255, 255, 255));
        panelDetail.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 218, 208)));

        lblPatientTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblPatientTitle.setText("Detail tagihan");

        panelInfoGrid.setBackground(new java.awt.Color(255, 255, 255));

        lblDokter.setText("Dokter");

        lblDiagnosa.setText("Diagnosa");

        lblTanggal.setText("Tgl periksa");

        lblJenisPasien.setText("Jenis kunjungan");

        lblNoAntrian.setText("No. antrian");

        lblDokterValue.setText("-");

        lblDiagnosaValue.setText("-");

        lblTanggalValue.setText("-");

        lblJenisPasienValue.setText("-");

        lblNoAntrianValue.setText("-");

        javax.swing.GroupLayout panelInfoGridLayout = new javax.swing.GroupLayout(panelInfoGrid);
        panelInfoGrid.setLayout(panelInfoGridLayout);
        panelInfoGridLayout.setHorizontalGroup(
            panelInfoGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInfoGridLayout.createSequentialGroup()
                .addGroup(panelInfoGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDokter)
                    .addComponent(lblDokterValue))
                .addGap(18, 18, 18)
                .addGroup(panelInfoGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDiagnosa)
                    .addComponent(lblDiagnosaValue))
                .addGap(18, 18, 18)
                .addGroup(panelInfoGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTanggal)
                    .addComponent(lblTanggalValue))
                .addGap(18, 18, 18)
                .addGroup(panelInfoGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblJenisPasien)
                    .addComponent(lblJenisPasienValue))
                .addGap(18, 18, 18)
                .addGroup(panelInfoGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNoAntrian)
                    .addComponent(lblNoAntrianValue))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelInfoGridLayout.setVerticalGroup(
            panelInfoGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInfoGridLayout.createSequentialGroup()
                .addGroup(panelInfoGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDokter)
                    .addComponent(lblDiagnosa)
                    .addComponent(lblTanggal)
                    .addComponent(lblJenisPasien)
                    .addComponent(lblNoAntrian))
                .addGap(4, 4, 4)
                .addGroup(panelInfoGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDokterValue)
                    .addComponent(lblDiagnosaValue)
                    .addComponent(lblTanggalValue)
                    .addComponent(lblJenisPasienValue)
                    .addComponent(lblNoAntrianValue)))
        );

        tblTagihan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ITEM", "QTY", "HARGA SATUAN", "SUBTOTAL"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrollTagihan.setViewportView(tblTagihan);

        panelTotalBottom.setBackground(new java.awt.Color(255, 255, 255));

        lblTotalBottom.setText("Total tagihan");

        lblTotalBottomValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotalBottomValue.setText("Rp 0");

        javax.swing.GroupLayout panelTotalBottomLayout = new javax.swing.GroupLayout(panelTotalBottom);
        panelTotalBottom.setLayout(panelTotalBottomLayout);
        panelTotalBottomLayout.setHorizontalGroup(
            panelTotalBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTotalBottomLayout.createSequentialGroup()
                .addComponent(lblTotalBottom)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblTotalBottomValue, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelTotalBottomLayout.setVerticalGroup(
            panelTotalBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(lblTotalBottom)
            .addComponent(lblTotalBottomValue)
        );

        javax.swing.GroupLayout panelDetailLayout = new javax.swing.GroupLayout(panelDetail);
        panelDetail.setLayout(panelDetailLayout);
        panelDetailLayout.setHorizontalGroup(
            panelDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDetailLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(separatorDetail)
                    .addComponent(panelInfoGrid, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrollTagihan, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                    .addComponent(panelTotalBottom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblPatientTitle))
                .addGap(20, 20, 20))
        );
        panelDetailLayout.setVerticalGroup(
            panelDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDetailLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(lblPatientTitle)
                .addGap(14, 14, 14)
                .addComponent(separatorDetail, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(panelInfoGrid, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(scrollTagihan, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(panelTotalBottom, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        panelProcess.setBackground(new java.awt.Color(255, 255, 255));
        panelProcess.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 218, 208)));

        lblProcessTitle.setText("Proses pembayaran");

        lblMetode.setText("Metode pembayaran *");

        panelMethodGrid.setBackground(new java.awt.Color(255, 255, 255));

        rdoTunai.setText("Tunai");

        rdoTransfer.setText("Transfer");

        rdoBpjs.setText("BPJS");

        javax.swing.GroupLayout panelMethodGridLayout = new javax.swing.GroupLayout(panelMethodGrid);
        panelMethodGrid.setLayout(panelMethodGridLayout);
        panelMethodGridLayout.setHorizontalGroup(
            panelMethodGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMethodGridLayout.createSequentialGroup()
                .addGroup(panelMethodGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rdoTunai, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                    .addComponent(rdoBpjs, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
                .addGap(14, 14, 14)
                .addComponent(rdoTransfer, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
        );
        panelMethodGridLayout.setVerticalGroup(
            panelMethodGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMethodGridLayout.createSequentialGroup()
                .addGroup(panelMethodGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdoTunai)
                    .addComponent(rdoTransfer))
                .addGap(8, 8, 8)
                .addComponent(rdoBpjs))
        );

        lblUangDiterima.setText("Uang diterima");

        panelSummary.setBackground(new java.awt.Color(243, 240, 233));

        lblTotalTagihan.setText("Total tagihan");

        lblTotalTagihanValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotalTagihanValue.setText("Rp 0");

        lblKembalian.setText("Kembalian");

        lblKembalianValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblKembalianValue.setText("Rp 0");

        javax.swing.GroupLayout panelSummaryLayout = new javax.swing.GroupLayout(panelSummary);
        panelSummary.setLayout(panelSummaryLayout);
        panelSummaryLayout.setHorizontalGroup(
            panelSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSummaryLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTotalTagihan)
                    .addComponent(lblKembalian))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTotalTagihanValue, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblKembalianValue, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16))
        );
        panelSummaryLayout.setVerticalGroup(
            panelSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSummaryLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(panelSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotalTagihan)
                    .addComponent(lblTotalTagihanValue))
                .addGap(6, 6, 6)
                .addGroup(panelSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblKembalian)
                    .addComponent(lblKembalianValue))
                .addGap(12, 12, 12))
        );

        lblKeterangan.setText("Keterangan");

        btnKonfirmasi.setText("Konfirmasi Lunas");

        btnCetakStruk.setText("Cetak Struk");
        btnCetakStruk.setEnabled(false);

        btnKembali.setText("< Kembali");

        javax.swing.GroupLayout panelProcessLayout = new javax.swing.GroupLayout(panelProcess);
        panelProcess.setLayout(panelProcessLayout);
        panelProcessLayout.setHorizontalGroup(
            panelProcessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProcessLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(panelProcessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(separatorProcess)
                    .addComponent(panelMethodGrid, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtUangDiterima)
                    .addComponent(panelSummary, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtKeterangan)
                    .addComponent(btnKonfirmasi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCetakStruk, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnKembali, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblProcessTitle)
                    .addComponent(lblMetode)
                    .addComponent(lblUangDiterima)
                    .addComponent(lblKeterangan))
                .addGap(18, 18, 18))
        );
        panelProcessLayout.setVerticalGroup(
            panelProcessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProcessLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(lblProcessTitle)
                .addGap(14, 14, 14)
                .addComponent(separatorProcess, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblMetode)
                .addGap(6, 6, 6)
                .addComponent(panelMethodGrid, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(lblUangDiterima)
                .addGap(6, 6, 6)
                .addComponent(txtUangDiterima, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(panelSummary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblKeterangan)
                .addGap(6, 6, 6)
                .addComponent(txtKeterangan, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnKonfirmasi, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnCetakStruk, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnKembali, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout panelBodyLayout = new javax.swing.GroupLayout(panelBody);
        panelBody.setLayout(panelBodyLayout);
        panelBodyLayout.setHorizontalGroup(
            panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBodyLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(panelDetail, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(16, 16, 16)
                .addComponent(panelProcess, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );
        panelBodyLayout.setVerticalGroup(
            panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBodyLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(panelBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelDetail, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelProcess, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout panelRootLayout = new javax.swing.GroupLayout(panelRoot);
        panelRoot.setLayout(panelRootLayout);
        panelRootLayout.setHorizontalGroup(
            panelRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelBody, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelRootLayout.setVerticalGroup(
            panelRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRootLayout.createSequentialGroup()
                .addComponent(panelHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(panelBody, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelRoot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelRoot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCetakStruk;
    private javax.swing.JButton btnKembali;
    private javax.swing.JButton btnKonfirmasi;
    private javax.swing.JButton btnRiwayat;
    private javax.swing.JLabel lblDiagnosa;
    private javax.swing.JLabel lblDiagnosaValue;
    private javax.swing.JLabel lblDokter;
    private javax.swing.JLabel lblDokterValue;
    private javax.swing.JLabel lblInvoice;
    private javax.swing.JLabel lblJenisPasien;
    private javax.swing.JLabel lblJenisPasienValue;
    private javax.swing.JLabel lblKembalian;
    private javax.swing.JLabel lblKembalianValue;
    private javax.swing.JLabel lblKeterangan;
    private javax.swing.JLabel lblMetode;
    private javax.swing.JLabel lblNoAntrian;
    private javax.swing.JLabel lblNoAntrianValue;
    private javax.swing.JLabel lblPatientTitle;
    private javax.swing.JLabel lblProcessTitle;
    private javax.swing.JLabel lblTanggal;
    private javax.swing.JLabel lblTanggalValue;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTotalBottom;
    private javax.swing.JLabel lblTotalBottomValue;
    private javax.swing.JLabel lblTotalTagihan;
    private javax.swing.JLabel lblTotalTagihanValue;
    private javax.swing.JLabel lblUangDiterima;
    private javax.swing.JPanel panelBody;
    private javax.swing.JPanel panelDetail;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelInfoGrid;
    private javax.swing.JPanel panelMethodGrid;
    private javax.swing.JPanel panelProcess;
    private javax.swing.JPanel panelRoot;
    private javax.swing.JPanel panelSummary;
    private javax.swing.JPanel panelTotalBottom;
    private javax.swing.JRadioButton rdoBpjs;
    private javax.swing.JRadioButton rdoTransfer;
    private javax.swing.JRadioButton rdoTunai;
    private javax.swing.JScrollPane scrollTagihan;
    private javax.swing.JSeparator separatorDetail;
    private javax.swing.JSeparator separatorProcess;
    private javax.swing.JTable tblTagihan;
    private javax.swing.JTextField txtKeterangan;
    private javax.swing.JTextField txtUangDiterima;
    // End of variables declaration//GEN-END:variables
}
