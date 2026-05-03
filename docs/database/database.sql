-- ============================================
-- DATABASE: db_gaharu_medika
-- ============================================

CREATE DATABASE IF NOT EXISTS db_gaharu_medika
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE db_gaharu_medika;

-- 1. USERS
CREATE TABLE users (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  username      VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  nama_lengkap  VARCHAR(100) NOT NULL,
  role          ENUM('admin','petugas') NOT NULL DEFAULT 'petugas',
  is_active     TINYINT(1) NOT NULL DEFAULT 1,
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. PASIEN
CREATE TABLE pasien (
  id                INT AUTO_INCREMENT PRIMARY KEY,
  no_rm             VARCHAR(10) NOT NULL UNIQUE,
  nik               CHAR(16) NOT NULL UNIQUE,
  nama              VARCHAR(100) NOT NULL,
  jenis_kelamin     ENUM('L','P') NOT NULL,
  tanggal_lahir     DATE NOT NULL,
  tempat_lahir      VARCHAR(60),
  golongan_darah    ENUM('A+','A-','B+','B-','AB+','AB-','O+','O-','Tidak Diketahui'),
  alamat            TEXT,
  kelurahan         VARCHAR(60),
  kecamatan         VARCHAR(60),
  kota              VARCHAR(60),
  provinsi          VARCHAR(60),
  kode_pos          VARCHAR(10),
  no_hp             VARCHAR(20),
  nama_kontak_darurat VARCHAR(100),
  no_hp_darurat     VARCHAR(20),
  hubungan_darurat  VARCHAR(30),
  pekerjaan         VARCHAR(100),
  alergi_obat       TEXT,
  riwayat_penyakit  TEXT,
  jenis_pasien      ENUM('umum','bpjs','asuransi') NOT NULL DEFAULT 'umum',
  no_bpjs           VARCHAR(20),
  created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. DOKTER
CREATE TABLE dokter (
  id                INT AUTO_INCREMENT PRIMARY KEY,
  kode_dokter       VARCHAR(10) NOT NULL UNIQUE,
  nama              VARCHAR(100) NOT NULL,
  spesialisasi      VARCHAR(60) NOT NULL,
  poli_unit         VARCHAR(60),
  no_str            VARCHAR(30) NOT NULL,
  no_sip            VARCHAR(30) NOT NULL,
  no_hp             VARCHAR(20),
  tarif_konsultasi  DECIMAL(10,0) NOT NULL DEFAULT 0,
  status            ENUM('aktif','nonaktif') NOT NULL DEFAULT 'aktif',
  created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 4. JADWAL_DOKTER
CREATE TABLE jadwal_dokter (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  dokter_id    INT NOT NULL,
  hari         ENUM('Senin','Selasa','Rabu','Kamis','Jumat','Sabtu') NOT NULL,
  jam_mulai    TIME NOT NULL,
  jam_selesai  TIME NOT NULL,
  kuota_pasien INT NOT NULL DEFAULT 20,
  FOREIGN KEY (dokter_id) REFERENCES dokter(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. PERAWAT
CREATE TABLE perawat (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  kode_perawat VARCHAR(10) NOT NULL UNIQUE,
  nama        VARCHAR(100) NOT NULL,
  no_sipp     VARCHAR(30) NOT NULL,
  shift       ENUM('pagi','sore','malam') NOT NULL,
  poli_tugas  VARCHAR(60),
  no_hp       VARCHAR(20),
  status      ENUM('aktif','nonaktif','cuti') NOT NULL DEFAULT 'aktif',
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 6. OBAT
CREATE TABLE obat (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  kode_obat     VARCHAR(10) NOT NULL UNIQUE,
  nama_obat     VARCHAR(100) NOT NULL,
  jenis         VARCHAR(40) NOT NULL,
  satuan        VARCHAR(20) NOT NULL,
  kandungan_dosis VARCHAR(100),
  harga_beli    DECIMAL(10,0) NOT NULL DEFAULT 0,
  harga_jual    DECIMAL(10,0) NOT NULL DEFAULT 0,
  stok_saat_ini INT NOT NULL DEFAULT 0,
  stok_minimum  INT NOT NULL DEFAULT 10,
  kadaluarsa    DATE,
  supplier      VARCHAR(100),
  lokasi_rak    VARCHAR(20),
  keterangan    TEXT,
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 7. KUNJUNGAN
CREATE TABLE kunjungan (
  id               INT AUTO_INCREMENT PRIMARY KEY,
  no_antrian       VARCHAR(10) NOT NULL,
  no_kunjungan     VARCHAR(20) NOT NULL UNIQUE,
  pasien_id        INT NOT NULL,
  dokter_id        INT NOT NULL,
  perawat_id       INT,
  tanggal_kunjungan DATE NOT NULL,
  jenis_kunjungan  ENUM('baru','kontrol') NOT NULL DEFAULT 'baru',
  keluhan_utama    TEXT,
  diagnosa         VARCHAR(200),
  kode_icd10       VARCHAR(10),
  tekanan_darah    VARCHAR(15),
  berat_badan      DECIMAL(5,1),
  tinggi_badan     DECIMAL(5,1),
  suhu             DECIMAL(4,1),
  nadi             INT,
  saturasi_o2      INT,
  catatan_dokter   TEXT,
  status           ENUM('menunggu','periksa','selesai') NOT NULL DEFAULT 'menunggu',
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (pasien_id)  REFERENCES pasien(id),
  FOREIGN KEY (dokter_id)  REFERENCES dokter(id),
  FOREIGN KEY (perawat_id) REFERENCES perawat(id)
) ENGINE=InnoDB;

-- 8. RESEP_OBAT
CREATE TABLE resep_obat (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  kunjungan_id INT NOT NULL,
  obat_id      INT NOT NULL,
  jumlah       INT NOT NULL DEFAULT 1,
  aturan_pakai VARCHAR(100),
  keterangan   VARCHAR(200),
  FOREIGN KEY (kunjungan_id) REFERENCES kunjungan(id) ON DELETE CASCADE,
  FOREIGN KEY (obat_id)      REFERENCES obat(id)
) ENGINE=InnoDB;

-- 9. PEMBAYARAN
CREATE TABLE pembayaran (
  id                INT AUTO_INCREMENT PRIMARY KEY,
  no_invoice        VARCHAR(20) NOT NULL UNIQUE,
  kunjungan_id      INT NOT NULL UNIQUE,
  tanggal_bayar     DATE NOT NULL,
  biaya_konsultasi  DECIMAL(10,0) NOT NULL DEFAULT 0,
  biaya_obat        DECIMAL(10,0) NOT NULL DEFAULT 0,
  total_tagihan     DECIMAL(10,0) NOT NULL DEFAULT 0,
  metode_bayar      ENUM('tunai','transfer','bpjs') NOT NULL DEFAULT 'tunai',
  uang_diterima     DECIMAL(10,0),
  kembalian         DECIMAL(10,0),
  status            ENUM('lunas','pending') NOT NULL DEFAULT 'pending',
  petugas_id        INT,
  created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (kunjungan_id) REFERENCES kunjungan(id),
  FOREIGN KEY (petugas_id)   REFERENCES users(id)
) ENGINE=InnoDB;

-- 10. STOK_LOG
CREATE TABLE stok_log (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  obat_id       INT NOT NULL,
  tanggal       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  jenis_mutasi  ENUM('masuk','keluar','adjustment') NOT NULL,
  jumlah        INT NOT NULL,
  keterangan    VARCHAR(200),
  referensi_id  INT,
  petugas_id    INT,
  FOREIGN KEY (obat_id)    REFERENCES obat(id),
  FOREIGN KEY (petugas_id) REFERENCES users(id)
) ENGINE=InnoDB;
