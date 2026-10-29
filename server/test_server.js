const http = require('http');

function request(options, data = null) {
    return new Promise((resolve, reject) => {
        const req = http.request(options, (res) => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => {
                try {
                    resolve({ status: res.statusCode, body: JSON.parse(body) });
                } catch (e) {
                    resolve({ status: res.statusCode, raw: body });
                }
            });
        });
        req.on('error', reject);
        if (data) req.write(JSON.stringify(data));
        req.end();
    });
}

async function runTests() {
    console.log('--- 🧪 STARTING GEARA CRM BACKEND SERVER & PAYMENT TESTS ---');

    // 1. Health check
    const health = await request({ host: '127.0.0.1', port: 3000, path: '/api/v1/health', method: 'GET' });
    console.log('✅ Health check status:', health.status, health.body.message);

    // 2. Get Products & check initial stock of prod_1
    const productsRes = await request({ host: '127.0.0.1', port: 3000, path: '/api/v1/products', method: 'GET' });
    const prod1Before = productsRes.body.data.find(p => p.id === 'prod_1');
    console.log(`✅ Produk ditemukan: ${productsRes.body.data.length} item.`);
    console.log(`📦 Stok awal '${prod1Before.name}': ${prod1Before.stock} unit.`);

    // 3. Post New Order -> Must be PENDING (Menunggu Pembayaran)
    console.log('🛒 Membuat pesanan checkout baru...');
    const orderPayload = {
        items: [{ product_id: 'prod_1', quantity: 2 }],
        subtotal: 190000,
        delivery_fee: 15000,
        discount: 0,
        total_amount: 205000,
        delivery_address: 'Jl. Salemba Tengah No. 34, Jakarta Pusat',
        courier_name: 'JNE Express - YES (Kilat)',
        payment_method: 'QRIS'
    };
    const orderRes = await request({
        host: '127.0.0.1',
        port: 3000,
        path: '/api/v1/orders',
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    }, orderPayload);
    
    const newOrderId = orderRes.body.data.id;
    const initialStatus = orderRes.body.data.status;
    console.log(`✅ Pesanan dibuat: ${newOrderId}, Status awal: ${initialStatus} (Menunggu Pembayaran)`);

    if (initialStatus !== 'PENDING') {
        console.error('❌ Status awal pesanan harus PENDING!');
        process.exit(1);
    }

    // 4. Cek Payment Status sebelum Webhook
    const payStatusBefore = await request({
        host: '127.0.0.1',
        port: 3000,
        path: `/api/v1/orders/${newOrderId}/payment-status`,
        method: 'GET'
    });
    console.log(`🔍 Status Pembayaran Sebelum Webhook: is_paid=${payStatusBefore.body.is_paid}, status=${payStatusBefore.body.order_status}`);

    // 5. Trigger Webhook Payment Gateway (Midtrans Settlement Notification)
    console.log('💳 Mengirim Webhook Konfirmasi Pembayaran dari Payment Gateway...');
    const webhookPayload = {
        order_id: newOrderId,
        transaction_status: 'settlement',
        payment_type: 'qris',
        gross_amount: 205000
    };
    const webhookRes = await request({
        host: '127.0.0.1',
        port: 3000,
        path: '/api/v1/payments/webhook',
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    }, webhookPayload);
    console.log('✅ Webhook Response:', webhookRes.status, webhookRes.body.message);

    // 6. Cek Status Pesanan Setelah Webhook -> Harus berubah ke ACTIVE!
    const orderAfterRes = await request({
        host: '127.0.0.1',
        port: 3000,
        path: `/api/v1/orders/${newOrderId}`,
        method: 'GET'
    });
    const statusAfter = orderAfterRes.body.data.status;
    console.log(`🚀 Status Pesanan Setelah Webhook: ${statusAfter}`);

    if (statusAfter !== 'ACTIVE') {
        console.error('❌ Status pesanan gagal diubah ke ACTIVE oleh webhook!');
        process.exit(1);
    }

    console.log('🎉 SEMUA PENGUJIAN PAYMENT GATEWAY & WEBHOOK BERHASIL 100%!');
    process.exit(0);
}

// Start server in background then run tests
const { fork } = require('child_process');
const serverProc = fork('./server.js', { cwd: __dirname });

setTimeout(async () => {
    try {
        await runTests();
    } catch (e) {
        console.error('Test error:', e);
        process.exit(1);
    } finally {
        serverProc.kill();
    }
}, 1000);
