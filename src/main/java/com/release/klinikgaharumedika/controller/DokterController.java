package com.release.klinikgaharumedika.controller;

import com.release.klinikgaharumedika.model.Dokter;
import com.release.klinikgaharumedika.model.PageResult;
import com.release.klinikgaharumedika.repository.DokterRepository;
import java.sql.SQLException;
import java.util.List;

public class DokterController {

    private final DokterRepository repository;

    public DokterController() {
        this.repository = new DokterRepository();
    }

    public PageResult<Dokter> loadPage(int page, int pageSize, String keyword) throws SQLException {
        return repository.findAll(page, pageSize, keyword);
    }

    public String generateKodeDokter() throws SQLException {
        return repository.generateKodeDokter();
    }

    public boolean tambah(Dokter d) throws SQLException {
        return repository.insert(d);
    }

    public boolean edit(Dokter d) throws SQLException {
        return repository.update(d);
    }

    public boolean hapus(int id) throws SQLException {
        return repository.delete(id);
    }

    public Dokter findById(int id) throws SQLException {
        return repository.findById(id);
    }

    public List<Dokter> findAllAktif() throws SQLException {
        return repository.findAllAktif();
    }
}
