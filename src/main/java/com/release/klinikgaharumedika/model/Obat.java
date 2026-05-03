package com.release.klinikgaharumedika.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Obat {

    private int id;
    private String kodeObat;
    private String namaObat;
    private String jenis;
    private String satuan;
    private String kandunganDosis;
    private BigDecimal hargaBeli;
    private BigDecimal hargaJual;
    private int stokSaatIni;
    private int stokMinimum;
    private LocalDate kadaluarsa;
    private String supplier;
    private String lokasiRak;
    private String keterangan;

    public Obat() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getKodeObat() { return kodeObat; }
    public void setKodeObat(String kodeObat) { this.kodeObat = kodeObat; }

    public String getNamaObat() { return namaObat; }
    public void setNamaObat(String namaObat) { this.namaObat = namaObat; }

    public String getJenis() { return jenis; }
    public void setJenis(String jenis) { this.jenis = jenis; }

    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }

    public String getKandunganDosis() { return kandunganDosis; }
    public void setKandunganDosis(String kandunganDosis) { this.kandunganDosis = kandunganDosis; }

    public BigDecimal getHargaBeli() { return hargaBeli; }
    public void setHargaBeli(BigDecimal hargaBeli) { this.hargaBeli = hargaBeli; }

    public BigDecimal getHargaJual() { return hargaJual; }
    public void setHargaJual(BigDecimal hargaJual) { this.hargaJual = hargaJual; }

    public int getStokSaatIni() { return stokSaatIni; }
    public void setStokSaatIni(int stokSaatIni) { this.stokSaatIni = stokSaatIni; }

    public int getStokMinimum() { return stokMinimum; }
    public void setStokMinimum(int stokMinimum) { this.stokMinimum = stokMinimum; }

    public LocalDate getKadaluarsa() { return kadaluarsa; }
    public void setKadaluarsa(LocalDate kadaluarsa) { this.kadaluarsa = kadaluarsa; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getLokasiRak() { return lokasiRak; }
    public void setLokasiRak(String lokasiRak) { this.lokasiRak = lokasiRak; }

    public String getKeterangan() { return keterangan; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }

    public String getStatusStok() {
        if (stokSaatIni <= 0) return "Kritis";
        if (stokSaatIni <= stokMinimum) return "Menipis";
        return "Aman";
    }
}
