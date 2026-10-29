package id.ac.stmi.aplikasiutsgibran.data

import id.ac.stmi.aplikasiutsgibran.R
import id.ac.stmi.aplikasiutsgibran.data.local.CartEntity
import id.ac.stmi.aplikasiutsgibran.data.local.GearaDao
import id.ac.stmi.aplikasiutsgibran.data.local.ProductEntity
import id.ac.stmi.aplikasiutsgibran.data.remote.CreateOrderRequest
import id.ac.stmi.aplikasiutsgibran.data.remote.GearaApiService
import id.ac.stmi.aplikasiutsgibran.data.remote.OrderDto
import id.ac.stmi.aplikasiutsgibran.data.remote.PaymentStatusResponse
import id.ac.stmi.aplikasiutsgibran.data.remote.RegisterFcmTokenRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GearaRepositoryImpl @Inject constructor(
    private val gearaDao: GearaDao,
    private val apiService: GearaApiService
) : GearaRepository {

    // --- API RETROFIT INTEGRATION (BACKEND NYATA DENGAN OFFLINE-FIRST ROOM CACHING) ---
    // 1. Ambil Katalog Produk: Jika online, sinkronkan ke Room DB; jika offline, baca langsung dari Room DB
    override suspend fun getProductsFromApi(): List<Product> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProducts()
            val remoteProducts = response.data.map { dto ->
                Product(
                    id = dto.id,
                    name = dto.name,
                    brand = dto.brand,
                    category = dto.category,
                    price = dto.price,
                    originalPrice = dto.originalPrice ?: (dto.price + 20000),
                    discountPercent = dto.discountPercent ?: 10,
                    rating = dto.rating ?: 4.8f,
                    soldCount = dto.soldCount ?: 100,
                    stock = dto.stock ?: 50,
                    inStock = dto.inStock ?: ((dto.stock ?: 50) > 0),
                    imageRes = R.drawable.ic_geara_logo, // fallback offline drawable
                    imageUrl = dto.imageUrl ?: "${GearaRepository.CLOUD_STORAGE_BASE_URL}${dto.id}.png",
                    description = dto.description ?: "Suku cadang original tersertifikasi CRM Geara.",
                    motorCompatibility = dto.motorCompatibility ?: listOf("Semua Motor"),
                    partNumber = dto.partNumber ?: "API-${dto.id}"
                )
            }

            // Simpan pembaruan katalog ke Room Database SQLite sebagai cache offline
            gearaDao.insertCachedProducts(
                remoteProducts.map { p ->
                    ProductEntity(
                        id = p.id,
                        name = p.name,
                        brand = p.brand,
                        category = p.category,
                        price = p.price,
                        originalPrice = p.originalPrice,
                        discountPercent = p.discountPercent,
                        rating = p.rating,
                        soldCount = p.soldCount,
                        stock = p.stock,
                        inStock = p.inStock,
                        imageRes = p.imageRes,
                        imageUrl = p.imageUrl,
                        description = p.description,
                        motorCompatibility = p.motorCompatibility.joinToString(","),
                        partNumber = p.partNumber
                    )
                }
            )
            remoteProducts
        } catch (e: Exception) {
            e.printStackTrace()
            // OFFLINE FALLBACK: SINKRONKAN SUKU CADANG RESMI MOBIL GEARA KE ROOM DATABASE
            gearaDao.insertCachedProducts(
                GearaRepository.products.map { p ->
                    ProductEntity(
                        id = p.id,
                        name = p.name,
                        brand = p.brand,
                        category = p.category,
                        price = p.price,
                        originalPrice = p.originalPrice,
                        discountPercent = p.discountPercent,
                        rating = p.rating,
                        soldCount = p.soldCount,
                        stock = p.stock,
                        inStock = p.inStock,
                        imageRes = p.imageRes,
                        imageUrl = p.imageUrl,
                        description = p.description,
                        motorCompatibility = p.motorCompatibility.joinToString(","),
                        partNumber = p.partNumber
                    )
                }
            )
            GearaRepository.products
        }
    }

    // Helper pemetaan OrderDto ke Domain Order Model
    private fun mapDtoToOrder(dto: OrderDto): Order {
        return Order(
            id = dto.id,
            date = dto.date,
            status = when (dto.status.uppercase()) {
                "PENDING", "MENUNGGU", "MENUNGGU PEMBAYARAN" -> OrderStatus.PENDING
                "COMPLETED", "SELESAI" -> OrderStatus.COMPLETED
                "CANCELED", "DIBATALKAN" -> OrderStatus.CANCELED
                else -> OrderStatus.ACTIVE
            },
            items = dto.items.map { itemDto ->
                CartItem(
                    product = GearaRepository.products.find { it.id == itemDto.productId } ?: Product(
                        id = itemDto.productId,
                        name = itemDto.productName,
                        brand = itemDto.brand,
                        category = "Suku Cadang",
                        price = itemDto.price,
                        originalPrice = itemDto.price,
                        discountPercent = 0,
                        rating = 4.8f,
                        soldCount = 50,
                        stock = 50,
                        imageRes = R.drawable.ic_geara_logo,
                        imageUrl = itemDto.imageUrl ?: "${GearaRepository.CLOUD_STORAGE_BASE_URL}${itemDto.productId}.png",
                        description = "",
                        motorCompatibility = emptyList(),
                        partNumber = itemDto.productId
                    ),
                    quantity = itemDto.quantity,
                    selected = true
                )
            },
            subtotal = dto.subtotal,
            deliveryFee = dto.deliveryFee,
            discount = dto.discount,
            totalAmount = dto.totalAmount,
            deliveryAddress = dto.deliveryAddress,
            courierName = dto.courierName,
            courierPhone = dto.courierPhone ?: "0812-9888-2121",
            resiNumber = dto.resiNumber,
            estimatedArrival = dto.estimatedArrival ?: "Hari ini, 17:00 WIB",
            trackingSteps = dto.trackingSteps?.map { stepDto ->
                TrackingStep(
                    title = stepDto.title,
                    time = stepDto.time,
                    description = stepDto.description,
                    isCompleted = stepDto.isCompleted,
                    isCurrent = stepDto.isCurrent
                )
            } ?: emptyList(),
            paymentMethod = dto.paymentMethod ?: "QRIS / Virtual Account",
            vaNumber = dto.vaNumber,
            paymentToken = dto.paymentToken
        )
    }

    // 2. Ambil Riwayat Pesanan Pelanggan dari Server API
    override suspend fun getOrdersFromApi(): List<Order> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getOrders()
            response.data.map { dto -> mapDtoToOrder(dto) }
        } catch (e: Exception) {
            e.printStackTrace()
            GearaRepository.initialOrders
        }
    }

    // 3. Kirim Transaksi Pesanan Baru ke Server (Status PENDING, menunggu pembayaran Gateway)
    override suspend fun createOrderApi(request: CreateOrderRequest): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createOrder(request)
            val dto = response.data
            Result.success(mapDtoToOrder(dto))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // 4. Cek Status Pembayaran Real-time dari Payment Gateway Webhook
    override suspend fun getPaymentStatus(orderId: String): Result<PaymentStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPaymentStatus(orderId)
            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // 5. Simulasi Webhook Pembayaran Lunas (Midtrans/Xendit Sandbox Callback)
    override suspend fun simulatePaymentSuccess(orderId: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.simulatePaymentSuccess(mapOf("order_id" to orderId))
            val dto = response.data
            Result.success(mapDtoToOrder(dto))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // 6. Ambil Notifikasi CRM Tersentralisasi dari Server
    override suspend fun getNotificationsFromApi(): List<NotificationItem> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getNotifications()
            response.data.map { dto ->
                NotificationItem(
                    id = dto.id,
                    title = dto.title,
                    message = dto.message,
                    time = dto.time,
                    category = dto.category,
                    isRead = dto.isRead
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            GearaRepository.initialNotifications
        }
    }

    // --- ROOM DATABASE INTEGRATION ---
    // Fungsi untuk mendapatkan data keranjang dari Room Database (SQLite lokal)
    override fun getCartItemsFromRoom(): Flow<List<CartItem>> {
        return gearaDao.getAllCartItems().map { entities ->
            entities.map { entity ->
                CartItem(
                    product = GearaRepository.products.find { it.id == entity.productId } ?: Product(
                        id = entity.productId,
                        name = entity.name,
                        brand = entity.brand,
                        category = entity.category,
                        price = entity.price,
                        originalPrice = entity.price,
                        discountPercent = 0,
                        rating = 0f,
                        soldCount = 0,
                        imageRes = entity.imageRes,
                        imageUrl = entity.imageUrl,
                        description = "",
                        motorCompatibility = emptyList(),
                        partNumber = ""
                    ),
                    quantity = entity.quantity,
                    selected = entity.isSelected
                )
            }
        }
    }

    override suspend fun insertOrUpdateCartToRoom(item: CartItem) {
        withContext(Dispatchers.IO) {
            gearaDao.insertCartItem(
                CartEntity(
                    productId = item.product.id,
                    name = item.product.name,
                    brand = item.product.brand,
                    price = item.product.price,
                    imageRes = item.product.imageRes,
                    imageUrl = item.product.imageUrl,
                    category = item.product.category,
                    quantity = item.quantity,
                    isSelected = item.selected
                )
            )
        }
    }

    override suspend fun removeCartItemFromRoom(productId: String) {
        withContext(Dispatchers.IO) {
            gearaDao.deleteCartItemById(productId)
        }
    }

    override suspend fun clearSelectedCartInRoom() {
        withContext(Dispatchers.IO) {
            gearaDao.clearSelectedCartItems()
        }
    }

    override suspend fun registerFcmToken(token: String, userId: String?, email: String?): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.registerFcmToken(
                RegisterFcmTokenRequest(
                    token = token,
                    userId = userId,
                    email = email
                )
            )
            Result.success(response.message)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
