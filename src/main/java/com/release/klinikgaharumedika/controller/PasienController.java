package com.release.klinikgaharumedika.controller;

import com.release.klinikgaharumedika.model.PageResult;
import com.release.klinikgaharumedika.model.Pasien;
import com.release.klinikgaharumedika.repository.PasienRepository;
import java.sql.SQLException;

public class PasienController {

    private final PasienRepository repository;

    public PasienController() {
        this.repository = new PasienRepository();
    }

    public PageResult<Pasien> loadPage(int page, int pageSize, String keyword) throws SQLException {
        return repository.findAll(page, pageSize, keyword);
    }

    public boolean tambah(Pasien p) throws SQLException {
        p.setNoRm(repository.generateNoRm());
        return repository.insert(p);
    }

    public boolean edit(Pasien p) throws SQLException {
        return repository.update(p);
    }

    public boolean hapus(int id) throws SQLException {
        return repository.delete(id);
    }

    public Pasien findById(int id) throws SQLException {
        return repository.findById(id);
    }
}
