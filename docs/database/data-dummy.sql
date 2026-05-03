-- ============================================
-- DUMMY DATA: db_gaharu_medika (v2 - FIXED)
-- Jalankan setelah database.sql pada database fresh
-- ============================================
-- Perbaikan dari versi sebelumnya:
--   1. kunjungan: hanya assign dokter aktif + perawat aktif/tidak cuti
--   2. kunjungan: pasien 1-5 punya >1 kunjungan (realistis)
--   3. resep_obat: hanya untuk kunjungan status='selesai' (kunjungan 1-22)
--                 kunjungan 1-8 punya 2 item resep, sisanya 1 item
--   4. pembayaran: hanya untuk kunjungan status='selesai' (22 baris)
--                 biaya_obat dihitung dari resep x harga_jual obat
--   5. stok_log: 20 masuk + 7 keluar + 3 adjustment = 30 baris
--                keluar di-link ke kunjungan yang benar via referensi_id
--   6. obat.stok_saat_ini mencerminkan masuk-keluar ± adjustment
--   7. jadwal_dokter: dokter 1-5 punya 2 jadwal (hari berbeda)
--   8. 3 obat sengaja low-stock untuk testing alert: obat 4, 14, 24
--
-- Catatan login:
--   admin   / admin123
--   loket   / loket123
--   farmasi / farmasi123
--   kasir   / kasir123
--
-- CATATAN KEAMANAN: password_hash menyimpan plain text untuk DEV only.
--   Di PRODUCTION wajib hash menggunakan bcrypt / Argon2.
-- ============================================

USE db_gaharu_medika;

DELIMITER $$

DROP PROCEDURE IF EXISTS seed_master_data $$
CREATE PROCEDURE seed_master_data()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE created_time DATETIME;
  DECLARE stock_qty INT;

  -- ============================================
  -- 1. USERS (30 data)
  -- ============================================
  SET i = 1;
  WHILE i <= 30 DO
    SET created_time = DATE_ADD('2026-04-01 08:00:00', INTERVAL (i - 1) * 5 MINUTE);
    INSERT INTO users (id, username, password_hash, nama_lengkap, role, is_active, created_at)
    VALUES (
      i,
      CASE
        WHEN i = 1 THEN 'admin'
        WHEN i = 2 THEN 'loket'
        WHEN i = 3 THEN 'farmasi'
        WHEN i = 4 THEN 'kasir'
        WHEN i = 5 THEN 'auditor'
        ELSE CONCAT('petugas', LPAD(i, 2, '0'))
      END,
      CASE
        WHEN i = 1 THEN 'admin123'
        WHEN i = 2 THEN 'loket123'
        WHEN i = 3 THEN 'farmasi123'
        WHEN i = 4 THEN 'kasir123'
        WHEN i = 5 THEN 'auditor123'
        ELSE CONCAT('petugas', LPAD(i, 2, '0'), '123')
      END,
      CASE
        WHEN i = 1 THEN 'Siti Rahmawati'
        WHEN i = 2 THEN 'Rian Saputra'
        WHEN i = 3 THEN 'Dewi Anggraini'
        WHEN i = 4 THEN 'Bima Prakoso'
        WHEN i = 5 THEN 'Farhan Maulana'
        ELSE CONCAT('Petugas Klinik ', LPAD(i, 2, '0'))
      END,
      CASE WHEN i = 1 THEN 'admin' ELSE 'petugas' END,
      CASE WHEN i IN (18, 27) THEN 0 ELSE 1 END,
      created_time
    );
    SET i = i + 1;
  END WHILE;

  -- ============================================
  -- 2. PASIEN (30 data)
  -- ============================================
  SET i = 1;
  WHILE i <= 30 DO
    SET created_time = DATE_ADD('2026-04-01 08:30:00', INTERVAL (i - 1) * 5 MINUTE);
    INSERT INTO pasien (
      id, no_rm, nik, nama, jenis_kelamin, tanggal_lahir, tempat_lahir, golongan_darah,
      alamat, kelurahan, kecamatan, kota, provinsi, kode_pos, no_hp, nama_kontak_darurat,
      no_hp_darurat, hubungan_darurat, pekerjaan, alergi_obat, riwayat_penyakit,
      jenis_pasien, no_bpjs, created_at, updated_at
    )
    VALUES (
      i,
      CONCAT('RM', LPAD(i, 7, '0')),
      CONCAT('3175', LPAD(10 + MOD(i, 90), 2, '0'), LPAD(MOD(i, 12) + 1, 2, '0'),
             LPAD(MOD(i, 28) + 1, 2, '0'), '000', LPAD(i, 3, '0')),
      CASE
        WHEN i = 1  THEN 'Ahmad Fauzi'       WHEN i = 2  THEN 'Sari Wulandari'
        WHEN i = 3  THEN 'Budi Santoso'      WHEN i = 4  THEN 'Lina Marlina'
        WHEN i = 5  THEN 'Dimas Pratama'     WHEN i = 6  THEN 'Nabila Putri'
        WHEN i = 7  THEN 'Rudi Hartono'      WHEN i = 8  THEN 'Maya Salsabila'
        WHEN i = 9  THEN 'Fajar Ramadhan'    WHEN i = 10 THEN 'Intan Permata'
        ELSE CONCAT('Pasien Dummy ', LPAD(i, 2, '0'))
      END,
      CASE WHEN MOD(i, 2) = 1 THEN 'L' ELSE 'P' END,
      STR_TO_DATE(CONCAT(1980 + MOD(i, 20), '-', LPAD(MOD(i, 12) + 1, 2, '0'), '-',
                         LPAD(MOD(i, 28) + 1, 2, '0')), '%Y-%m-%d'),
      ELT(MOD(i-1,10)+1,'Jakarta','Bandung','Bogor','Bekasi','Depok',
                         'Tangerang','Cirebon','Tasikmalaya','Sukabumi','Karawang'),
      ELT(MOD(i-1,9)+1,'O+','A+','B+','AB+','O-','A-','B-','AB-','Tidak Diketahui'),
      CONCAT('Jl Contoh Sehat No ', i),
      ELT(MOD(i-1,10)+1,'Cempaka Putih','Sukamaju','Tanah Sareal','Harapan Jaya','Beji',
                         'Rawamangun','Kejaksan','Kawalu','Cikole','Karang Tengah'),
      ELT(MOD(i-1,10)+1,'Cempaka Putih','Cibeunying','Tanah Sareal','Bekasi Utara','Beji',
                         'Pulogadung','Kejaksan','Kawalu','Cikole','Karang Tengah'),
      ELT(MOD(i-1,10)+1,'Jakarta Pusat','Bandung','Bogor','Bekasi','Depok',
                         'Jakarta Timur','Cirebon','Tasikmalaya','Sukabumi','Tangerang'),
      ELT(MOD(i-1,10)+1,'DKI Jakarta','Jawa Barat','Jawa Barat','Jawa Barat','Jawa Barat',
                         'DKI Jakarta','Jawa Barat','Jawa Barat','Jawa Barat','Banten'),
      CONCAT('4', LPAD(1000 + i, 4, '0')),
      CONCAT('08121111', LPAD(i, 4, '0')),
      CASE
        WHEN i = 1  THEN 'Hendra Fauzi'      WHEN i = 2  THEN 'Rina Wulandari'
        WHEN i = 3  THEN 'Dewi Santoso'      WHEN i = 4  THEN 'Asep Marlina'
        WHEN i = 5  THEN 'Bagus Pratama'     WHEN i = 6  THEN 'Tari Putri'
        WHEN i = 7  THEN 'Yusuf Hartono'     WHEN i = 8  THEN 'Alya Salsabila'
        WHEN i = 9  THEN 'Rina Ramadhan'     WHEN i = 10 THEN 'Dian Permata'
        ELSE CONCAT('Kontak Darurat ', LPAD(i, 2, '0'))
      END,
      CONCAT('08131111', LPAD(i, 4, '0')),
      ELT(MOD(i-1,7)+1,'Ayah','Ibu','Suami','Istri','Anak','Kakak','Adik'),
      ELT(MOD(i-1,10)+1,'Karyawan Swasta','Ibu Rumah Tangga','Wiraswasta','Guru','Mahasiswa',
                         'Pegawai Negeri','Pedagang','Desainer','Perawat','Teknisi'),
      ELT(MOD(i-1,7)+1,'Tidak ada','Penicillin','Aspirin','Ibuprofen','Debu','Seafood','Sulfa'),
      ELT(MOD(i-1,7)+1,'Tidak ada','Maag','Hipertensi','Asma','Diabetes melitus tipe 2',
                        'Alergi debu','Kolesterol'),
      CASE
        WHEN MOD(i, 5) = 0 THEN 'asuransi'
        WHEN MOD(i, 3) = 0 THEN 'bpjs'
        ELSE 'umum'
      END,
      CASE
        WHEN MOD(i, 5) = 0 THEN CONCAT('ASN-', LPAD(i, 6, '0'))
        WHEN MOD(i, 3) = 0 THEN CONCAT('0001456799', LPAD(i, 3, '0'))
        ELSE NULL
      END,
      created_time, created_time
    );
    SET i = i + 1;
  END WHILE;

  -- ============================================
  -- 3. DOKTER (30 data)
  -- nonaktif: id 12, 21, 30 — tidak boleh di-assign ke kunjungan
  -- ============================================
  SET i = 1;
  WHILE i <= 30 DO
    SET created_time = DATE_ADD('2026-04-01 09:10:00', INTERVAL (i - 1) * 5 MINUTE);
    INSERT INTO dokter (id, kode_dokter, nama, spesialisasi, poli_unit, no_str, no_sip, no_hp,
                        tarif_konsultasi, status, created_at)
    VALUES (
      i,
      CONCAT('DR', LPAD(i, 3, '0')),
      CASE
        WHEN i = 1  THEN 'dr. Andi Saputra'     WHEN i = 2  THEN 'dr. Ratna Permata'
        WHEN i = 3  THEN 'dr. Yoga Prasetyo'     WHEN i = 4  THEN 'dr. Fajar Nugroho'
        WHEN i = 5  THEN 'dr. Anita Maharani'    WHEN i = 6  THEN 'dr. Dedi Kurniawan'
        WHEN i = 7  THEN 'dr. Lestari Wibowo'    WHEN i = 8  THEN 'dr. Rahmat Hidayat'
        WHEN i = 9  THEN 'dr. Cindy Amelia'      WHEN i = 10 THEN 'dr. Bimo Prakoso'
        ELSE CONCAT('dr. Dokter ', LPAD(i, 2, '0'))
      END,
      ELT(MOD(i-1,10)+1,'Umum','Penyakit Dalam','Anak','Jantung','Saraf',
                         'Kulit','THT','Mata','Gigi','Paru'),
      ELT(MOD(i-1,10)+1,'Poli Umum','Poli Penyakit Dalam','Poli Anak','Poli Jantung','Poli Saraf',
                         'Poli Kulit','Poli THT','Poli Mata','Poli Gigi','Poli Paru'),
      CONCAT('STR-2026-', LPAD(i, 3, '0')),
      CONCAT('SIP-2026-', LPAD(i, 3, '0')),
      CONCAT('08122222', LPAD(i, 4, '0')),
      150000 + MOD(i-1, 10) * 10000,
      CASE WHEN i IN (12, 21, 30) THEN 'nonaktif' ELSE 'aktif' END,
      created_time
    );
    SET i = i + 1;
  END WHILE;

  -- ============================================
  -- 4. JADWAL_DOKTER (30 data)
  -- dokter 1-5 punya 2 jadwal (hari berbeda)
  -- jadwal id 1-25: satu jadwal per dokter 1-25
  -- jadwal id 26-30: jadwal ke-2 untuk dokter 1-5
  -- ============================================
  SET i = 1;
  WHILE i <= 30 DO
    INSERT INTO jadwal_dokter (id, dokter_id, hari, jam_mulai, jam_selesai, kuota_pasien)
    VALUES (
      i,
      CASE WHEN i <= 25 THEN i ELSE i - 25 END,
      CASE
        WHEN i <= 25 THEN ELT(MOD(i-1,6)+1,'Senin','Selasa','Rabu','Kamis','Jumat','Sabtu')
        ELSE ELT(MOD(i,6)+1,'Senin','Selasa','Rabu','Kamis','Jumat','Sabtu')
      END,
      CASE
        WHEN MOD(i-1,3) = 0 THEN '08:00:00'
        WHEN MOD(i-1,3) = 1 THEN '13:00:00'
        ELSE '09:00:00'
      END,
      CASE
        WHEN MOD(i-1,3) = 0 THEN '12:00:00'
        WHEN MOD(i-1,3) = 1 THEN '17:00:00'
        ELSE '12:00:00'
      END,
      18 + MOD(i, 8)
    );
    SET i = i + 1;
  END WHILE;

  -- ============================================
  -- 5. PERAWAT (30 data)
  -- cuti: id 9, 19 — nonaktif: id 28 — tidak di-assign ke kunjungan
  -- ============================================
  SET i = 1;
  WHILE i <= 30 DO
    SET created_time = DATE_ADD('2026-04-01 09:30:00', INTERVAL (i - 1) * 5 MINUTE);
    INSERT INTO perawat (id, kode_perawat, nama, no_sipp, shift, poli_tugas, no_hp,
                         status, created_at)
    VALUES (
      i,
      CONCAT('PR', LPAD(i, 3, '0')),
      CASE
        WHEN i = 1  THEN 'Nur Aisyah'       WHEN i = 2  THEN 'Tono Wijaya'
        WHEN i = 3  THEN 'Mila Safitri'     WHEN i = 4  THEN 'Yuni Lestari'
        WHEN i = 5  THEN 'Raka Saputra'     WHEN i = 6  THEN 'Dewi Kartika'
        WHEN i = 7  THEN 'Beni Setiawan'    WHEN i = 8  THEN 'Lusi Handayani'
        WHEN i = 9  THEN 'Arum Melati'      WHEN i = 10 THEN 'Rizal Hidayat'
        ELSE CONCAT('Perawat ', LPAD(i, 2, '0'))
      END,
      CONCAT('SIPP-PR-', LPAD(i, 3, '0')),
      ELT(MOD(i-1,3)+1,'pagi','sore','malam'),
      ELT(MOD(i-1,6)+1,'Poli Umum','Poli Penyakit Dalam','Poli Anak',
                        'Poli Jantung','Poli Saraf','Poli Kulit'),
      CONCAT('08123333', LPAD(i, 4, '0')),
      CASE WHEN i IN (9, 19) THEN 'cuti' WHEN i = 28 THEN 'nonaktif' ELSE 'aktif' END,
      created_time
    );
    SET i = i + 1;
  END WHILE;

  -- ============================================
  -- 6. OBAT (30 data)
  -- stok_saat_ini = masuk (stok_log) - keluar (resep) ± adjustment
  -- obat 4, 14, 24 sengaja low-stock untuk testing alert
  -- ============================================
  SET i = 1;
  WHILE i <= 30 DO
    SET created_time = DATE_ADD('2026-04-01 10:00:00', INTERVAL (i - 1) * 5 MINUTE);
    -- stok_saat_ini sudah memperhitungkan semua transaksi di stok_log & resep_obat
    SET stock_qty = CASE
      WHEN i = 1  THEN 98   -- masuk 100, keluar 2 (resep id 1)
      WHEN i = 2  THEN 99   -- masuk 100, keluar 1 (resep id 3)
      WHEN i = 3  THEN 97   -- masuk 100, keluar 3 (resep id 5)
      WHEN i = 4  THEN 8    -- masuk 10 (batch kecil), keluar 2 → LOW STOCK (min=14)
      WHEN i = 5  THEN 99   -- masuk 100, keluar 1 (resep id 9)
      WHEN i = 6  THEN 97   -- masuk 100, keluar 3 (resep id 11)
      WHEN i = 7  THEN 98   -- masuk 100, keluar 2 (resep id 13)
      WHEN i = 8  THEN 99   -- masuk 100, keluar 1 (resep id 15)
      WHEN i = 9  THEN 98   -- masuk 100, keluar 2 (resep id 17)
      WHEN i = 10 THEN 99   -- masuk 100, keluar 1 (resep id 18)
      WHEN i = 11 THEN 89   -- masuk 80, keluar 1 (resep id 2), adjustment +10
      WHEN i = 12 THEN 78   -- masuk 80, keluar 2 (resep id 4)
      WHEN i = 13 THEN 79   -- masuk 80, keluar 1 (resep id 6)
      WHEN i = 14 THEN 9    -- masuk 10 (batch kecil), keluar 1 → LOW STOCK (min=12)
      WHEN i = 15 THEN 78   -- masuk 80, keluar 2 (resep id 10)
      WHEN i = 16 THEN 79   -- masuk 80, keluar 1 (resep id 12)
      WHEN i = 17 THEN 79   -- masuk 80, keluar 1 (resep id 14)
      WHEN i = 18 THEN 77   -- masuk 80, keluar 3 (resep id 16)
      WHEN i = 19 THEN 78   -- masuk 80, keluar 2 (resep id 19)
      WHEN i = 20 THEN 79   -- masuk 80, keluar 1 (resep id 20)
      WHEN i = 21 THEN 48   -- opening 50, keluar 2 (resep id 21)
      WHEN i = 22 THEN 49   -- opening 50, keluar 1 (resep id 22)
      WHEN i = 23 THEN 47   -- opening 50, keluar 3 (resep id 23)
      WHEN i = 24 THEN 6    -- opening 8, keluar 2 (resep id 24) → LOW STOCK (min=10)
      WHEN i = 25 THEN 49   -- opening 50, keluar 1 (resep id 25)
      WHEN i = 26 THEN 48   -- opening 50, keluar 2 (resep id 26)
      WHEN i = 27 THEN 49   -- opening 50, keluar 1 (resep id 27)
      WHEN i = 28 THEN 47   -- opening 50, keluar 3 (resep id 28)
      WHEN i = 29 THEN 53   -- opening 50, keluar 2 (resep id 29), adjustment +5
      WHEN i = 30 THEN 47   -- opening 50, keluar 1 (resep id 30), adjustment -2
    END;

    INSERT INTO obat (id, kode_obat, nama_obat, jenis, satuan, kandungan_dosis, harga_beli, harga_jual,
                      stok_saat_ini, stok_minimum, kadaluarsa, supplier, lokasi_rak, keterangan, created_at, updated_at)
    VALUES (
      i,
      CONCAT('OB', LPAD(i, 3, '0')),
      CASE
        WHEN i = 1  THEN 'Paracetamol 500 mg'    WHEN i = 2  THEN 'Amoxicillin 500 mg'
        WHEN i = 3  THEN 'Antasida DOEN'          WHEN i = 4  THEN 'Cetirizine 10 mg'
        WHEN i = 5  THEN 'Omeprazole 20 mg'       WHEN i = 6  THEN 'Salbutamol Syrup'
        WHEN i = 7  THEN 'Asam Mefenamat 500 mg'  WHEN i = 8  THEN 'Vitamin C 500 mg'
        WHEN i = 9  THEN 'Oralit Sachet'           WHEN i = 10 THEN 'CTM'
        ELSE CONCAT('Obat Dummy ', LPAD(i, 2, '0'))
      END,
      CASE
        WHEN i IN (1,3,4,7,10)  THEN 'Tablet'
        WHEN i IN (2,5)         THEN 'Kapsul'
        WHEN i IN (6,8)         THEN 'Sirup'
        WHEN i = 9              THEN 'Sachet'
        ELSE ELT(MOD(i-1,6)+1,'Tablet','Kapsul','Sirup','Sachet','Cairan','Salep')
      END,
      CASE
        WHEN i IN (1,2,3,4,5,7,10) THEN 'Strip'
        WHEN i IN (6,8)             THEN 'Botol'
        WHEN i = 9                  THEN 'Box'
        ELSE ELT(MOD(i-1,5)+1,'Strip','Botol','Box','Vial','Tube')
      END,
      CASE
        WHEN i = 1  THEN '500 mg'             WHEN i = 2  THEN '500 mg'
        WHEN i = 3  THEN '200 mg / 200 mg'    WHEN i = 4  THEN '10 mg'
        WHEN i = 5  THEN '20 mg'              WHEN i = 6  THEN '2 mg / 5 ml'
        WHEN i = 7  THEN '500 mg'             WHEN i = 8  THEN '500 mg / 5 ml'
        WHEN i = 9  THEN '200 ml / sachet'    WHEN i = 10 THEN '4 mg'
        ELSE ELT(MOD(i-1,8)+1,'500 mg','250 mg','5 mg','10 mg','60 ml','100 ml','1 g','2 mg / 5 ml')
      END,
      7000 + i * 1500,
      12000 + i * 1500 + MOD(i, 4) * 1000,
      stock_qty,
      10 + MOD(i, 6),
      STR_TO_DATE(CONCAT(2027 + MOD(i,2), '-', LPAD(MOD(i+2,12)+1,2,'0'), '-28'), '%Y-%m-%d'),
      ELT(MOD(i-1,8)+1,'PT Sehat Sentosa','PT Farma Utama','CV Medika Jaya','PT Nusantara Farma',
                         'PT Sumber Waras','CV Gaharu Distribusi','PT Prima Husada','PT Kimia Sejahtera'),
      ELT(MOD(i-1,15)+1,'A1','A2','A3','A4','A5','B1','B2','B3','B4','B5',
                         'C1','C2','C3','C4','C5'),
      ELT(MOD(i-1,8)+1,'Simpan di suhu ruang','Jauhkan dari sinar matahari langsung',
                         'Simpan di tempat kering','Periksa tanggal kedaluwarsa sebelum serah',
                         'Obat keras, gunakan sesuai resep','Simpan rapat setelah dibuka',
                         'Pisahkan dari obat sirup','Susun berdasarkan FEFO'),
      created_time, created_time
    );
    SET i = i + 1;
  END WHILE;

END $$

DELIMITER ;

START TRANSACTION;
CALL seed_master_data();
COMMIT;
DROP PROCEDURE IF EXISTS seed_master_data;


-- ============================================
-- 7. KUNJUNGAN (30 data) — static INSERT
-- ============================================
-- Desain:
--   pasien 1  → kunjungan 1 (baru), 2 (kontrol), 3 (kontrol) — 3 kunjungan
--   pasien 2  → kunjungan 4 (baru), 5 (kontrol) — 2 kunjungan
--   pasien 3  → kunjungan 6 (baru), 7 (kontrol) — 2 kunjungan (pasien BPJS)
--   pasien 4  → kunjungan 8 (baru), 9 (kontrol) — 2 kunjungan
--   pasien 5  → kunjungan 10 (baru), 11 (kontrol) — 2 kunjungan
--   pasien 6-24 → kunjungan 12-30 (1 kunjungan masing-masing)
--
--   dokter aktif yang digunakan: 1-11, 13-16 (skip nonaktif: 12, 21, 30)
--   perawat aktif yang digunakan: 1-8 cycling (skip cuti/nonaktif: 9, 19, 28)
--
--   status: kunjungan 1-22 = selesai, 23-26 = periksa, 27-30 = menunggu
-- ============================================

INSERT INTO kunjungan (
  id, no_antrian, no_kunjungan, pasien_id, dokter_id, perawat_id,
  tanggal_kunjungan, jenis_kunjungan, keluhan_utama, diagnosa, kode_icd10,
  tekanan_darah, berat_badan, tinggi_badan, suhu, nadi, saturasi_o2,
  catatan_dokter, status, created_at
) VALUES
-- Pasien 1 (Ahmad Fauzi) — 3 kunjungan
(1,  'A001', 'KJ260401001', 1,  1, 1, '2026-04-01', 'baru',
 'Demam dan batuk sejak 2 hari',
 'Influenza', 'J11', '120/80', 70.0, 170.0, 38.2, 88, 97,
 'Istirahat cukup, minum air putih, kontrol jika tidak membaik.',
 'selesai', '2026-04-01 09:15:00'),

(2,  'A001', 'KJ260405001', 1,  1, 2, '2026-04-05', 'kontrol',
 'Demam sudah turun, batuk masih ada',
 'Influenza', 'J11', '118/78', 70.0, 170.0, 37.1, 82, 98,
 'Kondisi membaik. Lanjut obat batuk 3 hari.',
 'selesai', '2026-04-05 09:20:00'),

(3,  'A001', 'KJ260408001', 1,  2, 3, '2026-04-08', 'kontrol',
 'Batuk masih ada, sedikit pilek',
 'ISPA', 'J06', '119/79', 70.0, 170.0, 36.8, 80, 98,
 'Kontrol 1 minggu jika batuk belum reda.',
 'selesai', '2026-04-08 09:10:00'),

-- Pasien 2 (Sari Wulandari) — 2 kunjungan
(4,  'A002', 'KJ260401002', 2,  2, 4, '2026-04-01', 'baru',
 'Nyeri ulu hati disertai mual sejak semalam',
 'Gastritis', 'K29', '110/70', 55.0, 158.0, 36.7, 78, 98,
 'Hindari makanan pedas dan asam. Makan teratur.',
 'selesai', '2026-04-01 10:05:00'),

(5,  'A002', 'KJ260405002', 2,  3, 5, '2026-04-05', 'kontrol',
 'Nyeri ulu hati berkurang, masih mual ringan',
 'Gastritis', 'K29', '112/72', 55.0, 158.0, 36.6, 76, 99,
 'Perbaikan signifikan. Lanjut omeprazole 1 minggu.',
 'selesai', '2026-04-05 10:15:00'),

-- Pasien 3 (Budi Santoso) — 2 kunjungan, BPJS (pasien_id 3 = MOD(3,3)=0 → bpjs)
(6,  'A001', 'KJ260402001', 3,  3, 6, '2026-04-02', 'baru',
 'Sesak napas ringan saat malam hari',
 'Asma', 'J45', '125/82', 68.0, 165.0, 36.9, 90, 96,
 'Hindari pemicu alergi. Gunakan inhaler sesuai anjuran.',
 'selesai', '2026-04-02 09:30:00'),

(7,  'A001', 'KJ260406001', 3,  4, 7, '2026-04-06', 'kontrol',
 'Sesak napas berkurang, masih kadang muncul malam',
 'Asma', 'J45', '122/80', 68.0, 165.0, 36.7, 86, 97,
 'Kondisi terkontrol. Lanjut bronkodilator.',
 'selesai', '2026-04-06 09:25:00'),

-- Pasien 4 (Lina Marlina) — 2 kunjungan
(8,  'A002', 'KJ260402002', 4,  4, 8, '2026-04-02', 'baru',
 'Pusing dan tekanan darah tinggi',
 'Hipertensi', 'I10', '155/95', 72.0, 160.0, 36.5, 85, 98,
 'Kurangi garam dan stress. Olahraga ringan teratur.',
 'selesai', '2026-04-02 10:20:00'),

(9,  'A002', 'KJ260406002', 4,  5, 1, '2026-04-06', 'kontrol',
 'Pusing berkurang, tekanan darah mulai turun',
 'Hipertensi', 'I10', '140/88', 72.0, 160.0, 36.6, 82, 98,
 'TD membaik. Pertahankan pola makan rendah garam.',
 'selesai', '2026-04-06 10:30:00'),

-- Pasien 5 (Dimas Pratama) — 2 kunjungan
(10, 'A003', 'KJ260402003', 5,  5, 2, '2026-04-02', 'baru',
 'Pilek alergi dan gatal pada hidung',
 'Rhinitis alergi', 'J30', '115/75', 65.0, 172.0, 36.6, 80, 99,
 'Hindari debu dan bulu hewan. Pakai masker di luar.',
 'selesai', '2026-04-02 11:00:00'),

(11, 'A001', 'KJ260407001', 5,  6, 3, '2026-04-07', 'kontrol',
 'Pilek berkurang, masih kadang bersin',
 'Rhinitis alergi', 'J30', '116/76', 65.0, 172.0, 36.5, 78, 99,
 'Gejala terkontrol. Antihistamin lanjut 1 minggu.',
 'selesai', '2026-04-07 09:00:00'),

-- Pasien 6-22: masing-masing 1 kunjungan (kunjungan 12-28)
(12, 'A001', 'KJ260403001', 6,  6, 4, '2026-04-03', 'baru',
 'Perut mulas dan diare sejak subuh',
 'Gastroenteritis', 'A09', '108/68', 58.0, 155.0, 37.0, 88, 97,
 'Perbanyak cairan dan oralit. Hindari makanan berminyak.',
 'selesai', '2026-04-03 09:15:00'),

(13, 'A002', 'KJ260403002', 7,  7, 5, '2026-04-03', 'baru',
 'Demam dan nyeri tenggorokan sejak 2 hari',
 'Faringitis akut', 'J02', '118/76', 62.0, 168.0, 38.0, 90, 97,
 'Gargle air garam hangat. Istirahat dan minum cukup.',
 'selesai', '2026-04-03 10:00:00'),

(14, 'A003', 'KJ260403003', 8,  7, 6, '2026-04-03', 'baru',
 'Sakit kepala berulang sebelah kanan',
 'Migraine', 'G43', '120/78', 60.0, 162.0, 36.7, 82, 98,
 'Hindari pemicu migraine (kafein, stres). Istirahat di ruangan gelap.',
 'selesai', '2026-04-03 11:00:00'),

(15, 'A001', 'KJ260404001', 9,  8, 7, '2026-04-04', 'baru',
 'Nyeri sendi lutut setelah olahraga berat',
 'Myalgia', 'M79', '122/80', 78.0, 175.0, 36.6, 84, 98,
 'Kompres dingin, istirahat. Batasi aktivitas berat.',
 'selesai', '2026-04-04 09:30:00'),

(16, 'A002', 'KJ260404002', 10, 8, 8, '2026-04-04', 'baru',
 'Demam dan batuk berdahak sejak 3 hari',
 'Influenza', 'J11', '117/75', 55.0, 157.0, 38.5, 92, 96,
 'Banyak istirahat, perbanyak minum air hangat.',
 'selesai', '2026-04-04 10:15:00'),

(17, 'A003', 'KJ260404003', 11, 9, 1, '2026-04-04', 'baru',
 'Nyeri ulu hati dan kembung setelah makan',
 'Gastritis', 'K29', '113/73', 60.0, 163.0, 36.8, 80, 98,
 'Makan kecil-sering, hindari kopi dan alkohol.',
 'selesai', '2026-04-04 11:00:00'),

(18, 'A004', 'KJ260404004', 12, 9, 2, '2026-04-04', 'baru',
 'Sesak napas dan pilek, riwayat asma',
 'Asma', 'J45', '128/84', 64.0, 160.0, 37.2, 94, 96,
 'Gunakan spacer pada inhaler. Kontrol rutin.',
 'selesai', '2026-04-04 11:45:00'),

(19, 'A003', 'KJ260405003', 13, 10, 3, '2026-04-05', 'baru',
 'Pusing berkepanjangan dan mual ringan',
 'Migraine', 'G43', '118/76', 57.0, 159.0, 36.7, 78, 99,
 'Cukup tidur, hindari layar terlalu lama.',
 'selesai', '2026-04-05 09:00:00'),

(20, 'A004', 'KJ260405004', 14, 10, 4, '2026-04-05', 'baru',
 'Perut mulas dan mual sejak pagi',
 'Gastroenteritis', 'A09', '110/70', 65.0, 169.0, 37.1, 86, 98,
 'Perbanyak cairan elektrolit. Diet BRAT (pisang, nasi, apel, roti).',
 'selesai', '2026-04-05 10:00:00'),

(21, 'A005', 'KJ260405005', 15, 11, 5, '2026-04-05', 'baru',
 'Batuk dan pilek, tenggorokan gatal',
 'ISPA', 'J06', '115/73', 52.0, 153.0, 36.9, 80, 98,
 'Istirahat cukup. Vitamin C dan madu dapat membantu.',
 'selesai', '2026-04-05 11:00:00'),

(22, 'A003', 'KJ260406003', 16, 11, 6, '2026-04-06', 'baru',
 'Nyeri sendi pergelangan kaki setelah aktivitas',
 'Myalgia', 'M79', '119/77', 70.0, 167.0, 36.6, 82, 99,
 'Kompres dingin, elevasi kaki. Kurangi aktivitas berat.',
 'selesai', '2026-04-06 09:45:00'),

-- Kunjungan 23-26: status periksa (vital signs diisi, diagnosa belum)
(23, 'A002', 'KJ260408002', 17, 13, 7, '2026-04-08', 'baru',
 'Demam tinggi dan badan lemas sejak kemarin',
 NULL, NULL, '130/85', 66.0, 166.0, 39.1, 98, 96,
 NULL, 'periksa', '2026-04-08 10:00:00'),

(24, 'A003', 'KJ260408003', 18, 13, 8, '2026-04-08', 'baru',
 'Batuk berdahak dan nyeri dada ringan',
 NULL, NULL, '121/79', 72.0, 173.0, 37.8, 88, 97,
 NULL, 'periksa', '2026-04-08 10:45:00'),

(25, 'A001', 'KJ260409001', 19, 14, 1, '2026-04-09', 'baru',
 'Mual dan muntah, tidak nafsu makan',
 NULL, NULL, '109/68', 50.0, 152.0, 37.3, 84, 98,
 NULL, 'periksa', '2026-04-09 09:30:00'),

(26, 'A002', 'KJ260409002', 20, 14, 2, '2026-04-09', 'baru',
 'Nyeri kepala hebat dan penglihatan kabur',
 NULL, NULL, '148/96', 74.0, 170.0, 36.9, 92, 97,
 NULL, 'periksa', '2026-04-09 10:30:00'),

-- Kunjungan 27-30: status menunggu (hanya keluhan, vital signs belum)
(27, 'A001', 'KJ260410001', 21, 15, 3, '2026-04-10', 'baru',
 'Demam sejak tadi pagi dan pilek',
 NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 NULL, 'menunggu', '2026-04-10 08:30:00'),

(28, 'A002', 'KJ260410002', 22, 15, 4, '2026-04-10', 'baru',
 'Nyeri perut kanan bawah',
 NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 NULL, 'menunggu', '2026-04-10 09:00:00'),

(29, 'A001', 'KJ260411001', 23, 16, 5, '2026-04-11', 'baru',
 'Gatal-gatal di kulit lengan dan punggung',
 NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 NULL, 'menunggu', '2026-04-11 08:15:00'),

(30, 'A002', 'KJ260411002', 24, 16, 6, '2026-04-11', 'baru',
 'Kontrol rutin tekanan darah dan cek kolesterol',
 NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 NULL, 'menunggu', '2026-04-11 08:45:00');


-- ============================================
-- 8. RESEP_OBAT (30 data) — static INSERT
-- ============================================
-- Hanya untuk kunjungan status='selesai' (kunjungan 1-22)
-- Kunjungan 1-8: 2 item resep masing-masing (resep id 1-16)
-- Kunjungan 9-22: 1 item resep masing-masing (resep id 17-30)
--
-- Harga jual obat: 12000 + id*1500 + MOD(id,4)*1000
--   obat 1=14500  obat 2=17000  obat 3=19500  obat 4=18000
--   obat 5=20500  obat 6=23000  obat 7=25500  obat 8=24000
--   obat 9=26500  obat10=29000  obat11=31500  obat12=30000
--   obat13=32500  obat14=35000  obat15=37500  obat16=36000
--   obat17=38500  obat18=41000  obat19=43500  obat20=42000
--   obat21=44500  obat22=47000  obat23=49500  obat24=48000
--   obat25=50500  obat26=53000  obat27=55500  obat28=54000
--   obat29=56500  obat30=59000
-- ============================================

INSERT INTO resep_obat (id, kunjungan_id, obat_id, jumlah, aturan_pakai, keterangan) VALUES
-- kunjungan 1: 2 item → biaya_obat = 2×14500 + 1×31500 = 60500
(1,  1,  1,  2, '3x1 setelah makan',   'Antipiretik utama'),
(2,  1,  11, 1, '1x1 malam',           'Obat batuk malam'),
-- kunjungan 2: 2 item → 1×17000 + 2×30000 = 77000
(3,  2,  2,  1, '3x1 setelah makan',   'Antibiotik lanjutan'),
(4,  2,  12, 2, '2x1 setelah makan',   'Obat pendukung'),
-- kunjungan 3: 2 item → 3×19500 + 1×32500 = 91000
(5,  3,  3,  3, '3x1 setelah makan',   'Antasida lanjutan'),
(6,  3,  13, 1, '1x1 malam',           'Obat batuk'),
-- kunjungan 4: 2 item → 2×18000 + 1×35000 = 71000
(7,  4,  4,  2, '2x1 setelah makan',   'Antihistamin'),
(8,  4,  14, 1, '1x1 malam',           'Antasida'),
-- kunjungan 5: 2 item → 1×20500 + 2×37500 = 95500
(9,  5,  5,  1, '1x1 malam',           'PPI lanjutan'),
(10, 5,  15, 2, '3x1 setelah makan',   'Obat mual'),
-- kunjungan 6: 2 item → 3×23000 + 1×36000 = 105000 (pasien BPJS)
(11, 6,  6,  3, '3x5 ml',             'Bronkodilator'),
(12, 6,  16, 1, '1x1 malam',           'Kortikosteroid inhalasi'),
-- kunjungan 7: 2 item → 2×25500 + 1×38500 = 89500 (pasien BPJS)
(13, 7,  7,  2, '3x1 setelah makan',   'Analgesik'),
(14, 7,  17, 1, '2x1 setelah makan',   'Bronkodilator tablet'),
-- kunjungan 8: 2 item → 1×24000 + 3×41000 = 147000
(15, 8,  8,  1, '1x1 pagi',           'Suplemen'),
(16, 8,  18, 3, '3x1 setelah makan',   'Antihipertensi'),
-- kunjungan 9-22: masing-masing 1 item
(17, 9,  9,  2, '1 sachet bila perlu', 'Oralit rehidrasi'),         -- 53000
(18, 10, 10, 1, '3x1 setelah makan',   'Antihistamin'),             -- 29000
(19, 11, 19, 2, '2x1 setelah makan',   'Dekongestan'),              -- 87000
(20, 12, 20, 1, '1 sachet bila perlu', 'Oralit'),                   -- 42000
(21, 13, 21, 2, '3x1 setelah makan',   'Antibiotik faringitis'),    -- 89000
(22, 14, 22, 1, '1x1 malam',           'Analgesik migraine'),       -- 47000
(23, 15, 23, 3, '3x1 setelah makan',   'NSAID nyeri sendi'),        -- 148500
(24, 16, 24, 2, '2x1 setelah makan',   'Antihistamin'),             -- 96000
(25, 17, 25, 1, '1x1 malam',           'Antasida'),                 -- 50500
(26, 18, 26, 2, '3x5 ml',             'Bronkodilator sirup'),       -- 106000
(27, 19, 27, 1, '2x1 setelah makan',   'Analgesik'),               -- 55500
(28, 20, 28, 3, '1 sachet bila perlu', 'Rehidrasi oral'),           -- 162000
(29, 21, 29, 2, '3x1 setelah makan',   'Ekspektoran'),             -- 113000
(30, 22, 30, 1, '2x1 setelah makan',   'Analgesik topikal');       -- 59000


-- ============================================
-- 9. PEMBAYARAN (22 data) — static INSERT
-- ============================================
-- Hanya untuk kunjungan status='selesai' (kunjungan 1-22)
-- biaya_konsultasi = dokter.tarif_konsultasi
-- biaya_obat = SUM(resep.jumlah × obat.harga_jual)
-- total = biaya_konsultasi + biaya_obat
--
-- BPJS (pasien 3,6,9,12,15 → kunjungan 6,7,12,15,18,21):
--   uang_diterima=NULL, kembalian=NULL, status=lunas
-- Tunai lunas: uang=CEILING(total/50000)*50000
-- Transfer lunas: uang=total, kembalian=0
-- Pending (kunjungan 20,22): uang=NULL, kembalian=NULL
-- ============================================

INSERT INTO pembayaran (
  id, no_invoice, kunjungan_id, tanggal_bayar,
  biaya_konsultasi, biaya_obat, total_tagihan,
  metode_bayar, uang_diterima, kembalian, status, petugas_id, created_at
) VALUES
-- kunjungan 1: dok=1 tarif=150000, obat=60500, total=210500, tunai
(1,  'INV260401001', 1,  '2026-04-01', 150000,  60500, 210500, 'tunai',    250000,  39500, 'lunas',   4, '2026-04-01 11:30:00'),
-- kunjungan 2: dok=1 tarif=150000, obat=77000, total=227000, transfer
(2,  'INV260405001', 2,  '2026-04-05', 150000,  77000, 227000, 'transfer', 227000,      0, 'lunas',   4, '2026-04-05 11:00:00'),
-- kunjungan 3: dok=2 tarif=160000, obat=91000, total=251000, tunai
(3,  'INV260408001', 3,  '2026-04-08', 160000,  91000, 251000, 'tunai',    300000,  49000, 'lunas',   4, '2026-04-08 10:30:00'),
-- kunjungan 4: dok=2 tarif=160000, obat=71000, total=231000, tunai
(4,  'INV260401002', 4,  '2026-04-01', 160000,  71000, 231000, 'tunai',    250000,  19000, 'lunas',   4, '2026-04-01 12:00:00'),
-- kunjungan 5: dok=3 tarif=170000, obat=95500, total=265500, transfer
(5,  'INV260405002', 5,  '2026-04-05', 170000,  95500, 265500, 'transfer', 265500,      0, 'lunas',   4, '2026-04-05 11:30:00'),
-- kunjungan 6: dok=3 tarif=170000, obat=105000, total=275000, BPJS
(6,  'INV260402001', 6,  '2026-04-02', 170000, 105000, 275000, 'bpjs',       NULL,   NULL, 'lunas',   4, '2026-04-02 11:00:00'),
-- kunjungan 7: dok=4 tarif=180000, obat=89500, total=269500, BPJS
(7,  'INV260406001', 7,  '2026-04-06', 180000,  89500, 269500, 'bpjs',       NULL,   NULL, 'lunas',   4, '2026-04-06 11:00:00'),
-- kunjungan 8: dok=4 tarif=180000, obat=147000, total=327000, tunai
(8,  'INV260402002', 8,  '2026-04-02', 180000, 147000, 327000, 'tunai',    350000,  23000, 'lunas',   4, '2026-04-02 12:00:00'),
-- kunjungan 9: dok=5 tarif=190000, obat=53000, total=243000, transfer
(9,  'INV260406002', 9,  '2026-04-06', 190000,  53000, 243000, 'transfer', 243000,      0, 'lunas',   4, '2026-04-06 12:00:00'),
-- kunjungan 10: dok=5 tarif=190000, obat=29000, total=219000, tunai
(10, 'INV260402003', 10, '2026-04-02', 190000,  29000, 219000, 'tunai',    250000,  31000, 'lunas',   4, '2026-04-02 12:30:00'),
-- kunjungan 11: dok=6 tarif=200000, obat=87000, total=287000, transfer
(11, 'INV260407001', 11, '2026-04-07', 200000,  87000, 287000, 'transfer', 287000,      0, 'lunas',   4, '2026-04-07 10:30:00'),
-- kunjungan 12: dok=6 tarif=200000, obat=42000, total=242000, BPJS (pasien 6 = bpjs)
(12, 'INV260403001', 12, '2026-04-03', 200000,  42000, 242000, 'bpjs',       NULL,   NULL, 'lunas',   4, '2026-04-03 11:00:00'),
-- kunjungan 13: dok=7 tarif=210000, obat=89000, total=299000, tunai
(13, 'INV260403002', 13, '2026-04-03', 210000,  89000, 299000, 'tunai',    300000,   1000, 'lunas',   4, '2026-04-03 11:30:00'),
-- kunjungan 14: dok=7 tarif=210000, obat=47000, total=257000, transfer
(14, 'INV260403003', 14, '2026-04-03', 210000,  47000, 257000, 'transfer', 257000,      0, 'lunas',   4, '2026-04-03 12:00:00'),
-- kunjungan 15: dok=8 tarif=220000, obat=148500, total=368500, BPJS (pasien 9 = bpjs)
(15, 'INV260404001', 15, '2026-04-04', 220000, 148500, 368500, 'bpjs',       NULL,   NULL, 'lunas',   4, '2026-04-04 11:00:00'),
-- kunjungan 16: dok=8 tarif=220000, obat=96000, total=316000, tunai
(16, 'INV260404002', 16, '2026-04-04', 220000,  96000, 316000, 'tunai',    350000,  34000, 'lunas',   4, '2026-04-04 12:00:00'),
-- kunjungan 17: dok=9 tarif=230000, obat=50500, total=280500, transfer
(17, 'INV260404003', 17, '2026-04-04', 230000,  50500, 280500, 'transfer', 280500,      0, 'lunas',   4, '2026-04-04 13:00:00'),
-- kunjungan 18: dok=9 tarif=230000, obat=106000, total=336000, BPJS (pasien 12 = bpjs)
(18, 'INV260404004', 18, '2026-04-04', 230000, 106000, 336000, 'bpjs',       NULL,   NULL, 'lunas',   4, '2026-04-04 13:30:00'),
-- kunjungan 19: dok=10 tarif=240000, obat=55500, total=295500, tunai
(19, 'INV260405003', 19, '2026-04-05', 240000,  55500, 295500, 'tunai',    300000,   4500, 'lunas',   4, '2026-04-05 12:00:00'),
-- kunjungan 20: dok=10 tarif=240000, obat=162000, total=402000, transfer — PENDING
(20, 'INV260405004', 20, '2026-04-05', 240000, 162000, 402000, 'transfer',   NULL,   NULL, 'pending', 4, '2026-04-05 13:00:00'),
-- kunjungan 21: dok=11 tarif=150000, obat=113000, total=263000, BPJS (pasien 15 = bpjs)
(21, 'INV260405005', 21, '2026-04-05', 150000, 113000, 263000, 'bpjs',       NULL,   NULL, 'lunas',   4, '2026-04-05 13:30:00'),
-- kunjungan 22: dok=11 tarif=150000, obat=59000, total=209000, tunai — PENDING
(22, 'INV260406003', 22, '2026-04-06', 150000,  59000, 209000, 'tunai',      NULL,   NULL, 'pending', 4, '2026-04-06 11:00:00');


-- ============================================
-- 10. STOK_LOG (30 data) — static INSERT
-- ============================================
-- id  1-10: masuk obat 1-10 (pembelian batch 1, Maret 2026)
--           obat 4 sengaja qty kecil (10) → stok akan rendah
-- id 11-20: masuk obat 11-20 (pembelian batch 2, Maret 2026)
--           obat 14 sengaja qty kecil (10) → stok akan rendah
-- id 21-27: keluar (dispensing resep ke pasien)
--           referensi_id = kunjungan.id
-- id 28-30: adjustment koreksi stok opname
--
-- obat 21-30 tidak ada masuk di stok_log:
--   stok_saat_ini = opening balance (saat obat pertama diinput)
--   dikurangi resep keluar — dikelola oleh aplikasi
-- ============================================

INSERT INTO stok_log (
  id, obat_id, tanggal, jenis_mutasi, jumlah, keterangan, referensi_id, petugas_id
) VALUES
-- Batch 1: masuk obat 1-10 (pembelian PO Maret 2026)
(1,  1,  '2026-03-15 08:00:00', 'masuk', 100, 'Pembelian batch PO-1001', 1001, 3),
(2,  2,  '2026-03-15 09:00:00', 'masuk', 100, 'Pembelian batch PO-1001', 1001, 3),
(3,  3,  '2026-03-15 10:00:00', 'masuk', 100, 'Pembelian batch PO-1001', 1001, 3),
(4,  4,  '2026-03-15 11:00:00', 'masuk',  10, 'Pembelian batch PO-1001 (sisa stok distributor)', 1001, 3),
(5,  5,  '2026-03-15 12:00:00', 'masuk', 100, 'Pembelian batch PO-1001', 1001, 3),
(6,  6,  '2026-03-16 08:00:00', 'masuk', 100, 'Pembelian batch PO-1002', 1002, 3),
(7,  7,  '2026-03-16 09:00:00', 'masuk', 100, 'Pembelian batch PO-1002', 1002, 3),
(8,  8,  '2026-03-16 10:00:00', 'masuk', 100, 'Pembelian batch PO-1002', 1002, 3),
(9,  9,  '2026-03-16 11:00:00', 'masuk', 100, 'Pembelian batch PO-1002', 1002, 3),
(10, 10, '2026-03-16 12:00:00', 'masuk', 100, 'Pembelian batch PO-1002', 1002, 3),
-- Batch 2: masuk obat 11-20 (pembelian PO Maret 2026)
(11, 11, '2026-03-20 08:00:00', 'masuk',  80, 'Pembelian batch PO-1003', 1003, 3),
(12, 12, '2026-03-20 09:00:00', 'masuk',  80, 'Pembelian batch PO-1003', 1003, 3),
(13, 13, '2026-03-20 10:00:00', 'masuk',  80, 'Pembelian batch PO-1003', 1003, 3),
(14, 14, '2026-03-20 11:00:00', 'masuk',  10, 'Pembelian batch PO-1003 (sisa stok distributor)', 1003, 3),
(15, 15, '2026-03-20 12:00:00', 'masuk',  80, 'Pembelian batch PO-1003', 1003, 3),
(16, 16, '2026-03-21 08:00:00', 'masuk',  80, 'Pembelian batch PO-1004', 1004, 3),
(17, 17, '2026-03-21 09:00:00', 'masuk',  80, 'Pembelian batch PO-1004', 1004, 3),
(18, 18, '2026-03-21 10:00:00', 'masuk',  80, 'Pembelian batch PO-1004', 1004, 3),
(19, 19, '2026-03-21 11:00:00', 'masuk',  80, 'Pembelian batch PO-1004', 1004, 3),
(20, 20, '2026-03-21 12:00:00', 'masuk',  80, 'Pembelian batch PO-1004', 1004, 3),
-- Keluar: dispensing resep — referensi_id = kunjungan.id
(21, 1,  '2026-04-01 11:00:00', 'keluar',  2, 'Pemakaian resep kunjungan KJ260401001', 1,  3),
(22, 2,  '2026-04-05 10:30:00', 'keluar',  1, 'Pemakaian resep kunjungan KJ260405001', 2,  3),
(23, 3,  '2026-04-08 10:00:00', 'keluar',  3, 'Pemakaian resep kunjungan KJ260408001', 3,  3),
(24, 4,  '2026-04-01 11:30:00', 'keluar',  2, 'Pemakaian resep kunjungan KJ260401002', 4,  3),
(25, 5,  '2026-04-05 11:00:00', 'keluar',  1, 'Pemakaian resep kunjungan KJ260405002', 5,  3),
(26, 14, '2026-04-01 11:35:00', 'keluar',  1, 'Pemakaian resep kunjungan KJ260401002', 4,  3),
(27, 24, '2026-04-04 12:30:00', 'keluar',  2, 'Pemakaian resep kunjungan KJ260404002', 16, 3),
-- Adjustment: koreksi stok opname bulanan (7 April 2026)
(28, 29, '2026-04-07 09:00:00', 'adjustment',  5, 'Koreksi stok opname — stok fisik lebih 5', NULL, 3),
(29, 30, '2026-04-07 09:20:00', 'adjustment', -2, 'Koreksi stok opname — stok fisik kurang 2', NULL, 3),
(30, 11, '2026-04-07 09:40:00', 'adjustment', 10, 'Penambahan darurat dari apotek mitra (non-PO)', NULL, 3);
