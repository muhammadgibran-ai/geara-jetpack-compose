package id.ac.stmi.aplikasiutsgibran.data

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val category: String, // "Semua", "Promo", "Pesanan", "Servis"
    val isRead: Boolean = false
)
