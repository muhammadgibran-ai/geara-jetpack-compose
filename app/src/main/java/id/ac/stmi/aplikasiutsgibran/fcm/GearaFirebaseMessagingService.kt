package id.ac.stmi.aplikasiutsgibran.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import id.ac.stmi.aplikasiutsgibran.MainActivity
import id.ac.stmi.aplikasiutsgibran.R
import id.ac.stmi.aplikasiutsgibran.auth.SessionManager
import id.ac.stmi.aplikasiutsgibran.data.GearaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Firebase Cloud Messaging Service untuk GEARA CRM Suku Cadang Otomotif.
 *
 * Menangani penerimaan push notifications seketika (real-time) di latar belakang (background)
 * maupun saat aplikasi sedang aktif dibuka (foreground), mencakup:
 * 1. Notifikasi Pelunasan Pembayaran Virtual Account / QRIS
 * 2. Notifikasi Kurir Mulai Mengantar Paket Suku Cadang ke Bengkel/Rumah
 * 3. CRM Auto-Reminder Servis Berkala Otomotif (Ganti Oli, Cek Rem, Servis Rutin)
 */
@AndroidEntryPoint
class GearaFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var repository: GearaRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "GearaFCM"
        const val CHANNEL_ID = "geara_crm_channel"
        const val CHANNEL_NAME = "Notifikasi CRM & Pesanan GEARA"
        const val CHANNEL_DESC = "Pemberitahuan status transaksi, pelacakan pengiriman kurir, dan pengingat servis suku cadang."

        // Tipe Event CRM
        const val TYPE_PAYMENT_SUCCESS = "PAYMENT_SUCCESS"
        const val TYPE_DELIVERY_START = "DELIVERY_START"
        const val TYPE_CRM_SERVICE_REMINDER = "CRM_SERVICE_REMINDER"
    }

    /**
     * Dipanggil saat token FCM baru pertama kali di-generate atau diperbarui oleh Firebase.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM Registration Token: $token")

        serviceScope.launch {
            sessionManager.saveFcmToken(token)
            val userId = sessionManager.userId.first()
            val email = sessionManager.userEmail.first()
            repository.registerFcmToken(token = token, userId = userId, email = email)
        }
    }

    /**
     * Dipanggil saat pesan push notification diterima dari server Firebase.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message Received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val type = data["type"] ?: TYPE_CRM_SERVICE_REMINDER
        val orderId = data["order_id"] ?: ""
        
        // Ambil judul dan isi pesan dari data payload atau notification payload
        val title = data["title"] ?: notification?.title ?: getCrmTitleForType(type, orderId)
        val body = data["body"] ?: notification?.body ?: getCrmBodyForType(type, orderId)

        showHeadsUpNotification(
            title = title,
            message = body,
            type = type,
            orderId = orderId
        )
    }

    private fun getCrmTitleForType(type: String, orderId: String): String {
        return when (type) {
            TYPE_PAYMENT_SUCCESS -> "Pembayaran Terverifikasi! ✅"
            TYPE_DELIVERY_START -> "Kurir Mulai Mengantar Paket! 🛵"
            TYPE_CRM_SERVICE_REMINDER -> "CRM Pengingat Servis Otomotif 🔧"
            else -> "Pemberitahuan GEARA Suku Cadang"
        }
    }

    private fun getCrmBodyForType(type: String, orderId: String): String {
        return when (type) {
            TYPE_PAYMENT_SUCCESS -> "Pembayaran pesanan #$orderId via Virtual Account / QRIS telah sukses diverifikasi. Paket sedang disiapkan gudang."
            TYPE_DELIVERY_START -> "Kurir ekspedisi sedang meluncur membawa suku cadang pesanan #$orderId menuju lokasi Anda!"
            TYPE_CRM_SERVICE_REMINDER -> "Hai Pelanggan Setia GEARA! Sudah 2.000 KM sejak servis terakhir, waktunya ganti oli mesin dan periksa sistem transmisi!"
            else -> "Cek pembaruan terbaru di aplikasi GEARA CRM."
        }
    }

    /**
     * Menampilkan notifikasi visual dengan NotificationChannel berprioritas tinggi (Heads-up).
     */
    private fun showHeadsUpNotification(
        title: String,
        message: String,
        type: String,
        orderId: String
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Buat NotificationChannel untuk Android 8.0 (API 26) ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                lightColor = Color.rgb(255, 184, 0) // Warna GearaYellow
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Pending Intent untuk membuka MainActivity dan mengarahkan ke tab/layar tujuan
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("fcm_type", type)
            putExtra("order_id", orderId)
            putExtra("target_screen", if (type == TYPE_CRM_SERVICE_REMINDER) "home" else "orders")
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            pendingIntentFlags
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_geara_logo)
            .setColor(0xFFFFB800.toInt()) // Geara Brand Accent
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
