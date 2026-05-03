package com.release.klinikgaharumedika.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Pembayaran {

    private int id;
    private String noInvoice;
    private int kunjunganId;
    private LocalDate tanggalBayar;
    private BigDecimal biayaKonsultasi;
    private BigDecimal biayaObat;
    private BigDecimal totalTagihan;
    private String metodeBayar;
    private BigDecimal uangDiterima;
    private BigDecimal kembalian;
    private String status;
    private Integer petugasId;
    // joined fields
    private String namaPasien;
    private String noAntrian;

    public Pembayaran() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNoInvoice() { return noInvoice; }
    public void setNoInvoice(String noInvoice) { this.noInvoice = noInvoice; }

    public int getKunjunganId() { return kunjunganId; }
    public void setKunjunganId(int kunjunganId) { this.kunjunganId = kunjunganId; }

    public LocalDate getTanggalBayar() { return tanggalBayar; }
    public void setTanggalBayar(LocalDate tanggalBayar) { this.tanggalBayar = tanggalBayar; }

    public BigDecimal getBiayaKonsultasi() { return biayaKonsultasi; }
    public void setBiayaKonsultasi(BigDecimal biayaKonsultasi) { this.biayaKonsultasi = biayaKonsultasi; }

    public BigDecimal getBiayaObat() { return biayaObat; }
    public void setBiayaObat(BigDecimal biayaObat) { this.biayaObat = biayaObat; }

    public BigDecimal getTotalTagihan() { return totalTagihan; }
    public void setTotalTagihan(BigDecimal totalTagihan) { this.totalTagihan = totalTagihan; }

    public String getMetodeBayar() { return metodeBayar; }
    public void setMetodeBayar(String metodeBayar) { this.metodeBayar = metodeBayar; }

    public BigDecimal getUangDiterima() { return uangDiterima; }
    public void setUangDiterima(BigDecimal uangDiterima) { this.uangDiterima = uangDiterima; }

    public BigDecimal getKembalian() { return kembalian; }
    public void setKembalian(BigDecimal kembalian) { this.kembalian = kembalian; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getPetugasId() { return petugasId; }
    public void setPetugasId(Integer petugasId) { this.petugasId = petugasId; }

    public String getNamaPasien() { return namaPasien; }
    public void setNamaPasien(String namaPasien) { this.namaPasien = namaPasien; }

    public String getNoAntrian() { return noAntrian; }
    public void setNoAntrian(String noAntrian) { this.noAntrian = noAntrian; }

    public String getMetodeBayarDisplay() {
        if (metodeBayar == null) return "-";
        switch (metodeBayar.toLowerCase()) {
            case "tunai": return "Tunai";
            case "transfer": return "Transfer";
            case "bpjs": return "BPJS";
            default: return metodeBayar;
        }
    }
}
