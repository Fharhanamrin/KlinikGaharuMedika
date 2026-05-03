package com.release.klinikgaharumedika.service.report;

import com.release.klinikgaharumedika.config.DatabaseConnection;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

public class TableReportService {

    public enum ReportType {
        PASIEN("laporan-pasien", "Laporan Data Pasien"),
        PEMERIKSAAN("laporan-pemeriksaan", "Laporan Pemeriksaan"),
        OBAT("laporan-penjualan-obat", "Laporan Penjualan Obat");

        private final String filePrefix;
        private final String title;

        ReportType(String filePrefix, String title) {
            this.filePrefix = filePrefix;
            this.title = title;
        }

        public String filePrefix() {
            return filePrefix;
        }

        public String title() {
            return title;
        }
    }

    private static final String TEMPLATE_PATH = "/reports/laporan_tabel.jrxml";
    private static final Locale REPORT_LOCALE = new Locale("id", "ID");
    private static final DateTimeFormatter HUMAN_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", REPORT_LOCALE);
    private static final DateTimeFormatter PRINTED_AT_FORMAT =
            DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", REPORT_LOCALE);
    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");
    private JasperReport compiledReport;

    public JasperPrint buildReport(ReportType type, LocalDate tanggalDari, LocalDate tanggalSampai)
            throws SQLException, JRException, IOException {
        validatePeriod(tanggalDari, tanggalSampai);
        ReportDefinition definition = definitionFor(type);

        try (Connection connection = DatabaseConnection.getConnection()) {
            List<Map<String, ?>> rows = loadRows(connection, definition, tanggalDari, tanggalSampai);
            Map<String, Object> parameters = buildParameters(definition, tanggalDari, tanggalSampai, rows);
            return JasperFillManager.fillReport(getCompiledReport(), parameters, new JRMapCollectionDataSource(rows));
        }
    }

    public void showPreview(ReportType type, JasperPrint jasperPrint) {
        JasperViewer viewer = new JasperViewer(jasperPrint, false);
        viewer.setTitle("Preview " + type.title());
        viewer.setVisible(true);
    }

    public Path exportPdf(JasperPrint jasperPrint, Path outputPath) throws JRException {
        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath.toString());
        return outputPath;
    }

    public String buildDefaultFileName(ReportType type, LocalDate tanggalDari, LocalDate tanggalSampai) {
        return type.filePrefix()
                + "-"
                + tanggalDari.format(FILE_DATE_FORMAT)
                + "-"
                + tanggalSampai.format(FILE_DATE_FORMAT)
                + ".pdf";
    }

    private List<Map<String, ?>> loadRows(
            Connection connection,
            ReportDefinition definition,
            LocalDate tanggalDari,
            LocalDate tanggalSampai
    ) throws SQLException {
        List<Map<String, ?>> rows = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(definition.sql())) {
            if (definition.usesPeriod()) {
                statement.setDate(1, Date.valueOf(tanggalDari));
                statement.setDate(2, Date.valueOf(tanggalSampai));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, String> row = new HashMap<>();
                    for (int i = 1; i <= 7; i++) {
                        row.put("col" + i, safe(resultSet.getString("col" + i)));
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    private Map<String, Object> buildParameters(
            ReportDefinition definition,
            LocalDate tanggalDari,
            LocalDate tanggalSampai,
            List<Map<String, ?>> rows
    ) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(JRParameter.REPORT_LOCALE, REPORT_LOCALE);
        parameters.put("KLINIK_NAMA", "Klinik Gaharu Medika");
        parameters.put("KLINIK_INITIAL", "G");
        parameters.put(
                "KLINIK_ALAMAT",
                "Jl. Gaharu Medika No. 1, Jakarta | Telp. (021) 555-1200 | Email: admin@gaharumedika.id"
        );
        parameters.put("REPORT_TITLE", definition.type().title());
        parameters.put(
                "PERIODE_LABEL",
                definition.usesPeriod()
                        ? "Periode: " + formatDate(tanggalDari) + " s.d. " + formatDate(tanggalSampai)
                        : "Data per: " + formatDate(LocalDate.now())
        );
        parameters.put("DICETAK_LABEL", "Dicetak: " + PRINTED_AT_FORMAT.format(LocalDateTime.now()));
        parameters.put("TEMPAT_TANGGAL_LABEL", "Jakarta, " + formatDate(LocalDate.now()));
        for (int i = 0; i < definition.columns().length; i++) {
            parameters.put("COLUMN_" + (i + 1), definition.columns()[i]);
        }
        parameters.put("SUMMARY_LABEL", definition.summaryLabel());
        parameters.put("SUMMARY_VALUE", definition.summaryValue(rows, buildCurrencyFormat()));
        return parameters;
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

    private ReportDefinition definitionFor(ReportType type) {
        return switch (type) {
            case PASIEN -> new ReportDefinition(
                    type,
                    false,
                    new String[]{"No. RM", "Nama", "NIK", "Tgl Lahir", "JK", "Status", "No. HP"},
                    "Total Pasien",
                    "SELECT no_rm AS col1, nama AS col2, CONCAT(LEFT(nik, 6), '...', RIGHT(nik, 4)) AS col3, "
                            + "DATE_FORMAT(tanggal_lahir, '%d/%m/%Y') AS col4, "
                            + "jenis_kelamin AS col5, COALESCE(jenis_pasien, '-') AS col6, COALESCE(no_hp, '-') AS col7 "
                            + "FROM pasien ORDER BY no_rm ASC"
            );
            case PEMERIKSAAN -> new ReportDefinition(
                    type,
                    true,
                    new String[]{"No. KJ", "Pasien", "Dokter", "Tanggal", "Jenis", "Diagnosa", "Status"},
                    "Total Data",
                    "SELECT k.no_kunjungan AS col1, p.nama AS col2, d.nama AS col3, "
                            + "DATE_FORMAT(k.tanggal_kunjungan, '%d/%m/%Y') AS col4, "
                            + "k.jenis_kunjungan AS col5, COALESCE(k.diagnosa, '-') AS col6, k.status AS col7 "
                            + "FROM kunjungan k "
                            + "JOIN pasien p ON k.pasien_id = p.id "
                            + "JOIN dokter d ON k.dokter_id = d.id "
                            + "WHERE k.tanggal_kunjungan BETWEEN ? AND ? "
                            + "ORDER BY k.tanggal_kunjungan ASC, k.no_kunjungan ASC"
            );
            case OBAT -> new ReportDefinition(
                    type,
                    true,
                    new String[]{"Kode", "Nama Obat", "Jenis", "Satuan", "Terjual", "Stok", "Harga"},
                    "Total Item",
                    "SELECT o.kode_obat AS col1, o.nama_obat AS col2, o.jenis AS col3, o.satuan AS col4, "
                            + "CAST(COALESCE(SUM(ro.jumlah), 0) AS CHAR) AS col5, "
                            + "CAST(o.stok_saat_ini AS CHAR) AS col6, "
                            + "CONCAT('Rp ', REPLACE(FORMAT(o.harga_jual, 0), ',', '.')) AS col7 "
                            + "FROM obat o "
                            + "LEFT JOIN resep_obat ro ON ro.obat_id = o.id "
                            + "LEFT JOIN kunjungan k ON k.id = ro.kunjungan_id "
                            + "WHERE k.tanggal_kunjungan BETWEEN ? AND ? OR k.id IS NULL "
                            + "GROUP BY o.id, o.kode_obat, o.nama_obat, o.jenis, o.satuan, o.stok_saat_ini, o.harga_jual "
                            + "ORDER BY o.kode_obat ASC"
            );
        };
    }

    private NumberFormat buildCurrencyFormat() {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(REPORT_LOCALE);
        return new DecimalFormat("'Rp' #,##0", symbols);
    }

    private String formatDate(LocalDate date) {
        return HUMAN_DATE_FORMAT.format(date);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void validatePeriod(LocalDate tanggalDari, LocalDate tanggalSampai) {
        if (tanggalDari == null || tanggalSampai == null) {
            throw new IllegalArgumentException("Periode laporan harus diisi lengkap.");
        }
        if (tanggalDari.isAfter(tanggalSampai)) {
            throw new IllegalArgumentException("Tanggal awal tidak boleh melebihi tanggal akhir.");
        }
    }

    private record ReportDefinition(
            ReportType type,
            boolean usesPeriod,
            String[] columns,
            String summaryLabel,
            String sql
    ) {
        String summaryValue(List<Map<String, ?>> rows, NumberFormat currencyFormat) {
            return String.valueOf(rows.size());
        }
    }
}
