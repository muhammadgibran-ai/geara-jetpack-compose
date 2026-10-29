/**
 * GEARA CRM Suku Cadang - Real-time Backend Server
 * 
 * Standalone Node.js HTTP Server dengan dukungan:
 * - REST API Produk & URL Cloud Storage
 * - Manajemen Inventaris Stok Real-time (Auto-decrement stock saat checkout)
 * - Transaksi & Pelacakan Pesanan Terpusat
 * - Notifikasi CRM
 * - Zero external dependency (Bisa dijalankan langsung dengan: node server.js)
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');
const crypto = require('crypto');

const PORT = process.env.PORT || 3000;
const DATA_DIR = path.join(__dirname, 'data');
const PRODUCTS_FILE = path.join(DATA_DIR, 'products.json');
const ORDERS_FILE = path.join(DATA_DIR, 'orders.json');
const NOTIFICATIONS_FILE = path.join(DATA_DIR, 'notifications.json');
const USERS_FILE = path.join(DATA_DIR, 'users.json');
const FCM_TOKENS_FILE = path.join(DATA_DIR, 'fcm_tokens.json');
const JWT_SECRET = process.env.JWT_SECRET || 'geara_jwt_secret_key_super_secure_2026';

// --- Helper Persistensi Data ---
function loadJson(filePath, defaultValue) {
    try {
        if (!fs.existsSync(filePath)) {
            fs.writeFileSync(filePath, JSON.stringify(defaultValue, null, 2), 'utf-8');
            return defaultValue;
        }
        const content = fs.readFileSync(filePath, 'utf-8');
        return JSON.parse(content);
    } catch (err) {
        console.error(`[ERROR] Gagal membaca ${filePath}:`, err.message);
        return defaultValue;
    }
}

function saveJson(filePath, data) {
    try {
        fs.writeFileSync(filePath, JSON.stringify(data, null, 2), 'utf-8');
        return true;
    } catch (err) {
        console.error(`[ERROR] Gagal menyimpan ${filePath}:`, err.message);
        return false;
    }
}

// Inisialisasi Database
let products = loadJson(PRODUCTS_FILE, []);
let orders = loadJson(ORDERS_FILE, []);
let notifications = loadJson(NOTIFICATIONS_FILE, []);
let fcmTokens = loadJson(FCM_TOKENS_FILE, []);
let users = loadJson(USERS_FILE, [
    {
        id: "usr_gibran_001",
        name: "Gibran Rakabuming",
        email: "gibran@geara.co.id",
        phone: "+62 812-3456-7890",
        password: "Password123!",
        created_at: new Date().toISOString()
    }
]);

// --- FCM PUSH NOTIFICATIONS DISPATCHER ---
function dispatchFcmNotification({ title, message, type, order_id }) {
    const timestamp = new Date().toISOString();
    console.log(`\n======================================================`);
    console.log(`🔔 [FCM PUSH NOTIFICATION DISPATCHED]`);
    console.log(`📌 Tipe Event : ${type}`);
    console.log(`🏷️  Judul      : ${title}`);
    console.log(`💬 Pesan      : ${message}`);
    console.log(`📦 Order ID   : ${order_id || '-'}`);
    console.log(`👥 Target Perangkat: ${fcmTokens.length} token FCM aktif`);
    console.log(`======================================================\n`);

    const newNotif = {
        id: `notif_${Date.now()}`,
        title: title,
        message: message,
        time: 'Baru saja',
        category: type === 'CRM_SERVICE_REMINDER' ? 'Promo & Servis' : 'Pesanan',
        is_read: false,
        created_at: timestamp
    };
    notifications.unshift(newNotif);
    saveJson(NOTIFICATIONS_FILE, notifications);
    return newNotif;
}

// --- JWT Token Generator (HMAC SHA-256) ---
function signJwt(payload) {
    const header = Buffer.from(JSON.stringify({ alg: 'HS256', typ: 'JWT' })).toString('base64url');
    const expPayload = {
        ...payload,
        iss: 'geara-backend-api',
        iat: Math.floor(Date.now() / 1000),
        exp: Math.floor(Date.now() / 1000) + (30 * 24 * 3600) // 30 hari masa aktif token
    };
    const body = Buffer.from(JSON.stringify(expPayload)).toString('base64url');
    const signature = crypto.createHmac('sha256', JWT_SECRET).update(`${header}.${body}`).digest('base64url');
    return `${header}.${body}.${signature}`;
}

function verifyJwt(token) {
    if (!token) return null;
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const [header, body, signature] = parts;
    const expectedSig = crypto.createHmac('sha256', JWT_SECRET).update(`${header}.${body}`).digest('base64url');
    if (signature !== expectedSig) return null;
    try {
        const payload = JSON.parse(Buffer.from(body, 'base64url').toString('utf-8'));
        if (payload.exp && payload.exp < Math.floor(Date.now() / 1000)) {
            return null; // Expired
        }
        return payload;
    } catch {
        return null;
    }
}

// --- Helper Response JSON ---
function sendJson(res, statusCode, payload) {
    res.writeHead(statusCode, {
        'Content-Type': 'application/json; charset=utf-8',
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
        'Access-Control-Allow-Headers': 'Content-Type, Authorization, Accept',
    });
    res.end(JSON.stringify(payload, null, 2));
}

// --- Parse Request Body ---
function parseBody(req) {
    return new Promise((resolve, reject) => {
        let body = '';
        req.on('data', chunk => {
            body += chunk.toString();
        });
        req.on('end', () => {
            if (!body) {
                resolve({});
                return;
            }
            try {
                resolve(JSON.parse(body));
            } catch (e) {
                reject(new Error('Invalid JSON format: ' + e.message));
            }
        });
        req.on('error', err => reject(err));
    });
}

// --- SERVER INSTANCE ---
const server = http.createServer(async (req, res) => {
    const fullUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
    const pathname = fullUrl.pathname;
    const method = req.method.toUpperCase();

    // CORS Preflight
    if (method === 'OPTIONS') {
        res.writeHead(204, {
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
            'Access-Control-Allow-Headers': 'Content-Type, Authorization, Accept',
        });
        res.end();
        return;
    }

    console.log(`[${new Date().toLocaleTimeString()}] ${method} ${pathname}`);

    try {
        // 1. HEALTH CHECK & ROOT
        if ((pathname === '/' || pathname === '/api/v1/health') && method === 'GET') {
            sendJson(res, 200, {
                status: 'success',
                message: 'GEARA CRM Suku Cadang - Real-time Server Running 🚀',
                version: '1.0.0',
                total_products: products.length,
                total_orders: orders.length,
                total_users: users.length,
                timestamp: new Date().toISOString(),
                available_endpoints: [
                    'POST /api/v1/auth/login (Custom Auth with JWT)',
                    'POST /api/v1/auth/register (Registrasi akun baru)',
                    'POST /api/v1/auth/forgot-password (Kirim email pemulihan/OTP)',
                    'GET  /api/v1/auth/me (Verifikasi JWT Token)',
                    'GET  /api/v1/products',
                    'GET  /api/v1/products/:id',
                    'GET  /api/v1/orders',
                    'POST /api/v1/orders (Checkout & Real-time Stock decrement)',
                    'GET  /api/v1/orders/:id',
                    'GET  /api/v1/notifications',
                    'POST /api/v1/notifications/register-token (Daftarkan Token FCM)',
                    'POST /api/v1/notifications/trigger-payment-success (Push Notif Pembayaran Lunas)',
                    'POST /api/v1/notifications/trigger-delivery (Push Notif Kurir Mulai Kirim)',
                    'POST /api/v1/notifications/trigger-service-reminder (CRM Auto-Reminder Servis Berkala)',
                    'POST /api/v1/inventory/restock'
                ]
            });
            return;
        }

        // --- 1.1 AUTHENTICATION: LOGIN ---
        if (pathname === '/api/v1/auth/login' && method === 'POST') {
            const body = await parseBody(req);
            const { email, password } = body;

            if (!email || !password) {
                sendJson(res, 400, {
                    status: 'error',
                    message: 'Email dan password wajib diisi.'
                });
                return;
            }

            const cleanEmail = email.trim().toLowerCase();
            const user = users.find(u => u.email.toLowerCase() === cleanEmail);

            if (!user || user.password !== password) {
                sendJson(res, 401, {
                    status: 'error',
                    message: 'Kombinasi email atau password salah.'
                });
                return;
            }

            const token = signJwt({
                user_id: user.id,
                email: user.email,
                name: user.name
            });

            console.log(`[AUTH] 🔑 Login sukses untuk user: ${user.email}`);

            sendJson(res, 200, {
                status: 'success',
                message: 'Login berhasil.',
                data: {
                    user: {
                        id: user.id,
                        name: user.name,
                        email: user.email,
                        phone: user.phone || ''
                    },
                    token: token,
                    token_type: 'Bearer',
                    expires_in: 30 * 24 * 3600
                }
            });
            return;
        }

        // --- 1.2 AUTHENTICATION: REGISTER ---
        if (pathname === '/api/v1/auth/register' && method === 'POST') {
            const body = await parseBody(req);
            const { name, email, phone, password } = body;

            if (!email || !password || !name) {
                sendJson(res, 400, {
                    status: 'error',
                    message: 'Nama lengkap, email, dan kata sandi wajib diisi.'
                });
                return;
            }

            const cleanEmail = email.trim().toLowerCase();
            const existingUser = users.find(u => u.email.toLowerCase() === cleanEmail);
            if (existingUser) {
                sendJson(res, 409, {
                    status: 'error',
                    message: 'Alamat email ini sudah terdaftar di sistem GEARA.'
                });
                return;
            }

            const newUser = {
                id: `usr_${Date.now()}_${Math.floor(Math.random() * 1000)}`,
                name: name.trim(),
                email: cleanEmail,
                phone: phone ? phone.trim() : '',
                password: password,
                created_at: new Date().toISOString()
            };

            users.push(newUser);
            saveJson(USERS_FILE, users);

            const token = signJwt({
                user_id: newUser.id,
                email: newUser.email,
                name: newUser.name
            });

            console.log(`[AUTH] 👤 User baru terdaftar: ${newUser.email}`);

            sendJson(res, 201, {
                status: 'success',
                message: 'Akun berhasil didaftarkan.',
                data: {
                    user: {
                        id: newUser.id,
                        name: newUser.name,
                        email: newUser.email,
                        phone: newUser.phone
                    },
                    token: token,
                    token_type: 'Bearer',
                    expires_in: 30 * 24 * 3600
                }
            });
            return;
        }

        // --- 1.3 AUTHENTICATION: FORGOT PASSWORD & RESET TOKEN ---
        if (pathname === '/api/v1/auth/forgot-password' && method === 'POST') {
            const body = await parseBody(req);
            const { email } = body;

            if (!email) {
                sendJson(res, 400, {
                    status: 'error',
                    message: 'Email wajib diisi untuk reset kata sandi.'
                });
                return;
            }

            const cleanEmail = email.trim().toLowerCase();
            const user = users.find(u => u.email.toLowerCase() === cleanEmail);

            // Generate 6-digit OTP & Reset Token
            const otpCode = Math.floor(100000 + Math.random() * 900000).toString();
            const resetToken = crypto.randomBytes(24).toString('hex');

            console.log(`[AUTH] ✉️ [Simulated Email Sender] Dikirim ke ${cleanEmail}: Kode OTP Reset: ${otpCode}, Token: ${resetToken}`);

            sendJson(res, 200, {
                status: 'success',
                message: `Instruksi pemulihan kata sandi telah dikirim ke ${cleanEmail}. Periksa folder Kotak Masuk atau Spam Anda.`,
                data: {
                    email: cleanEmail,
                    otp_sent: true,
                    expires_in_seconds: 900
                }
            });
            return;
        }

        // --- 1.4 AUTHENTICATION: VERIFY SESSION / ME ---
        if (pathname === '/api/v1/auth/me' && method === 'GET') {
            const authHeader = req.headers['authorization'] || '';
            const token = authHeader.replace(/^Bearer\s+/i, '');
            const decoded = verifyJwt(token);

            if (!decoded) {
                sendJson(res, 401, {
                    status: 'error',
                    message: 'Sesi tidak valid atau telah kedaluwarsa. Silakan login kembali.'
                });
                return;
            }

            const user = users.find(u => u.id === decoded.user_id);
            if (!user) {
                sendJson(res, 404, { status: 'error', message: 'User tidak ditemukan.' });
                return;
            }

            sendJson(res, 200, {
                status: 'success',
                data: {
                    id: user.id,
                    name: user.name,
                    email: user.email,
                    phone: user.phone || ''
                }
            });
            return;
        }

        // 2. GET ALL PRODUCTS (KATALOG & REAL-TIME STOK)
        if (pathname === '/api/v1/products' && method === 'GET') {
            sendJson(res, 200, {
                status: 'success',
                total: products.length,
                data: products
            });
            return;
        }

        // 3. GET PRODUCT BY ID
        if (pathname.startsWith('/api/v1/products/') && method === 'GET') {
            const id = pathname.replace('/api/v1/products/', '');
            const product = products.find(p => p.id === id);
            if (!product) {
                sendJson(res, 404, { status: 'error', message: `Produk dengan ID ${id} tidak ditemukan.` });
                return;
            }
            sendJson(res, 200, {
                status: 'success',
                data: product
            });
            return;
        }

        // 4. GET ALL ORDERS (RIWAYAT TRANSAKSI)
        if (pathname === '/api/v1/orders' && method === 'GET') {
            sendJson(res, 200, {
                status: 'success',
                total: orders.length,
                data: orders
            });
            return;
        }

        // 5. GET ORDER BY ID
        if (pathname.startsWith('/api/v1/orders/') && !pathname.includes('/payment-status') && method === 'GET') {
            const id = pathname.replace('/api/v1/orders/', '');
            const order = orders.find(o => o.id === id);
            if (!order) {
                sendJson(res, 404, { status: 'error', message: `Pesanan dengan ID ${id} tidak ditemukan.` });
                return;
            }
            sendJson(res, 200, {
                status: 'success',
                data: order
            });
            return;
        }

        // 6. POST CREATE ORDER (TRANSAKSI CHECKOUT & REAL-TIME INVENTORY MANAGEMENT)
        if (pathname === '/api/v1/orders' && method === 'POST') {
            const body = await parseBody(req);
            const { items, subtotal, delivery_fee, discount, total_amount, delivery_address, courier_name, payment_method } = body;

            if (!items || !Array.isArray(items) || items.length === 0) {
                sendJson(res, 400, { status: 'error', message: 'Daftar item pesanan tidak boleh kosong.' });
                return;
            }

            // Validasi ketersediaan stok inventaris terlebih dahulu
            const stockErrors = [];
            const resolvedItems = [];

            for (const item of items) {
                const prodId = item.product_id || item.productId;
                const qty = item.quantity || 1;
                const prod = products.find(p => p.id === prodId);

                if (!prod) {
                    stockErrors.push(`Produk ID '${prodId}' tidak ditemukan di katalog.`);
                    continue;
                }

                if (prod.stock < qty) {
                    stockErrors.push(`Stok '${prod.name}' tidak mencukupi! Tersisa ${prod.stock} unit (diminta: ${qty}).`);
                } else {
                    resolvedItems.push({
                        product_id: prod.id,
                        product_name: prod.name,
                        brand: prod.brand,
                        price: prod.price,
                        quantity: qty,
                        image_url: prod.image_url
                    });
                }
            }

            if (stockErrors.length > 0) {
                sendJson(res, 400, {
                    status: 'error',
                    message: 'Transaksi gagal: Masalah stok inventaris.',
                    errors: stockErrors
                });
                return;
            }

            // Potong stok produk di inventaris pusat
            for (const item of items) {
                const prodId = item.product_id || item.productId;
                const qty = item.quantity || 1;
                const prod = products.find(p => p.id === prodId);
                prod.stock = Math.max(0, prod.stock - qty);
                prod.sold_count = (prod.sold_count || 0) + qty;
                prod.in_stock = prod.stock > 0;
                console.log(`[INVENTORY] 📉 Stok '${prod.name}' berkurang ${qty} unit. Sisa stok: ${prod.stock}`);
            }
            saveJson(PRODUCTS_FILE, products);

            // Buat Record Order Baru
            const randomNum = Math.floor(1000000 + Math.random() * 9000000);
            const orderId = `GR-${randomNum}`;
            const resiNum = `GR${randomNum}8X`;
            const now = new Date();
            const dateStr = `${now.getDate()} ${['Jan','Feb','Mar','Apr','Mei','Jun','Jul','Agu','Sep','Okt','Nov','Des'][now.getMonth()]} ${now.getFullYear()}, ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')} WIB`;

            // Status awal transaksi adalah PENDING (Menunggu Pembayaran)
            // Status baru akan berubah ke ACTIVE setelah Webhook Payment Gateway mengonfirmasi pembayaran
            const newOrder = {
                id: orderId,
                date: dateStr,
                status: 'PENDING',
                items: resolvedItems,
                subtotal: subtotal || resolvedItems.reduce((acc, i) => acc + (i.price * i.quantity), 0),
                delivery_fee: delivery_fee || 15000,
                discount: discount || 0,
                total_amount: total_amount || 0,
                delivery_address: delivery_address || 'Alamat CRM Terdaftar',
                courier_name: courier_name || 'JNE Express - YES (Kilat)',
                courier_phone: '0812-9888-2121',
                resi_number: resiNum,
                estimated_arrival: 'Besok, 12:00 WIB',
                payment_method: payment_method || 'QRIS / Virtual Account',
                payment_token: `SNAP-${orderId}`,
                va_number: `1280${String(randomNum).padStart(12, '0')}`,
                payment_url: `https://app.sandbox.midtrans.com/snap/v2/vtweb/SNAP-${orderId}`,
                tracking_steps: [
                    {
                        title: 'Menunggu Pembayaran Gateway',
                        time: `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`,
                        description: `Menunggu konfirmasi pelunasan sebesar Rp ${(total_amount || 0).toLocaleString('id-ID')} via Virtual Account, QRIS, atau E-Wallet.`,
                        is_completed: false,
                        is_current: true
                    },
                    {
                        title: 'Suku Cadang Disiapkan di Gudang',
                        time: 'Estimasi +30 menit',
                        description: 'Gudang pusat Geara memverifikasi fisik suku cadang dan menyiapkan packaging anti benturan.',
                        is_completed: false,
                        is_current: false
                    },
                    {
                        title: 'Penyerahan ke Kurir',
                        time: 'Estimasi +1 jam',
                        description: `Paket diserahkan ke ${courier_name || 'Kurir Ekspedisi'}.`,
                        is_completed: false,
                        is_current: false
                    },
                    {
                        title: 'Pesanan Diterima',
                        time: 'Besok',
                        description: 'Paket suku cadang tiba di alamat tujuan.',
                        is_completed: false,
                        is_current: false
                    }
                ]
            };

            orders.unshift(newOrder);
            saveJson(ORDERS_FILE, orders);

            // Tambahkan notifikasi CRM baru secara otomatis
            const newNotif = {
                id: `notif_${Date.now()}`,
                title: `Menunggu Pembayaran #${orderId}`,
                message: `Pesanan dibuat. Silakan selesaikan pembayaran sebesar Rp ${newOrder.total_amount.toLocaleString('id-ID')} via Payment Gateway.`,
                time: 'Baru saja',
                category: 'Pesanan',
                is_read: false
            };
            notifications.unshift(newNotif);
            saveJson(NOTIFICATIONS_FILE, notifications);

            console.log(`[ORDER CREATED] ⏳ Pesanan baru dibuat (PENDING PAYMENT): ${orderId} (Total: Rp ${newOrder.total_amount})`);

            sendJson(res, 201, {
                status: 'success',
                message: 'Pesanan berhasil dibuat. Silakan lakukan pembayaran melalui Payment Gateway.',
                data: newOrder
            });
            return;
        }

        // 7. WEBHOOK PAYMENT GATEWAY (MIDTRANS / XENDIT / STRIPE)
        // Server mendengarkan konfirmasi pembayaran sebelum status pesanan diubah dari PENDING ke ACTIVE
        if (pathname === '/api/v1/payments/webhook' && method === 'POST') {
            const body = await parseBody(req);
            const { order_id, transaction_status, payment_type, gross_amount } = body;

            console.log(`[PAYMENT WEBHOOK] 🔔 Webhook masuk: Order=${order_id}, Status=${transaction_status}, Type=${payment_type}`);

            const order = orders.find(o => o.id === order_id);
            if (!order) {
                sendJson(res, 404, { status: 'error', message: `Pesanan ID ${order_id} tidak ditemukan di sistem server.` });
                return;
            }

            // Jika status pembayaran settlement atau capture (sukses dibayar)
            if (transaction_status === 'settlement' || transaction_status === 'capture') {
                order.status = 'ACTIVE';
                const now = new Date();
                const timeStr = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;

                if (order.tracking_steps && order.tracking_steps.length > 0) {
                    order.tracking_steps[0] = {
                        title: `Pembayaran Lunas (${(payment_type || 'PAYMENT GATEWAY').toUpperCase()})`,
                        time: timeStr,
                        description: `Pembayaran sebesar Rp ${(gross_amount || order.total_amount).toLocaleString('id-ID')} telah diverifikasi oleh Payment Gateway via Webhook.`,
                        is_completed: true,
                        is_current: false
                    };
                    if (order.tracking_steps.length > 1) {
                        order.tracking_steps[1].is_current = true;
                    }
                }
                saveJson(ORDERS_FILE, orders);

                // Trigger Push Notification & simpan ke riwayat CRM
                dispatchFcmNotification({
                    title: `Pembayaran Pesanan #${order.id} Berhasil! ✅`,
                    message: `Pembayaran via ${(payment_type || 'Gateway').toUpperCase()} sebesar Rp ${(gross_amount || order.total_amount).toLocaleString('id-ID')} telah sukses diverifikasi. Paket sedang disiapkan oleh gudang pusat GEARA.`,
                    type: 'PAYMENT_SUCCESS',
                    order_id: order.id
                });

                console.log(`[PAYMENT VERIFIED] 💳 Webhook SUKSES! Pesanan ${order.id} status diubah: PENDING -> ACTIVE 🚀`);

                sendJson(res, 200, {
                    status: 'success',
                    message: 'Konfirmasi pembayaran berhasil diproses melalui Webhook. Status pesanan kini ACTIVE.',
                    data: order
                });
                return;
            }

            sendJson(res, 200, {
                status: 'success',
                message: `Notifikasi webhook diterima (Status: ${transaction_status}).`,
                data: order
            });
            return;
        }

        // 8. CEK STATUS PEMBAYARAN REAL-TIME
        if (pathname.startsWith('/api/v1/orders/') && pathname.endsWith('/payment-status') && method === 'GET') {
            const orderId = pathname.replace('/api/v1/orders/', '').replace('/payment-status', '');
            const order = orders.find(o => o.id === orderId);
            if (!order) {
                sendJson(res, 404, { status: 'error', message: `Pesanan ${orderId} tidak ditemukan.` });
                return;
            }
            sendJson(res, 200, {
                status: 'success',
                order_id: order.id,
                order_status: order.status,
                payment_type: order.payment_method,
                is_paid: order.status !== 'PENDING'
            });
            return;
        }

        // 9. SIMULASI PEMBAYARAN LUNAS (UNTUK PENGUJIAN GATEWAY SANDBOX)
        if (pathname === '/api/v1/payments/simulate-success' && method === 'POST') {
            const body = await parseBody(req);
            const orderId = body.order_id || body.orderId;
            const order = orders.find(o => o.id === orderId);
            if (!order) {
                sendJson(res, 404, { status: 'error', message: `Pesanan ${orderId} tidak ditemukan.` });
                return;
            }

            order.status = 'ACTIVE';
            const now = new Date();
            const timeStr = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;

            if (order.tracking_steps && order.tracking_steps.length > 0) {
                order.tracking_steps[0] = {
                    title: 'Pembayaran Lunas (MIDTRANS SANDBOX)',
                    time: timeStr,
                    description: `Pembayaran sebesar Rp ${order.total_amount.toLocaleString('id-ID')} berhasil divalidasi via simulasi Payment Gateway.`,
                    is_completed: true,
                    is_current: false
                };
                if (order.tracking_steps.length > 1) {
                    order.tracking_steps[1].is_current = true;
                }
            }
            saveJson(ORDERS_FILE, orders);

            // Trigger Push Notification & simpan ke riwayat CRM
            dispatchFcmNotification({
                title: `Pembayaran Pesanan #${order.id} Berhasil! ✅`,
                message: `Pembayaran via Virtual Account / QRIS sebesar Rp ${order.total_amount.toLocaleString('id-ID')} telah sukses diverifikasi. Paket sedang disiapkan oleh gudang pusat GEARA.`,
                type: 'PAYMENT_SUCCESS',
                order_id: order.id
            });

            console.log(`[SIMULATE SUCCESS] 💳 Pesanan ${order.id} disimulasikan lunas! Status: PENDING -> ACTIVE`);

            sendJson(res, 200, {
                status: 'success',
                message: 'Simulasi pembayaran sukses. Status pesanan diubah ke ACTIVE dan Push Notification FCM terkirim.',
                data: order
            });
            return;
        }

        // 7. GET NOTIFICATIONS
        if (pathname === '/api/v1/notifications' && method === 'GET') {
            sendJson(res, 200, {
                status: 'success',
                total: notifications.length,
                data: notifications
            });
            return;
        }

        // --- 7.1 FCM TOKEN REGISTRATION ---
        if (pathname === '/api/v1/notifications/register-token' && method === 'POST') {
            const body = await parseBody(req);
            const { token, user_id, email, device_info } = body;

            if (!token) {
                sendJson(res, 400, { status: 'error', message: 'Token FCM wajib disertakan.' });
                return;
            }

            const existingIndex = fcmTokens.findIndex(t => t.token === token);
            const tokenRecord = {
                token: token,
                user_id: user_id || 'anonymous',
                email: email || '',
                device_info: device_info || 'Android Device',
                updated_at: new Date().toISOString()
            };

            if (existingIndex >= 0) {
                fcmTokens[existingIndex] = tokenRecord;
            } else {
                fcmTokens.push(tokenRecord);
            }
            saveJson(FCM_TOKENS_FILE, fcmTokens);

            console.log(`[FCM] 📲 Token FCM terdaftar: ${token.substring(0, 15)}... (User: ${email || user_id})`);

            sendJson(res, 200, {
                status: 'success',
                message: 'Token FCM perangkat berhasil didaftarkan di backend GEARA CRM.',
                data: tokenRecord
            });
            return;
        }

        // --- 7.2 FCM TRIGGER: PEMBAYARAN SUKSES (VA / QRIS) ---
        if (pathname === '/api/v1/notifications/trigger-payment-success' && method === 'POST') {
            const body = await parseBody(req);
            const orderId = body.order_id || body.orderId || 'GR-9821033';

            const notif = dispatchFcmNotification({
                title: 'Pembayaran Terverifikasi! ✅',
                message: `Pembayaran pesanan #${orderId} via Virtual Account / QRIS telah sukses diverifikasi. Paket sedang disiapkan oleh gudang pusat GEARA.`,
                type: 'PAYMENT_SUCCESS',
                order_id: orderId
            });

            sendJson(res, 200, {
                status: 'success',
                message: `Push notification pembayaran sukses untuk pesanan #${orderId} berhasil dikirim ke perangkat.`,
                data: notif
            });
            return;
        }

        // --- 7.3 FCM TRIGGER: KURIR MULAI MENGANTAR ---
        if (pathname === '/api/v1/notifications/trigger-delivery' && method === 'POST') {
            const body = await parseBody(req);
            const orderId = body.order_id || body.orderId || 'GR-9821033';
            const courier = body.courier_name || 'JNE Express YES (Kilat)';

            const notif = dispatchFcmNotification({
                title: 'Kurir Mulai Mengantar Paket! 🛵',
                message: `Kurir ekspedisi ${courier} sedang meluncur membawa suku cadang pesanan #${orderId} menuju bengkel atau alamat Anda.`,
                type: 'DELIVERY_START',
                order_id: orderId
            });

            sendJson(res, 200, {
                status: 'success',
                message: `Push notification kurir pengantaran untuk pesanan #${orderId} berhasil dikirim ke perangkat.`,
                data: notif
            });
            return;
        }

        // --- 7.4 FCM TRIGGER: CRM AUTO-REMINDER SERVIS BERKALA OTOMOTIF ---
        if (pathname === '/api/v1/notifications/trigger-service-reminder' && method === 'POST') {
            const body = await parseBody(req);
            const customerName = body.customer_name || 'Gibran';
            const km = body.mileage || '2.000';
            const motorType = body.motor_type || 'Honda Vario 160';

            const notif = dispatchFcmNotification({
                title: 'CRM Pengingat Servis Otomotif 🔧',
                message: `Hai ${customerName}! Sudah ${km} KM sejak servis terakhir motor ${motorType} Anda, waktunya ganti oli mesin dan periksa sistem transmisi (V-Belt & Roller) di bengkel rekanan GEARA!`,
                type: 'CRM_SERVICE_REMINDER'
            });

            sendJson(res, 200, {
                status: 'success',
                message: `Push notification CRM Auto-Reminder servis berkala berhasil dikirim ke pelanggan ${customerName}.`,
                data: notif
            });
            return;
        }

        // 8. RESTOCK INVENTORY (OPSIONAL MANAGEMENT API)
        if (pathname === '/api/v1/inventory/restock' && method === 'POST') {
            const body = await parseBody(req);
            const { product_id, quantity } = body;
            const prod = products.find(p => p.id === product_id);
            if (!prod) {
                sendJson(res, 404, { status: 'error', message: 'Produk tidak ditemukan.' });
                return;
            }
            prod.stock += (quantity || 10);
            prod.in_stock = true;
            saveJson(PRODUCTS_FILE, products);
            console.log(`[RESTOCK] 📈 Stok '${prod.name}' bertambah. Total stok sekarang: ${prod.stock}`);

            sendJson(res, 200, {
                status: 'success',
                message: `Stok produk ${prod.name} berhasil ditambah.`,
                data: prod
            });
            return;
        }

        // Route Not Found
        sendJson(res, 404, {
            status: 'error',
            message: `Endpoint ${method} ${pathname} tidak ditemukan di server API Geara.`
        });

    } catch (err) {
        console.error('[SERVER EXCEPTION]', err);
        sendJson(res, 500, {
            status: 'error',
            message: 'Internal Server Error: ' + err.message
        });
    }
});

server.listen(PORT, '0.0.0.0', () => {
    console.log(`
=====================================================
⚙️  GEARA CRM SUKU CADANG - BACKEND API SERVER
=====================================================
🟢 Server Status  : AKTIF
📡 Listening on   : http://0.0.0.0:${PORT}
📱 Android Emul.  : http://10.0.2.2:${PORT}
💻 Localhost      : http://localhost:${PORT}
📦 Total Produk   : ${products.length} item
📋 Total Pesanan  : ${orders.length} pesanan
=====================================================
Tekan Ctrl + C untuk mematikan server.
    `);
});
