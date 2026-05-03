package com.release.klinikgaharumedika.controller;

import com.release.klinikgaharumedika.model.Kunjungan;
import com.release.klinikgaharumedika.model.Obat;
import com.release.klinikgaharumedika.model.PageResult;
import com.release.klinikgaharumedika.model.Pasien;
import com.release.klinikgaharumedika.model.Dokter;
import com.release.klinikgaharumedika.model.ResepObatItem;
import com.release.klinikgaharumedika.repository.DokterRepository;
import com.release.klinikgaharumedika.repository.KunjunganRepository;
import com.release.klinikgaharumedika.repository.ObatRepository;
import com.release.klinikgaharumedika.repository.PasienRepository;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class KunjunganController {

    private final KunjunganRepository repository;
    private final PasienRepository pasienRepository;
    private final DokterRepository dokterRepository;
    private final ObatRepository obatRepository;

    public KunjunganController() {
        this.repository = new KunjunganRepository();
        this.pasienRepository = new PasienRepository();
        this.dokterRepository = new DokterRepository();
        this.obatRepository = new ObatRepository();
    }

    public PageResult<Kunjungan> loadPage(int page, int pageSize, String keyword) throws SQLException {
        return repository.findAll(page, pageSize, keyword);
    }

    public boolean tambah(Kunjungan k) throws SQLException {
        return tambah(k, List.of());
    }

    public boolean tambah(Kunjungan k, List<ResepObatItem> resepItems) throws SQLException {
        LocalDate tanggalKunjungan = k.getTanggalKunjungan() != null ? k.getTanggalKunjungan() : LocalDate.now();
        k.setTanggalKunjungan(tanggalKunjungan);
        k.setNoAntrian(repository.generateNoAntrian(tanggalKunjungan));
        k.setNoKunjungan(repository.generateNoKunjungan(tanggalKunjungan));
        if (k.getStatus() == null || k.getStatus().isBlank()) {
            k.setStatus("menunggu");
        }
        return repository.insert(k, resepItems);
    }

    public boolean edit(Kunjungan k) throws SQLException {
        return edit(k, List.of());
    }

    public boolean edit(Kunjungan k, List<ResepObatItem> resepItems) throws SQLException {
        return repository.update(k, resepItems);
    }

    public boolean hapus(int id) throws SQLException {
        return repository.delete(id);
    }

    public Kunjungan findById(int id) throws SQLException {
        return repository.findById(id);
    }

    public List<ResepObatItem> findResepByKunjunganId(int kunjunganId) throws SQLException {
        return repository.findResepByKunjunganId(kunjunganId);
    }

    public List<Pasien> findAllPasien() throws SQLException {
        return pasienRepository.findAllForLookup();
    }

    public List<Dokter> findAllDokterAktif() throws SQLException {
        return dokterRepository.findAllAktif();
    }

    public List<Obat> findAllObat() throws SQLException {
        return obatRepository.findAllForLookup();
    }
}
