package com.release.klinikgaharumedika.model;

public class Perawat {

    private int id;
    private String kodePerawat;
    private String nama;
    private String noSipp;
    private String shift;
    private String poliTugas;
    private String noHp;
    private String status;

    public Perawat() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getKodePerawat() { return kodePerawat; }
    public void setKodePerawat(String kodePerawat) { this.kodePerawat = kodePerawat; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getNoSipp() { return noSipp; }
    public void setNoSipp(String noSipp) { this.noSipp = noSipp; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public String getPoliTugas() { return poliTugas; }
    public void setPoliTugas(String poliTugas) { this.poliTugas = poliTugas; }

    public String getNoHp() { return noHp; }
    public void setNoHp(String noHp) { this.noHp = noHp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getShiftDisplay() {
        if (shift == null) return "-";
        switch (shift.toLowerCase()) {
            case "pagi": return "Pagi (07-14)";
            case "sore": return "Sore (14-21)";
            case "malam": return "Malam (21-07)";
            default: return shift;
        }
    }
}
