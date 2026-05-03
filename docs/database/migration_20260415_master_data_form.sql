-- Jalankan sekali pada database yang SUDAH dibuat dari schema lama.
-- Tujuan: menyesuaikan master data pasien, dokter, dan obat
-- agar kompatibel dengan form CRUD versi terbaru di aplikasi.

USE db_gaharu_medika;

ALTER TABLE pasien
  MODIFY COLUMN golongan_darah VARCHAR(20) NULL;

UPDATE pasien
SET golongan_darah = 'Tidak Diketahui'
WHERE golongan_darah IS NULL
   OR golongan_darah NOT IN ('A+','A-','B+','B-','AB+','AB-','O+','O-','Tidak Diketahui');

UPDATE pasien
SET no_rm = CONCAT('RM', LPAD(CAST(REPLACE(no_rm, 'RM-', '') AS UNSIGNED), 7, '0'))
WHERE no_rm LIKE 'RM-%';

ALTER TABLE pasien
  MODIFY COLUMN no_rm VARCHAR(10) NOT NULL,
  MODIFY COLUMN golongan_darah ENUM('A+','A-','B+','B-','AB+','AB-','O+','O-','Tidak Diketahui') NULL,
  ADD COLUMN provinsi VARCHAR(60) NULL AFTER kota,
  ADD COLUMN kode_pos VARCHAR(10) NULL AFTER provinsi,
  ADD COLUMN nama_kontak_darurat VARCHAR(100) NULL AFTER no_hp,
  ADD COLUMN pekerjaan VARCHAR(100) NULL AFTER hubungan_darurat,
  MODIFY COLUMN jenis_pasien ENUM('umum','bpjs','asuransi') NOT NULL DEFAULT 'umum';

ALTER TABLE dokter
  ADD COLUMN poli_unit VARCHAR(60) NULL AFTER spesialisasi;

ALTER TABLE obat
  ADD COLUMN kandungan_dosis VARCHAR(100) NULL AFTER satuan,
  ADD COLUMN supplier VARCHAR(100) NULL AFTER kadaluarsa,
  ADD COLUMN keterangan TEXT NULL AFTER lokasi_rak;
