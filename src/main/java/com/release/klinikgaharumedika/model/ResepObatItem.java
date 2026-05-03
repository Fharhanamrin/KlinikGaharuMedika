package com.release.klinikgaharumedika.model;

public class ResepObatItem {

    private int id;
    private int kunjunganId;
    private int obatId;
    private String namaObat;
    private String satuan;
    private int jumlah;
    private String aturanPakai;
    private String keterangan;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getKunjunganId() {
        return kunjunganId;
    }

    public void setKunjunganId(int kunjunganId) {
        this.kunjunganId = kunjunganId;
    }

    public int getObatId() {
        return obatId;
    }

    public void setObatId(int obatId) {
        this.obatId = obatId;
    }

    public String getNamaObat() {
        return namaObat;
    }

    public void setNamaObat(String namaObat) {
        this.namaObat = namaObat;
    }

    public String getSatuan() {
        return satuan;
    }

    public void setSatuan(String satuan) {
        this.satuan = satuan;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public String getAturanPakai() {
        return aturanPakai;
    }

    public void setAturanPakai(String aturanPakai) {
        this.aturanPakai = aturanPakai;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }
}
