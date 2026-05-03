package com.release.klinikgaharumedika.repository;

import com.release.klinikgaharumedika.config.DatabaseConnection;
import com.release.klinikgaharumedika.model.PageResult;
import com.release.klinikgaharumedika.model.Perawat;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PerawatRepository {

    public PageResult<Perawat> findAll(int page, int pageSize, String keyword) throws SQLException {
        int total = countAll(keyword);
        int offset = (page - 1) * pageSize;
        List<Perawat> items = new ArrayList<>();

        String sql = "SELECT id, kode_perawat, nama, no_sipp, shift, poli_tugas, no_hp, status "
                + "FROM perawat WHERE (nama LIKE ? OR no_sipp LIKE ? OR shift LIKE ? OR poli_tugas LIKE ? OR no_hp LIKE ? OR status LIKE ?) "
                + "ORDER BY kode_perawat ASC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            ps.setString(6, like);
            ps.setInt(7, pageSize);
            ps.setInt(8, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) items.add(mapRow(rs));
            }
        }
        return new PageResult<>(items, total, page, pageSize);
    }

    public int countAll(String keyword) throws SQLException {
        String sql = "SELECT COUNT(*) FROM perawat WHERE (nama LIKE ? OR no_sipp LIKE ? OR shift LIKE ? OR poli_tugas LIKE ? OR no_hp LIKE ? OR status LIKE ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            ps.setString(6, like);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public String generateKodePerawat() throws SQLException {
        String sql = "SELECT kode_perawat FROM perawat ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                String last = rs.getString("kode_perawat");
                try {
                    int nomorTerakhir = Integer.parseInt(last.replace("PR", ""));
                    return String.format("PR%03d", nomorTerakhir + 1);
                } catch (NumberFormatException ex) {
                    return "PR001";
                }
            }
        }
        return "PR001";
    }

    public boolean insert(Perawat p) throws SQLException {
        String sql = "INSERT INTO perawat (kode_perawat, nama, no_sipp, shift, poli_tugas, no_hp, status) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getKodePerawat()); ps.setString(2, p.getNama());
            ps.setString(3, p.getNoSipp()); ps.setString(4, p.getShift());
            ps.setString(5, p.getPoliTugas()); ps.setString(6, p.getNoHp());
            ps.setString(7, p.getStatus());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Perawat p) throws SQLException {
        String sql = "UPDATE perawat SET nama=?, no_sipp=?, shift=?, poli_tugas=?, no_hp=?, status=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNama()); ps.setString(2, p.getNoSipp());
            ps.setString(3, p.getShift()); ps.setString(4, p.getPoliTugas());
            ps.setString(5, p.getNoHp()); ps.setString(6, p.getStatus());
            ps.setInt(7, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM perawat WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Perawat findById(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM perawat WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    private Perawat mapRow(ResultSet rs) throws SQLException {
        Perawat p = new Perawat();
        p.setId(rs.getInt("id"));
        p.setKodePerawat(rs.getString("kode_perawat"));
        p.setNama(rs.getString("nama"));
        p.setNoSipp(rs.getString("no_sipp"));
        p.setShift(rs.getString("shift"));
        p.setPoliTugas(rs.getString("poli_tugas"));
        p.setNoHp(rs.getString("no_hp"));
        p.setStatus(rs.getString("status"));
        return p;
    }
}
