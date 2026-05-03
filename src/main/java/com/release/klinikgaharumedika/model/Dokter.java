package com.release.klinikgaharumedika.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Dokter {

    private int id;
    private String kodeDokter;
    private String nama;
    private String spesialisasi;
    private String poliUnit;
    private String noStr;
    private String noSip;
    private String noHp;
    private BigDecimal tarifKonsultasi;
    private String status;
    private List<JadwalDokter> jadwalPraktik = new ArrayList<>();

    public Dokter() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getKodeDokter() { return kodeDokter; }
    public void setKodeDokter(String kodeDokter) { this.kodeDokter = kodeDokter; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getSpesialisasi() { return spesialisasi; }
    public void setSpesialisasi(String spesialisasi) { this.spesialisasi = spesialisasi; }

    public String getPoliUnit() { return poliUnit; }
    public void setPoliUnit(String poliUnit) { this.poliUnit = poliUnit; }

    public String getNoStr() { return noStr; }
    public void setNoStr(String noStr) { this.noStr = noStr; }

    public String getNoSip() { return noSip; }
    public void setNoSip(String noSip) { this.noSip = noSip; }

    public String getNoHp() { return noHp; }
    public void setNoHp(String noHp) { this.noHp = noHp; }

    public BigDecimal getTarifKonsultasi() { return tarifKonsultasi; }
    public void setTarifKonsultasi(BigDecimal tarifKonsultasi) { this.tarifKonsultasi = tarifKonsultasi; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<JadwalDokter> getJadwalPraktik() { return jadwalPraktik; }
    public void setJadwalPraktik(List<JadwalDokter> jadwalPraktik) {
        this.jadwalPraktik = jadwalPraktik != null ? jadwalPraktik : new ArrayList<>();
    }

    public String getPoli() {
        if (poliUnit != null && !poliUnit.isBlank()) {
            return poliUnit;
        }
        if (spesialisasi == null) return "-";
        return "Poli " + spesialisasi;
    }
}
