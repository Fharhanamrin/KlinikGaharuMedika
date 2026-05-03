package com.release.klinikgaharumedika.repository;

import com.release.klinikgaharumedika.config.DatabaseConnection;
import com.release.klinikgaharumedika.model.Obat;
import com.release.klinikgaharumedika.model.PageResult;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ObatRepository {

    public PageResult<Obat> findAll(int page, int pageSize, String keyword) throws SQLException {
        int total = countAll(keyword);
        int offset = (page - 1) * pageSize;
        List<Obat> items = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(buildFindAllSql(
                     hasKandunganDosis(conn),
                     hasSupplier(conn)
             ))) {
            boolean hasKandunganDosis = hasKandunganDosis(conn);
            boolean hasSupplier = hasSupplier(conn);
            String like = "%" + keyword + "%";
            int index = 1;
            ps.setString(index++, like);
            ps.setString(index++, like);
            ps.setString(index++, like);
            ps.setString(index++, like);
            if (hasKandunganDosis) {
                ps.setString(index++, like);
            }
            if (hasSupplier) {
                ps.setString(index++, like);
            }
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
             PreparedStatement ps = conn.prepareStatement(buildCountSql(
                     hasKandunganDosis(conn),
                     hasSupplier(conn)
             ))) {
            boolean hasKandunganDosis = hasKandunganDosis(conn);
            boolean hasSupplier = hasSupplier(conn);
            String like = "%" + keyword + "%";
            int index = 1;
            ps.setString(index++, like);
            ps.setString(index++, like);
            ps.setString(index++, like);
            ps.setString(index++, like);
            if (hasKandunganDosis) {
                ps.setString(index++, like);
            }
            if (hasSupplier) {
                ps.setString(index++, like);
            }
            ps.setString(index, like);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public boolean insert(Obat o) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(buildInsertSql(
                     hasKandunganDosis(conn),
                     hasSupplier(conn),
                     hasKeterangan(conn)
             ))) {
            boolean hasKandunganDosis = hasKandunganDosis(conn);
            boolean hasSupplier = hasSupplier(conn);
            boolean hasKeterangan = hasKeterangan(conn);
            int index = 1;
            ps.setString(index++, o.getKodeObat());
            ps.setString(index++, o.getNamaObat());
            ps.setString(index++, o.getJenis());
            ps.setString(index++, o.getSatuan());
            if (hasKandunganDosis) {
                ps.setString(index++, o.getKandunganDosis());
            }
            ps.setBigDecimal(index++, safeAmount(o.getHargaBeli()));
            ps.setBigDecimal(index++, safeAmount(o.getHargaJual()));
            ps.setInt(index++, o.getStokSaatIni());
            ps.setInt(index++, o.getStokMinimum());
            ps.setDate(index++, o.getKadaluarsa() != null ? Date.valueOf(o.getKadaluarsa()) : null);
            if (hasSupplier) {
                ps.setString(index++, o.getSupplier());
            }
            ps.setString(index++, o.getLokasiRak());
            if (hasKeterangan) {
                ps.setString(index++, o.getKeterangan());
            }
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Obat o) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(buildUpdateSql(
                     hasKandunganDosis(conn),
                     hasSupplier(conn),
                     hasKeterangan(conn)
             ))) {
            boolean hasKandunganDosis = hasKandunganDosis(conn);
            boolean hasSupplier = hasSupplier(conn);
            boolean hasKeterangan = hasKeterangan(conn);
            int index = 1;
            ps.setString(index++, o.getNamaObat());
            ps.setString(index++, o.getJenis());
            ps.setString(index++, o.getSatuan());
            if (hasKandunganDosis) {
                ps.setString(index++, o.getKandunganDosis());
            }
            ps.setBigDecimal(index++, safeAmount(o.getHargaBeli()));
            ps.setBigDecimal(index++, safeAmount(o.getHargaJual()));
            ps.setInt(index++, o.getStokSaatIni());
            ps.setInt(index++, o.getStokMinimum());
            ps.setDate(index++, o.getKadaluarsa() != null ? Date.valueOf(o.getKadaluarsa()) : null);
            if (hasSupplier) {
                ps.setString(index++, o.getSupplier());
            }
            ps.setString(index++, o.getLokasiRak());
            if (hasKeterangan) {
                ps.setString(index++, o.getKeterangan());
            }
            ps.setInt(index, o.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM obat WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Obat findById(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM obat WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Obat> findAllForLookup() throws SQLException {
        List<Obat> items = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM obat ORDER BY nama_obat ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        }
        return items;
    }

    public String generateKodeObat() throws SQLException {
        String sql = "SELECT kode_obat FROM obat ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                String last = rs.getString("kode_obat");
                try {
                    int nomorTerakhir = Integer.parseInt(last.replace("OB", ""));
                    return String.format("OB%03d", nomorTerakhir + 1);
                } catch (NumberFormatException ex) {
                    return "OB001";
                }
            }
        }
        return "OB001";
    }

    private Obat mapRow(ResultSet rs) throws SQLException {
        Obat o = new Obat();
        o.setId(rs.getInt("id"));
        o.setKodeObat(rs.getString("kode_obat"));
        o.setNamaObat(rs.getString("nama_obat"));
        o.setJenis(rs.getString("jenis"));
        o.setSatuan(rs.getString("satuan"));
        o.setKandunganDosis(getStringIfPresent(rs, "kandungan_dosis"));
        BigDecimal hb = rs.getBigDecimal("harga_beli");
        o.setHargaBeli(hb != null ? hb : BigDecimal.ZERO);
        BigDecimal hj = rs.getBigDecimal("harga_jual");
        o.setHargaJual(hj != null ? hj : BigDecimal.ZERO);
        o.setStokSaatIni(rs.getInt("stok_saat_ini"));
        o.setStokMinimum(rs.getInt("stok_minimum"));
        Date kd = rs.getDate("kadaluarsa");
        if (kd != null) o.setKadaluarsa(kd.toLocalDate());
        o.setSupplier(getStringIfPresent(rs, "supplier"));
        o.setLokasiRak(rs.getString("lokasi_rak"));
        o.setKeterangan(getStringIfPresent(rs, "keterangan"));
        return o;
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private boolean hasKandunganDosis(Connection conn) throws SQLException {
        return SchemaSupport.hasColumn(conn, "obat", "kandungan_dosis");
    }

    private boolean hasSupplier(Connection conn) throws SQLException {
        return SchemaSupport.hasColumn(conn, "obat", "supplier");
    }

    private boolean hasKeterangan(Connection conn) throws SQLException {
        return SchemaSupport.hasColumn(conn, "obat", "keterangan");
    }

    private String buildFindAllSql(boolean hasKandunganDosis, boolean hasSupplier) {
        return "SELECT * FROM obat WHERE (kode_obat LIKE ? OR nama_obat LIKE ? OR jenis LIKE ? OR satuan LIKE ?"
                + (hasKandunganDosis ? " OR COALESCE(kandungan_dosis, '') LIKE ?" : "")
                + (hasSupplier ? " OR COALESCE(supplier, '') LIKE ?" : "")
                + " OR COALESCE(lokasi_rak, '') LIKE ?)"
                + " ORDER BY kode_obat ASC LIMIT ? OFFSET ?";
    }

    private String buildCountSql(boolean hasKandunganDosis, boolean hasSupplier) {
        return "SELECT COUNT(*) FROM obat WHERE (kode_obat LIKE ? OR nama_obat LIKE ? OR jenis LIKE ? OR satuan LIKE ?"
                + (hasKandunganDosis ? " OR COALESCE(kandungan_dosis, '') LIKE ?" : "")
                + (hasSupplier ? " OR COALESCE(supplier, '') LIKE ?" : "")
                + " OR COALESCE(lokasi_rak, '') LIKE ?)";
    }

    private String buildInsertSql(boolean hasKandunganDosis, boolean hasSupplier, boolean hasKeterangan) {
        return "INSERT INTO obat (kode_obat, nama_obat, jenis, satuan"
                + (hasKandunganDosis ? ", kandungan_dosis" : "")
                + ", harga_beli, harga_jual, stok_saat_ini, stok_minimum, kadaluarsa"
                + (hasSupplier ? ", supplier" : "")
                + ", lokasi_rak"
                + (hasKeterangan ? ", keterangan" : "")
                + ") VALUES (?,?,?,?"
                + (hasKandunganDosis ? ",?" : "")
                + ",?,?,?,?,?"
                + (hasSupplier ? ",?" : "")
                + ",?"
                + (hasKeterangan ? ",?" : "")
                + ")";
    }

    private String buildUpdateSql(boolean hasKandunganDosis, boolean hasSupplier, boolean hasKeterangan) {
        return "UPDATE obat SET nama_obat=?, jenis=?, satuan=?"
                + (hasKandunganDosis ? ", kandungan_dosis=?" : "")
                + ", harga_beli=?, harga_jual=?, stok_saat_ini=?, stok_minimum=?, kadaluarsa=?"
                + (hasSupplier ? ", supplier=?" : "")
                + ", lokasi_rak=?"
                + (hasKeterangan ? ", keterangan=?" : "")
                + " WHERE id=?";
    }

    private String getStringIfPresent(ResultSet rs, String columnName) throws SQLException {
        return SchemaSupport.hasColumn(rs, columnName) ? rs.getString(columnName) : null;
    }
}
