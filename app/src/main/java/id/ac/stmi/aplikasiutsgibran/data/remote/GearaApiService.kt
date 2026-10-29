package id.ac.stmi.aplikasiutsgibran.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface GearaApiService {
    
    // Ambil seluruh katalog produk dari server API tersentralisasi (termasuk status stok)
    @GET("api/v1/products")
    suspend fun getProducts(): ProductResponse

    // Ambil detail satu produk berdasarkan ID beserta stok terbarunya
    @GET("api/v1/products/{id}")
    suspend fun getProductById(@Path("id") id: String): ProductDetailResponse

    // Ambil riwayat seluruh transaksi pesanan pelanggan dari server
    @GET("api/v1/orders")
    suspend fun getOrders(): OrderResponse

    // Ambil satu pesanan berdasarkan ID
    @GET("api/v1/orders/{id}")
    suspend fun getOrderById(@Path("id") id: String): OrderDetailResponse

    // Kirim checkout pesanan baru ke server (Status awal: PENDING, menunggu pembayaran Payment Gateway)
    @POST("api/v1/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): OrderDetailResponse

    // Webhook Payment Gateway (Midtrans/Xendit/Stripe) untuk mengubah status pesanan dari PENDING ke ACTIVE
    @POST("api/v1/payments/webhook")
    suspend fun sendPaymentWebhook(@Body webhook: PaymentWebhookRequest): OrderDetailResponse

    // Cek status pembayaran real-time suatu pesanan
    @GET("api/v1/orders/{id}/payment-status")
    suspend fun getPaymentStatus(@Path("id") id: String): PaymentStatusResponse

    // Simulasi pelunasan pembayaran sukses via Payment Gateway Sandbox
    @POST("api/v1/payments/simulate-success")
    suspend fun simulatePaymentSuccess(@Body request: Map<String, String>): OrderDetailResponse

    // Ambil daftar notifikasi CRM dari server
    @GET("api/v1/notifications")
    suspend fun getNotifications(): NotificationResponse

    // Registrasi Token FCM perangkat ke backend
    @POST("api/v1/notifications/register-token")
    suspend fun registerFcmToken(@Body request: RegisterFcmTokenRequest): FcmTokenResponse
}

