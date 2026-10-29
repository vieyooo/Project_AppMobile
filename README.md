[project_readme.md](https://github.com/user-attachments/files/34142726/project_readme.md)
# 🚜 Heavy Equipment Booking & Tracking Mobile App

Aplikasi Android berbasis **Kotlin** yang dirancang khusus untuk industri penyewaan alat berat. Aplikasi ini memudahkan *customer* (penyewa) dalam memilih unit alat berat, menentukan jadwal sewa beserta operator berlisensi, melacak proses mobilisasi unit secara real-time, hingga melakukan verifikasi inspeksi keselamatan kerja (K3).

Proyek ini dikembangkan untuk memenuhi tugas Evaluasi Tengah Semester (ETS) Mata Kuliah **Pemrograman Aplikasi Mobile**.

---

## 👥 Tim Pengembang (Kelompok)
* **Novi** - Auth & Operator Selection (`LoginActivity`, `OperatorActivity`)
* **Bagus** - Katalog Utama & Detail Pemesanan (`MainActivity`, `DetailActivity`, `RecyclerView`)
* **Bila** - Pelacakan Pengantaran & Logistik (`TrackingActivity`)
* **Oshi** - Inspeksi K3 Digital & Pengaturan (`ChecklistK3Activity`, `SettingsActivity`)

---

## 📱 Fitur Utama & Materi Kuliah (Pertemuan 1 - 7)

1. **Activity & Intent (Pertemuan 3):** Navigasi antar halaman secara mulus dan pengiriman data objek antar-Activity (`Intent.putExtra`).
2. **Layout & Form Widget (Pertemuan 4 & 5):** Implementasi *ConstraintLayout*, *CardView*, *RecyclerView*, *EditText*, *Button*, *CheckBox*, dan `MaterialDatePicker`.
3. **Menu Navigasi (Pertemuan 6):** Options Menu (titik tiga) pada top-bar untuk pengaturan profil, bantuan, dan logout sesi.
4. **Local Data Persistence (Pertemuan 7):** Penggunaan `SharedPreferences` via `SessionManager` untuk menyimpan sesi login pengguna secara aman dan ringan di perangkat lokal.

---

## 🛠️ Tech Stack
* **Bahasa Pemrograman:** Kotlin
* **UI Toolkit:** Android Views, Material Components
* **Arsitektur:** Single-Activity / Multi-Activity Native Android

---

## 🚀 Cara Menjalankan Proyek
1. Clone repository ini ke komputer lokal Anda:
   ```bash
   git clone https://github.com/vieyooo/Project_AppMobile.git
   ```
2. Buka **Android Studio**.
3. Pilih *Open an Existing Project* dan arahkan ke folder hasil clone.
4. Sambungkan perangkat Android fisik Anda (aktifkan **USB Debugging**), lalu klik tombol **Run (`Shift + F10`)**.
