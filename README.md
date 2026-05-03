# 🏥 Klinik Gaharu Medika

**Sistem Informasi Manajemen Klinik** — Aplikasi desktop berbasis Java Swing untuk mengelola operasional klinik secara menyeluruh, mulai dari pendaftaran pasien, kunjungan, resep obat, hingga pembayaran dan pelaporan.

> Dibangun sebagai proyek Kuliah Kerja Praktek (KKP) menggunakan arsitektur **MVC** dengan **Java 17**, **Swing GUI**, dan **MySQL**.

---

## 📋 Daftar Isi

- [Fitur Utama](#-fitur-utama)
- [Screenshots](#-screenshots)
- [Tech Stack](#-tech-stack)
- [Arsitektur](#-arsitektur)
- [Struktur Project](#-struktur-project)
- [Database Schema](#-database-schema)
- [Prasyarat](#-prasyarat)
- [Instalasi & Setup](#-instalasi--setup)
- [Menjalankan Aplikasi](#-menjalankan-aplikasi)
- [Dokumentasi Tambahan](#-dokumentasi-tambahan)
- [Lisensi](#-lisensi)

---

## ✨ Fitur Utama

### Autentikasi & Otorisasi
- Login sistem dengan role-based access (`admin`, `petugas`)
- Session management untuk user yang sedang aktif

### Dashboard
- Ringkasan statistik klinik (jumlah pasien, kunjungan hari ini, antrian aktif)
- Daftar antrian pasien aktif dengan pagination
- Riwayat kunjungan terbaru

### Manajemen Master Data
| Modul | Fitur |
|-------|-------|
| **Pasien** | CRUD data pasien lengkap (NIK, No. RM, alamat, kontak darurat, alergi, riwayat penyakit, jenis pasien BPJS/umum/asuransi) |
| **Dokter** | CRUD data dokter (spesialisasi, poli/unit, No. STR/SIP, tarif konsultasi, jadwal praktik) |
| **Perawat** | CRUD data perawat (No. SIPP, shift pagi/sore/malam, poli tugas) |
| **Obat** | CRUD data obat (kode, jenis, satuan, dosis, harga beli/jual, stok, kadaluarsa, supplier, lokasi rak) |

### Kunjungan & Rekam Medis
- Pendaftaran kunjungan baru/kontrol dengan nomor antrian otomatis
- Input vital signs (tekanan darah, berat/tinggi badan, suhu, nadi, saturasi O₂)
- Diagnosa & kode ICD-10
- Catatan dokter
- Status kunjungan: `menunggu` → `periksa` → `selesai`

### Resep Obat
- Penambahan resep obat per kunjungan
- Aturan pakai dan keterangan per item obat
- Otomatis mengurangi stok obat (dengan log mutasi)

### Pembayaran
- Pembuatan invoice otomatis per kunjungan
- Kalkulasi biaya konsultasi + biaya obat
- Metode bayar: tunai, transfer, BPJS
- Hitung uang diterima & kembalian
- Status: `pending` / `lunas`

### Laporan (JasperReports)
- **Laporan Tabel** — Ekspor data master ke PDF
- **Laporan Pendapatan** — Rekapitulasi pendapatan klinik dengan filter periode

---

## 🖼️ Screenshots

> Lihat folder [`docs/ui-neetbeans/`](docs/ui-neetbeans/) untuk screenshot tampilan UI aplikasi.

---

## 🛠️ Tech Stack

| Komponen | Teknologi | Versi |
|----------|-----------|-------|
| **Bahasa** | Java | 17 |
| **GUI Framework** | Swing | — |
| **Build Tool** | Maven | 3.x |
| **Database** | MySQL | 8.x |
| **JDBC Driver** | mysql-connector-j | 9.6.0 |
| **Reporting** | JasperReports | 6.21.3 |
| **IDE** | Apache NetBeans | — |
| **Form Designer** | NetBeans GUI Builder (Matisse) | — |

---

## 🏗️ Arsitektur

Arsitektur mengikuti pola **MVC (Model-View-Controller)** yang diadaptasi dengan layer Repository:

```
┌─────────────────────────────────────────────────────┐
│                    VIEW (Swing)                     │
│  LoginForm ─► DashboardForm ─► Panel (per modul)   │
└───────────────────────┬─────────────────────────────┘
                        │ user action
                        ▼
┌─────────────────────────────────────────────────────┐
│                   CONTROLLER                        │
│  AuthController, PasienController, DokterController │
│  KunjunganController, ObatController, dll           │
└───────────────────────┬─────────────────────────────┘
                        │ business logic
                        ▼
┌─────────────────────────────────────────────────────┐
│                   REPOSITORY                        │
│  UserRepo, PasienRepo, DokterRepo, KunjunganRepo   │
│  ObatRepo, PembayaranRepo, PerawatRepo, dll         │
└───────────────────────┬─────────────────────────────┘
                        │ JDBC query
                        ▼
┌─────────────────────────────────────────────────────┐
│              DATABASE CONNECTION                    │
│         DatabaseConnection (Singleton)              │
│              MySQL — db_gaharu_medika               │
└─────────────────────────────────────────────────────┘
```

### Alur Data

```
User klik tombol (View)
  └─► Controller menerima event
        ├─► Validasi input
        ├─► Repository.method() → SQL query via JDBC
        │     └─► DatabaseConnection.getConnection()
        └─► Update View (tampilkan hasil / error)
```

---

## 📁 Struktur Project

```
klinikGaharuMedika/
│
├── pom.xml                          ← Maven config & dependencies
│
├── docs/
│   ├── database/
│   │   ├── database.sql             ← Schema lengkap (10 tabel)
│   │   ├── data-dummy.sql           ← Data contoh untuk testing
│   │   ├── column-reference.md      ← Referensi kolom per tabel
│   │   ├── migration_*.sql          ← Script migrasi database
│   │   └── flow-diagram/            ← Diagram alur database
│   ├── flow/dashboard/              ← Dokumentasi alur dashboard
│   ├── report-pdf/                  ← Contoh output laporan PDF
│   ├── running-neatbeans/           ← Panduan menjalankan di NetBeans
│   └── ui-neetbeans/                ← Screenshot tampilan UI
│
└── src/main/
    ├── java/com/release/klinikgaharumedika/
    │   │
    │   ├── KlinikGaharuMedika.java          ← Entry point (main)
    │   │
    │   ├── config/
    │   │   └── DatabaseConnection.java      ← Koneksi DB (singleton)
    │   │
    │   ├── model/                           ← POJO / Entity (15 class)
    │   │   ├── User.java
    │   │   ├── Pasien.java
    │   │   ├── Dokter.java
    │   │   ├── JadwalDokter.java
    │   │   ├── Perawat.java
    │   │   ├── Obat.java
    │   │   ├── Kunjungan.java
    │   │   ├── ResepObatItem.java
    │   │   ├── Pembayaran.java
    │   │   ├── DashboardData.java
    │   │   ├── DashboardQueueEntry.java
    │   │   ├── DashboardActiveQueuePage.java
    │   │   ├── DashboardRecentVisit.java
    │   │   ├── DashboardRecentVisitPage.java
    │   │   └── PageResult.java
    │   │
    │   ├── repository/                      ← Data Access Layer (9 class)
    │   │   ├── UserRepository.java
    │   │   ├── PasienRepository.java
    │   │   ├── DokterRepository.java
    │   │   ├── PerawatRepository.java
    │   │   ├── ObatRepository.java
    │   │   ├── KunjunganRepository.java
    │   │   ├── PembayaranRepository.java
    │   │   ├── DashboardRepository.java
    │   │   └── SchemaSupport.java
    │   │
    │   ├── controller/                      ← Business Logic (9 class)
    │   │   ├── AuthController.java
    │   │   ├── AuthResult.java
    │   │   ├── DashboardController.java
    │   │   ├── PasienController.java
    │   │   ├── DokterController.java
    │   │   ├── PerawatController.java
    │   │   ├── ObatController.java
    │   │   ├── KunjunganController.java
    │   │   └── PembayaranController.java
    │   │
    │   ├── service/report/                  ← Laporan (JasperReports)
    │   │   ├── PendapatanReportService.java
    │   │   └── TableReportService.java
    │   │
    │   ├── state/
    │   │   └── SessionManager.java          ← State user yang login
    │   │
    │   └── view/                            ← Swing UI
    │       ├── LoginForm.java / .form
    │       ├── DashboardForm.java / .form   ← Shell utama + sidebar nav
    │       └── panel/                       ← Panel per modul
    │           ├── FormUiStyle.java          ← Design system & styling
    │           ├── DashboardPanel.*          ← Halaman dashboard
    │           ├── PasienPanel.*             ← List pasien
    │           ├── PasienFormPanel.*         ← Form CRUD pasien
    │           ├── DokterPanel.*             ← List dokter
    │           ├── DokterFormPanel.*         ← Form CRUD dokter
    │           ├── PerawatPanel.*            ← List perawat
    │           ├── PerawatFormPanel.*         ← Form CRUD perawat
    │           ├── ObatPanel.*               ← List obat
    │           ├── ObatFormPanel.*           ← Form CRUD obat
    │           ├── KunjunganPanel.*          ← List kunjungan
    │           ├── KunjunganFormPanel.*      ← Form kunjungan + resep
    │           ├── PembayaranPanel.*         ← List pembayaran
    │           ├── PembayaranFormPanel.*     ← Form pembayaran
    │           └── LaporanPanel.*           ← Halaman laporan
    │
    └── resources/reports/
        ├── laporan_tabel.jrxml              ← Template laporan tabel
        └── laporan_pendapatan.jrxml         ← Template laporan pendapatan
```

---

## 🗄️ Database Schema

Database `db_gaharu_medika` terdiri dari **10 tabel**:

```
┌──────────┐     ┌──────────┐     ┌──────────────┐
│  users   │     │  pasien  │     │   dokter     │
│──────────│     │──────────│     │──────────────│
│ id (PK)  │     │ id (PK)  │     │ id (PK)      │
│ username │     │ no_rm    │     │ kode_dokter  │
│ password │     │ nik      │     │ spesialisasi │
│ role     │     │ nama     │     │ tarif        │
└────┬─────┘     └────┬─────┘     └──────┬───────┘
     │                │                   │
     │                │    ┌──────────────┤
     │                │    │              │
     │           ┌────▼────▼───┐    ┌─────▼────────┐
     │           │  kunjungan  │    │jadwal_dokter │
     │           │─────────────│    └──────────────┘
     │           │ no_antrian  │
     │           │ no_kunjungan│    ┌──────────┐
     │           │ diagnosa    │    │ perawat  │
     │           │ vital signs │    │──────────│
     │           └──┬──────┬──┘    │ id (PK)  │
     │              │      │       │ shift    │
     │         ┌────▼──┐ ┌─▼────────▼────┐
     │         │ resep  │ │  pembayaran   │
     │         │ _obat  │ │──────────────│
     │         └───┬───┘ │ no_invoice    │
     │             │     │ total_tagihan │
     │        ┌────▼──┐  │ metode_bayar  │
     │        │ obat  │  └───────────────┘
     │        │───────│
     │        │ stok  │  ┌──────────┐
     │        └───┬───┘  │ stok_log │
     │            └──────►──────────│
     │                   │ mutasi   │
     └───────────────────► petugas  │
                         └──────────┘
```

| No | Tabel | Keterangan |
|----|-------|------------|
| 1 | `users` | Akun pengguna (admin/petugas) |
| 2 | `pasien` | Data pasien (RM, NIK, alamat, kontak darurat, riwayat) |
| 3 | `dokter` | Data dokter (STR, SIP, spesialisasi, tarif) |
| 4 | `jadwal_dokter` | Jadwal praktik dokter per hari |
| 5 | `perawat` | Data perawat (SIPP, shift, poli tugas) |
| 6 | `obat` | Master obat (stok, harga, kadaluarsa, supplier) |
| 7 | `kunjungan` | Kunjungan pasien (antrian, vital signs, diagnosa, ICD-10) |
| 8 | `resep_obat` | Item resep per kunjungan |
| 9 | `pembayaran` | Invoice & pembayaran per kunjungan |
| 10 | `stok_log` | Log mutasi stok obat (masuk/keluar/adjustment) |

---

## 📌 Prasyarat

Pastikan sudah terinstall di sistem:

- **Java JDK 17** atau lebih baru
- **Apache Maven 3.x**
- **MySQL Server 8.x**
- **Apache NetBeans** (opsional, untuk GUI Builder)

---

## 🚀 Instalasi & Setup

### 1. Clone Repository

```bash
git clone git@github.com:Fharhanamrin/KlinikGaharuMedika.git
cd KlinikGaharuMedika
```

### 2. Setup Database

```bash
# Login ke MySQL
mysql -u root -p

# Jalankan schema
source docs/database/database.sql

# (Opsional) Load data dummy untuk testing
source docs/database/data-dummy.sql
```

### 3. Konfigurasi Koneksi Database

Koneksi database dapat dikonfigurasi melalui **environment variable** atau **system property**:

| Setting | System Property | Environment Variable | Default |
|---------|----------------|---------------------|---------|
| Host | `app.db.host` | `APP_DB_HOST` | `localhost` |
| Port | `app.db.port` | `APP_DB_PORT` | `3306` |
| Database | `app.db.name` | `APP_DB_NAME` | `db_gaharu_medika` |
| Username | `app.db.user` | `APP_DB_USER` | `root` |
| Password | `app.db.password` | `APP_DB_PASSWORD` | *(kosong)* |

> Jika menggunakan default MySQL lokal tanpa password, tidak perlu konfigurasi apa-apa.

### 4. Install Dependencies

```bash
mvn clean install
```

---

## ▶️ Menjalankan Aplikasi

### Via Maven (CLI)

```bash
mvn exec:java
```

### Via NetBeans

1. Buka project di NetBeans (`File > Open Project`)
2. Klik kanan project → `Run`
3. Atau buka `KlinikGaharuMedika.java` → klik ▶️ Run

> Lihat panduan lengkap di [`docs/running-neatbeans/`](docs/running-neatbeans/)

### Alur Penggunaan

```
Login (admin/petugas)
  └─► Dashboard (statistik & antrian)
        ├─► Pasien   → Tambah/Edit/Hapus data pasien
        ├─► Dokter   → Tambah/Edit/Hapus data dokter + jadwal
        ├─► Perawat  → Tambah/Edit/Hapus data perawat
        ├─► Obat     → Tambah/Edit/Hapus stok obat
        ├─► Kunjungan → Daftar kunjungan + input rekam medis + resep
        ├─► Pembayaran → Proses invoice & pembayaran
        └─► Laporan  → Cetak laporan tabel & pendapatan (PDF)
```

---

## 📖 Dokumentasi Tambahan

| Dokumen | Path |
|---------|------|
| Schema Database | [`docs/database/database.sql`](docs/database/database.sql) |
| Referensi Kolom | [`docs/database/column-reference.md`](docs/database/column-reference.md) |
| Data Dummy | [`docs/database/data-dummy.sql`](docs/database/data-dummy.sql) |
| Migrasi Database | [`docs/database/migration_*.sql`](docs/database/) |
| Flow Diagram | [`docs/database/flow-diagram/`](docs/database/flow-diagram/) |
| Alur Dashboard | [`docs/flow/dashboard/`](docs/flow/dashboard/) |
| Contoh Laporan PDF | [`docs/report-pdf/`](docs/report-pdf/) |
| Panduan NetBeans | [`docs/running-neatbeans/`](docs/running-neatbeans/) |
| Screenshot UI | [`docs/ui-neetbeans/`](docs/ui-neetbeans/) |

---

## 📄 Lisensi

Project ini dibuat untuk keperluan akademik (Kuliah Kerja Praktek).

---

<p align="center">
  <b>Klinik Gaharu Medika</b> — Sistem Informasi Klinik<br>
  Built with ❤️ using Java Swing & MySQL
</p>
