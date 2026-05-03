package com.release.klinikgaharumedika.controller;

import com.release.klinikgaharumedika.model.PageResult;
import com.release.klinikgaharumedika.model.Pembayaran;
import com.release.klinikgaharumedika.repository.PembayaranRepository;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;

public class PembayaranController {

    private final PembayaranRepository repository;

    public PembayaranController() {
        this.repository = new PembayaranRepository();
    }

    public PageResult<Pembayaran> loadPage(int page, int pageSize, String keyword, LocalDate tanggal) throws SQLException {
        return repository.findAll(page, pageSize, keyword, tanggal);
    }

    public int countBelumBayar(LocalDate tanggal) throws SQLException {
        return repository.countBelumBayar(tanggal);
    }

    public BigDecimal sumPendapatan(LocalDate tanggal) throws SQLException {
        return repository.sumPendapatan(tanggal);
    }

    public int countTransaksi(LocalDate tanggal) throws SQLException {
        return repository.countTransaksi(tanggal);
    }

    public boolean bayar(int id, String metode, BigDecimal uangDiterima, Integer petugasId) throws SQLException {
        BigDecimal kembalian = BigDecimal.ZERO;
        Pembayaran pb = repository.findById(id);
        if (pb != null && uangDiterima != null && pb.getTotalTagihan() != null) {
            kembalian = uangDiterima.subtract(pb.getTotalTagihan());
            if (kembalian.compareTo(BigDecimal.ZERO) < 0) kembalian = BigDecimal.ZERO;
        }
        return repository.updateStatus(id, "lunas", metode, uangDiterima, kembalian, petugasId);
    }

    public Pembayaran createPendingForKunjungan(int kunjunganId, Integer petugasId) throws SQLException {
        return repository.createPendingForKunjungan(kunjunganId, petugasId);
    }

    public boolean hapus(int id) throws SQLException {
        return repository.delete(id);
    }

    public Pembayaran findById(int id) throws SQLException {
        return repository.findById(id);
    }
}
