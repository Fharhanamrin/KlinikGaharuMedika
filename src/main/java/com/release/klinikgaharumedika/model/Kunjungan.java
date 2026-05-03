package com.release.klinikgaharumedika.model;

import java.time.LocalDate;

public class Kunjungan {

    private int id;
    private String noAntrian;
    private String noKunjungan;
    private int pasienId;
    private int dokterId;
    private Integer perawatId;
    private LocalDate tanggalKunjungan;
    private String jenisKunjungan;
    private String keluhanUtama;
    private String diagnosa;
    private String kodeIcd10;
    private String tekananDarah;
    private Double beratBadan;
    private Double tinggiBadan;
    private Double suhu;
    private Integer nadi;
    private Integer saturasiO2;
    private String catatanDokter;
    private String status;
    // joined fields
    private String namaPasien;
    private String namaDokter;
    private String statusBayar;

    public Kunjungan() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNoAntrian() { return noAntrian; }
    public void setNoAntrian(String noAntrian) { this.noAntrian = noAntrian; }

    public String getNoKunjungan() { return noKunjungan; }
    public void setNoKunjungan(String noKunjungan) { this.noKunjungan = noKunjungan; }

    public int getPasienId() { return pasienId; }
    public void setPasienId(int pasienId) { this.pasienId = pasienId; }

    public int getDokterId() { return dokterId; }
    public void setDokterId(int dokterId) { this.dokterId = dokterId; }

    public Integer getPerawatId() { return perawatId; }
    public void setPerawatId(Integer perawatId) { this.perawatId = perawatId; }

    public LocalDate getTanggalKunjungan() { return tanggalKunjungan; }
    public void setTanggalKunjungan(LocalDate tanggalKunjungan) { this.tanggalKunjungan = tanggalKunjungan; }

    public String getJenisKunjungan() { return jenisKunjungan; }
    public void setJenisKunjungan(String jenisKunjungan) { this.jenisKunjungan = jenisKunjungan; }

    public String getKeluhanUtama() { return keluhanUtama; }
    public void setKeluhanUtama(String keluhanUtama) { this.keluhanUtama = keluhanUtama; }

    public String getDiagnosa() { return diagnosa; }
    public void setDiagnosa(String diagnosa) { this.diagnosa = diagnosa; }

    public String getKodeIcd10() { return kodeIcd10; }
    public void setKodeIcd10(String kodeIcd10) { this.kodeIcd10 = kodeIcd10; }

    public String getTekananDarah() { return tekananDarah; }
    public void setTekananDarah(String tekananDarah) { this.tekananDarah = tekananDarah; }

    public Double getBeratBadan() { return beratBadan; }
    public void setBeratBadan(Double beratBadan) { this.beratBadan = beratBadan; }

    public Double getTinggiBadan() { return tinggiBadan; }
    public void setTinggiBadan(Double tinggiBadan) { this.tinggiBadan = tinggiBadan; }

    public Double getSuhu() { return suhu; }
    public void setSuhu(Double suhu) { this.suhu = suhu; }

    public Integer getNadi() { return nadi; }
    public void setNadi(Integer nadi) { this.nadi = nadi; }

    public Integer getSaturasiO2() { return saturasiO2; }
    public void setSaturasiO2(Integer saturasiO2) { this.saturasiO2 = saturasiO2; }

    public String getCatatanDokter() { return catatanDokter; }
    public void setCatatanDokter(String catatanDokter) { this.catatanDokter = catatanDokter; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNamaPasien() { return namaPasien; }
    public void setNamaPasien(String namaPasien) { this.namaPasien = namaPasien; }

    public String getNamaDokter() { return namaDokter; }
    public void setNamaDokter(String namaDokter) { this.namaDokter = namaDokter; }

    public String getStatusBayar() { return statusBayar; }
    public void setStatusBayar(String statusBayar) { this.statusBayar = statusBayar; }

    public String getDiagnosaDisplay() {
        if (diagnosa == null) return "-";
        String disp = diagnosa;
        if (kodeIcd10 != null && !kodeIcd10.isBlank()) {
            disp += " (" + kodeIcd10 + ")";
        }
        return disp;
    }
}
