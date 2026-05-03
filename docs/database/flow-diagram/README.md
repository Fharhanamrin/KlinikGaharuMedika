# Alur Data — db_gaharu_medika
> Klinik Gaharu Medika — Database Flow Documentation

---

## Ringkasan 10 Tabel

| # | Tabel | Kategori | Peran |
|---|-------|----------|-------|
| 1 | `users` | Master | Pengguna sistem: admin, loket, farmasi, kasir |
| 2 | `pasien` | Master | Data pasien + no_rm, NIK, BPJS |
| 3 | `dokter` | Master | Data dokter + tarif konsultasi |
| 4 | `jadwal_dokter` | Master | Jadwal praktek dokter per hari |
| 5 | `perawat` | Master | Data perawat + shift + poli |
| 6 | `obat` | Master | Inventori obat + stok |
| 7 | `kunjungan` | Transaksi | Inti kunjungan: antrian, vital signs, diagnosa |
| 8 | `resep_obat` | Transaksi | Detail resep obat per kunjungan |
| 9 | `pembayaran` | Transaksi | Invoice & pelunasan biaya |
| 10 | `stok_log` | Audit | Riwayat mutasi stok obat (masuk/keluar/adjustment) |

---

## Entity Relationship Diagram

```mermaid
erDiagram
    users {
        int id PK
        varchar_50 username UK "NOT NULL"
        varchar_255 password_hash "NOT NULL"
        varchar_100 nama_lengkap "NOT NULL"
        enum role "admin | petugas — DEFAULT petugas"
        tinyint is_active "DEFAULT 1"
        datetime created_at "DEFAULT CURRENT_TIMESTAMP"
    }

    pasien {
        int id PK
        varchar_10 no_rm UK "NOT NULL"
        char_16 nik UK "NOT NULL"
        varchar_100 nama "NOT NULL"
        enum jenis_kelamin "L | P — NOT NULL"
        date tanggal_lahir "NOT NULL"
        varchar_60 tempat_lahir
        enum golongan_darah "A+ | A- | B+ | B- | AB+ | AB- | O+ | O- | Tidak Diketahui"
        text alamat
        varchar_60 kelurahan
        varchar_60 kecamatan
        varchar_60 kota
        varchar_20 no_hp
        varchar_20 no_hp_darurat
        varchar_30 hubungan_darurat
        text alergi_obat
        text riwayat_penyakit
        enum jenis_pasien "umum | bpjs — DEFAULT umum"
        varchar_20 no_bpjs
        datetime created_at "DEFAULT CURRENT_TIMESTAMP"
        datetime updated_at "ON UPDATE CURRENT_TIMESTAMP"
    }

    dokter {
        int id PK
        varchar_10 kode_dokter UK "NOT NULL"
        varchar_100 nama "NOT NULL"
        varchar_60 spesialisasi "NOT NULL"
        varchar_30 no_str "NOT NULL"
        varchar_30 no_sip "NOT NULL"
        varchar_20 no_hp
        decimal_10 tarif_konsultasi "NOT NULL DEFAULT 0"
        enum status "aktif | nonaktif — DEFAULT aktif"
        datetime created_at "DEFAULT CURRENT_TIMESTAMP"
    }

    jadwal_dokter {
        int id PK
        int dokter_id FK "NOT NULL"
        enum hari "Senin | Selasa | Rabu | Kamis | Jumat | Sabtu"
        time jam_mulai "NOT NULL"
        time jam_selesai "NOT NULL"
        int kuota_pasien "DEFAULT 20"
    }

    perawat {
        int id PK
        varchar_10 kode_perawat UK "NOT NULL"
        varchar_100 nama "NOT NULL"
        varchar_30 no_sipp "NOT NULL"
        enum shift "pagi | sore | malam — NOT NULL"
        varchar_60 poli_tugas
        varchar_20 no_hp
        enum status "aktif | nonaktif | cuti — DEFAULT aktif"
        datetime created_at "DEFAULT CURRENT_TIMESTAMP"
    }

    obat {
        int id PK
        varchar_10 kode_obat UK "NOT NULL"
        varchar_100 nama_obat "NOT NULL"
        varchar_40 jenis "NOT NULL"
        varchar_20 satuan "NOT NULL"
        decimal_10 harga_beli "NOT NULL DEFAULT 0"
        decimal_10 harga_jual "NOT NULL DEFAULT 0"
        int stok_saat_ini "DEFAULT 0"
        int stok_minimum "DEFAULT 10"
        date kadaluarsa
        varchar_20 lokasi_rak
        datetime created_at "DEFAULT CURRENT_TIMESTAMP"
        datetime updated_at "ON UPDATE CURRENT_TIMESTAMP"
    }

    kunjungan {
        int id PK
        varchar_10 no_antrian "NOT NULL"
        varchar_20 no_kunjungan UK "NOT NULL"
        int pasien_id FK "NOT NULL"
        int dokter_id FK "NOT NULL"
        int perawat_id FK
        date tanggal_kunjungan "NOT NULL"
        enum jenis_kunjungan "baru | kontrol — DEFAULT baru"
        text keluhan_utama
        varchar_200 diagnosa
        varchar_10 kode_icd10
        varchar_15 tekanan_darah
        decimal_5_1 berat_badan
        decimal_5_1 tinggi_badan
        decimal_4_1 suhu
        int nadi
        int saturasi_o2
        text catatan_dokter
        enum status "menunggu | periksa | selesai — DEFAULT menunggu"
        datetime created_at "DEFAULT CURRENT_TIMESTAMP"
    }

    resep_obat {
        int id PK
        int kunjungan_id FK "NOT NULL"
        int obat_id FK "NOT NULL"
        int jumlah "NOT NULL DEFAULT 1"
        varchar_100 aturan_pakai
        varchar_200 keterangan
    }

    pembayaran {
        int id PK
        varchar_20 no_invoice UK "NOT NULL"
        int kunjungan_id FK "NOT NULL UNIQUE"
        date tanggal_bayar "NOT NULL"
        decimal_10 biaya_konsultasi "NOT NULL DEFAULT 0"
        decimal_10 biaya_obat "NOT NULL DEFAULT 0"
        decimal_10 total_tagihan "NOT NULL DEFAULT 0"
        enum metode_bayar "tunai | transfer | bpjs — DEFAULT tunai"
        decimal_10 uang_diterima
        decimal_10 kembalian
        enum status "lunas | pending — DEFAULT pending"
        int petugas_id FK
        datetime created_at "DEFAULT CURRENT_TIMESTAMP"
    }

    stok_log {
        int id PK
        int obat_id FK "NOT NULL"
        datetime tanggal "DEFAULT CURRENT_TIMESTAMP"
        enum jenis_mutasi "masuk | keluar | adjustment — NOT NULL"
        int jumlah "NOT NULL"
        varchar_200 keterangan
        int referensi_id
        int petugas_id FK
    }

    dokter ||--o{ jadwal_dokter : "memiliki"
    pasien ||--o{ kunjungan : "melakukan"
    dokter ||--o{ kunjungan : "menangani"
    perawat ||--o{ kunjungan : "mendampingi"
    kunjungan ||--o{ resep_obat : "menghasilkan"
    obat ||--o{ resep_obat : "diresepkan"
    kunjungan ||--|| pembayaran : "dibayar"
    users ||--o{ pembayaran : "diproses oleh"
    obat ||--o{ stok_log : "dicatat"
    users ||--o{ stok_log : "dicatat oleh"
```

---

## Alur Kunjungan Pasien (End-to-End)

```mermaid
flowchart TD
    subgraph FASE_0["⚙️ Fase 0 — Setup Master Data (sekali)"]
        A1[users\nadmin · loket · farmasi · kasir] 
        A2[dokter\nkode, spesialisasi, tarif]
        A3[jadwal_dokter\nhari, jam, kuota]
        A4[perawat\nshift, poli_tugas]
        A5[obat\nstok, harga]
        A6[stok_log\njenis_mutasi = masuk\nketerangan: pembelian awal]
        A2 --> A3
        A5 --> A6
    end

    subgraph FASE_1["🏥 Fase 1 — Pendaftaran (Loket)"]
        B1{Pasien\nsudah terdaftar?}
        B2[pasien\nINSERT baru\ngenerate no_rm = RM0000001]
        B3[pasien\nGET by no_rm / NIK]
        B4[kunjungan\nINSERT\nno_antrian = A001\nstatus = menunggu]
        B1 -- Belum --> B2
        B1 -- Sudah --> B3
        B2 --> B4
        B3 --> B4
    end

    subgraph FASE_2["🩺 Fase 2 — Triase Vital Signs (Perawat)"]
        C1[kunjungan\nUPDATE\ntekanan_darah, BB, TB\nsuhu, nadi, saturasi_o2\nstatus = periksa]
    end

    subgraph FASE_3["👨‍⚕️ Fase 3 — Pemeriksaan Dokter"]
        D1[kunjungan\nUPDATE\nkeluhan_utama, diagnosa\nkode_icd10, catatan_dokter\nstatus = selesai]
        D2[resep_obat\nINSERT per item obat\njumlah, aturan_pakai]
        D1 --> D2
    end

    subgraph FASE_4["💊 Fase 4 — Dispensing Obat (Farmasi)"]
        E1[resep_obat\nGET by kunjungan_id]
        E2[obat\nUPDATE stok_saat_ini - jumlah]
        E3[stok_log\nINSERT\njenis_mutasi = keluar\nreferensi_id = kunjungan_id]
        E1 --> E2
        E2 --> E3
    end

    subgraph FASE_5["💳 Fase 5 — Pembayaran (Kasir)"]
        F1[pembayaran\nINSERT\nbiaya_konsultasi dari dokter.tarif\nbiaya_obat dari resep\ntotal_tagihan = konsultasi + obat\nstatus = pending]
        F2{Metode\nbayar?}
        F3[tunai:\nuang_diterima, kembalian]
        F4[transfer:\nuang_diterima = total]
        F5[bpjs:\ntanpa pembayaran tunai]
        F6[pembayaran\nUPDATE status = lunas]
        F1 --> F2
        F2 -- tunai --> F3 --> F6
        F2 -- transfer --> F4 --> F6
        F2 -- bpjs --> F5 --> F6
    end

    FASE_0 --> FASE_1
    FASE_1 --> FASE_2
    FASE_2 --> FASE_3
    FASE_3 --> FASE_4
    FASE_4 --> FASE_5
```

---

## Detail Per Fase

### Fase 0 — Setup Master Data
Dilakukan sekali saat sistem pertama kali dijalankan.

| Tabel | Aksi | Keterangan |
|-------|------|------------|
| `users` | INSERT | Buat akun admin, loket, farmasi, kasir |
| `dokter` | INSERT | Daftarkan dokter beserta `tarif_konsultasi` |
| `jadwal_dokter` | INSERT | Set jadwal praktek per hari + kuota pasien |
| `perawat` | INSERT | Daftarkan perawat, shift, dan poli tugas |
| `obat` | INSERT | Input data obat + stok awal + harga |
| `stok_log` | INSERT | Catat pengisian stok awal (`jenis_mutasi = masuk`) |

---

### Fase 1 — Pendaftaran Pasien (Loket)
Petugas loket mendaftarkan pasien dan membuat antrian kunjungan.

**Pasien baru:**
```
INSERT pasien → no_rm = RM + 7 digit auto (RM0000001)
                NIK unik, pilih jenis_pasien (umum/bpjs)
                jika bpjs → isi no_bpjs
```

**Pasien lama:**
```
GET pasien WHERE no_rm = ? OR nik = ?
```

**Buat kunjungan:**
```
INSERT kunjungan
  no_antrian   = A001 (generate per hari)
  no_kunjungan = KJ + tanggal + sequence (KJ260401001)
  pasien_id    = pasien.id
  dokter_id    = pilih dokter yang tersedia (cek jadwal_dokter)
  jenis_kunjungan = baru | kontrol
  status       = menunggu
```

---

### Fase 2 — Triase Vital Signs (Perawat)
Perawat memanggil pasien sesuai antrian dan mengisi tanda-tanda vital.

```
UPDATE kunjungan SET
  tekanan_darah = '120/80',
  berat_badan   = 65.0,
  tinggi_badan  = 165.0,
  suhu          = 36.7,
  nadi          = 80,
  saturasi_o2   = 98,
  perawat_id    = perawat.id,
  status        = 'periksa'
WHERE id = kunjungan.id
```

---

### Fase 3 — Pemeriksaan Dokter
Dokter memeriksa pasien, mengisi diagnosa, dan membuat resep.

```
UPDATE kunjungan SET
  keluhan_utama  = 'Demam dan batuk',
  diagnosa       = 'Influenza',
  kode_icd10     = 'J11',
  catatan_dokter = 'Kontrol kembali 3 hari lagi.',
  status         = 'selesai'
WHERE id = kunjungan.id

-- Per item obat yang diresepkan:
INSERT resep_obat (kunjungan_id, obat_id, jumlah, aturan_pakai)
VALUES (kunjungan.id, obat.id, 3, '3x1 setelah makan')
```

---

### Fase 4 — Dispensing Obat (Farmasi)
Petugas farmasi membaca resep, menyiapkan obat, dan mencatat mutasi stok.

```
-- Baca resep
SELECT r.*, o.nama_obat, o.stok_saat_ini
FROM resep_obat r JOIN obat o ON r.obat_id = o.id
WHERE r.kunjungan_id = ?

-- Kurangi stok
UPDATE obat SET stok_saat_ini = stok_saat_ini - jumlah
WHERE id = obat.id

-- Catat mutasi
INSERT stok_log (obat_id, jenis_mutasi, jumlah, keterangan, referensi_id, petugas_id)
VALUES (obat.id, 'keluar', jumlah, 'Pemakaian resep', kunjungan.id, user.id)
```

> **Peringatan stok:** jika `stok_saat_ini <= stok_minimum`, sistem harus memberi notifikasi restock.

---

### Fase 5 — Pembayaran (Kasir)
Kasir membuat invoice dan mencatat pelunasan.

```
-- Hitung biaya
biaya_konsultasi = dokter.tarif_konsultasi
biaya_obat       = SUM(resep_obat.jumlah × obat.harga_jual)
total_tagihan    = biaya_konsultasi + biaya_obat

-- Buat invoice
INSERT pembayaran (no_invoice, kunjungan_id, tanggal_bayar,
                   biaya_konsultasi, biaya_obat, total_tagihan,
                   metode_bayar, status, petugas_id)
VALUES ('INV260401001', kunjungan.id, TODAY,
        150000, 45000, 195000,
        'tunai', 'pending', user.id)

-- Lunaskan
UPDATE pembayaran SET
  uang_diterima = 200000,
  kembalian     = 5000,
  status        = 'lunas'
WHERE id = pembayaran.id
```

---

## Relasi Kunci Antar Tabel

```
pasien ──────────────────────────────► kunjungan
dokter ──────┬──► jadwal_dokter         │
             └──────────────────────►  │
perawat ─────────────────────────────► │
                                       │
                    kunjungan ─────────┤──► resep_obat ◄── obat
                                       │                     │
                                       │                     ▼
                                       └──► pembayaran   stok_log
                                                │
                                           users (kasir)
```

---

## Status Lifecycle Kunjungan

```
menunggu  →  periksa  →  selesai
   │              │           │
Loket buat    Perawat      Dokter
antrian       input        diagnosa
              vital        + resep
              signs
```

## Status Lifecycle Pembayaran

```
pending  →  lunas
   │           │
Kasir buat   Pembayaran
invoice      diterima
```

---

## Catatan Teknis

- **`no_rm`** — format `RM` + 7 digit, unik per pasien, tidak berubah seumur hidup pasien.
- **`no_kunjungan`** — format `KJ` + tanggal + sequence, unik per kunjungan.
- **`no_antrian`** — format `A` + 3 digit, di-reset setiap hari.
- **`no_invoice`** — format `INV` + tanggal + sequence, unik per pembayaran.
- **`kode_icd10`** — standar diagnosa internasional (ICD-10), contoh: `J11` = Influenza, `I10` = Hipertensi.
- **`stok_log.referensi_id`** — menunjuk ke `kunjungan.id` (untuk mutasi keluar) atau nomor PO (untuk mutasi masuk).
- **Pasien BPJS** — `metode_bayar = 'bpjs'`, tidak ada `uang_diterima` / `kembalian`.
