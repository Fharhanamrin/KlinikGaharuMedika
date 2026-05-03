# UI NetBeans Designer di Project Ini

Dokumen ini menjelaskan cara kerja UI Java Swing + NetBeans Form Designer yang sekarang dipakai di project Klinik Gaharu Medika.

Fokus dokumen ini:
- memahami struktur `JFrame Form` dan `JPanel Form`
- memahami hubungan file `.java` dan `.form`
- memahami template UI yang sekarang dipakai di project
- tahu batas antara desain visual dan logic aplikasi
- menghindari rusaknya guarded block NetBeans

## 1. Prinsip Utama Project Ini

Untuk UI, project ini sekarang memakai aturan:

- desain layout dibuat lewat NetBeans Form Designer
- setiap form visual punya file `.java` dan `.form`
- jangan menggambar layout besar dengan code manual
- logic aplikasi boleh ditulis manual, tapi desain visual harus tetap designer-friendly

Artinya:
- `JFrame` dipakai sebagai window utama
- `JPanel` dipakai sebagai isi halaman atau modul
- struktur layout utama dibuat di Design View NetBeans
- manual code hanya untuk event, navigasi, binding data, dan logic UI ringan

## 2. Arsitektur UI yang Dipakai Sekarang

Struktur UI saat ini:

```text
LoginForm (JFrame Form)
DashboardForm (JFrame Form)
└── content host
    ├── DashboardPanel (JPanel Form)
    ├── PasienPanel (JPanel Form)
    ├── DokterPanel (JPanel Form)
    ├── ObatPanel (JPanel Form)
    ├── PerawatPanel (JPanel Form)
    ├── KunjunganPanel (JPanel Form)
    ├── PembayaranPanel (JPanel Form)
    └── LaporanPanel (JPanel Form)
```

Tujuannya:
- aplikasi tetap berada di satu window dashboard
- isi konten di tengah bisa diganti tanpa membuka window baru
- tiap modul tetap bisa diedit sendiri di NetBeans Designer

## 3. File UI yang Sudah Ada

### Form utama

- [`LoginForm.java`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/LoginForm.java)
- [`LoginForm.form`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/LoginForm.form)
- [`DashboardForm.java`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/DashboardForm.java)
- [`DashboardForm.form`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/DashboardForm.form)

### Panel modul

- [`DashboardPanel.java`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/panel/DashboardPanel.java)
- [`DashboardPanel.form`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/panel/DashboardPanel.form)
- [`PasienPanel.java`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/panel/PasienPanel.java)
- [`PasienPanel.form`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/panel/PasienPanel.form)
- [`DokterPanel.java`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/panel/DokterPanel.java)
- [`DokterPanel.form`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/panel/DokterPanel.form)
- [`ObatPanel.java`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/panel/ObatPanel.java)
- [`ObatPanel.form`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/panel/ObatPanel.form)
- [`PerawatPanel.java`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/panel/PerawatPanel.java)
- [`PerawatPanel.form`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/panel/PerawatPanel.form)
- [`KunjunganPanel.java`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/panel/KunjunganPanel.java)
- [`KunjunganPanel.form`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/panel/KunjunganPanel.form)
- [`PembayaranPanel.java`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/panel/PembayaranPanel.java)
- [`PembayaranPanel.form`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/panel/PembayaranPanel.form)
- [`LaporanPanel.java`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/panel/LaporanPanel.java)
- [`LaporanPanel.form`](/Users/users/Desktop/desktop/KKP/klinikGaharuMedika/src/main/java/com/release/klinikgaharumedika/view/panel/LaporanPanel.form)

## 4. JFrame dan JPanel di Project Ini

### JFrame

`JFrame` adalah window utama aplikasi.

Di project ini:
- `LoginForm` adalah `JFrame Form`
- `DashboardForm` adalah `JFrame Form`

Gunanya:
- sebagai container besar
- mengatur shell utama aplikasi
- menampilkan satu layar penuh

### JPanel

`JPanel` adalah area isi yang ditempel ke dalam `JFrame`.

Di project ini:
- modul seperti `Pasien`, `Dokter`, `Obat`, `Perawat`, `Kunjungan`, `Pembayaran`, dan `Laporan` dibuat sebagai `JPanel Form`

Gunanya:
- mengganti isi konten tanpa buka window baru
- memisahkan desain per fitur
- memudahkan edit di Design View

Kesimpulan singkat:
- `JFrame` = window
- `JPanel` = isi halaman atau modul

## 5. Kenapa Pakai JPanel Form untuk Modul

Karena target project ini adalah:
- dashboard tetap satu window
- modul bisa pindah-pindah di area content
- tiap modul bisa diedit lewat drag and drop

Kalau semua modul dibuat `JFrame`, hasilnya:
- banyak window terbuka
- UX jelek
- susah maintain

Kalau semua modul dibuat `JPanel Form`, hasilnya:
- UI lebih rapi
- navigasi lebih enak
- desain tetap editable di NetBeans

## 6. Hubungan File .java dan .form

Setiap form NetBeans terdiri dari:

- file `.java`
- file `.form`

Contoh:
- `PasienPanel.java`
- `PasienPanel.form`

Perannya:
- `.form` menyimpan metadata desain
- `.java` menyimpan generated code dan code manual untuk event

NetBeans membaca keduanya sebagai satu unit.

Kalau salah satu rusak, designer bisa gagal membaca form.

## 7. Guarded Block NetBeans

Di file `.java` NetBeans ada generated block seperti:

```java
//GEN-BEGIN:initComponents
//GEN-END:initComponents
```

dan:

```java
//GEN-BEGIN:variables
//GEN-END:variables
```

Gunanya:
- menandai area yang dikontrol Form Designer
- menjaga sinkronisasi dengan file `.form`

Aturan penting:
- jangan hapus komentar `GEN-BEGIN/GEN-END`
- jangan mengedit isi generated block sembarangan
- desain visual ubah lewat tab `Design`, bukan edit source generated

Kalau dilanggar:
- form bisa dianggap corrupted
- NetBeans membuka designer dalam mode read-only

## 8. Batas Desain vs Logic

### Yang sebaiknya dilakukan lewat Design View

- tambah/hapus komponen
- ubah ukuran dan posisi komponen
- ubah font, warna, teks default
- ubah struktur layout
- tambah panel, tabel, field, tombol

### Yang wajar ditulis manual di Java

- `actionPerformed`
- load data dari database
- validasi input
- navigasi antar panel
- memanggil controller/repository
- mengisi `JTable` dengan data

Aturan sederhananya:
- visual = designer
- logic = java code

## 9. Pola Navigasi Dashboard yang Dipakai

`DashboardForm` adalah shell utama.

Bagian utamanya:
- sidebar kiri
- header halaman
- `panelContentHost` di area konten

Saat tombol menu diklik:
- `DashboardForm` mengganti `JPanel` yang ditampilkan di `panelContentHost`

Contoh:
- klik `Pasien`
- `panelContentHost` menampilkan `PasienPanel`

Ini membuat:
- aplikasi tetap satu window
- panel tetap bisa di-design terpisah

## 10. Template Panel yang Dipakai Sekarang

Untuk tahap awal, semua panel modul masih memakai struktur isi yang mirip:
- judul modul
- subtitle
- action button placeholder
- area preview
- area catatan

Tujuan template ini:
- memberi starting point visual
- memudahkan kamu buka dan ubah langsung di designer
- tiap modul sudah punya bentuk dasar yang sama

Setelah ini kamu bebas:
- hapus placeholder
- tambah `JTable`
- tambah form input
- tambah `JComboBox`
- tambah `JTextArea`
- ubah layout total

## 11. Komponen Swing yang Paling Sering Dipakai

### JLabel

Untuk:
- judul
- label field
- info status
- helper text

### JButton

Untuk:
- tambah
- simpan
- edit
- hapus
- refresh
- cari

### JTextField

Untuk:
- nama
- username
- no RM
- no HP
- pencarian

### JPasswordField

Untuk:
- password login

### JTextArea

Untuk:
- alamat
- catatan
- keluhan

### JComboBox

Untuk:
- role
- jenis kelamin
- status
- metode bayar

### JTable

Untuk:
- daftar pasien
- daftar dokter
- daftar obat
- daftar kunjungan
- daftar pembayaran

### JScrollPane

Untuk:
- membungkus `JTable`
- membungkus `JTextArea`

### JPanel

Untuk:
- section layout
- grup field
- card UI
- wrapper konten

## 12. Workflow yang Benar Saat Mendesain UI

Langkah kerja yang disarankan:

1. buka file `.form` modul yang ingin diubah
2. edit layout di tab `Design`
3. atur property komponen dari panel `Properties`
4. kalau perlu event, generate event dari NetBeans
5. simpan
6. baru tulis logic manual di area non-generated

Contoh:
- buka `PasienPanel.form`
- tambahkan `JTable`, tombol, dan field filter
- generate event tombol `Tambah`
- isi logic CRUD di method event atau helper method

## 13. Cara Menambah Modul Baru

Kalau nanti mau tambah modul baru, misalnya `ResepPanel`, langkah yang benar:

1. buat `JPanel Form` baru di package `view.panel`
2. beri nama `ResepPanel`
3. NetBeans akan membuat:
   - `ResepPanel.java`
   - `ResepPanel.form`
4. desain panel di tab `Design`
5. daftarkan panel itu di `DashboardForm`
6. tambahkan tombol menu kalau diperlukan

Jangan buat modul visual baru sebagai class Java biasa kalau targetnya ingin tetap editable di designer.

## 14. Kesalahan yang Harus Dihindari

### Menggambar layout full manual lewat Java

Risiko:
- susah diubah di Design View
- tidak sesuai workflow project ini

### Mengedit generated block

Risiko:
- file `.form` rusak
- designer read-only

### Menaruh query database di form

Risiko:
- code UI berantakan
- susah maintain

### Menaruh semua isi dashboard dalam satu form raksasa

Risiko:
- sulit diedit
- struktur tidak modular

## 15. Aturan Praktis Project Ini

Untuk UI project ini, pegang aturan berikut:

- form utama pakai `JFrame Form`
- modul konten pakai `JPanel Form`
- file `.form` wajib dipertahankan
- drag and drop dilakukan di Design View
- code manual hanya untuk logic, bukan layout utama
- desain modul diedit per panel, bukan ditumpuk semua di satu file

## 16. Mapping ke Kebiasaan Flutter

Kalau disamakan dengan Flutter:

- `JFrame` mirip halaman utama besar
- `JPanel` mirip widget section atau screen content
- `.form` mirip file desain visual yang dipelihara builder
- event button mirip callback `onPressed`
- controller/repository tetap menangani logic dan data

Jadi pola sederhananya tetap:

`UI Form -> Controller -> Repository -> Database`

## 17. Rekomendasi Lanjutan

Urutan kerja yang paling aman setelah ini:

1. pilih satu modul dulu, misalnya `PasienPanel`
2. desain UI final lewat NetBeans Designer
3. setelah layout fix, sambungkan ke controller dan repository
4. isi `JTable` dari database
5. lanjut ke modul berikutnya

Rekomendasi teknis:
- jadikan `PasienPanel` sebagai CRUD pertama
- setelah pola CRUD matang, copy pendekatan yang sama ke `Dokter`, `Obat`, dan modul lain

## 18. Ringkasan Singkat

Versi paling singkatnya:

- `JFrame` = window utama
- `JPanel` = isi modul
- `.form` = file desain NetBeans
- layout harus designer-friendly
- jangan edit generated block
- dashboard tetap satu window
- tiap modul punya `.java + .form` sendiri
- logic boleh manual, desain utama jangan manual
