package com.release.klinikgaharumedika.controller;

import com.release.klinikgaharumedika.model.Obat;
import com.release.klinikgaharumedika.model.PageResult;
import com.release.klinikgaharumedika.repository.ObatRepository;
import java.sql.SQLException;

public class ObatController {

    private final ObatRepository repository;

    public ObatController() {
        this.repository = new ObatRepository();
    }

    public PageResult<Obat> loadPage(int page, int pageSize, String keyword) throws SQLException {
        return repository.findAll(page, pageSize, keyword);
    }

    public String generateKodeObat() throws SQLException {
        return repository.generateKodeObat();
    }

    public boolean tambah(Obat o) throws SQLException {
        return repository.insert(o);
    }

    public boolean edit(Obat o) throws SQLException {
        return repository.update(o);
    }

    public boolean hapus(int id) throws SQLException {
        return repository.delete(id);
    }

    public Obat findById(int id) throws SQLException {
        return repository.findById(id);
    }
}
