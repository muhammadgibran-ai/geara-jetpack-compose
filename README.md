# 🏍️ GEARA - CRM & Auto Parts Store Mobile App

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%26%20Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![NodeJS](https://img.shields.io/badge/Backend-Node.js%20%2F%20Express-339933?style=for-the-badge&logo=nodedotjs&logoColor=white)

**GEARA** adalah aplikasi mobile modern berbasis **Android Native (Jetpack Compose)** untuk katalog suku cadang kendaraan, pemesanan onderdil, manajemen pesanan (CRM), dan layanan purna jual kendaraan. 

---

## ✨ Fitur Utama (Key Features)

- 🔐 **Autentikasi Pengguna**: Layar Login, Registrasi (Sign Up), Set Password, dan Mock Biometric Fingerprint.
- 🏍️ **Katalog Suku Cadang Dinamis**: Navigasi kategori suku cadang (Rem, Mesin, Filter, Lampu, Oli) dengan pencarian dan filter harga/brand.
- 🛒 **Keranjang & Checkout**: Manajemen item keranjang belanja, ringkasan transaksi, pemilihan alamat pengiriman, dan metode pembayaran.
- 📦 **Manajemen Pesanan (My Orders & CRM)**: Status pesanan aktif, riwayat pesanan selesai, detail pelacakan resi, hingga pembatalan pesanan.
- ⭐ **Favorit & Wishlist**: Simpan suku cadang favorit untuk akses cepat.
- 🎧 **Pusat Bantuan & Layanan Pelanggan (Support)**: FAQ interaktif, kontak dukungan teknis, dan informasi garansi suku cadang.
- 🌐 **Full-Stack Ready**: Dilengkapi backend REST API berbasis Express.js di folder `/server`.

---

## 🛠️ Tech Stack & Arsitektur

### Mobile Client (Android)
- **Language**: Kotlin 2.0+
- **UI Toolkit**: Jetpack Compose dengan Material Design 3 (Material3)
- **Architecture**: Single-Activity Component-driven UI
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`)
- **Compatibility**: Min SDK 24 (Android 7.0) — Target SDK 35+

### Backend Server (`/server`)
- **Runtime**: Node.js & Express.js
- **Format Data**: JSON RESTful API
- **Containerization**: Dockerfile & Render/Cloud Build ready

---

## 📂 Struktur Project

```text
├── app/
│   ├── src/main/java/id/ac/stmi/aplikasiutsgibran/
│   │   ├── ui/                    # Layar & Komponen Jetpack Compose
│   │   │   ├── LoginScreen.kt
│   │   │   ├── SignUpScreen.kt
│   │   │   ├── CatalogScreen.kt
│   │   │   ├── CartScreen.kt
│   │   │   ├── CheckoutScreen.kt
│   │   │   ├── PaymentScreen.kt
│   │   │   ├── OrdersScreen.kt
│   │   │   ├── FavoritesScreen.kt
│   │   │   └── SupportScreen.kt
│   │   ├── MainActivity.kt        # Entry Point Aplikasi
│   │   └── ui/theme/              # Tema, Warna & Tipografi Material 3
│   └── src/main/res/drawable/     # Vector Drawable Icons & Assets
├── server/                        # REST API Backend Service
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 Cara Menjalankan Project

### 1. Android App
1. Clone repository ini:
   ```bash
   git clone https://github.com/muhammadgibran-ai/geara-jetpack-compose.git
   ```
2. Buka project di **Android Studio** (Ladybug / Koala atau versi terbaru).
3. Biarkan Gradle melakukan sinkronisasi dependensi (`Sync Project with Gradle Files`).
4. Jalankan aplikasi pada Emulator atau Device fisik Android (`Run 'app'`).

### 2. Backend Server (Opsional)
```bash
cd server
npm install
npm start
```

---

## 👤 Author

* **Muhammad Gibran Ramadhan** ([@muhammadgibran-ai](https://github.com/muhammadgibran-ai))
* **Politeknik STMI Jakarta**
