# Report PDF di Project Ini

Dokumen ini menjelaskan teori dan implementasi report PDF yang dipakai di project Klinik Gaharu Medika.

Fokus dokumen ini:
- menjelaskan library apa yang dipakai untuk report PDF
- menjelaskan apakah kita memakai plugin, tool designer, atau library Java
- menjelaskan preview report pakai apa
- menjelaskan export atau download PDF pakai apa
- menjelaskan alur kerja report dari tombol Swing sampai file PDF jadi
- menjelaskan struktur file report yang sekarang ada di project

## 1. Jawaban Singkat

Untuk report PDF di project ini, stack yang dipakai adalah:

- `JasperReports` sebagai engine report utama di aplikasi Java
- `Jaspersoft Studio` sebagai tool designer report jika nanti mau desain visual `.jrxml`
- `JasperViewer` untuk preview report di desktop app
- `JasperExportManager` untuk export menjadi file `.pdf`

Jadi:
- yang benar-benar dipakai di runtime aplikasi adalah `JasperReports`
- yang dipakai untuk desain report secara visual adalah `Jaspersoft Studio`
- yang dipakai untuk preview adalah `JasperViewer`
- yang dipakai untuk menyimpan PDF adalah `JasperExportManager`

## 2. Apakah Ini Pakai Plugin?

Secara teknis, yang dipakai oleh aplikasi Java ini bukan plugin browser dan bukan plugin NetBeans wajib.

Yang dipakai ada 2 jenis:

### Runtime library

Ini library Java yang masuk ke project Maven dan dipakai saat aplikasi dijalankan.

Di project ini:

- `net.sf.jasperreports:jasperreports:6.21.3`

Dependency ini sekarang ada di:

- [`pom.xml`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/pom.xml)

Artinya:
- project bisa compile class Jasper
- aplikasi bisa generate report saat tombol report diklik
- preview dan export PDF dilakukan dari code Java

### Design tool

Kalau mau desain report dengan drag and drop, biasanya orang pakai:

- `Jaspersoft Studio`

Ini bukan bagian wajib dari runtime app.
Ini hanya tool bantu untuk membuat atau mengedit file `.jrxml`.

Jadi bedanya:
- `JasperReports` = mesin report di aplikasi
- `Jaspersoft Studio` = editor visual untuk bikin template report

Kalau mau, file `.jrxml` bisa dibuat:
- manual dengan editor teks
- atau visual lewat Jaspersoft Studio

Di project ini, template pertama dibuat dalam format `.jrxml`, jadi ke depan tetap kompatibel kalau nanti ingin dibuka lagi lewat Jaspersoft Studio.

## 3. Kenapa Tidak Pakai NetBeans GUI Builder untuk PDF

NetBeans GUI Builder dipakai untuk:
- `JFrame`
- `JPanel`
- layout form Swing
- tombol, tabel, input, panel

Tapi report PDF bukan UI Swing biasa.

PDF punya kebutuhan berbeda:
- ukuran halaman tetap
- header dan footer per halaman
- tabel yang bisa lanjut ke halaman berikutnya
- group, summary, total, page number
- layout print-friendly

Kalau dipaksa dibuat lewat Swing lalu dicetak ke PDF:
- layout cepat berantakan
- sulit bikin multi-page
- susah bikin summary dan footer halaman
- maintenance lebih berat

Karena itu arsitektur yang benar adalah:
- Swing tetap untuk layar filter dan tombol aksi
- JasperReports untuk dokumen report

## 4. Struktur Report PDF yang Dipakai Sekarang

Implementasi report PDF saat ini dibagi jadi beberapa layer:

### Layer UI

UI filter dan tombol ada di:

- [`LaporanPanel.java`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/panel/LaporanPanel.java)

Tanggung jawabnya:
- memilih periode tanggal
- menangani klik tombol `Preview`
- menangani klik tombol `PDF`
- menampilkan dialog simpan file
- menampilkan popup error atau sukses

UI ini tidak bertugas menyusun isi PDF.

### Layer service report

Logika report pendapatan ada di:

- [`PendapatanReportService.java`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/service/report/PendapatanReportService.java)

Tanggung jawabnya:
- membuka koneksi database
- compile template report
- mengirim parameter report
- mengisi report dengan data database
- menampilkan preview
- export hasil report ke file PDF

### Layer template report

Template layout report ada di:

- [`laporan_pendapatan.jrxml`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/resources/reports/laporan_pendapatan.jrxml)

Tanggung jawabnya:
- menentukan desain halaman report
- menentukan kolom tabel report
- menentukan query data report
- menentukan header, footer, dan summary

## 5. Alur Kerja Report PDF

Secara konsep, flow report PDF sekarang seperti ini:

```text
User pilih periode di LaporanPanel
-> klik Preview atau PDF
-> LaporanPanel ambil tanggal dari spinner
-> LaporanPanel panggil PendapatanReportService
-> service compile template .jrxml
-> service isi parameter report
-> JasperReports jalankan query ke database
-> JasperReports bentuk JasperPrint
-> kalau Preview: tampilkan pakai JasperViewer
-> kalau PDF: export ke file .pdf pakai JasperExportManager
```

Kalau dibagi lebih detail:

1. User pilih `tanggalDari` dan `tanggalSampai`.
2. `LaporanPanel` validasi bahwa tanggal awal tidak lebih besar dari tanggal akhir.
3. `LaporanPanel` menjalankan proses di `SwingWorker`, jadi UI tidak freeze.
4. `PendapatanReportService` membuka koneksi ke MySQL lewat `DatabaseConnection`.
5. Service membaca template `laporan_pendapatan.jrxml`.
6. Template di-compile menjadi object `JasperReport`.
7. Service mengirim parameter seperti:
   - nama klinik
   - label periode
   - waktu cetak
   - total ringkasan
8. Jasper menjalankan query SQL yang ada di template.
9. Hasil akhir dibentuk menjadi `JasperPrint`.
10. `JasperPrint` lalu:
    - ditampilkan ke layar untuk preview
    - atau disimpan ke file PDF

## 6. Preview Report Pakai Apa?

Preview report desktop sekarang memakai:

- `JasperViewer`

`JasperViewer` adalah viewer bawaan JasperReports untuk aplikasi desktop Java.

Fungsinya:
- menampilkan hasil report sebelum disimpan
- menunjukkan tampilan halaman seperti hasil print/PDF
- memungkinkan user cek layout, total, dan isi data lebih dulu

Di implementasi sekarang, preview dipanggil dari:

- [`PendapatanReportService.showPreview(...)`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/service/report/PendapatanReportService.java)

Secara teori:

- `JasperReport` = template yang sudah di-compile
- `JasperPrint` = hasil report yang sudah terisi data
- `JasperViewer` = jendela desktop untuk menampilkan `JasperPrint`

Jadi preview tidak membaca `.jrxml` langsung.
Preview selalu menampilkan hasil akhir yang sudah diisi data.

## 7. Ketika User Klik Download PDF, Apa yang Terjadi?

Di UI sekarang, tombol `PDF` di `LaporanPanel` menjalankan flow ini:

1. ambil periode tanggal dari spinner
2. validasi periode
3. buka `JFileChooser`
4. user pilih nama dan lokasi file
5. service generate `JasperPrint`
6. `JasperExportManager` menyimpan hasil report ke file `.pdf`
7. UI tampilkan popup sukses berisi lokasi file

Jadi istilah "download PDF" di desktop app ini sebenarnya lebih tepat disebut:

- `export PDF`
- `save PDF`

Karena file tidak di-download dari internet.
File dibuat langsung di komputer user dari data database lokal.

## 8. Export PDF Pakai Apa?

Untuk menyimpan file PDF, kita pakai:

- `JasperExportManager`

Fungsinya:
- mengubah `JasperPrint` menjadi file PDF fisik
- menyimpan hasil ke path yang dipilih user

Secara teori:

```text
Template (.jrxml)
-> compile jadi JasperReport
-> fill data jadi JasperPrint
-> export jadi PDF file
```

Class yang berperan:

- `JasperCompileManager`
- `JasperFillManager`
- `JasperExportManager`

Peran masing-masing:

- `JasperCompileManager`:
  mengubah template `.jrxml` menjadi report yang siap dipakai engine

- `JasperFillManager`:
  mengisi report dengan parameter dan data database

- `JasperExportManager`:
  menyimpan hasil akhir menjadi file `.pdf`

## 9. Teori Object Penting di JasperReports

Supaya tidak bingung, ini urutan object yang paling penting:

### `.jrxml`

Ini adalah file template mentah.

Isinya:
- layout report
- query SQL
- parameter
- field
- band seperti title, detail, footer, summary

Belum bisa langsung dipreview kalau belum diproses.

### `JasperReport`

Ini hasil compile dari `.jrxml`.

Anggap saja ini template yang sudah siap dieksekusi.

### `JasperPrint`

Ini hasil report final yang sudah berisi data.

Object inilah yang:
- bisa dipreview
- bisa diexport ke PDF
- bisa di-print

## 10. Kenapa Kita Pakai `SwingWorker`

Generate report bisa butuh waktu karena:
- buka koneksi database
- compile template
- eksekusi query SQL
- render report
- export file

Kalau semua ini jalan di Event Dispatch Thread Swing:
- window bisa freeze
- tombol jadi terasa macet
- user mengira aplikasi crash

Karena itu `LaporanPanel` memakai `SwingWorker`.

Tujuannya:
- proses berat jalan di background thread
- UI tetap responsif
- setelah selesai, hasil dikirim balik ke UI thread

## 11. Kenapa Template Report Disimpan di `resources`

Template report sekarang diletakkan di:

- `src/main/resources/reports/`

Ini pilihan yang benar karena:
- file dianggap bagian dari resource aplikasi
- mudah dibundle ke dalam JAR
- mudah di-load lewat classpath
- tidak tergantung path absolut komputer tertentu

Keuntungan besar:
- report tetap bisa jalan di laptop teman lain
- tidak perlu hardcode path seperti `C:\\report\\...`
- lebih aman saat project di-build jadi JAR

## 12. Kenapa Query Report Ditaruh di Template

Untuk report pertama, query SQL ditaruh langsung di `.jrxml`.

Keuntungannya:
- cepat untuk report awal
- desain dan sumber data ada di satu tempat
- cocok untuk laporan yang strukturnya stabil

Tapi secara arsitektur, ada dua pendekatan:

### Pendekatan A: query di `.jrxml`

Cocok kalau:
- report sederhana
- query tidak sering dipakai ulang
- target utama adalah cepat jadi

### Pendekatan B: data disiapkan di Java lalu dikirim ke Jasper

Cocok kalau:
- logika data lebih kompleks
- query ingin dites terpisah
- satu data source dipakai banyak report

Untuk saat ini, report pendapatan masih wajar pakai pendekatan A.

## 13. Teori Band di JasperReports

Jasper punya konsep `band`.

Band adalah area layout yang muncul pada bagian tertentu dari report.

Band yang dipakai sekarang:

- `title`
- `columnHeader`
- `detail`
- `pageFooter`
- `summary`

Fungsinya:

### `title`

Muncul di awal report.

Dipakai untuk:
- nama klinik
- judul report
- label periode
- waktu cetak

### `columnHeader`

Header untuk kolom tabel.

Dipakai untuk:
- Tanggal
- Invoice
- Pasien
- Antrian
- Metode
- Konsultasi
- Obat
- Total

### `detail`

Bagian ini diulang untuk setiap baris data hasil query.

Artinya:
- satu transaksi pembayaran lunas
- satu baris di detail report

### `pageFooter`

Muncul di bawah setiap halaman.

Dipakai untuk:
- label dokumen otomatis
- nomor halaman

### `summary`

Muncul di bagian akhir report.

Dipakai untuk:
- total transaksi
- total konsultasi
- total obat
- total pendapatan

## 14. Kenapa Kemarin Error Schema Jasper Bisa Terjadi

Jasper `.jrxml` harus mengikuti urutan elemen XML yang ketat.

Contohnya:
- `pageFooter`
- `summary`
- `noData`

Kalau urutannya salah, template gagal di-compile walaupun isi SQL benar.

Error seperti:

```text
Invalid content was found starting with element "pageFooter"
```

biasanya berarti:
- posisi tag tidak sesuai schema Jasper
- bukan berarti query SQL-nya salah

Jadi di JasperReports, validitas XML template sama pentingnya dengan validitas query.

## 15. Tool yang Disarankan untuk Mendesain Report Selanjutnya

Kalau nanti mau bikin report lain seperti:
- laporan pasien
- laporan pemeriksaan
- laporan obat

saran terbaik tetap:

- desain visual pakai `Jaspersoft Studio`
- simpan hasilnya sebagai `.jrxml`
- load dan jalankan dari aplikasi Java yang sama

Alasannya:
- lebih cepat desain header dan tabel
- gampang atur alignment
- gampang tambah summary, group, logo, footer
- lebih aman dibanding layout manual PDF

## 16. Perbedaan JasperReports vs PDFBox vs iText

Supaya jelas kenapa project ini memilih Jasper:

### JasperReports

Kuat untuk:
- laporan tabel
- multi-page report
- summary
- header/footer
- preview report
- export PDF dan Excel

Paling cocok untuk:
- aplikasi klinik
- laporan transaksi
- laporan periodik

### PDFBox

Kuat untuk:
- manipulasi file PDF
- merge/split PDF
- edit dokumen PDF yang sudah ada

Kurang ideal untuk:
- laporan tabel besar dari nol
- report business-style dengan summary dan page layout kompleks

### iText

Kuat untuk:
- generate PDF manual lewat code
- layout kustom sangat spesifik

Tapi untuk report bisnis:
- code bisa cepat jadi panjang
- maintenance lebih berat
- preview desktop tidak seenak Jasper

Jadi untuk project ini, JasperReports memang pilihan paling tepat.

## 17. Implementasi Report Pendapatan Saat Ini

Report PDF pertama yang sudah dipasang sekarang adalah:

- `Laporan Pendapatan`

Data yang dipakai:
- tabel `pembayaran`
- hanya status `lunas`
- filter `tanggal_bayar BETWEEN tanggalDari AND tanggalSampai`

Isi report:
- tanggal bayar
- nomor invoice
- nama pasien
- nomor antrian
- metode bayar
- biaya konsultasi
- biaya obat
- total tagihan

Ringkasan:
- total transaksi
- total konsultasi
- total obat
- total pendapatan

## 18. Pola yang Akan Dipakai untuk Report Lain

Report lain nanti sebaiknya mengikuti pola yang sama:

```text
LaporanPanel
-> ReportService spesifik
-> Template .jrxml
-> Preview dengan JasperViewer
-> Export PDF dengan JasperExportManager
```

Contoh:
- `PasienReportService`
- `PemeriksaanReportService`
- `ObatReportService`

Keuntungannya:
- struktur konsisten
- mudah maintain
- mudah debug
- mudah tambah export format lain nanti

## 19. Ringkasan Akhir

Kalau disingkat:

- project ini pakai `JasperReports` sebagai engine PDF report
- desain visual report sebaiknya pakai `Jaspersoft Studio`
- preview report desktop pakai `JasperViewer`
- simpan file PDF pakai `JasperExportManager`
- UI filter tetap dibuat di NetBeans Swing Designer
- PDF layout tidak dibuat dengan NetBeans GUI Builder

Formula mental paling gampang:

```text
NetBeans Designer = layar aplikasi
JasperReports = mesin report
Jaspersoft Studio = desainer report
JasperViewer = preview report
JasperExportManager = simpan PDF
```

## 20. File yang Terkait dengan Report PDF Saat Ini

- [`docs/report-pdf/README.md`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/docs/report-pdf/README.md)
- [`pom.xml`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/pom.xml)
- [`LaporanPanel.java`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/panel/LaporanPanel.java)
- [`PendapatanReportService.java`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/service/report/PendapatanReportService.java)
- [`laporan_pendapatan.jrxml`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/resources/reports/laporan_pendapatan.jrxml)
