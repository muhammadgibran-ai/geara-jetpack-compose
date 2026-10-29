package id.ac.stmi.aplikasiutsgibran.data

enum class OrderStatus(val label: String) {
    PENDING("Belum Bayar"),
    PACKED("Dikemas"),
    SHIPPED("Dikirim"),
    ACTIVE("Dikirim"),
    COMPLETED("Selesai"),
    CANCELED("Dibatalkan")
}

data class TrackingStep(
    val title: String,
    val time: String,
    val description: String,
    val location: String = "Jakarta",
    val isCompleted: Boolean = false,
    val isCurrent: Boolean = false
)

data class Order(
    val id: String,
    val date: String,
    val status: OrderStatus,
    val items: List<CartItem>,
    val subtotal: Int,
    val deliveryFee: Int,
    val discount: Int = 0,
    val totalAmount: Int,
    val deliveryAddress: String,
    val courierName: String = "SPX Express - Sameday",
    val courierServiceType: String = "Kurir Internal",
    val courierPhone: String = "0812-3456-7890",
    val courierDriverName: String = "Budi Santoso",
    val courierVehicle: String = "Honda Vario 160 • B 4821 GBR",
    val resiNumber: String = "SPXID02948219488",
    val estimatedArrival: String = "Hari ini, 16:30 WIB",
    val trackingSteps: List<TrackingStep> = emptyList(),
    val paymentMethod: String = "QRIS / Virtual Account",
    val vaNumber: String? = null,
    val paymentToken: String? = null
) {
    val formattedTotal: String
        get() = "Rp " + "%,d".format(totalAmount).replace(',', '.')
}
