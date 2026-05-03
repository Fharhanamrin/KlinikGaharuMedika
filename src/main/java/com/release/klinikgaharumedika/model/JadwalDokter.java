package com.release.klinikgaharumedika.model;

import java.time.LocalTime;

public class JadwalDokter {

    private int id;
    private int dokterId;
    private String hari;
    private LocalTime jamMulai;
    private LocalTime jamSelesai;
    private int kuotaPasien;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDokterId() { return dokterId; }
    public void setDokterId(int dokterId) { this.dokterId = dokterId; }

    public String getHari() { return hari; }
    public void setHari(String hari) { this.hari = hari; }

    public LocalTime getJamMulai() { return jamMulai; }
    public void setJamMulai(LocalTime jamMulai) { this.jamMulai = jamMulai; }

    public LocalTime getJamSelesai() { return jamSelesai; }
    public void setJamSelesai(LocalTime jamSelesai) { this.jamSelesai = jamSelesai; }

    public int getKuotaPasien() { return kuotaPasien; }
    public void setKuotaPasien(int kuotaPasien) { this.kuotaPasien = kuotaPasien; }
}
