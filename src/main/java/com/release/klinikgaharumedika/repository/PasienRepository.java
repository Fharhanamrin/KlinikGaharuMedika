package com.release.klinikgaharumedika.repository;

import com.release.klinikgaharumedika.config.DatabaseConnection;
import com.release.klinikgaharumedika.model.PageResult;
import com.release.klinikgaharumedika.model.Pasien;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PasienRepository {

    public PageResult<Pasien> findAll(int page, int pageSize, String keyword) throws SQLException {
        int total = countAll(keyword);
        int offset = (page - 1) * pageSize;
        List<Pasien> items = new ArrayList<>();

        String sql = "SELECT * FROM pasien WHERE (nama LIKE ? OR no_rm LIKE ? OR nik LIKE ? OR COALESCE(no_hp, '') LIKE ? OR COALESCE(jenis_pasien, '') LIKE ?) "
                + "ORDER BY no_rm ASC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            ps.setInt(6, pageSize);
            ps.setInt(7, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        }
        return new PageResult<>(items, total, page, pageSize);
    }

    public int countAll(String keyword) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pasien WHERE (nama LIKE ? OR no_rm LIKE ? OR nik LIKE ? OR COALESCE(no_hp, '') LIKE ? OR COALESCE(jenis_pasien, '') LIKE ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public boolean insert(Pasien p) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(buildInsertSql(
                     hasProvinsi(conn),
                     hasKodePos(conn),
                     hasNamaKontakDarurat(conn),
                     hasPekerjaan(conn)
             ))) {
            boolean hasProvinsi = hasProvinsi(conn);
            boolean hasKodePos = hasKodePos(conn);
            boolean hasNamaKontakDarurat = hasNamaKontakDarurat(conn);
            boolean hasPekerjaan = hasPekerjaan(conn);
            String jenisPasien = normalizeJenisPasien(conn, p.getJenisPasien());
            String noBpjs = normalizeNoBpjs(conn, jenisPasien, p.getNoBpjs());

            int index = 1;
            ps.setString(index++, p.getNoRm());
            ps.setString(index++, p.getNik());
            ps.setString(index++, p.getNama());
            ps.setString(index++, p.getJenisKelamin());
            ps.setDate(index++, p.getTanggalLahir() != null ? Date.valueOf(p.getTanggalLahir()) : null);
            ps.setString(index++, p.getTempatLahir());
            ps.setString(index++, p.getGolonganDarah());
            ps.setString(index++, p.getAlamat());
            ps.setString(index++, p.getKelurahan());
            ps.setString(index++, p.getKecamatan());
            ps.setString(index++, p.getKota());
            if (hasProvinsi) {
                ps.setString(index++, p.getProvinsi());
            }
            if (hasKodePos) {
                ps.setString(index++, p.getKodePos());
            }
            ps.setString(index++, p.getNoHp());
            if (hasNamaKontakDarurat) {
                ps.setString(index++, p.getNamaKontakDarurat());
            }
            ps.setString(index++, p.getNoHpDarurat());
            ps.setString(index++, p.getHubunganDarurat());
            if (hasPekerjaan) {
                ps.setString(index++, p.getPekerjaan());
            }
            ps.setString(index++, jenisPasien);
            ps.setString(index++, noBpjs);
            ps.setString(index++, p.getAlergiObat());
            ps.setString(index, p.getRiwayatPenyakit());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Pasien p) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(buildUpdateSql(
                     hasProvinsi(conn),
                     hasKodePos(conn),
                     hasNamaKontakDarurat(conn),
                     hasPekerjaan(conn)
             ))) {
            boolean hasProvinsi = hasProvinsi(conn);
            boolean hasKodePos = hasKodePos(conn);
            boolean hasNamaKontakDarurat = hasNamaKontakDarurat(conn);
            boolean hasPekerjaan = hasPekerjaan(conn);
            String jenisPasien = normalizeJenisPasien(conn, p.getJenisPasien());
            String noBpjs = normalizeNoBpjs(conn, jenisPasien, p.getNoBpjs());

            int index = 1;
            ps.setString(index++, p.getNik());
            ps.setString(index++, p.getNama());
            ps.setString(index++, p.getJenisKelamin());
            ps.setDate(index++, p.getTanggalLahir() != null ? Date.valueOf(p.getTanggalLahir()) : null);
            ps.setString(index++, p.getTempatLahir());
            ps.setString(index++, p.getGolonganDarah());
            ps.setString(index++, p.getAlamat());
            ps.setString(index++, p.getKelurahan());
            ps.setString(index++, p.getKecamatan());
            ps.setString(index++, p.getKota());
            if (hasProvinsi) {
                ps.setString(index++, p.getProvinsi());
            }
            if (hasKodePos) {
                ps.setString(index++, p.getKodePos());
            }
            ps.setString(index++, p.getNoHp());
            if (hasNamaKontakDarurat) {
                ps.setString(index++, p.getNamaKontakDarurat());
            }
            ps.setString(index++, p.getNoHpDarurat());
            ps.setString(index++, p.getHubunganDarurat());
            if (hasPekerjaan) {
                ps.setString(index++, p.getPekerjaan());
            }
            ps.setString(index++, jenisPasien);
            ps.setString(index++, noBpjs);
            ps.setString(index++, p.getAlergiObat());
            ps.setString(index++, p.getRiwayatPenyakit());
            ps.setInt(index, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM pasien WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Pasien findById(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM pasien WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Pasien> findAllForLookup() throws SQLException {
        List<Pasien> items = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM pasien ORDER BY nama ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        }
        return items;
    }

    public String generateNoRm() throws SQLException {
        String sql = "SELECT no_rm FROM pasien ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                String last = rs.getString("no_rm");
                try {
                    int num = Integer.parseInt(last.replaceAll("\\D", ""));
                    return formatNoRm(num + 1);
                } catch (NumberFormatException e) {
                    return formatNoRm(1);
                }
            }
        }
        return formatNoRm(1);
    }

    private String formatNoRm(int sequence) {
        return String.format("RM%07d", sequence);
    }

    private Pasien mapRow(ResultSet rs) throws SQLException {
        Pasien p = new Pasien();
        p.setId(rs.getInt("id"));
        p.setNoRm(rs.getString("no_rm"));
        p.setNik(rs.getString("nik"));
        p.setNama(rs.getString("nama"));
        p.setJenisKelamin(rs.getString("jenis_kelamin"));
        Date tl = rs.getDate("tanggal_lahir");
        if (tl != null) p.setTanggalLahir(tl.toLocalDate());
        p.setTempatLahir(getStringIfPresent(rs, "tempat_lahir"));
        p.setGolonganDarah(getStringIfPresent(rs, "golongan_darah"));
        p.setAlamat(getStringIfPresent(rs, "alamat"));
        p.setKelurahan(getStringIfPresent(rs, "kelurahan"));
        p.setKecamatan(getStringIfPresent(rs, "kecamatan"));
        p.setKota(getStringIfPresent(rs, "kota"));
        p.setProvinsi(getStringIfPresent(rs, "provinsi"));
        p.setKodePos(getStringIfPresent(rs, "kode_pos"));
        p.setNoHp(getStringIfPresent(rs, "no_hp"));
        p.setNamaKontakDarurat(getStringIfPresent(rs, "nama_kontak_darurat"));
        p.setNoHpDarurat(getStringIfPresent(rs, "no_hp_darurat"));
        p.setHubunganDarurat(getStringIfPresent(rs, "hubungan_darurat"));
        p.setPekerjaan(getStringIfPresent(rs, "pekerjaan"));
        p.setJenisPasien(getStringIfPresent(rs, "jenis_pasien"));
        p.setNoBpjs(getStringIfPresent(rs, "no_bpjs"));
        p.setAlergiObat(getStringIfPresent(rs, "alergi_obat"));
        p.setRiwayatPenyakit(getStringIfPresent(rs, "riwayat_penyakit"));
        return p;
    }

    private boolean hasProvinsi(Connection conn) throws SQLException {
        return SchemaSupport.hasColumn(conn, "pasien", "provinsi");
    }

    private boolean hasKodePos(Connection conn) throws SQLException {
        return SchemaSupport.hasColumn(conn, "pasien", "kode_pos");
    }

    private boolean hasNamaKontakDarurat(Connection conn) throws SQLException {
        return SchemaSupport.hasColumn(conn, "pasien", "nama_kontak_darurat");
    }

    private boolean hasPekerjaan(Connection conn) throws SQLException {
        return SchemaSupport.hasColumn(conn, "pasien", "pekerjaan");
    }

    private String buildInsertSql(boolean hasProvinsi, boolean hasKodePos, boolean hasNamaKontakDarurat, boolean hasPekerjaan) {
        return "INSERT INTO pasien (no_rm, nik, nama, jenis_kelamin, tanggal_lahir, tempat_lahir, golongan_darah, alamat, kelurahan, kecamatan, kota"
                + (hasProvinsi ? ", provinsi" : "")
                + (hasKodePos ? ", kode_pos" : "")
                + ", no_hp"
                + (hasNamaKontakDarurat ? ", nama_kontak_darurat" : "")
                + ", no_hp_darurat, hubungan_darurat"
                + (hasPekerjaan ? ", pekerjaan" : "")
                + ", jenis_pasien, no_bpjs, alergi_obat, riwayat_penyakit)"
                + " VALUES (?,?,?,?,?,?,?,?,?,?,?"
                + (hasProvinsi ? ",?" : "")
                + (hasKodePos ? ",?" : "")
                + ",?"
                + (hasNamaKontakDarurat ? ",?" : "")
                + ",?,?"
                + (hasPekerjaan ? ",?" : "")
                + ",?,?,?,?)";
    }

    private String buildUpdateSql(boolean hasProvinsi, boolean hasKodePos, boolean hasNamaKontakDarurat, boolean hasPekerjaan) {
        return "UPDATE pasien SET nik=?, nama=?, jenis_kelamin=?, tanggal_lahir=?, tempat_lahir=?, golongan_darah=?, alamat=?, kelurahan=?, kecamatan=?, kota=?"
                + (hasProvinsi ? ", provinsi=?" : "")
                + (hasKodePos ? ", kode_pos=?" : "")
                + ", no_hp=?"
                + (hasNamaKontakDarurat ? ", nama_kontak_darurat=?" : "")
                + ", no_hp_darurat=?, hubungan_darurat=?"
                + (hasPekerjaan ? ", pekerjaan=?" : "")
                + ", jenis_pasien=?, no_bpjs=?, alergi_obat=?, riwayat_penyakit=? WHERE id=?";
    }

    private String normalizeJenisPasien(Connection conn, String jenisPasien) throws SQLException {
        if (jenisPasien == null || jenisPasien.isBlank()) {
            return "umum";
        }
        if (!hasProvinsi(conn) && "asuransi".equalsIgnoreCase(jenisPasien)) {
            return "umum";
        }
        return jenisPasien;
    }

    private String normalizeNoBpjs(Connection conn, String jenisPasien, String noBpjs) throws SQLException {
        if ("bpjs".equalsIgnoreCase(jenisPasien)) {
            return noBpjs;
        }
        if (hasProvinsi(conn) && "asuransi".equalsIgnoreCase(jenisPasien)) {
            return noBpjs;
        }
        return null;
    }

    private String getStringIfPresent(ResultSet rs, String columnName) throws SQLException {
        return SchemaSupport.hasColumn(rs, columnName) ? rs.getString(columnName) : null;
    }
}
