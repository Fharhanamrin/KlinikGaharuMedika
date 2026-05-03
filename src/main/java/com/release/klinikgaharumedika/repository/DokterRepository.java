package com.release.klinikgaharumedika.repository;

import com.release.klinikgaharumedika.config.DatabaseConnection;
import com.release.klinikgaharumedika.model.Dokter;
import com.release.klinikgaharumedika.model.JadwalDokter;
import com.release.klinikgaharumedika.model.PageResult;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DokterRepository {

    public PageResult<Dokter> findAll(int page, int pageSize, String keyword) throws SQLException {
        int total = countAll(keyword);
        int offset = (page - 1) * pageSize;
        List<Dokter> items = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(buildFindAllSql(hasPoliUnit(conn)))) {
            boolean hasPoliUnit = hasPoliUnit(conn);
            String like = "%" + keyword + "%";
            int index = 1;
            ps.setString(index++, like);
            ps.setString(index++, like);
            ps.setString(index++, like);
            if (hasPoliUnit) {
                ps.setString(index++, like);
            }
            ps.setString(index++, like);
            ps.setString(index++, like);
            ps.setString(index++, like);
            ps.setString(index++, like);
            ps.setInt(index++, pageSize);
            ps.setInt(index, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) items.add(mapRow(rs));
            }
        }
        return new PageResult<>(items, total, page, pageSize);
    }

    public int countAll(String keyword) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(buildCountSql(hasPoliUnit(conn)))) {
            boolean hasPoliUnit = hasPoliUnit(conn);
            String like = "%" + keyword + "%";
            int index = 1;
            ps.setString(index++, like);
            ps.setString(index++, like);
            ps.setString(index++, like);
            if (hasPoliUnit) {
                ps.setString(index++, like);
            }
            ps.setString(index++, like);
            ps.setString(index++, like);
            ps.setString(index++, like);
            ps.setString(index, like);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public boolean insert(Dokter d) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean hasPoliUnit = hasPoliUnit(conn);
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(buildInsertSql(hasPoliUnit), Statement.RETURN_GENERATED_KEYS)) {
                int index = 1;
                ps.setString(index++, d.getKodeDokter());
                ps.setString(index++, d.getNama());
                ps.setString(index++, d.getSpesialisasi());
                if (hasPoliUnit) {
                    ps.setString(index++, d.getPoliUnit());
                }
                ps.setString(index++, d.getNoStr());
                ps.setString(index++, d.getNoSip());
                ps.setString(index++, d.getNoHp());
                ps.setBigDecimal(index++, safeAmount(d.getTarifKonsultasi()));
                ps.setString(index, d.getStatus());
                int affected = ps.executeUpdate();
                if (affected <= 0) {
                    conn.rollback();
                    return false;
                }
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        d.setId(keys.getInt(1));
                    }
                }
                insertSchedules(conn, d.getId(), d.getJadwalPraktik());
                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean update(Dokter d) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean hasPoliUnit = hasPoliUnit(conn);
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(buildUpdateSql(hasPoliUnit))) {
                int index = 1;
                ps.setString(index++, d.getNama());
                ps.setString(index++, d.getSpesialisasi());
                if (hasPoliUnit) {
                    ps.setString(index++, d.getPoliUnit());
                }
                ps.setString(index++, d.getNoStr());
                ps.setString(index++, d.getNoSip());
                ps.setString(index++, d.getNoHp());
                ps.setBigDecimal(index++, safeAmount(d.getTarifKonsultasi()));
                ps.setString(index++, d.getStatus());
                ps.setInt(index, d.getId());
                int affected = ps.executeUpdate();
                if (affected <= 0) {
                    conn.rollback();
                    return false;
                }
                deleteSchedules(conn, d.getId());
                insertSchedules(conn, d.getId(), d.getJadwalPraktik());
                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM dokter WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Dokter findById(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM dokter WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Dokter dokter = mapRow(rs);
                    dokter.setJadwalPraktik(findSchedulesByDokterId(conn, id));
                    return dokter;
                }
            }
        }
        return null;
    }

    public List<Dokter> findAllAktif() throws SQLException {
        List<Dokter> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM dokter WHERE status='aktif' ORDER BY nama")) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public String generateKodeDokter() throws SQLException {
        String sql = "SELECT kode_dokter FROM dokter ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                String last = rs.getString("kode_dokter");
                try {
                    int nomorTerakhir = Integer.parseInt(last.replace("DR", ""));
                    return String.format("DR%03d", nomorTerakhir + 1);
                } catch (NumberFormatException ex) {
                    return "DR001";
                }
            }
        }
        return "DR001";
    }

    private void deleteSchedules(Connection conn, int dokterId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM jadwal_dokter WHERE dokter_id=?")) {
            ps.setInt(1, dokterId);
            ps.executeUpdate();
        }
    }

    private void insertSchedules(Connection conn, int dokterId, List<JadwalDokter> schedules) throws SQLException {
        if (schedules == null || schedules.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO jadwal_dokter (dokter_id, hari, jam_mulai, jam_selesai, kuota_pasien) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (JadwalDokter schedule : schedules) {
                ps.setInt(1, dokterId);
                ps.setString(2, schedule.getHari());
                ps.setTime(3, Time.valueOf(schedule.getJamMulai()));
                ps.setTime(4, Time.valueOf(schedule.getJamSelesai()));
                ps.setInt(5, schedule.getKuotaPasien());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<JadwalDokter> findSchedulesByDokterId(Connection conn, int dokterId) throws SQLException {
        List<JadwalDokter> schedules = new ArrayList<>();
        String sql = "SELECT id, dokter_id, hari, jam_mulai, jam_selesai, kuota_pasien FROM jadwal_dokter WHERE dokter_id=? ORDER BY id ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dokterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    schedules.add(mapScheduleRow(rs));
                }
            }
        }
        return schedules;
    }

    private Dokter mapRow(ResultSet rs) throws SQLException {
        Dokter d = new Dokter();
        d.setId(rs.getInt("id"));
        d.setKodeDokter(rs.getString("kode_dokter"));
        d.setNama(rs.getString("nama"));
        d.setSpesialisasi(rs.getString("spesialisasi"));
        d.setPoliUnit(getStringIfPresent(rs, "poli_unit"));
        d.setNoStr(rs.getString("no_str"));
        d.setNoSip(rs.getString("no_sip"));
        d.setNoHp(rs.getString("no_hp"));
        BigDecimal tarif = rs.getBigDecimal("tarif_konsultasi");
        d.setTarifKonsultasi(tarif != null ? tarif : BigDecimal.ZERO);
        d.setStatus(rs.getString("status"));
        return d;
    }

    private JadwalDokter mapScheduleRow(ResultSet rs) throws SQLException {
        JadwalDokter schedule = new JadwalDokter();
        schedule.setId(rs.getInt("id"));
        schedule.setDokterId(rs.getInt("dokter_id"));
        schedule.setHari(rs.getString("hari"));
        Time jamMulai = rs.getTime("jam_mulai");
        if (jamMulai != null) {
            schedule.setJamMulai(jamMulai.toLocalTime());
        }
        Time jamSelesai = rs.getTime("jam_selesai");
        if (jamSelesai != null) {
            schedule.setJamSelesai(jamSelesai.toLocalTime());
        }
        schedule.setKuotaPasien(rs.getInt("kuota_pasien"));
        return schedule;
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private boolean hasPoliUnit(Connection conn) throws SQLException {
        return SchemaSupport.hasColumn(conn, "dokter", "poli_unit");
    }

    private String buildFindAllSql(boolean hasPoliUnit) {
        return "SELECT * FROM dokter WHERE (kode_dokter LIKE ? OR nama LIKE ? OR spesialisasi LIKE ?"
                + (hasPoliUnit ? " OR COALESCE(poli_unit, '') LIKE ?" : "")
                + " OR no_str LIKE ? OR no_sip LIKE ? OR no_hp LIKE ? OR status LIKE ?)"
                + " ORDER BY kode_dokter ASC LIMIT ? OFFSET ?";
    }

    private String buildCountSql(boolean hasPoliUnit) {
        return "SELECT COUNT(*) FROM dokter WHERE (kode_dokter LIKE ? OR nama LIKE ? OR spesialisasi LIKE ?"
                + (hasPoliUnit ? " OR COALESCE(poli_unit, '') LIKE ?" : "")
                + " OR no_str LIKE ? OR no_sip LIKE ? OR no_hp LIKE ? OR status LIKE ?)";
    }

    private String buildInsertSql(boolean hasPoliUnit) {
        return "INSERT INTO dokter (kode_dokter, nama, spesialisasi"
                + (hasPoliUnit ? ", poli_unit" : "")
                + ", no_str, no_sip, no_hp, tarif_konsultasi, status) VALUES (?,?,?"
                + (hasPoliUnit ? ",?" : "")
                + ",?,?,?,?,?)";
    }

    private String buildUpdateSql(boolean hasPoliUnit) {
        return "UPDATE dokter SET nama=?, spesialisasi=?"
                + (hasPoliUnit ? ", poli_unit=?" : "")
                + ", no_str=?, no_sip=?, no_hp=?, tarif_konsultasi=?, status=? WHERE id=?";
    }

    private String getStringIfPresent(ResultSet rs, String columnName) throws SQLException {
        return SchemaSupport.hasColumn(rs, columnName) ? rs.getString(columnName) : null;
    }
}
