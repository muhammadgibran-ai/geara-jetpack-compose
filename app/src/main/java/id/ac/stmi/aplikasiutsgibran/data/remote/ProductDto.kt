package id.ac.stmi.aplikasiutsgibran.data.remote

import com.google.gson.annotations.SerializedName

// --- PRODUCT DTO ---
data class ProductDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("price") val price: Int,
    @SerializedName("original_price") val originalPrice: Int? = null,
    @SerializedName("discount_percent") val discountPercent: Int? = null,
    @SerializedName("category") val category: String,
    @SerializedName("rating") val rating: Float? = null,
    @SerializedName("sold_count") val soldCount: Int? = null,
    @SerializedName("stock") val stock: Int? = null,
    @SerializedName("in_stock") val inStock: Boolean? = null,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("description") val description: String? = null,
    @SerializedName("motor_compatibility") val motorCompatibility: List<String>? = null,
    @SerializedName("part_number") val partNumber: String? = null
)

data class ProductResponse(
    @SerializedName("status") val status: String,
    @SerializedName("total") val total: Int? = null,
    @SerializedName("data") val data: List<ProductDto>
)

data class ProductDetailResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: ProductDto
)

// --- ORDER DTO ---
data class OrderItemDto(
    @SerializedName("product_id") val productId: String,
    @SerializedName("product_name") val productName: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("price") val price: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("image_url") val imageUrl: String? = null
)

data class TrackingStepDto(
    @SerializedName("title") val title: String,
    @SerializedName("time") val time: String,
    @SerializedName("description") val description: String,
    @SerializedName("is_completed") val isCompleted: Boolean = false,
    @SerializedName("is_current") val isCurrent: Boolean = false
)

data class OrderDto(
    @SerializedName("id") val id: String,
    @SerializedName("date") val date: String,
    @SerializedName("status") val status: String,
    @SerializedName("items") val items: List<OrderItemDto>,
    @SerializedName("subtotal") val subtotal: Int,
    @SerializedName("delivery_fee") val deliveryFee: Int,
    @SerializedName("discount") val discount: Int = 0,
    @SerializedName("total_amount") val totalAmount: Int,
    @SerializedName("delivery_address") val deliveryAddress: String,
    @SerializedName("courier_name") val courierName: String,
    @SerializedName("courier_phone") val courierPhone: String? = null,
    @SerializedName("resi_number") val resiNumber: String,
    @SerializedName("estimated_arrival") val estimatedArrival: String? = null,
    @SerializedName("tracking_steps") val trackingSteps: List<TrackingStepDto>? = null,
    @SerializedName("payment_method") val paymentMethod: String? = null,
    @SerializedName("payment_token") val paymentToken: String? = null,
    @SerializedName("va_number") val vaNumber: String? = null,
    @SerializedName("payment_url") val paymentUrl: String? = null
)

data class OrderResponse(
    @SerializedName("status") val status: String,
    @SerializedName("total") val total: Int? = null,
    @SerializedName("data") val data: List<OrderDto>
)

data class OrderDetailResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: OrderDto
)

// --- CREATE ORDER REQUEST DTO ---
data class CreateOrderItemRequest(
    @SerializedName("product_id") val productId: String,
    @SerializedName("quantity") val quantity: Int
)

data class CreateOrderRequest(
    @SerializedName("items") val items: List<CreateOrderItemRequest>,
    @SerializedName("subtotal") val subtotal: Int,
    @SerializedName("delivery_fee") val deliveryFee: Int,
    @SerializedName("discount") val discount: Int,
    @SerializedName("total_amount") val totalAmount: Int,
    @SerializedName("delivery_address") val deliveryAddress: String,
    @SerializedName("courier_name") val courierName: String,
    @SerializedName("payment_method") val paymentMethod: String
)

// --- NOTIFICATION DTO ---
data class NotificationDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("time") val time: String,
    @SerializedName("category") val category: String,
    @SerializedName("is_read") val isRead: Boolean = false
)

data class NotificationResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<NotificationDto>
)

// --- PAYMENT WEBHOOK & GATEWAY DTO ---
data class PaymentWebhookRequest(
    @SerializedName("order_id") val orderId: String,
    @SerializedName("transaction_status") val transactionStatus: String = "settlement",
    @SerializedName("payment_type") val paymentType: String = "qris",
    @SerializedName("gross_amount") val grossAmount: Int,
    @SerializedName("signature_key") val signatureKey: String? = null
)

data class PaymentStatusResponse(
    @SerializedName("status") val status: String,
    @SerializedName("order_id") val orderId: String,
    @SerializedName("order_status") val orderStatus: String,
    @SerializedName("payment_type") val paymentType: String? = null,
    @SerializedName("is_paid") val isPaid: Boolean
)

// --- FCM PUSH NOTIFICATION DTO ---
data class RegisterFcmTokenRequest(
    @SerializedName("token") val token: String,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("device_info") val deviceInfo: String = "Android Device"
)

data class FcmTokenResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

