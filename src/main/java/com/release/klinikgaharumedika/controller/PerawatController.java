package com.release.klinikgaharumedika.controller;

import com.release.klinikgaharumedika.model.PageResult;
import com.release.klinikgaharumedika.model.Perawat;
import com.release.klinikgaharumedika.repository.PerawatRepository;
import java.sql.SQLException;

public class PerawatController {

    private final PerawatRepository repository;

    public PerawatController() {
        this.repository = new PerawatRepository();
    }

    public PageResult<Perawat> loadPage(int page, int pageSize, String keyword) throws SQLException {
        return repository.findAll(page, pageSize, keyword);
    }

    public String generateKodePerawat() throws SQLException {
        return repository.generateKodePerawat();
    }

    public boolean tambah(Perawat p) throws SQLException {
        return repository.insert(p);
    }

    public boolean edit(Perawat p) throws SQLException {
        return repository.update(p);
    }

    public boolean hapus(int id) throws SQLException {
        return repository.delete(id);
    }

    public Perawat findById(int id) throws SQLException {
        return repository.findById(id);
    }
}
