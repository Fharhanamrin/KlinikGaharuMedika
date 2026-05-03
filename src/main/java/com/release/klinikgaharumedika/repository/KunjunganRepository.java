package com.release.klinikgaharumedika.repository;

import com.release.klinikgaharumedika.config.DatabaseConnection;
import com.release.klinikgaharumedika.model.Kunjungan;
import com.release.klinikgaharumedika.model.PageResult;
import com.release.klinikgaharumedika.model.ResepObatItem;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class KunjunganRepository {

    private static final DateTimeFormatter NOMOR_KUNJUNGAN_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");

    public PageResult<Kunjungan> findAll(int page, int pageSize, String keyword) throws SQLException {
        int total = countAll(keyword);
        int offset = (page - 1) * pageSize;
        List<Kunjungan> items = new ArrayList<>();

        String sql = "SELECT k.id, k.no_antrian, k.no_kunjungan, k.pasien_id, k.dokter_id, k.perawat_id, "
                + "k.tanggal_kunjungan, k.jenis_kunjungan, k.keluhan_utama, k.diagnosa, k.kode_icd10, "
                + "k.tekanan_darah, k.berat_badan, k.tinggi_badan, k.suhu, k.nadi, k.saturasi_o2, "
                + "k.catatan_dokter, k.status, "
                + "p.nama AS nama_pasien, d.nama AS nama_dokter, "
                + "COALESCE(pb.status, 'pending') AS status_bayar "
                + "FROM kunjungan k "
                + "JOIN pasien p ON k.pasien_id = p.id "
                + "JOIN dokter d ON k.dokter_id = d.id "
                + "LEFT JOIN pembayaran pb ON pb.kunjungan_id = k.id "
                + "WHERE (k.no_kunjungan LIKE ? OR k.no_antrian LIKE ? OR p.nama LIKE ? OR d.nama LIKE ? "
                + "OR k.jenis_kunjungan LIKE ? OR k.status LIKE ? OR COALESCE(k.diagnosa, '') LIKE ?) "
                + "ORDER BY k.tanggal_kunjungan DESC, k.no_antrian DESC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            ps.setString(6, like);
            ps.setString(7, like);
            ps.setInt(8, pageSize);
            ps.setInt(9, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        }
        return new PageResult<>(items, total, page, pageSize);
    }

    public int countAll(String keyword) throws SQLException {
        String sql = "SELECT COUNT(*) FROM kunjungan k "
                + "JOIN pasien p ON k.pasien_id = p.id "
                + "JOIN dokter d ON k.dokter_id = d.id "
                + "WHERE (k.no_kunjungan LIKE ? OR k.no_antrian LIKE ? OR p.nama LIKE ? OR d.nama LIKE ? "
                + "OR k.jenis_kunjungan LIKE ? OR k.status LIKE ? OR COALESCE(k.diagnosa, '') LIKE ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            ps.setString(6, like);
            ps.setString(7, like);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public boolean insert(Kunjungan k) throws SQLException {
        return insert(k, List.of());
    }

    public boolean insert(Kunjungan k, List<ResepObatItem> resepItems) throws SQLException {
        String sql = "INSERT INTO kunjungan (no_antrian, no_kunjungan, pasien_id, dokter_id, perawat_id, "
                + "tanggal_kunjungan, jenis_kunjungan, keluhan_utama, diagnosa, kode_icd10, tekanan_darah, "
                + "berat_badan, tinggi_badan, suhu, nadi, saturasi_o2, catatan_dokter, status) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bindKunjunganForUpsert(ps, k, false);
                int affected = ps.executeUpdate();
                if (affected <= 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        k.setId(keys.getInt(1));
                    }
                }

                insertResepItems(conn, k.getId(), resepItems);
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

    public boolean update(Kunjungan k) throws SQLException {
        return update(k, List.of());
    }

    public boolean update(Kunjungan k, List<ResepObatItem> resepItems) throws SQLException {
        String sql = "UPDATE kunjungan SET pasien_id=?, dokter_id=?, perawat_id=?, tanggal_kunjungan=?, "
                + "jenis_kunjungan=?, keluhan_utama=?, diagnosa=?, kode_icd10=?, tekanan_darah=?, berat_badan=?, "
                + "tinggi_badan=?, suhu=?, nadi=?, saturasi_o2=?, catatan_dokter=?, status=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bindKunjunganForUpsert(ps, k, true);
                int affected = ps.executeUpdate();
                if (affected <= 0) {
                    conn.rollback();
                    return false;
                }

                deleteResepItems(conn, k.getId());
                insertResepItems(conn, k.getId(), resepItems);
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
             PreparedStatement ps = conn.prepareStatement("DELETE FROM kunjungan WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Kunjungan findById(int id) throws SQLException {
        String sql = "SELECT k.*, p.nama AS nama_pasien, d.nama AS nama_dokter, COALESCE(pb.status,'pending') AS status_bayar "
                + "FROM kunjungan k JOIN pasien p ON k.pasien_id=p.id JOIN dokter d ON k.dokter_id=d.id "
                + "LEFT JOIN pembayaran pb ON pb.kunjungan_id=k.id WHERE k.id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<ResepObatItem> findResepByKunjunganId(int kunjunganId) throws SQLException {
        List<ResepObatItem> items = new ArrayList<>();
        String sql = "SELECT ro.id, ro.kunjungan_id, ro.obat_id, ro.jumlah, ro.aturan_pakai, ro.keterangan, "
                + "o.nama_obat, o.satuan "
                + "FROM resep_obat ro "
                + "JOIN obat o ON o.id = ro.obat_id "
                + "WHERE ro.kunjungan_id = ? "
                + "ORDER BY ro.id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, kunjunganId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResepRow(rs));
                }
            }
        }
        return items;
    }

    public String generateNoAntrian(LocalDate tanggal) throws SQLException {
        String sql = "SELECT COUNT(*) FROM kunjungan WHERE tanggal_kunjungan=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tanggal));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1) + 1;
                    return String.format("A%03d", count);
                }
            }
        }
        return "A001";
    }

    public String generateNoKunjungan(LocalDate tanggal) throws SQLException {
        String sql = "SELECT COUNT(*) FROM kunjungan WHERE tanggal_kunjungan=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tanggal));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1) + 1;
                    return "KJ" + tanggal.format(NOMOR_KUNJUNGAN_FORMAT) + String.format("%03d", count);
                }
            }
        }
        return "KJ" + tanggal.format(NOMOR_KUNJUNGAN_FORMAT) + "001";
    }

    private void bindKunjunganForUpsert(PreparedStatement ps, Kunjungan k, boolean includeIdAtEnd) throws SQLException {
        int index = 1;
        if (!includeIdAtEnd) {
            ps.setString(index++, k.getNoAntrian());
            ps.setString(index++, k.getNoKunjungan());
        }
        ps.setInt(index++, k.getPasienId());
        ps.setInt(index++, k.getDokterId());
        if (k.getPerawatId() != null) {
            ps.setInt(index++, k.getPerawatId());
        } else {
            ps.setNull(index++, Types.INTEGER);
        }
        ps.setDate(index++, k.getTanggalKunjungan() != null ? Date.valueOf(k.getTanggalKunjungan()) : null);
        ps.setString(index++, k.getJenisKunjungan());
        ps.setString(index++, k.getKeluhanUtama());
        ps.setString(index++, k.getDiagnosa());
        ps.setString(index++, k.getKodeIcd10());
        ps.setString(index++, k.getTekananDarah());
        setNullableDouble(ps, index++, k.getBeratBadan());
        setNullableDouble(ps, index++, k.getTinggiBadan());
        setNullableDouble(ps, index++, k.getSuhu());
        setNullableInt(ps, index++, k.getNadi());
        setNullableInt(ps, index++, k.getSaturasiO2());
        ps.setString(index++, k.getCatatanDokter());
        ps.setString(index++, k.getStatus());
        if (includeIdAtEnd) {
            ps.setInt(index, k.getId());
        }
    }

    private void insertResepItems(Connection conn, int kunjunganId, List<ResepObatItem> resepItems) throws SQLException {
        if (resepItems == null || resepItems.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO resep_obat (kunjungan_id, obat_id, jumlah, aturan_pakai, keterangan) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ResepObatItem item : resepItems) {
                if (item == null || item.getObatId() <= 0 || item.getJumlah() <= 0) {
                    continue;
                }
                ps.setInt(1, kunjunganId);
                ps.setInt(2, item.getObatId());
                ps.setInt(3, item.getJumlah());
                ps.setString(4, item.getAturanPakai());
                ps.setString(5, item.getKeterangan());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void deleteResepItems(Connection conn, int kunjunganId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM resep_obat WHERE kunjungan_id=?")) {
            ps.setInt(1, kunjunganId);
            ps.executeUpdate();
        }
    }

    private void setNullableDouble(PreparedStatement ps, int index, Double value) throws SQLException {
        if (value != null) {
            ps.setDouble(index, value);
        } else {
            ps.setNull(index, Types.DOUBLE);
        }
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value != null) {
            ps.setInt(index, value);
        } else {
            ps.setNull(index, Types.INTEGER);
        }
    }

    private Kunjungan mapRow(ResultSet rs) throws SQLException {
        Kunjungan k = new Kunjungan();
        k.setId(rs.getInt("id"));
        k.setNoAntrian(rs.getString("no_antrian"));
        k.setNoKunjungan(rs.getString("no_kunjungan"));
        k.setPasienId(rs.getInt("pasien_id"));
        k.setDokterId(rs.getInt("dokter_id"));
        int pId = rs.getInt("perawat_id");
        if (!rs.wasNull()) {
            k.setPerawatId(pId);
        }
        Date tgl = rs.getDate("tanggal_kunjungan");
        if (tgl != null) {
            k.setTanggalKunjungan(tgl.toLocalDate());
        }
        k.setJenisKunjungan(rs.getString("jenis_kunjungan"));
        k.setKeluhanUtama(rs.getString("keluhan_utama"));
        k.setDiagnosa(rs.getString("diagnosa"));
        k.setKodeIcd10(rs.getString("kode_icd10"));
        k.setTekananDarah(rs.getString("tekanan_darah"));
        double berat = rs.getDouble("berat_badan");
        if (!rs.wasNull()) {
            k.setBeratBadan(berat);
        }
        double tinggi = rs.getDouble("tinggi_badan");
        if (!rs.wasNull()) {
            k.setTinggiBadan(tinggi);
        }
        double suhu = rs.getDouble("suhu");
        if (!rs.wasNull()) {
            k.setSuhu(suhu);
        }
        int nadi = rs.getInt("nadi");
        if (!rs.wasNull()) {
            k.setNadi(nadi);
        }
        int saturasi = rs.getInt("saturasi_o2");
        if (!rs.wasNull()) {
            k.setSaturasiO2(saturasi);
        }
        k.setCatatanDokter(rs.getString("catatan_dokter"));
        k.setStatus(rs.getString("status"));
        k.setNamaPasien(rs.getString("nama_pasien"));
        k.setNamaDokter(rs.getString("nama_dokter"));
        k.setStatusBayar(rs.getString("status_bayar"));
        return k;
    }

    private ResepObatItem mapResepRow(ResultSet rs) throws SQLException {
        ResepObatItem item = new ResepObatItem();
        item.setId(rs.getInt("id"));
        item.setKunjunganId(rs.getInt("kunjungan_id"));
        item.setObatId(rs.getInt("obat_id"));
        item.setNamaObat(rs.getString("nama_obat"));
        item.setSatuan(rs.getString("satuan"));
        item.setJumlah(rs.getInt("jumlah"));
        item.setAturanPakai(rs.getString("aturan_pakai"));
        item.setKeterangan(rs.getString("keterangan"));
        return item;
    }
}
