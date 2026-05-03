package com.release.klinikgaharumedika.service.report;

import com.release.klinikgaharumedika.config.DatabaseConnection;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

public class PendapatanReportService {

    private static final String TEMPLATE_PATH = "/reports/laporan_pendapatan.jrxml";
    private static final Locale REPORT_LOCALE = new Locale("id", "ID");
    private static final DateTimeFormatter HUMAN_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", REPORT_LOCALE);
    private static final DateTimeFormatter SIGNATURE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", REPORT_LOCALE);
    private static final DateTimeFormatter PRINTED_AT_FORMAT =
            DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", REPORT_LOCALE);
    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");
    private JasperReport compiledReport;

    public JasperPrint buildReport(LocalDate tanggalDari, LocalDate tanggalSampai)
            throws SQLException, JRException, IOException {
        validatePeriod(tanggalDari, tanggalSampai);

        try (Connection connection = DatabaseConnection.getConnection()) {
            PendapatanSummary summary = loadSummary(connection, tanggalDari, tanggalSampai);
            Map<String, Object> parameters = buildParameters(tanggalDari, tanggalSampai, summary);
            return JasperFillManager.fillReport(getCompiledReport(), parameters, connection);
        }
    }

    public void showPreview(JasperPrint jasperPrint) {
        JasperViewer viewer = new JasperViewer(jasperPrint, false);
        viewer.setTitle("Preview Laporan Pendapatan");
        viewer.setVisible(true);
    }

    public Path exportPdf(JasperPrint jasperPrint, Path outputPath) throws JRException {
        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath.toString());
        return outputPath;
    }

    public String buildDefaultFileName(LocalDate tanggalDari, LocalDate tanggalSampai) {
        return "laporan-pendapatan-"
                + tanggalDari.format(FILE_DATE_FORMAT)
                + "-"
                + tanggalSampai.format(FILE_DATE_FORMAT)
                + ".pdf";
    }

    private Map<String, Object> buildParameters(
            LocalDate tanggalDari,
            LocalDate tanggalSampai,
            PendapatanSummary summary
    ) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(JRParameter.REPORT_LOCALE, REPORT_LOCALE);
        parameters.put("KLINIK_NAMA", "Klinik Gaharu Medika");
        parameters.put("KLINIK_INITIAL", "G");
        parameters.put(
                "KLINIK_ALAMAT",
                "Jl. Gaharu Medika No. 1, Jakarta | Telp. (021) 555-1200 | Email: admin@gaharumedika.id"
        );
        parameters.put("TANGGAL_DARI", Date.valueOf(tanggalDari));
        parameters.put("TANGGAL_SAMPAI", Date.valueOf(tanggalSampai));
        parameters.put(
                "PERIODE_LABEL",
                "Periode: " + formatDate(tanggalDari) + " s.d. " + formatDate(tanggalSampai)
        );
        parameters.put(
                "DICETAK_LABEL",
                "Dicetak: " + PRINTED_AT_FORMAT.format(LocalDateTime.now())
        );
        parameters.put(
                "TEMPAT_TANGGAL_LABEL",
                "Jakarta, " + SIGNATURE_DATE_FORMAT.format(LocalDate.now())
        );
        parameters.put("TOTAL_TRANSAKSI", summary.totalTransaksi());
        parameters.put("TOTAL_KONSULTASI", summary.totalKonsultasi());
        parameters.put("TOTAL_OBAT", summary.totalObat());
        parameters.put("TOTAL_PENDAPATAN", summary.totalPendapatan());
        parameters.put("FORMAT_MATA_UANG", buildCurrencyFormat());
        return parameters;
    }

    private PendapatanSummary loadSummary(
            Connection connection,
            LocalDate tanggalDari,
            LocalDate tanggalSampai
    ) throws SQLException {
        String sql = "SELECT COUNT(*) AS total_transaksi, "
                + "COALESCE(SUM(biaya_konsultasi), 0) AS total_konsultasi, "
                + "COALESCE(SUM(biaya_obat), 0) AS total_obat, "
                + "COALESCE(SUM(total_tagihan), 0) AS total_pendapatan "
                + "FROM pembayaran "
                + "WHERE status = 'lunas' AND tanggal_bayar BETWEEN ? AND ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tanggalDari));
            ps.setDate(2, Date.valueOf(tanggalSampai));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new PendapatanSummary(
                            rs.getInt("total_transaksi"),
                            defaultBigDecimal(rs.getBigDecimal("total_konsultasi")),
                            defaultBigDecimal(rs.getBigDecimal("total_obat")),
                            defaultBigDecimal(rs.getBigDecimal("total_pendapatan"))
                    );
                }
            }
        }

        return new PendapatanSummary(0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private synchronized JasperReport getCompiledReport() throws JRException, IOException {
        if (compiledReport != null) {
            return compiledReport;
        }

        try (InputStream templateStream = getClass().getResourceAsStream(TEMPLATE_PATH)) {
            if (templateStream == null) {
                throw new IOException("Template laporan tidak ditemukan: " + TEMPLATE_PATH);
            }
            compiledReport = JasperCompileManager.compileReport(templateStream);
            return compiledReport;
        }
    }

    private NumberFormat buildCurrencyFormat() {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(REPORT_LOCALE);
        return new DecimalFormat("'Rp' #,##0", symbols);
    }

    private BigDecimal defaultBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String formatDate(LocalDate date) {
        return HUMAN_DATE_FORMAT.format(date);
    }

    private void validatePeriod(LocalDate tanggalDari, LocalDate tanggalSampai) {
        if (tanggalDari == null || tanggalSampai == null) {
            throw new IllegalArgumentException("Periode laporan harus diisi lengkap.");
        }
        if (tanggalDari.isAfter(tanggalSampai)) {
            throw new IllegalArgumentException("Tanggal awal tidak boleh melebihi tanggal akhir.");
        }
    }

    private record PendapatanSummary(
            int totalTransaksi,
            BigDecimal totalKonsultasi,
            BigDecimal totalObat,
            BigDecimal totalPendapatan
    ) {
    }
}
