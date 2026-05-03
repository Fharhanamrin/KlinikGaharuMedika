package com.release.klinikgaharumedika.repository;

import com.release.klinikgaharumedika.config.DatabaseConnection;
import com.release.klinikgaharumedika.model.PageResult;
import com.release.klinikgaharumedika.model.Pembayaran;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PembayaranRepository {

    private static final DateTimeFormatter INVOICE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");

    public PageResult<Pembayaran> findAll(int page, int pageSize, String keyword, LocalDate tanggal) throws SQLException {
        int total = countAll(keyword, tanggal);
        int offset = (page - 1) * pageSize;
        List<Pembayaran> items = new ArrayList<>();

        String sql = "SELECT pb.id, pb.no_invoice, pb.kunjungan_id, pb.tanggal_bayar, pb.biaya_konsultasi, "
                + "pb.biaya_obat, pb.total_tagihan, pb.metode_bayar, pb.uang_diterima, pb.kembalian, pb.status, pb.petugas_id, "
                + "p.nama AS nama_pasien, k.no_antrian "
                + "FROM pembayaran pb "
                + "JOIN kunjungan k ON pb.kunjungan_id = k.id "
                + "JOIN pasien p ON k.pasien_id = p.id "
                + "WHERE (p.nama LIKE ? OR pb.no_invoice LIKE ?) "
                + (tanggal != null ? "AND DATE(pb.tanggal_bayar) = ? " : "")
                + "ORDER BY pb.tanggal_bayar DESC, pb.id DESC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like); ps.setString(2, like);
            int idx = 3;
            if (tanggal != null) { ps.setDate(idx++, Date.valueOf(tanggal)); }
            ps.setInt(idx++, pageSize); ps.setInt(idx, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) items.add(mapRow(rs));
            }
        }
        return new PageResult<>(items, total, page, pageSize);
    }

    public int countAll(String keyword, LocalDate tanggal) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pembayaran pb "
                + "JOIN kunjungan k ON pb.kunjungan_id = k.id "
                + "JOIN pasien p ON k.pasien_id = p.id "
                + "WHERE (p.nama LIKE ? OR pb.no_invoice LIKE ?) "
                + (tanggal != null ? "AND DATE(pb.tanggal_bayar) = ? " : "");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like); ps.setString(2, like);
            if (tanggal != null) ps.setDate(3, Date.valueOf(tanggal));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countBelumBayar(LocalDate tanggal) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pembayaran WHERE status='pending'"
                + (tanggal != null ? " AND DATE(tanggal_bayar)=?" : "");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (tanggal != null) ps.setDate(1, Date.valueOf(tanggal));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public BigDecimal sumPendapatan(LocalDate tanggal) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_tagihan),0) FROM pembayaran WHERE status='lunas'"
                + (tanggal != null ? " AND DATE(tanggal_bayar)=?" : "");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (tanggal != null) ps.setDate(1, Date.valueOf(tanggal));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        }
        return BigDecimal.ZERO;
    }

    public int countTransaksi(LocalDate tanggal) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pembayaran"
                + (tanggal != null ? " WHERE DATE(tanggal_bayar)=?" : "");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (tanggal != null) ps.setDate(1, Date.valueOf(tanggal));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public boolean insert(Pembayaran pb) throws SQLException {
        String sql = "INSERT INTO pembayaran (no_invoice, kunjungan_id, tanggal_bayar, biaya_konsultasi, biaya_obat, total_tagihan, metode_bayar, uang_diterima, kembalian, status, petugas_id) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pb.getNoInvoice()); ps.setInt(2, pb.getKunjunganId());
            ps.setDate(3, pb.getTanggalBayar() != null ? Date.valueOf(pb.getTanggalBayar()) : null);
            ps.setBigDecimal(4, pb.getBiayaKonsultasi()); ps.setBigDecimal(5, pb.getBiayaObat());
            ps.setBigDecimal(6, pb.getTotalTagihan()); ps.setString(7, pb.getMetodeBayar());
            ps.setBigDecimal(8, pb.getUangDiterima()); ps.setBigDecimal(9, pb.getKembalian());
            ps.setString(10, pb.getStatus());
            if (pb.getPetugasId() != null) ps.setInt(11, pb.getPetugasId()); else ps.setNull(11, Types.INTEGER);
            return ps.executeUpdate() > 0;
        }
    }

    public Pembayaran findByKunjunganId(int kunjunganId) throws SQLException {
        String sql = "SELECT pb.*, p.nama AS nama_pasien, k.no_antrian FROM pembayaran pb "
                + "JOIN kunjungan k ON pb.kunjungan_id=k.id JOIN pasien p ON k.pasien_id=p.id WHERE pb.kunjungan_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, kunjunganId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public Pembayaran createPendingForKunjungan(int kunjunganId, Integer petugasId) throws SQLException {
        Pembayaran existing = findByKunjunganId(kunjunganId);
        if (existing != null) {
            if ("pending".equalsIgnoreCase(existing.getStatus())) {
                Pembayaran recalculated = buildTagihan(kunjunganId);
                updatePendingTagihan(existing.getId(), recalculated, petugasId);
                return findById(existing.getId());
            }
            return existing;
        }

        Pembayaran pb = buildTagihan(kunjunganId);
        pb.setNoInvoice(generateNoInvoice(pb.getTanggalBayar()));
        pb.setMetodeBayar("tunai");
        pb.setStatus("pending");
        pb.setPetugasId(petugasId);
        if (!insert(pb)) {
            throw new SQLException("Gagal membuat invoice pembayaran.");
        }
        return findByKunjunganId(kunjunganId);
    }

    private void updatePendingTagihan(int pembayaranId, Pembayaran pb, Integer petugasId) throws SQLException {
        String sql = "UPDATE pembayaran SET tanggal_bayar=?, biaya_konsultasi=?, biaya_obat=?, total_tagihan=?, petugas_id=? "
                + "WHERE id=? AND status='pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, pb.getTanggalBayar() != null ? Date.valueOf(pb.getTanggalBayar()) : Date.valueOf(LocalDate.now()));
            ps.setBigDecimal(2, pb.getBiayaKonsultasi());
            ps.setBigDecimal(3, pb.getBiayaObat());
            ps.setBigDecimal(4, pb.getTotalTagihan());
            if (petugasId != null) ps.setInt(5, petugasId); else ps.setNull(5, Types.INTEGER);
            ps.setInt(6, pembayaranId);
            ps.executeUpdate();
        }
    }

    private Pembayaran buildTagihan(int kunjunganId) throws SQLException {
        String sql = "SELECT k.id AS kunjungan_id, k.tanggal_kunjungan, "
                + "COALESCE(d.tarif_konsultasi, 0) AS biaya_konsultasi, "
                + "COALESCE(SUM(ro.jumlah * o.harga_jual), 0) AS biaya_obat "
                + "FROM kunjungan k "
                + "JOIN dokter d ON d.id = k.dokter_id "
                + "LEFT JOIN resep_obat ro ON ro.kunjungan_id = k.id "
                + "LEFT JOIN obat o ON o.id = ro.obat_id "
                + "WHERE k.id = ? "
                + "GROUP BY k.id, k.tanggal_kunjungan, d.tarif_konsultasi";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, kunjunganId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Data kunjungan tidak ditemukan untuk pembuatan invoice.");
                }
                BigDecimal biayaKonsultasi = defaultZero(rs.getBigDecimal("biaya_konsultasi"));
                BigDecimal biayaObat = defaultZero(rs.getBigDecimal("biaya_obat"));
                Pembayaran pb = new Pembayaran();
                pb.setKunjunganId(rs.getInt("kunjungan_id"));
                Date tanggal = rs.getDate("tanggal_kunjungan");
                pb.setTanggalBayar(tanggal != null ? tanggal.toLocalDate() : LocalDate.now());
                pb.setBiayaKonsultasi(biayaKonsultasi);
                pb.setBiayaObat(biayaObat);
                pb.setTotalTagihan(biayaKonsultasi.add(biayaObat));
                return pb;
            }
        }
    }

    private String generateNoInvoice(LocalDate tanggal) throws SQLException {
        LocalDate invoiceDate = tanggal != null ? tanggal : LocalDate.now();
        String prefix = "INV" + invoiceDate.format(INVOICE_DATE_FORMAT);
        String sql = "SELECT COUNT(*) FROM pembayaran WHERE no_invoice LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                int sequence = rs.next() ? rs.getInt(1) + 1 : 1;
                return prefix + String.format("%03d", sequence);
            }
        }
    }

    public boolean updateStatus(int id, String status, String metodeBayar, BigDecimal uangDiterima, BigDecimal kembalian, Integer petugasId) throws SQLException {
        String sql = "UPDATE pembayaran SET status=?, metode_bayar=?, uang_diterima=?, kembalian=?, tanggal_bayar=NOW(), petugas_id=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status); ps.setString(2, metodeBayar);
            ps.setBigDecimal(3, uangDiterima); ps.setBigDecimal(4, kembalian);
            if (petugasId != null) ps.setInt(5, petugasId); else ps.setNull(5, Types.INTEGER);
            ps.setInt(6, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM pembayaran WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Pembayaran findById(int id) throws SQLException {
        String sql = "SELECT pb.*, p.nama AS nama_pasien, k.no_antrian FROM pembayaran pb "
                + "JOIN kunjungan k ON pb.kunjungan_id=k.id JOIN pasien p ON k.pasien_id=p.id WHERE pb.id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private Pembayaran mapRow(ResultSet rs) throws SQLException {
        Pembayaran pb = new Pembayaran();
        pb.setId(rs.getInt("id"));
        pb.setNoInvoice(rs.getString("no_invoice"));
        pb.setKunjunganId(rs.getInt("kunjungan_id"));
        Date tgl = rs.getDate("tanggal_bayar");
        if (tgl != null) pb.setTanggalBayar(tgl.toLocalDate());
        pb.setBiayaKonsultasi(rs.getBigDecimal("biaya_konsultasi"));
        pb.setBiayaObat(rs.getBigDecimal("biaya_obat"));
        pb.setTotalTagihan(rs.getBigDecimal("total_tagihan"));
        pb.setMetodeBayar(rs.getString("metode_bayar"));
        pb.setUangDiterima(rs.getBigDecimal("uang_diterima"));
        pb.setKembalian(rs.getBigDecimal("kembalian"));
        pb.setStatus(rs.getString("status"));
        int pid = rs.getInt("petugas_id");
        if (!rs.wasNull()) pb.setPetugasId(pid);
        pb.setNamaPasien(rs.getString("nama_pasien"));
        pb.setNoAntrian(rs.getString("no_antrian"));
        return pb;
    }
}
