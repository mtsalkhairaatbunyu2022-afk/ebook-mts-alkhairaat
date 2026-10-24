# Distribusi Buku Madrasah Siap Pakai & Pemisahan Akses Siswa-Guru

Menyediakan sistem perpustakaan digital madrasah di mana buku pelajaran langsung siap digunakan oleh siswa sejak pertama kali aplikasi dibuka tanpa perlu mengunggah secara mandiri, lengkap dengan pemisahan antarmuka antara Mode Siswa (fokus belajar & baca-saja) dan Mode Guru (manajemen, kalibrasi penomoran, & pembaruan buku dinamis).

---

## User Review & Critical Decisions

> [!IMPORTANT]
> Berdasarkan jawaban klarifikasi yang telah Anda berikan, sistem akan dikembangkan dengan prinsip utama berikut:

- **Keputusan 1: Distribusi Hybrid (Offline Utama + Pembaruan Dinamis)**
  - Buku paket utama (seperti *Bahasa Arab MTs VII* dan *Informatika SMP/MTs VII* beserta deteksi halaman cerdasnya) ditanam langsung ke dalam paket aplikasi (*pre-bundled assets*).
  - Siswa yang menginstal aplikasi langsung memiliki rak buku lengkap tanpa memerlukan koneksi internet ataupun kuota awal.
  - Aplikasi tetap menyediakan sinkronisasi pembaruan katalog secara dinamis untuk buku-buku baru atau revisi edisi mendatang.
- **Keputusan 2: Pemisahan Antarmuka Peran (Mode Siswa vs Mode Guru)**
  - **Mode Siswa (Tampilan Utama/Bawaan)**: Tampilan bersih, ramah siswa, bebas distraksi. Siswa hanya dapat membuka, membaca, menandai halaman (*bookmark*), membuat catatan pelajaran, dan mencari isi buku. Fitur unggah, edit, dan hapus disembunyikan.
  - **Mode Guru (Akses Terproteksi PIN/Sandi)**: Memungkinkan guru madrasah mengunggah buku PDF baru, memodifikasi informasi buku, mengatur kalibrasi penomoran halaman romawi vs angka biasa, serta membagikan/mengekspor paket buku ke murid.
- **Keputusan 3: Kapasitas Buku yang Fleksibel & Terukur**
  - Database lokal (Room SQLite) dirancang dinamis sehingga guru dapat menambah buku mapel dari Kelas VII, VIII, IX maupun literasi keagamaan kapan saja tanpa batasan kuota internal aplikasi.

---

## 1. Overview & Core Concept

- **Apa yang Dihadirkan**: 
  Aplikasi E-Book Madrasah yang dapat langsung dibagikan (via APK, ShareIt, Bluetooth, Flashdisk, atau link sekolah) ke perangkat siswa. Saat siswa membuka aplikasi, buku-buku yang telah diunggah dan disiapkan oleh guru sudah tertata rapi di rak buku sesuai jenjang kelasnya, dengan halaman fisik buku yang sudah terkalibrasi secara presisi.
- **Target Pengguna**: 
  - **Siswa MTs**: Siswa yang membutuhkan akses cepat ke modul & buku cetak digital tanpa repot mengunduh/mengunggah file PDF besar sendiri.
  - **Guru Madrasah**: Pengajar yang ingin mengontrol materi ajar seragam untuk seluruh kelas dan memastikan seluruh siswa membuka halaman buku yang sama persis saat kegiatan belajar mengajar (KBM).
- **Nilai Utama**: 
  - *Zero Setup for Students*: Tidak ada kendala teknis bagi siswa (tidak perlu login rumit atau upload file manual).
  - *Offline Independence*: Hemat kuota internet keluarga siswa di daerah kepulauan/pesisir.
  - *Akurasi Pembelajaran*: "Buka halaman 1" pada navigasi cepat langsung menampilkan Bab 1 buku fisik, bukan cover atau kata pengantar.

---

## 2. User Experience & Visual Design

### Alur Antarmuka Siswa (Student Journey)
1. **Buka Aplikasi (Splash & Auto-Seeding)**: Aplikasi memeriksa pustaka internal. Buku-buku paket guru otomatis siap dalam < 1 detik.
2. **Rak Buku Siswa**: 
   - Banner sambutan madrasah bernuansa islami-modern ("*Selamat Belajar di MTs Al-Khairaat*").
   - Filter cepat berdasarkan Mata Pelajaran (PAI, Bahasa Arab, Informatika, Umum) dan Kelas (VII, VIII, IX).
   - Kartu buku informatif menampilkan cover visual, jumlah bab, estimasi halaman, dan progress terakhir dibaca.
3. **Pembaca Buku (Reader View)**:
   - Navigasi cepat cerdas (Lompat ke Romawi *i, ii, iii...* atau Angka *1, 2, 3...*).
   - Mode layar penuh, zoom jernih, dan bookmark bab penting.
   - Tidak ada tombol "Hapus Buku" atau "Ganti File" yang berisiko tertekan oleh siswa.

### Alur Antarmuka Guru (Teacher Journey)
1. **Akses Pengelolaan**: Guru menekan ikon gembok/avatar di pojok kanan atas, memasukkan PIN Pengajar (default mudah diatur).
2. **Dashboard Manajemen Guru**:
   - Menu **"Tambah Buku Pelajaran Baru"** (pilih PDF dari memori HP/Drive guru).
   - Menu **"Kalibrasi Penomoran Cerdas"** untuk menguji kecocokan halaman 1 fisik buku sebelum dibagikan ke siswa.
   - Menu **"Ekspor / Siapkan Paket Rilis Siswa"** yang memastikan metadata dan file tersimpan permanen di database lokal bersama.

### Identitas Visual & Tema
- **Palet Warna**: Hijau Madrasah Modern (*Emerald Forest* `#006C4C`), Emas Elegan (*Kemenag Gold Accent* `#FFB74D`), dan Latar Netral Bersih (*Cream/Surface* `#FDFBF7`).
- **Tipografi**: Hirarki tegas dengan display font yang nyaman dibaca dan angka bab yang kontras.
- **Micro-interactions**: Efek kartu timbul saat disentuh, indikator status buku siap dibaca, dan dialog konfirmasi yang jelas.

---

## 3. Key Product Decisions & Trade-Offs

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            STRATEGI DISTRIBUSI BUKU                        │
├───────────────────────────────┬─────────────────────────────────────────────┤
│ Fitur                         │ Keputusan & Alasan Teknis                   │
├───────────────────────────────┼─────────────────────────────────────────────┤
│ 1. Penempatan Buku Bawaan     │ Ditempatkan di assets/books/ & disalin ke   │
│    (Pre-bundled Assets)       │ filesDir secara asynchronous saat boot.     │
│                               │ Alasan: Siswa dapat langsung membaca 100%   │
│                               │ tanpa internet pada pemasangan pertama.     │
├───────────────────────────────┼─────────────────────────────────────────────┤
│ 2. Mode Siswa vs Guru         │ Default app = Mode Siswa (Read-Only).       │
│    (Role Guarding)            │ Beralih ke Mode Guru menggunakan PIN cepat  │
│                               │ Alasan: Mencegah siswa mengubah/menghapus   │
│                               │ file kurikulum yang sudah disiapkan guru.   │
├───────────────────────────────┼─────────────────────────────────────────────┤
│ 3. Penyimpanan Data Lokal     │ Room Database (SQLite) + SharedPreferences  │
│    (Persistence Strategy)     │ Alasan: Menjaga buku tersimpan permanen di  │
│                               │ perangkat tanpa biaya server/API key luar.  │
├───────────────────────────────┼─────────────────────────────────────────────┤
│ 4. Penomoran Halaman Otomatis │ Deteksi Cerdas Romawi vs Angka Arab         │
│    (PdfPageNumberDetector)    │ Alasan: Memastikan navigasi halaman 1 sama  │
│                               │ persis dengan instruksi halaman guru di KBM.│
└───────────────────────────────┴─────────────────────────────────────────────┘
```

---

## 4. Technical Architecture & Data Strategy

### Diagram Arsitektur Komponen

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           LAPISAN TAMPILAN (UI)                        │
│                                                                         │
│  ┌───────────────────────────────┐     ┌─────────────────────────────┐  │
│  │         MODE SISWA            │     │          MODE GURU          │  │
│  │  - Rak Buku Pelajaran Siap    │     │  - Unggah Buku PDF Baru     │  │
│  │  - Filter Jenjang & Mapel     │     │  - Kalibrasi Angka Romawi   │  │
│  │  - Pembaca Buku (Bebas Edit)  │     │  - Hapus/Perbarui Buku      │  │
│  │  - Bookmark & Riwayat Belajar │     │  - Kelola Kurikulum Kelas   │  │
│  └───────────────┬───────────────┘     └──────────────┬──────────────┘  │
│                  │                                    │                 │
│                  └─────────────────┬──────────────────┘                 │
│                                    ▼                                    │
│                    ┌──────────────────────────────┐                     │
│                    │     ROLE GUARD & ROUTING     │                     │
│                    │ (PIN Dialog & Preferences)   │                     │
│                    └───────────────┬──────────────┘                     │
└────────────────────────────────────┼────────────────────────────────────┘
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     VIEWMODEL & LOGIKA BISNIS                           │
│  - AppViewModel (State: currentRole, bookCatalog, isTeacherUnlocked)    │
│  - PdfPageNumberDetector (Logika Deteksi Romawi vs Angka Buku)          │
└────────────────────────────────────┬────────────────────────────────────┘
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      DATA LAYER & PERSISTENSI                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ Repository:                                                      │   │
│  │  - seedBundledBooks(): Memastikan buku guru terpasang otomatis   │   │
│  │  - addNewTeacherBook(): Menyimpan PDF baru ke penyimpanan lokal  │   │
│  │  - getBooksForStudent(): Menyaring buku aktif untuk siswa        │   │
│  └──────────────────┬───────────────────────────────────────────────┘   │
│                     │                                                   │
│      ┌──────────────┴──────────────┐                                    │
│      ▼                             ▼                                    │
│  ┌──────────────────────┐   ┌────────────────────────────────────────┐  │
│  │  Room Database       │   │  Penyimpanan File Fisik                │  │
│  │  (BookEntity, Users, │   │  - Assets (Buku Bawaan: Informatika 7, │  │
│  │   Bookmarks, Notes)  │   │    Bahasa Arab 7, dsb)                 │  │
│  │                      │   │  - context.filesDir/books/ (Buku baru) │  │
│  └──────────────────────┘   └────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

### Tahapan Eksekusi Pengerjaan
1. **Langkah 1 (Persistensi Buku Bawaan Mandiri)**: Memperbarui inisialisasi `Repository.kt` agar seluruh buku PDF yang dimasukkan oleh guru langsung terdaftar dan aktif di database lokal siswa saat pertama kali aplikasi dibuka tanpa perlu tombol aktivasi manual.
2. **Langkah 2 (Pemisahan Mode Siswa & Mode Guru)**: 
   - Memasang sistem *Mode Switcher* dengan autentikasi PIN sederhana untuk Guru.
   - Menata ulang layar utama: Mode Siswa menampilkan rak buku bersih dengan kategori terstruktur (Bahasa Arab, Informatika, dsb.), sedangkan tombol unggah/edit/hapus hanya muncul saat Mode Guru aktif.
3. **Langkah 3 (Manajemen Pembaruan Buku Dinamis Guru)**: 
   - Guru dapat memilih file PDF baru dari perangkat, memasukkan judul & mapel, lalu sistem otomatis menjalankan detektor halaman awal romawi vs angka biasa.
   - Buku baru langsung tersimpan ke ruang penyimpanan lokal aplikasi dan muncul di rak siswa.
4. **Langkah 4 (Verifikasi Kompilasi & Pengujian)**:
   - Menjalankan uji unit `compile_applet` dan memastikan performa pembaca buku lancar tanpa lag di emulator/perangkat siswa.
