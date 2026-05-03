# Klinik Gaharu Medika
Sistem Informasi Klinik — Java Swing (NetBeans)

---

## Arsitektur Pattern

Pola yang dipakai mirip dengan Flutter (UI + Provider + Repository), disesuaikan ke Java Swing:

| Flutter         | Java Swing (Project ini)       | Keterangan                                    |
|-----------------|-------------------------------|-----------------------------------------------|
| `Model`         | `model/`                      | POJO class — representasi data (Pasien, Dokter, dll) |
| `Repository`    | `repository/`                 | Akses data / database (CRUD query)            |
| `Provider`      | `controller/`                 | Business logic + state, jembatan view ↔ repo  |
| `UI / Screen`   | `view/`                       | Swing JFrame/JPanel — file `.form` + `.java`  |
| *(extra)*       | `database/`                   | Koneksi ke database (singleton pattern)       |
| *(extra)*       | `util/`                       | Helper umum: SessionManager, Validator, dll   |

---

## Struktur Package

```
src/main/java/com/release/klinikgaharumedika/
│
├── KlinikGaharuMedika.java       ← Entry point (main)
│
├── model/                        ← POJO / Entity
│   ├── User.java
│   ├── Pasien.java
│   └── Dokter.java
│
├── repository/                   ← Data access layer
│   ├── interfaces/
│   │   └── ICrudRepository.java  ← Generic interface CRUD
│   ├── UserRepository.java
│   ├── PasienRepository.java
│   └── DokterRepository.java
│
├── controller/                   ← Business logic (= Provider di Flutter)
│   ├── AuthController.java
│   ├── PasienController.java
│   └── DokterController.java
│
├── view/                         ← UI Swing Forms (.form + .java)
│   ├── LoginForm.java / .form
│   ├── DashboardForm.java / .form
│   ├── PasienForm.java / .form
│   └── DokterForm.java / .form
│
├── database/                     ← Koneksi DB
│   └── DatabaseConnection.java
│
└── util/                         ← Helper / Utility
    ├── SessionManager.java       ← Simpan data user yang sedang login
    └── Validator.java
```

---

## Alur Data (Flow)

```
View (.form)
  └─► Controller       ← user action (klik tombol, dll)
        └─► Repository ← controller minta data
              └─► DatabaseConnection ← query ke MySQL
        └─► View       ← controller update tampilan (hasil / error)
```

---

## Tech Stack

- **Java 17**
- **Swing** (GUI)
- **Maven** (build tool)
- **MySQL** (database)
- **NetBeans GUI Builder** (drag-and-drop designer via `.form` files)
# KlinikGaharuMedika
