# Column Reference — db_gaharu_medika
> Penjelasan kolom-kolom yang tidak umum atau butuh konteks domain klinik

---

## `users`

### `role`
```
ENUM('admin', 'petugas') DEFAULT 'petugas'
```
Menentukan hak akses menu di aplikasi. Saat ini hanya 2 level — untuk granularitas lebih halus (loket, farmasi, kasir), perbedaan akses ditangani di layer aplikasi bukan di tabel ini.

| Nilai | Akses |
|-------|-------|
| `admin` | Semua menu: master data, laporan, manajemen user |
| `petugas` | Menu operasional: pendaftaran, kunjungan, farmasi, kasir |

### `is_active`
```
TINYINT(1) DEFAULT 1
```
Flag soft-delete. Nilai `0` berarti akun dinonaktifkan — user tidak bisa login tapi datanya tetap ada di database (tidak dihapus). Dipakai untuk audit trail dan integritas relasi ke tabel lain.

---

## `pasien`

### `no_rm`
```
VARCHAR(10) NOT NULL UNIQUE  →  format: RM0000001
```
**Nomor Rekam Medis** — identitas permanen pasien di klinik. Digenerate otomatis saat pertama kali daftar, **tidak pernah berubah** sepanjang pasien masih terdaftar. Dipakai sebagai referensi utama pencarian riwayat medis.

### `nik`
```
CHAR(16) NOT NULL UNIQUE
```
Nomor Induk Kependudukan — 16 digit sesuai KTP Indonesia. Dipakai untuk verifikasi identitas dan cegah duplikasi pasien. Harus persis 16 karakter (CHAR, bukan VARCHAR).

### `golongan_darah`
```
ENUM('A+','A-','B+','B-','AB+','AB-','O+','O-','Tidak Diketahui')
```
Nilai `'Tidak Diketahui'` dipakai saat pasien belum pernah tes golongan darah. Jangan dikosongkan (NULL), selalu pilih salah satu nilai.

### `no_hp_darurat` + `hubungan_darurat`
```
no_hp_darurat   VARCHAR(20)
hubungan_darurat VARCHAR(30)  -- 'Ayah','Ibu','Suami','Istri','Anak','Kakak','Adik'
```
Dua kolom ini **selalu berpasangan** — kontak yang dihubungi saat kondisi darurat. Jika `no_hp_darurat` diisi, `hubungan_darurat` wajib diisi juga (validasi di layer aplikasi).

### `alergi_obat`
```
TEXT  -- contoh: "Penicillin, Aspirin"
```
Dibaca oleh **petugas farmasi** sebelum dispensing obat. Jika berisi nilai, aplikasi harus menampilkan peringatan saat farmasi menyiapkan obat yang sama atau sekelasnya.

### `riwayat_penyakit`
```
TEXT  -- contoh: "Hipertensi, Diabetes melitus tipe 2"
```
Dibaca oleh **dokter** saat anamnesis (pengambilan riwayat kesehatan). Diisi saat pendaftaran awal dan bisa diperbarui kapan saja.

### `jenis_pasien`
```
ENUM('umum', 'bpjs', 'asuransi') DEFAULT 'umum'
```
Menentukan **alur pembayaran**:
- `umum` → pasien bayar sendiri, `metode_bayar` bisa tunai atau transfer
- `bpjs` → ditanggung BPJS, `metode_bayar` = `'bpjs'`, tidak ada uang diterima/kembalian
- `asuransi` → pasien ditanggung penjamin/asuransi lain sesuai data penjamin

### `no_bpjs`
```
VARCHAR(20)  -- nullable
```
Hanya diisi jika `jenis_pasien = 'bpjs'` atau `jenis_pasien = 'asuransi'`. Untuk BPJS, format: 13 digit angka dari kartu BPJS Kesehatan. Jika `jenis_pasien = 'umum'`, kolom ini harus `NULL`.

---

## `dokter`

### `no_str`
```
VARCHAR(30) NOT NULL  -- contoh: STR-2026-001
```
**Surat Tanda Registrasi** — izin praktik yang dikeluarkan oleh Konsil Kedokteran Indonesia (KKI). Berlaku nasional, wajib diperpanjang tiap 5 tahun. Dokter tidak boleh praktek tanpa STR aktif.

### `no_sip`
```
VARCHAR(30) NOT NULL  -- contoh: SIP-2026-001
```
**Surat Izin Praktik** — izin yang dikeluarkan Dinas Kesehatan setempat, spesifik per lokasi klinik. Satu dokter bisa punya beberapa SIP untuk lokasi yang berbeda. Dokter hanya boleh praktek di klinik yang sesuai SIP-nya.

> `no_str` = izin dari pusat (nasional), `no_sip` = izin dari daerah (per lokasi)

### `tarif_konsultasi`
```
DECIMAL(10,0) NOT NULL DEFAULT 0
```
Dipakai sebagai nilai awal `pembayaran.biaya_konsultasi` saat kunjungan selesai. Bisa berbeda per dokter (spesialis lebih mahal). Nilai `0` artinya dokter tersebut belum di-set tarifnya.

---

## `jadwal_dokter`

### `kuota_pasien`
```
INT NOT NULL DEFAULT 20
```
Batas maksimal pasien per sesi praktek. Saat loket membuat kunjungan baru, aplikasi harus mengecek:
```sql
SELECT COUNT(*) FROM kunjungan
WHERE dokter_id = ? AND tanggal_kunjungan = ?
```
Jika sudah mencapai kuota, loket tidak boleh mendaftarkan pasien ke dokter tersebut di hari itu.

---

## `perawat`

### `no_sipp`
```
VARCHAR(30) NOT NULL  -- contoh: SIPP-PR-001
```
**Surat Izin Praktik Perawat** — analog dengan `no_sip` milik dokter, dikeluarkan oleh Dinas Kesehatan. Wajib dimiliki perawat yang berpraktik di fasilitas kesehatan.

### `shift`
```
ENUM('pagi', 'sore', 'malam') NOT NULL
```
Dipakai untuk memfilter perawat yang tersedia saat loket menugaskan perawat ke kunjungan. Hanya perawat dengan shift yang sesuai jam kunjungan yang ditampilkan.

| Nilai | Jam (umum) |
|-------|-----------|
| `pagi` | 07:00 – 14:00 |
| `sore` | 14:00 – 21:00 |
| `malam` | 21:00 – 07:00 |

### `poli_tugas`
```
VARCHAR(60)  -- contoh: 'Poli Umum', 'Poli Penyakit Dalam'
```
Free text (bukan ENUM) agar fleksibel jika klinik menambah poli baru. Dipakai untuk filter perawat yang relevan saat kunjungan ke poli tertentu.

---

## `obat`

### `jenis`
```
VARCHAR(40) NOT NULL
```
Kategori bentuk sediaan obat. Nilai umum di data dummy:

| Nilai | Contoh |
|-------|--------|
| `Tablet` | Paracetamol, Cetirizine |
| `Kapsul` | Amoxicillin, Omeprazole |
| `Sirup` | Salbutamol Syrup |
| `Sachet` | Oralit |
| `Cairan` | Infus, larutan |
| `Salep` | Obat topikal |

### `satuan`
```
VARCHAR(20) NOT NULL
```
Satuan pembelian dan penjualan obat. Menentukan bagaimana stok dihitung.

| Nilai | Keterangan |
|-------|-----------|
| `Strip` | 1 strip = 10 tablet (umum) |
| `Botol` | Untuk sirup, cairan |
| `Box` | Untuk sachet/ampul dalam box |
| `Vial` | Untuk injeksi |
| `Tube` | Untuk salep/krim |

### `stok_minimum`
```
INT NOT NULL DEFAULT 10
```
Threshold peringatan restock. Setiap kali stok berkurang (via `stok_log`), aplikasi harus cek:
```sql
SELECT * FROM obat WHERE stok_saat_ini <= stok_minimum
```
Jika kondisi ini terpenuhi → tampilkan notifikasi ke petugas farmasi untuk melakukan pemesanan ulang.

### `lokasi_rak`
```
VARCHAR(20)  -- contoh: A1, B3, C5
```
Kode posisi fisik obat di rak gudang farmasi. Format: **[baris A-C][kolom 1-5]**. Memudahkan petugas farmasi menemukan obat saat dispensing tanpa harus hafal semua lokasi.

```
Kolom:  1    2    3    4    5
Baris A: A1   A2   A3   A4   A5
Baris B: B1   B2   B3   B4   B5
Baris C: C1   C2   C3   C4   C5
```

---

## `kunjungan`

### `no_antrian`
```
VARCHAR(10) NOT NULL  -- contoh: A001
```
Nomor urut panggilan pasien **per hari** (di-reset tiap hari). Format: huruf `A` + 3 digit. Dipakai perawat untuk memanggil pasien ke ruang periksa. Tidak unik secara global — bisa ada `A001` di dua tanggal berbeda.

### `no_kunjungan`
```
VARCHAR(20) NOT NULL UNIQUE  -- contoh: KJ260401001
```
Nomor berkas kunjungan, **unik permanen**. Format: `KJ` + tanggal (YYMMDD) + sequence. Ini yang dicantumkan di berkas fisik pasien dan jadi referensi lintas departemen (dokter, farmasi, kasir).

### `jenis_kunjungan`
```
ENUM('baru', 'kontrol') DEFAULT 'baru'
```
| Nilai | Kapan dipakai |
|-------|--------------|
| `baru` | Keluhan/penyakit baru, belum pernah ditangani |
| `kontrol` | Follow-up dari diagnosa sebelumnya, pasien datang lagi untuk cek perkembangan |

### `kode_icd10`
```
VARCHAR(10)  -- contoh: J11, I10, J45
```
Kode diagnosa standar **ICD-10** (International Classification of Diseases, WHO). Diisi dokter bersamaan dengan field `diagnosa`. Dipakai untuk pelaporan ke BPJS dan statistik klinik.

Contoh kode umum di klinik:

| Kode | Diagnosa |
|------|----------|
| `J11` | Influenza |
| `K29` | Gastritis |
| `J30` | Rhinitis alergi |
| `I10` | Hipertensi |
| `J02` | Faringitis akut |
| `A09` | Gastroenteritis |
| `J06` | ISPA |
| `J45` | Asma |
| `G43` | Migraine |
| `M79` | Myalgia |

### `tekanan_darah`
```
VARCHAR(15)  -- contoh: "120/80", "130/90"
```
Disimpan sebagai **string** format `sistolik/diastolik`, bukan angka terpisah. Alasan: lebih mudah ditampilkan langsung ke UI tanpa perlu menggabungkan dua kolom. Untuk analisis statistik, perlu di-parse di layer aplikasi.

### `saturasi_o2`
```
INT  -- contoh: 97, 98, 99
```
**SpO2** (Saturasi Oksigen) dalam persen, diukur dengan pulse oximeter. Nilai normal: **95–100%**. Di bawah 95% perlu perhatian medis.

### `status`
```
ENUM('menunggu', 'periksa', 'selesai') DEFAULT 'menunggu'
```
Lifecycle kunjungan — hanya boleh maju, tidak boleh mundur:

```
menunggu  ──►  periksa  ──►  selesai
   │               │              │
Loket buat    Perawat input    Dokter isi
kunjungan     vital signs      diagnosa
                               + resep
```

---

## `resep_obat`

### `aturan_pakai`
```
VARCHAR(100)  -- contoh: "3x1 setelah makan", "1 sachet bila perlu"
```
Instruksi konsumsi obat untuk **pasien**. Teks ini yang dicetak di label/stiker obat. Format bebas tapi harus jelas dan singkat.

Contoh nilai umum:
- `3x1 setelah makan`
- `2x1 setelah makan`
- `1x1 malam`
- `3x5 ml`
- `1 sachet bila perlu`

### `keterangan`
```
VARCHAR(200)  -- contoh: "Untuk kunjungan A001"
```
Catatan internal untuk **petugas farmasi** — tidak dicetak di label obat pasien. Bisa berisi instruksi khusus penyiapan, catatan dari dokter, atau referensi kunjungan.

---

## `pembayaran`

### `no_invoice`
```
VARCHAR(20) NOT NULL UNIQUE  -- contoh: INV260401011
```
Nomor dokumen tagihan resmi. Format: `INV` + tanggal (YYMMDD) + sequence. Dicetak di struk/kwitansi pasien. Unik permanen, tidak bisa diedit setelah dibuat.

### `metode_bayar`
```
ENUM('tunai', 'transfer', 'bpjs') DEFAULT 'tunai'
```
Menentukan field mana yang diisi dan alur kasir:

| Metode | `uang_diterima` | `kembalian` | Keterangan |
|--------|----------------|------------|------------|
| `tunai` | Wajib diisi | Wajib diisi | `kembalian = uang_diterima - total_tagihan` |
| `transfer` | = `total_tagihan` | `0` | Tidak ada kembalian |
| `bpjs` | `NULL` | `NULL` | Ditanggung BPJS, tidak ada transaksi tunai |

### `kembalian`
```
DECIMAL(10,0)  -- nullable
```
Hanya relevan untuk `metode_bayar = 'tunai'`. Untuk transfer dan BPJS nilainya `NULL`. Formula: `kembalian = uang_diterima - total_tagihan`.

---

## `stok_log`

### `jenis_mutasi`
```
ENUM('masuk', 'keluar', 'adjustment') NOT NULL
```
Tiga jenis pergerakan stok:

| Nilai | Trigger | `referensi_id` | `jumlah` |
|-------|---------|----------------|---------|
| `masuk` | Pembelian obat baru dari supplier | Nomor PO (purchase order) | Positif |
| `keluar` | Obat diambil untuk resep pasien | `kunjungan.id` | Positif |
| `adjustment` | Koreksi hasil stok opname | `NULL` | Bisa positif atau **negatif** |

### `referensi_id`
```
INT  -- nullable, polimorfik
```
**Tidak ada FOREIGN KEY constraint** karena kolom ini bisa menunjuk ke tabel berbeda tergantung `jenis_mutasi`:
- `masuk` → nomor PO (angka eksternal, bukan FK ke tabel manapun)
- `keluar` → `kunjungan.id`
- `adjustment` → `NULL`

Untuk membaca artinya, selalu lihat kolom `jenis_mutasi` terlebih dahulu.

### `jumlah`
```
INT NOT NULL
```
Jumlah unit yang bergerak. Untuk `adjustment`, nilai ini **bisa negatif** (contoh: `-2` artinya stok fisik kurang 2 dari catatan sistem). Untuk `masuk` dan `keluar` selalu positif.
