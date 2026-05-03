package com.release.klinikgaharumedika.model;

import java.time.LocalDate;

public class Pasien {

    private int id;
    private String noRm;
    private String nik;
    private String nama;
    private String jenisKelamin;
    private LocalDate tanggalLahir;
    private String tempatLahir;
    private String golonganDarah;
    private String alamat;
    private String kelurahan;
    private String kecamatan;
    private String kota;
    private String provinsi;
    private String kodePos;
    private String noHp;
    private String namaKontakDarurat;
    private String noHpDarurat;
    private String hubunganDarurat;
    private String pekerjaan;
    private String jenisPasien;
    private String noBpjs;
    private String alergiObat;
    private String riwayatPenyakit;

    public Pasien() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNoRm() { return noRm; }
    public void setNoRm(String noRm) { this.noRm = noRm; }

    public String getNik() { return nik; }
    public void setNik(String nik) { this.nik = nik; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getJenisKelamin() { return jenisKelamin; }
    public void setJenisKelamin(String jenisKelamin) { this.jenisKelamin = jenisKelamin; }

    public LocalDate getTanggalLahir() { return tanggalLahir; }
    public void setTanggalLahir(LocalDate tanggalLahir) { this.tanggalLahir = tanggalLahir; }

    public String getTempatLahir() { return tempatLahir; }
    public void setTempatLahir(String tempatLahir) { this.tempatLahir = tempatLahir; }

    public String getGolonganDarah() { return golonganDarah; }
    public void setGolonganDarah(String golonganDarah) { this.golonganDarah = golonganDarah; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public String getKelurahan() { return kelurahan; }
    public void setKelurahan(String kelurahan) { this.kelurahan = kelurahan; }

    public String getKecamatan() { return kecamatan; }
    public void setKecamatan(String kecamatan) { this.kecamatan = kecamatan; }

    public String getKota() { return kota; }
    public void setKota(String kota) { this.kota = kota; }

    public String getProvinsi() { return provinsi; }
    public void setProvinsi(String provinsi) { this.provinsi = provinsi; }

    public String getKodePos() { return kodePos; }
    public void setKodePos(String kodePos) { this.kodePos = kodePos; }

    public String getNoHp() { return noHp; }
    public void setNoHp(String noHp) { this.noHp = noHp; }

    public String getNamaKontakDarurat() { return namaKontakDarurat; }
    public void setNamaKontakDarurat(String namaKontakDarurat) { this.namaKontakDarurat = namaKontakDarurat; }

    public String getNoHpDarurat() { return noHpDarurat; }
    public void setNoHpDarurat(String noHpDarurat) { this.noHpDarurat = noHpDarurat; }

    public String getHubunganDarurat() { return hubunganDarurat; }
    public void setHubunganDarurat(String hubunganDarurat) { this.hubunganDarurat = hubunganDarurat; }

    public String getPekerjaan() { return pekerjaan; }
    public void setPekerjaan(String pekerjaan) { this.pekerjaan = pekerjaan; }

    public String getJenisPasien() { return jenisPasien; }
    public void setJenisPasien(String jenisPasien) { this.jenisPasien = jenisPasien; }

    public String getNoBpjs() { return noBpjs; }
    public void setNoBpjs(String noBpjs) { this.noBpjs = noBpjs; }

    public String getAlergiObat() { return alergiObat; }
    public void setAlergiObat(String alergiObat) { this.alergiObat = alergiObat; }

    public String getRiwayatPenyakit() { return riwayatPenyakit; }
    public void setRiwayatPenyakit(String riwayatPenyakit) { this.riwayatPenyakit = riwayatPenyakit; }

    public String getJenisKelaminDisplay() {
        if (jenisKelamin == null) return "-";
        return jenisKelamin.equalsIgnoreCase("L") ? "L" : "P";
    }
}
