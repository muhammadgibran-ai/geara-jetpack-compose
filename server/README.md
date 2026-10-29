# ⚙️ GEARA CRM Suku Cadang - Real-time Backend Server

Backend API server untuk aplikasi Android **GEARA CRM Suku Cadang**, mengelola:
1. **Katalog Produk & URL Gambar Cloud Storage** (AWS S3 / Firebase Storage).
2. **Manajemen Inventaris Stok Real-time** (Stok berkurang otomatis saat pesanan dibuat).
3. **Sistem Pembayaran Nyata (Payment Gateway)** (QRIS, BCA/Mandiri/BRI Virtual Account, E-Wallet, Kartu Kredit).
4. **Webhook Konfirmasi Pembayaran** (Mengubah status pesanan dari `PENDING` ke `ACTIVE` secara real-time).
5. **Pusat Transaksi Pesanan & Status Pelacakan Kurir Real-time**.
6. **Notifikasi CRM Terpusat**.

---

## 🚀 Cara Menjalankan Server

Server ini dibangun menggunakan modul bawaan Node.js tanpa memerlukan dependensi npm pihak ketiga eksternal, sehingga dapat langsung dijalankan:

### Di Terminal (PowerShell / Command Prompt):
```bash
cd server
node server.js
```
atau:
```bash
npm start
```

---

## 💳 Sistem Pembayaran & Webhook (Midtrans / Xendit Sandbox)

### Alur Transaksi:
1. **Pembuatan Pesanan**:
   - Pengguna menekan tombol **"Buat Pesanan"** di layar Checkout aplikasi Android.
   - Aplikasi memanggil endpoint `POST /api/v1/orders`.
   - Server memotong stok inventaris, menghasilkan token payment gateway, nomor Virtual Account, data QRIS, dan menyetel status pesanan awal ke **`PENDING`** (Menunggu Pembayaran).
2. **Pengalihan ke Gateway**:
   - Pengguna diarahkan ke antarmuka **Payment Gateway** (`PaymentGatewayScreen`).
   - Terdapat pilihan:
     - **QRIS**: Menampilkan kode QRIS standar nasional yang dapat discan oleh GoPay, OVO, Dana, ShopeePay, BCA Mobile.
     - **Virtual Account**: Nomor VA unik BCA, Mandiri, BRI, BNI dengan tombol salin otomatis dan panduan m-Banking/ATM.
     - **E-Wallet**: GoPay, OVO, Dana, ShopeePay direct pay.
3. **Webhook Konfirmasi Pembayaran**:
   - Gateway mengirimkan notifikasi pelunasan ke endpoint:
     `POST /api/v1/payments/webhook`
     ```json
     {
       "order_id": "GR-8842109",
       "transaction_status": "settlement",
       "payment_type": "qris",
       "gross_amount": 205000
     }
     ```
   - Server memverifikasi status pembayaran. Jika status adalah `settlement` atau `capture`, server **mengubah status pesanan dari `PENDING` ke `ACTIVE`**, mencatat tracking step konfirmasi pelunasan, dan mengirim notifikasi CRM.
4. **Simulasi Pengujian Sandbox**:
   - Pada antarmuka `PaymentGatewayScreen`, tersedia tombol **"⚡ Simulasikan Pembayaran Sukses"**.
   - Tombol ini memanggil `POST /api/v1/payments/simulate-success` atau mengirim webhook settlement, sehingga penguji dapat melihat perubahan status dan navigasi ke layar pesanan berhasil secara instan tanpa perlu transfer uang sungguhan di lingkungan sandbox!

---

## 📡 Daftar Endpoint REST API

| Method | Endpoint | Deskripsi |
|---|---|---|
| `GET` | `/api/v1/health` | Status kesehatan server dan daftar endpoint |
| `GET` | `/api/v1/products` | Mengambil seluruh katalog produk beserta stok dan URL gambar |
| `GET` | `/api/v1/products/:id` | Detail 1 produk dan stoknya |
| `GET` | `/api/v1/orders` | Mengambil seluruh riwayat transaksi pesanan CRM |
| `GET` | `/api/v1/orders/:id` | Detail pesanan & tahapan pelacakan kurir |
| `POST` | `/api/v1/orders` | **Buat pesanan baru (Status: PENDING & auto-decrement stok)** |
| `POST` | `/api/v1/payments/webhook` | **Webhook Gateway untuk verifikasi lunas (PENDING ➡️ ACTIVE)** |
| `GET` | `/api/v1/orders/:id/payment-status` | Cek status pelunasan pembayaran pesanan |
| `POST` | `/api/v1/payments/simulate-success` | Simulasi pelunasan instan untuk pengujian Sandbox |
| `GET` | `/api/v1/notifications` | Mengambil daftar notifikasi CRM |
| `POST` | `/api/v1/inventory/restock` | Restock suku cadang |

---

## 🧪 Pengujian Otomatis

Jalankan skrip uji coba otomatis end-to-end:
```bash
node server/test_server.js
```
Skrip ini akan memvalidasi:
- Health check
- Stok awal produk
- Pembuatan pesanan baru (Status PENDING)
- Pengurangan stok inventaris
- Pengecekan status pembayaran sebelum webhook (is_paid: false)
- Pengiriman Webhook konfirmasi pembayaran (settlement)
- Pengecekan status pesanan setelah webhook (Status berubah menjadi ACTIVE)

---

## ☁️ Panduan Deploy ke Cloud Publik (HTTPS)

Server ini telah dilengkapi `Dockerfile`, `render.yaml`, dan `cloudbuild.yaml` sehingga siap dideploy ke berbagai penyedia cloud publik:

### 1. Google Cloud Run (Jakarta Region: `asia-southeast2`)
```bash
# Pastikan Google Cloud SDK (gcloud) terpasang dan login:
gcloud auth login
gcloud config set project <PROJECT_ID>

# Deploy langsung dari direktori server:
cd server
gcloud run deploy geara-crm-api \
  --source . \
  --region asia-southeast2 \
  --allow-unauthenticated \
  --port 8080
```
*Cloud Run akan langsung memberikan URL ber-SSL otomatis, misal: `https://geara-crm-api-xxxx-as.a.run.app/`.*

### 2. Render.com (1-Click Deployment Gratis)
1. Hubungkan repository GitHub Anda ke [Render.com](https://render.com).
2. Pilih **New Web Service** > Arahkan ke folder `server`.
3. Render akan otomatis mendeteksi [`render.yaml`](file:///c:/Users/USER/AndroidStudioProjects/AplikasiUTSGibran/server/render.yaml) atau Dockerfile.
4. Klik **Deploy** — Render akan menerbitkan URL HTTPS gratis seperti `https://geara-crm-api.onrender.com`.

### 3. Railway.app / AWS App Runner
- **Railway**: Buka dashboard Railway > **New Project** > **Deploy from GitHub repo** > Pilih root atau direktori `server`.
- **AWS App Runner**: Pilih source container registry atau repository GitHub, port `8080`, langsung aktif dengan sertifikat HTTPS AWS ACM.

### 4. Menghubungkan Custom Domain Resmi (`https://api.geara.co.id/`)
Di dashboard DNS registrar domain Anda (Cloudflare / Niagahoster / Rumahweb):
- Tambahkan record: `CNAME api <target-domain-cloud-run-atau-render>`
- SSL/TLS sertifikat akan diterbitkan otomatis dalam 5-15 menit.
