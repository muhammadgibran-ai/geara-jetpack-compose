package id.ac.stmi.aplikasiutsgibran.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.ac.stmi.aplikasiutsgibran.auth.SessionManager
import id.ac.stmi.aplikasiutsgibran.data.remote.CreateOrderItemRequest
import id.ac.stmi.aplikasiutsgibran.data.remote.CreateOrderRequest
import id.ac.stmi.aplikasiutsgibran.data.remote.FirestoreSyncService
import id.ac.stmi.aplikasiutsgibran.util.network.ConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val id: String,
    val sender: String,
    val text: String,
    val time: String,
    val isFromUser: Boolean
)

sealed class CheckoutState {
    data object Idle : CheckoutState()
    data object Loading : CheckoutState()
    data class Success(val orderId: String) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}

@HiltViewModel
class GearaViewModel @Inject constructor(
    private val repository: GearaRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val sessionManager: SessionManager,
    private val firestoreSyncService: FirestoreSyncService
) : ViewModel() {

    // --- Pemantau Status Koneksi Jaringan Real-Time (Offline UX) ---
    val networkStatus: StateFlow<ConnectivityObserver.Status> = connectivityObserver.observe()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ConnectivityObserver.Status.Available
        )

    val isOffline: StateFlow<Boolean> = networkStatus.map { status ->
        status != ConnectivityObserver.Status.Available
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    private val _products = MutableStateFlow<List<Product>>(GearaRepository.products)
    val availableProducts: StateFlow<List<Product>> = _products.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(GearaRepository.initialOrders)
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(GearaRepository.initialNotifications)
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _appliedVoucher = MutableStateFlow<String?>(null)
    val appliedVoucher: StateFlow<String?> = _appliedVoucher.asStateFlow()

    // --- State Favorit Suku Cadang (Reaktif & Interaktif) ---
    private val _favoriteProductIds = MutableStateFlow<Set<String>>(
        setOf("prod_5", "prod_9", "prod_7", "prod_8", "prod_1", "prod_3")
    )
    val favoriteProductIds: StateFlow<Set<String>> = _favoriteProductIds.asStateFlow()

    fun isFavorite(productId: String): Boolean {
        return _favoriteProductIds.value.contains(productId)
    }

    fun toggleFavorite(productId: String): Boolean {
        val current = _favoriteProductIds.value.toMutableSet()
        val isNowFavorite = if (current.contains(productId)) {
            current.remove(productId)
            false
        } else {
            current.add(productId)
            true
        }
        _favoriteProductIds.value = current
        return isNowFavorite
    }

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("1", "Geara CRM Specialist", "Halo Gibran! Selamat datang di Layanan CRM After-Sales Geara Motors. Ada yang bisa kami bantu seputar spesifikasi suku cadang resmi atau jadwal servis berkala mobil Geara Anda?", "10:00", false),
            ChatMessage("2", "Gibran", "Halo min, apakah kampas rem ceramic cocok untuk mobil Geara Terra SUV 1.5L Turbo?", "10:02", true),
            ChatMessage("3", "Geara CRM Specialist", "Cocok sekali Pak Gibran! Part number GEARA-BP-CERAMIC-04465 kami 100% plug & play dan bergaransi resmi pabrikan Geara, bebas decit dan pengereman presisi.", "10:03", false)
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    val totalCartCount: Int
        get() = _cartItems.value.sumOf { it.quantity }

    val selectedSubtotal: Int
        get() = _cartItems.value.filter { it.selected }.sumOf { it.totalPrice }

    val deliveryFee: Int
        get() = if (selectedSubtotal > 0) 15000 else 0

    val discountAmount: Int
        get() = if (_appliedVoucher.value != null && selectedSubtotal > 0) 25000 else 0

    val grandTotal: Int
        get() = (selectedSubtotal + deliveryFee - discountAmount).coerceAtLeast(0)

    val formattedGrandTotal: String
        get() = "Rp " + "%,d".format(grandTotal).replace(',', '.')

    fun refreshDataFromApi() {
        viewModelScope.launch {
            val remoteProducts = repository.getProductsFromApi()
            _products.value = remoteProducts
            val remoteOrders = repository.getOrdersFromApi()
            _orders.value = remoteOrders
            val remoteNotifications = repository.getNotificationsFromApi()
            _notifications.value = remoteNotifications
        }
    }

    init {
        // Ambil data produk & sinkronkan katalog
        refreshDataFromApi()

        // Auto-reconnect: Jika koneksi pulih ke Available, perbarui data otomatis dari server
        viewModelScope.launch {
            networkStatus.collect { status ->
                if (status == ConnectivityObserver.Status.Available) {
                    refreshDataFromApi()
                }
            }
        }

        // Observasi Cart Items dari Local Storage (Room Database)
        viewModelScope.launch {
            repository.getCartItemsFromRoom().collect { localCart ->
                if (localCart.isEmpty() && _cartItems.value.isEmpty()) {
                    // Fallback inisiasi awal jika database kosong
                    _cartItems.value = GearaRepository.initialCart
                    GearaRepository.initialCart.forEach { 
                        repository.insertOrUpdateCartToRoom(it) 
                    }
                } else {
                    _cartItems.value = localCart
                }

                // Sinkronkan data keranjang ke Cloud Firestore jika user aktif
                val uid = sessionManager.userId.first()
                firestoreSyncService.syncCart(uid, _cartItems.value)
            }
        }

        // Seeding awal katalog produk ke Cloud Firestore (jika koleksi products di Firestore masih kosong)
        viewModelScope.launch {
            firestoreSyncService.seedInitialProducts(GearaRepository.products)
        }
    }

    fun refreshAllFromApi() {
        viewModelScope.launch {
            _products.value = repository.getProductsFromApi()
            _orders.value = repository.getOrdersFromApi()
            _notifications.value = repository.getNotificationsFromApi()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addToCart(product: Product, qty: Int = 1) {
        viewModelScope.launch {
            val currentList = _cartItems.value
            val existingItem = currentList.find { it.product.id == product.id }
            val newItem = if (existingItem != null) {
                existingItem.copy(quantity = existingItem.quantity + qty)
            } else {
                CartItem(product = product, quantity = qty, selected = true)
            }
            repository.insertOrUpdateCartToRoom(newItem)
        }
    }

    fun updateQuantity(productId: String, delta: Int) {
        viewModelScope.launch {
            val item = _cartItems.value.find { it.product.id == productId }
            if (item != null) {
                val newQty = item.quantity + delta
                if (newQty > 0) {
                    repository.insertOrUpdateCartToRoom(item.copy(quantity = newQty))
                } else {
                    repository.removeCartItemFromRoom(productId)
                }
            }
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            repository.removeCartItemFromRoom(productId)
        }
    }

    fun toggleSelection(productId: String) {
        viewModelScope.launch {
            val item = _cartItems.value.find { it.product.id == productId }
            if (item != null) {
                repository.insertOrUpdateCartToRoom(item.copy(selected = !item.selected))
            }
        }
    }

    fun selectAll(select: Boolean) {
        viewModelScope.launch {
            _cartItems.value.forEach { item ->
                if (item.selected != select) {
                    repository.insertOrUpdateCartToRoom(item.copy(selected = select))
                }
            }
        }
    }

    fun applyVoucher(code: String): Boolean {
        return if (code.trim().equals("GEARAPROMO", ignoreCase = true) || code.trim().equals("CRM2026", ignoreCase = true)) {
            _appliedVoucher.value = code.uppercase()
            true
        } else {
            false
        }
    }

    fun resetCheckoutState() {
        _checkoutState.value = CheckoutState.Idle
    }

    fun placeOrder(address: String, courier: String, payment: String) {
        val selectedItems = _cartItems.value.filter { it.selected }
        
        // Validasi Checkout
        if (selectedItems.isEmpty()) {
            _checkoutState.value = CheckoutState.Error("Keranjang kosong. Pilih barang untuk dicheckout.")
            return
        }
        
        if (address.isBlank() || address.length < 10) {
            _checkoutState.value = CheckoutState.Error("Alamat pengiriman terlalu pendek atau kosong.")
            return
        }
        
        _checkoutState.value = CheckoutState.Loading
        
        viewModelScope.launch {
            val orderRequest = CreateOrderRequest(
                items = selectedItems.map { CreateOrderItemRequest(productId = it.product.id, quantity = it.quantity) },
                subtotal = selectedSubtotal,
                deliveryFee = deliveryFee,
                discount = discountAmount,
                totalAmount = grandTotal,
                deliveryAddress = address,
                courierName = courier,
                paymentMethod = payment
            )

            // Kirim transaksi pesanan ke server backend nyata via Retrofit
            val apiResult = repository.createOrderApi(orderRequest)

            if (apiResult.isSuccess) {
                val serverOrder = apiResult.getOrNull()!!
                // Masukkan pesanan baru ke StateFlow
                _orders.update { listOf(serverOrder) + it }
                
                // Refresh stok produk tersentralisasi dari server setelah transaksi berhasil
                val updatedProducts = repository.getProductsFromApi()
                _products.value = updatedProducts

                // Bersihkan item yang dicheckout dari keranjang (RAM dan Room SQLite)
                _cartItems.update { current -> current.filterNot { it.selected } }
                repository.clearSelectedCartInRoom()
                _appliedVoucher.value = null
                
                // Sinkronkan order baru ke Cloud Firestore
                val currentUserId = sessionManager.userId.first()
                val currentUserName = sessionManager.userName.first()
                val currentUserEmail = sessionManager.userEmail.first()
                firestoreSyncService.syncOrder(
                    order = serverOrder,
                    userId = currentUserId,
                    userEmail = currentUserEmail,
                    userName = currentUserName
                )

                _checkoutState.value = CheckoutState.Success(serverOrder.id)
            } else {
                // Fallback offline jika server backend lokal belum dinyalakan:
                // Pesanan dibuat secara lokal dan stok dikurangi secara lokal agar aplikasi tetap berfungsi sempurna
                val courierServiceType = if (courier.contains("SPX")) "Kurir Internal Resmi" else "Kurir Pihak Ketiga"
                val driverName = if (courier.contains("SPX")) "Budi Santoso (Mitra SPX)"
                    else if (courier.contains("JNE")) "Rudi Pratama (Kurir JNE)"
                    else if (courier.contains("J&T")) "Ahmad Hidayat (Kurir J&T)"
                    else "Doni Kusuma (Kurir SiCepat)"
                val driverPhone = if (courier.contains("SPX")) "0812-9876-5432"
                    else if (courier.contains("JNE")) "0813-5566-7788"
                    else if (courier.contains("J&T")) "0821-1122-3344"
                    else "0856-7788-9900"
                val driverVehicle = if (courier.contains("SPX")) "Honda Vario 160 • B 4821 GBR"
                    else if (courier.contains("JNE")) "Motor Box JNE • B 3456 JNE"
                    else if (courier.contains("J&T")) "Blind Van GranMax • B 9123 JNT"
                    else "Motor SiCepat • B 6789 SCP"
                val generatedResi = when {
                    courier.contains("SPX") -> "SPXID" + (100000000..999999999).random().toString()
                    courier.contains("JNE") -> "JNE" + (100000000..999999999).random().toString()
                    courier.contains("J&T") -> "JT" + (100000000..999999999).random().toString()
                    else -> "SCP" + (100000000..999999999).random().toString()
                }

                val fallbackOrder = Order(
                    id = "GR-" + (1000000..9999999).random().toString(),
                    date = "Hari ini, 15:00 WIB",
                    status = OrderStatus.PENDING,
                    items = selectedItems,
                    subtotal = selectedSubtotal,
                    deliveryFee = deliveryFee,
                    discount = discountAmount,
                    totalAmount = grandTotal,
                    deliveryAddress = address,
                    courierName = courier,
                    courierServiceType = courierServiceType,
                    courierPhone = driverPhone,
                    courierDriverName = driverName,
                    courierVehicle = driverVehicle,
                    resiNumber = generatedResi,
                    estimatedArrival = if (courier.contains("Sameday")) "Hari ini, 16:30 WIB" else "Besok, 14:00 WIB",
                    trackingSteps = listOf(
                        TrackingStep("Pesanan Dibuat (Menunggu Pembayaran)", "15:00 WIB", "Silakan selesaikan pembayaran melalui Virtual Account atau QRIS.", isCompleted = false, isCurrent = true),
                        TrackingStep("Penjual Menyiapkan & Mengemas Paket", "Estimasi setelah bayar", "Gudang Geara akan memeriksa spesifikasi suku cadang Anda."),
                        TrackingStep("Pengiriman via $courier", "Estimasi setelah dikemas", "Paket akan diantar oleh $courier.")
                    ),
                    paymentMethod = payment,
                    vaNumber = "1280808123456789",
                    paymentToken = "SNAP-OFFLINE-GR"
                )

                // Update inventory lokal
                _products.update { currentList ->
                    currentList.map { p ->
                        val bought = selectedItems.find { it.product.id == p.id }
                        if (bought != null) {
                            val newStock = (p.stock - bought.quantity).coerceAtLeast(0)
                            p.copy(stock = newStock, inStock = newStock > 0)
                        } else p
                    }
                }

                _orders.update { listOf(fallbackOrder) + it }
                _cartItems.update { current -> current.filterNot { it.selected } }
                repository.clearSelectedCartInRoom()
                _appliedVoucher.value = null

                // Sinkronkan order baru ke Cloud Firestore
                val currentUserId = sessionManager.userId.first()
                val currentUserName = sessionManager.userName.first()
                val currentUserEmail = sessionManager.userEmail.first()
                firestoreSyncService.syncOrder(
                    order = fallbackOrder,
                    userId = currentUserId,
                    userEmail = currentUserEmail,
                    userName = currentUserName
                )

                _checkoutState.value = CheckoutState.Success(fallbackOrder.id)
            }
        }
    }

    fun getOrderById(orderId: String): Order? {
        return _orders.value.find { it.id == orderId }
    }

    fun checkPaymentStatus(orderId: String, onPaid: () -> Unit) {
        viewModelScope.launch {
            val result = repository.getPaymentStatus(orderId)
            if (result.isSuccess) {
                val statusResp = result.getOrNull()
                if (statusResp?.isPaid == true) {
                    _orders.update { list ->
                        list.map { o ->
                            if (o.id == orderId) o.copy(status = OrderStatus.PACKED) else o
                        }
                    }
                    val currentUserId = sessionManager.userId.first()
                    firestoreSyncService.updateOrderStatus(orderId, OrderStatus.PACKED, currentUserId)
                    onPaid()
                }
            }
        }
    }

    fun simulatePaymentSuccess(orderId: String, onPaid: () -> Unit) {
        viewModelScope.launch {
            val result = repository.simulatePaymentSuccess(orderId)
            if (result.isSuccess) {
                val updatedOrder = result.getOrNull()!!
                _orders.update { list ->
                    list.map { if (it.id == orderId) updatedOrder.copy(status = OrderStatus.PACKED) else it }
                }
                // Refresh notifications
                _notifications.value = repository.getNotificationsFromApi()
                val currentUserId = sessionManager.userId.first()
                firestoreSyncService.updateOrderStatus(orderId, OrderStatus.PACKED, currentUserId)
                onPaid()
            } else {
                // Offline fallback simulation
                _orders.update { list ->
                    list.map { o ->
                        if (o.id == orderId) o.copy(status = OrderStatus.PACKED) else o
                    }
                }
                val currentUserId = sessionManager.userId.first()
                firestoreSyncService.updateOrderStatus(orderId, OrderStatus.PACKED, currentUserId)
                onPaid()
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        _orders.update { list ->
            list.map { if (it.id == orderId) it.copy(status = newStatus) else it }
        }
        viewModelScope.launch {
            val currentUserId = sessionManager.userId.first()
            firestoreSyncService.updateOrderStatus(orderId, newStatus, currentUserId)
        }
    }

    fun processShipment(orderId: String, courierName: String, resiNumber: String) {
        _orders.update { list ->
            list.map { order ->
                if (order.id == orderId) {
                    val newSteps = listOf(
                        TrackingStep("Pesanan Dibuat & Pembayaran Dikonfirmasi", order.date, "Pembayaran berhasil diverifikasi.", "Jakarta", true, false),
                        TrackingStep("Penjual Telah Menyerahkan Paket ke $courierName", "Baru saja", "Resi pengiriman otomatis diterbitkan oleh sistem logistik.", "Gudang Geara", true, false),
                        TrackingStep("Paket Dalam Perjalanan ke Alamat Tujuan", "Sekarang", "Kurir sedang membawa pesanan Anda menuju alamat tujuan.", "Dalam Pengantaran", true, true),
                        TrackingStep("Pesanan Diterima Pembeli", "Estimasi Besok", "Paket sampai di alamat tujuan.", order.deliveryAddress, false, false)
                    )
                    order.copy(
                        status = OrderStatus.SHIPPED,
                        courierName = courierName,
                        resiNumber = resiNumber,
                        trackingSteps = newSteps
                    )
                } else order
            }
        }
        viewModelScope.launch {
            val currentUserId = sessionManager.userId.first()
            firestoreSyncService.updateOrderStatus(orderId, OrderStatus.SHIPPED, currentUserId)
        }
    }

    fun sendSupportMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(
            id = System.currentTimeMillis().toString(),
            sender = "Gibran",
            text = text,
            time = "Sekarang",
            isFromUser = true
        )
        _chatMessages.update { it + userMsg }

        // Simulated auto reply from CRM assistant
        val autoReply = ChatMessage(
            id = (System.currentTimeMillis() + 1).toString(),
            sender = "Geara CRM Specialist",
            text = "Terima kasih atas pertanyaannya! Teknisi kami siap membantu memeriksa ketersediaan suku cadang dan rekomendasi bengkel rekanan terdekat.",
            time = "Sekarang",
            isFromUser = false
        )
        _chatMessages.update { it + autoReply }
    }

    fun markNotificationAsRead(id: String) {
        _notifications.update { current ->
            current.map { if (it.id == id) it.copy(isRead = true) else it }
        }
    }
}
