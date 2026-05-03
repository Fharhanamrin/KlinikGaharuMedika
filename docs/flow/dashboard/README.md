# Flow Dashboard Manual Code

Dokumen ini fokus ke **1 alur utama** untuk 4 kartu statistik di dashboard:

- `Kunjungan Hari Ini`
- `Total Pasien`
- `Pendapatan Hari Ini`
- `Stok Obat Menipis`

Tujuannya supaya flow code mudah diikuti saat dijelaskan manual ke murid:

`View -> Controller -> Repository -> Model -> View`

Bagian tabel seperti `Kunjungan terbaru` dan `Antrian aktif` tetap ada, tapi **dipisahkan** dari flow statistik utama agar pembahasannya tidak bercampur.

## 1. File yang Terlibat

Flow statistik dashboard sekarang memakai file berikut:

- `src/main/java/com/release/klinikgaharumedika/view/panel/DashboardPanel.java`
- `src/main/java/com/release/klinikgaharumedika/view/panel/DashboardPanel.form`
- `src/main/java/com/release/klinikgaharumedika/controller/DashboardController.java`
- `src/main/java/com/release/klinikgaharumedika/repository/DashboardRepository.java`
- `src/main/java/com/release/klinikgaharumedika/model/DashboardData.java`

Schema database acuan:

- `docs/database/database.sql`

## 2. Variabel dari NetBeans GUI Builder

Empat label utama dibuat dulu dari NetBeans GUI Builder, lalu diberi nama variable yang jelas:

- `lblStatTodayVisitsValue`
- `lblStatPatientsValue`
- `lblStatRevenueValue`
- `lblStatLowStockValue`

Artinya murid bisa lihat dengan jelas:

1. UI dibuat dulu di Designer
2. variable `JLabel` dihasilkan oleh NetBeans
3. code manual tinggal mengisi `setText(...)`

## 3. Single Flow yang Dipakai

Flow statistik dashboard sekarang seperti ini:

```text
DashboardPanel
-> loadDashboardStats()
-> DashboardController.loadDashboardStats()
-> DashboardRepository.loadDashboardStats()
-> query MySQL
-> hasil dibungkus ke DashboardData
-> DashboardPanel.applyDashboardStats(...)
-> setText ke 4 JLabel
```

Ini sengaja dibuat satu jalur supaya gampang dijelaskan dan gampang diikuti manual.

## 4. Step by Step

### Step 1: View memulai proses

Di `DashboardPanel`, setelah `initComponents()`, panel masuk ke runtime state:

```java
private void initializeRuntimeState() {
    applyCurrentDate();
    setupSearch();
    setLoadingState();
    loadDashboardStats();
    loadActiveQueuePage(1);
    loadRecentVisitPage(1);
}
```

Khusus statistik, yang dipanggil adalah:

```java
public final void loadDashboardStats() {
    SwingWorker<DashboardData, Void> worker = new SwingWorker<>() {
        @Override
        protected DashboardData doInBackground() throws Exception {
            return dashboardController.loadDashboardStats();
        }

        @Override
        protected void done() {
            applyDashboardStats(get());
        }
    };

    worker.execute();
}
```

Kenapa pakai `SwingWorker`:

- query database berjalan di background
- UI tidak freeze
- setelah selesai, hasilnya dibind ke label

### Step 2: Controller jadi jembatan

Controller dibuat tipis supaya alurnya mudah dibaca:

```java
public DashboardData loadDashboardStats() throws SQLException {
    return dashboardRepository.loadDashboardStats();
}
```

Tugas controller di sini:

- menerima permintaan dari view
- meneruskan ke repository
- menjaga agar view tidak langsung bicara ke database

### Step 3: Repository ambil data database

Repository sengaja dibuat **1 method utama + 1 query utama** untuk statistik dashboard:

```java
public DashboardData loadDashboardStats() throws SQLException {
    try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(DASHBOARD_STATS_SQL);
            ResultSet resultSet = statement.executeQuery()
    ) {
        if (resultSet.next()) {
            return mapDashboardData(resultSet);
        }
    }

    return DashboardData.empty();
}
```

Query `DASHBOARD_STATS_SQL` mengambil semua angka yang dibutuhkan sekaligus:

- `visits_today`
- `visits_yesterday`
- `total_patients`
- `new_patients_this_month`
- `today_revenue`
- `average_daily_revenue`
- `low_stock_items`

Kenapa ini lebih mudah diajarkan:

- murid cukup cari **1 query**
- murid cukup cari **1 method repository**
- tidak perlu lompat ke banyak helper method hanya untuk membaca 4 kartu statistik

### Step 4: Model membungkus hasil query

Hasil query tidak langsung dilempar ke view, tapi dibungkus ke `DashboardData`.

```java
private DashboardData mapDashboardData(ResultSet resultSet) throws SQLException {
    return new DashboardData(
            resultSet.getInt("visits_today"),
            resultSet.getInt("visits_yesterday"),
            resultSet.getInt("total_patients"),
            resultSet.getInt("new_patients_this_month"),
            getAmountOrZero(resultSet, "today_revenue"),
            getAmountOrZero(resultSet, "average_daily_revenue"),
            resultSet.getInt("low_stock_items")
    );
}
```

Model ini menampung:

- nilai utama kartu statistik
- nilai pembanding untuk info kecil di bawah kartu

Contoh:

- `visitsToday` untuk angka utama
- `visitsYesterday` untuk teks `+X dari kemarin`
- `todayRevenue` untuk angka utama
- `averageDailyRevenue` untuk teks pembanding pendapatan

### Step 5: View bind ke label NetBeans

Setelah object `DashboardData` kembali ke panel, binding dilakukan di `applyDashboardStats(...)`.

```java
private void applyDashboardStats(DashboardData data) {
    applyTodayVisitsStat(data);
    applyTotalPatientsStat(data);
    applyTodayRevenueStat(data);
    applyLowStockStat(data);
}
```

Lalu masing-masing kartu mengisi variable `JLabel` yang dibuat di GUI Builder:

```java
private void applyTodayVisitsStat(DashboardData data) {
    lblStatTodayVisitsValue.setText(formatInteger(data.getVisitsToday()));
}

private void applyTotalPatientsStat(DashboardData data) {
    lblStatPatientsValue.setText(formatInteger(data.getTotalPatients()));
}

private void applyTodayRevenueStat(DashboardData data) {
    lblStatRevenueValue.setText(formatCompactCurrency(data.getTodayRevenue()));
}

private void applyLowStockStat(DashboardData data) {
    lblStatLowStockValue.setText(formatInteger(data.getLowStockItems()));
}
```

Di titik ini flow selesai.

## 5. Cara Menjelaskan ke Murid

Kalau mau ngajarin langkah demi langkah, pakai urutan ini:

1. Tunjukkan variable label di NetBeans Designer.
2. Buka `DashboardPanel.java` dan tunjukkan `loadDashboardStats()`.
3. Lompat ke `DashboardController.loadDashboardStats()`.
4. Lompat ke `DashboardRepository.loadDashboardStats()`.
5. Tunjukkan query `DASHBOARD_STATS_SQL`.
6. Tunjukkan `mapDashboardData(...)`.
7. Kembali ke `DashboardPanel.applyDashboardStats(...)`.
8. Tunjukkan `setText(...)` untuk 4 label utama.

Kalau urutannya dijaga seperti ini, murid akan lihat bahwa:

- view tidak query database langsung
- controller hanya sebagai jembatan
- repository fokus SQL
- model fokus bawa data
- view fokus isi komponen

## 6. Checklist Kalau Mau Tambah Kartu Baru

Kalau nanti mau tambah 1 kartu statistik baru, urutannya tetap sama:

1. Tambah `JLabel` di NetBeans Designer dan beri nama variable.
2. Tambah field baru di `DashboardData`.
3. Tambah kolom baru di query `DASHBOARD_STATS_SQL`.
4. Tambah mapping di `mapDashboardData(...)`.
5. Tambah getter di model.
6. Tambah binding di `DashboardPanel.apply...Stat(...)`.

Kalau murid mengikuti pola ini, mereka tidak akan bingung karena alurnya tetap satu jalur.

## 7. Catatan Penting

- Jangan letakkan SQL langsung di `DashboardPanel`.
- Jangan isi `JLabel` langsung dari `ResultSet`.
- Jangan mencampur query statistik dengan logic tabel/pagination.
- Simpan variable UI di NetBeans Designer, lalu isi datanya di code manual.

Itu inti pola yang dipakai sekarang untuk statistik dashboard.
