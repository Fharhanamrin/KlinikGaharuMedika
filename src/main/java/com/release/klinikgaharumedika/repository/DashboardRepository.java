package com.release.klinikgaharumedika.repository;

import com.release.klinikgaharumedika.config.DatabaseConnection;
import com.release.klinikgaharumedika.model.DashboardActiveQueuePage;
import com.release.klinikgaharumedika.model.DashboardData;
import com.release.klinikgaharumedika.model.DashboardQueueEntry;
import com.release.klinikgaharumedika.model.DashboardRecentVisit;
import com.release.klinikgaharumedika.model.DashboardRecentVisitPage;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardRepository {

    private static final String DASHBOARD_STATS_SQL = """
            SELECT
                (SELECT COUNT(*) FROM kunjungan WHERE tanggal_kunjungan = CURDATE()) AS visits_today,
                (SELECT COUNT(*) FROM kunjungan WHERE tanggal_kunjungan = CURDATE() - INTERVAL 1 DAY) AS visits_yesterday,
                (SELECT COUNT(*) FROM pasien) AS total_patients,
                (
                    SELECT COUNT(*)
                    FROM pasien
                    WHERE YEAR(created_at) = YEAR(CURDATE())
                      AND MONTH(created_at) = MONTH(CURDATE())
                ) AS new_patients_this_month,
                (
                    SELECT COALESCE(SUM(total_tagihan), 0)
                    FROM pembayaran
                    WHERE status = 'lunas'
                      AND tanggal_bayar = CURDATE()
                ) AS today_revenue,
                (
                    SELECT COALESCE(AVG(revenue_days.daily_total), 0)
                    FROM (
                        SELECT SUM(total_tagihan) AS daily_total
                        FROM pembayaran
                        WHERE status = 'lunas'
                          AND tanggal_bayar BETWEEN CURDATE() - INTERVAL 7 DAY AND CURDATE() - INTERVAL 1 DAY
                        GROUP BY tanggal_bayar
                    ) revenue_days
                ) AS average_daily_revenue,
                (
                    SELECT COUNT(*)
                    FROM obat
                    WHERE stok_saat_ini <= stok_minimum
                ) AS low_stock_items
            """;

    private static final String RECENT_VISITS_SELECT_SQL = """
            SELECT
                k.no_antrian,
                p.nama AS patient_name,
                d.nama AS doctor_name,
                d.spesialisasi,
                CASE
                    WHEN k.status = 'selesai' AND (pb.id IS NULL OR pb.status = 'pending') THEN 'Blm Bayar'
                    WHEN k.status = 'selesai' THEN 'Selesai'
                    WHEN k.status = 'periksa' THEN 'Periksa'
                    ELSE 'Menunggu'
                END AS status_label
            FROM kunjungan k
            JOIN pasien p ON p.id = k.pasien_id
            JOIN dokter d ON d.id = k.dokter_id
            LEFT JOIN pembayaran pb ON pb.kunjungan_id = k.id
            """;

    private static final String RECENT_VISITS_COUNT_SQL = """
            SELECT COUNT(*) AS total_items
            FROM kunjungan k
            JOIN pasien p ON p.id = k.pasien_id
            JOIN dokter d ON d.id = k.dokter_id
            LEFT JOIN pembayaran pb ON pb.kunjungan_id = k.id
            """;

    private static final String RECENT_VISITS_SEARCH_SQL = """
            WHERE (
                k.no_antrian LIKE ?
                OR p.nama LIKE ?
                OR d.nama LIKE ?
                OR d.spesialisasi LIKE ?
            )
            """;

    private static final String RECENT_VISITS_ORDER_SQL = """
            ORDER BY k.tanggal_kunjungan DESC, k.created_at DESC
            """;

    private static final String ACTIVE_QUEUE_SELECT_SQL = """
            SELECT
                k.no_antrian,
                p.nama AS patient_name,
                d.nama AS doctor_name,
                d.spesialisasi,
                CASE
                    WHEN k.status = 'periksa' THEN 'Periksa'
                    ELSE 'Menunggu'
                END AS status_label
            FROM kunjungan k
            JOIN pasien p ON p.id = k.pasien_id
            JOIN dokter d ON d.id = k.dokter_id
            WHERE k.tanggal_kunjungan = CURDATE()
              AND k.status IN ('menunggu', 'periksa')
            """;

    private static final String ACTIVE_QUEUE_COUNT_SQL = """
            SELECT COUNT(*) AS total_items
            FROM kunjungan k
            WHERE k.tanggal_kunjungan = CURDATE()
              AND k.status IN ('menunggu', 'periksa')
            """;

    private static final String ACTIVE_QUEUE_ORDER_SQL = """
            ORDER BY k.no_antrian ASC, k.created_at ASC
            """;

    // Satu query utama untuk empat kartu statistik dashboard.
    public DashboardData loadDashboardStats() throws SQLException {
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(DASHBOARD_STATS_SQL);
                ResultSet resultSet = statement.executeQuery()
        ) {
            if (resultSet.next()) {
                return mapDashboardData(resultSet);
            }
        }

        return DashboardData.empty();
    }

    public DashboardRecentVisitPage loadRecentVisitPage(int page, int pageSize, String searchQuery) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return loadRecentVisitPage(connection, page, pageSize, searchQuery);
        }
    }

    public DashboardActiveQueuePage loadActiveQueuePage(int page, int pageSize) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return loadActiveQueuePage(connection, page, pageSize);
        }
    }

    private DashboardData mapDashboardData(ResultSet resultSet) throws SQLException {
        return new DashboardData(
                resultSet.getInt("visits_today"),
                resultSet.getInt("visits_yesterday"),
                resultSet.getInt("total_patients"),
                resultSet.getInt("new_patients_this_month"),
                getAmountOrZero(resultSet, "today_revenue"),
                getAmountOrZero(resultSet, "average_daily_revenue"),
                resultSet.getInt("low_stock_items")
        );
    }

    private BigDecimal getAmountOrZero(ResultSet resultSet, String columnName) throws SQLException {
        BigDecimal amount = resultSet.getBigDecimal(columnName);
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private DashboardRecentVisitPage loadRecentVisitPage(
            Connection connection,
            int page,
            int pageSize,
            String searchQuery
    ) throws SQLException {
        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        String normalizedSearch = searchQuery == null ? "" : searchQuery.trim();
        int totalItems = countRecentVisits(connection, normalizedSearch);
        int totalPages = totalItems == 0 ? 1 : (int) Math.ceil(totalItems / (double) safePageSize);
        int currentPage = totalItems == 0 ? 1 : Math.min(Math.max(page, 1), totalPages);

        return new DashboardRecentVisitPage(
                loadRecentVisits(connection, currentPage, safePageSize, normalizedSearch),
                currentPage,
                safePageSize,
                totalItems,
                totalPages
        );
    }

    private int countRecentVisits(Connection connection, String searchQuery) throws SQLException {
        boolean hasSearch = searchQuery != null && !searchQuery.isBlank();
        StringBuilder sql = new StringBuilder(RECENT_VISITS_COUNT_SQL);
        if (hasSearch) {
            sql.append(RECENT_VISITS_SEARCH_SQL);
        }

        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            if (hasSearch) {
                bindRecentVisitSearchParameters(statement, searchQuery, 1);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total_items");
                }
            }
        }

        return 0;
    }

    private List<DashboardRecentVisit> loadRecentVisits(
            Connection connection,
            int page,
            int pageSize,
            String searchQuery
    ) throws SQLException {
        List<DashboardRecentVisit> recentVisits = new ArrayList<>();
        boolean hasSearch = searchQuery != null && !searchQuery.isBlank();
        int offset = Math.max(page - 1, 0) * pageSize;
        StringBuilder sql = new StringBuilder(RECENT_VISITS_SELECT_SQL);

        if (hasSearch) {
            sql.append(RECENT_VISITS_SEARCH_SQL);
        }

        sql.append(RECENT_VISITS_ORDER_SQL)
                .append("\nLIMIT ? OFFSET ?");

        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int parameterIndex = 1;
            if (hasSearch) {
                parameterIndex = bindRecentVisitSearchParameters(statement, searchQuery, parameterIndex);
            }
            statement.setInt(parameterIndex++, pageSize);
            statement.setInt(parameterIndex, offset);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    recentVisits.add(new DashboardRecentVisit(
                            resultSet.getString("no_antrian"),
                            resultSet.getString("patient_name"),
                            buildDoctorDisplay(resultSet.getString("doctor_name"), resultSet.getString("spesialisasi")),
                            resultSet.getString("status_label")
                    ));
                }
            }
        }

        return recentVisits;
    }

    private int bindRecentVisitSearchParameters(
            PreparedStatement statement,
            String searchQuery,
            int startIndex
    ) throws SQLException {
        String keyword = "%" + searchQuery.trim() + "%";
        int parameterIndex = startIndex;

        statement.setString(parameterIndex++, keyword);
        statement.setString(parameterIndex++, keyword);
        statement.setString(parameterIndex++, keyword);
        statement.setString(parameterIndex++, keyword);

        return parameterIndex;
    }

    private DashboardActiveQueuePage loadActiveQueuePage(
            Connection connection,
            int page,
            int pageSize
    ) throws SQLException {
        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        int totalItems = countActiveQueues(connection);
        int totalPages = totalItems == 0 ? 1 : (int) Math.ceil(totalItems / (double) safePageSize);
        int currentPage = totalItems == 0 ? 1 : Math.min(Math.max(page, 1), totalPages);

        return new DashboardActiveQueuePage(
                loadActiveQueues(connection, currentPage, safePageSize),
                currentPage,
                safePageSize,
                totalItems,
                totalPages
        );
    }

    private int countActiveQueues(Connection connection) throws SQLException {
        try (
                PreparedStatement statement = connection.prepareStatement(ACTIVE_QUEUE_COUNT_SQL);
                ResultSet resultSet = statement.executeQuery()
        ) {
            if (resultSet.next()) {
                return resultSet.getInt("total_items");
            }
        }

        return 0;
    }

    private List<DashboardQueueEntry> loadActiveQueues(
            Connection connection,
            int page,
            int pageSize
    ) throws SQLException {
        List<DashboardQueueEntry> queueEntries = new ArrayList<>();
        int offset = Math.max(page - 1, 0) * pageSize;
        String sql = ACTIVE_QUEUE_SELECT_SQL
                + "\n"
                + ACTIVE_QUEUE_ORDER_SQL
                + "\nLIMIT ? OFFSET ?";

        try (
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, pageSize);
            statement.setInt(2, offset);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    queueEntries.add(new DashboardQueueEntry(
                            resultSet.getString("no_antrian"),
                            resultSet.getString("patient_name"),
                            buildDoctorDisplay(resultSet.getString("doctor_name"), resultSet.getString("spesialisasi")),
                            resultSet.getString("status_label")
                    ));
                }
            }
        }

        return queueEntries;
    }

    private String buildDoctorDisplay(String doctorName, String specialization) {
        String normalizedDoctorName = doctorName == null ? "-" : doctorName.trim();
        if (!normalizedDoctorName.toLowerCase().startsWith("dr")) {
            normalizedDoctorName = "dr. " + normalizedDoctorName;
        }

        if (specialization == null || specialization.isBlank()) {
            return normalizedDoctorName;
        }

        return normalizedDoctorName + " · " + specialization.trim();
    }

}
