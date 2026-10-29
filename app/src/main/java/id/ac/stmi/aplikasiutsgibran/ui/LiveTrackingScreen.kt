package id.ac.stmi.aplikasiutsgibran.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.ac.stmi.aplikasiutsgibran.R
import id.ac.stmi.aplikasiutsgibran.data.GearaViewModel
import id.ac.stmi.aplikasiutsgibran.data.OrderStatus
import id.ac.stmi.aplikasiutsgibran.data.TrackingStep
import id.ac.stmi.aplikasiutsgibran.ui.components.GearaBottomNavBar
import id.ac.stmi.aplikasiutsgibran.ui.components.GearaImage
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaDarkBrown
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaYellow

@Composable
fun LiveTrackingScreen(
    orderId: String,
    navController: NavController,
    viewModel: GearaViewModel
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val orders by viewModel.orders.collectAsState()

    // Cari pesanan spesifik berdasarkan ID pesanan
    val currentOrder = remember(orders, orderId) {
        orders.find { it.id == orderId }
            ?: orders.find { it.status == OrderStatus.SHIPPED || it.status == OrderStatus.ACTIVE }
            ?: orders.firstOrNull()
    }

    // Status pesanan saat ini (Belum Bayar, Dikemas, Dikirim, atau Selesai)
    val orderStatus = currentOrder?.status ?: OrderStatus.SHIPPED
    val isPacked = orderStatus == OrderStatus.PACKED
    val isShipped = orderStatus == OrderStatus.SHIPPED || orderStatus == OrderStatus.ACTIVE
    val isPending = orderStatus == OrderStatus.PENDING
    val isCompleted = orderStatus == OrderStatus.COMPLETED

    // Data kurir tunggal yang melekat pada order ini (dipilih saat checkout)
    val courierName = currentOrder?.courierName ?: "SPX Express - Sameday"
    val courierServiceType = currentOrder?.courierServiceType ?: if (courierName.contains("SPX")) "Kurir Internal Resmi" else "Kurir Pihak Ketiga"
    val resiNumber = currentOrder?.resiNumber ?: "SPXID02948219488"
    val driverName = currentOrder?.courierDriverName ?: "Budi Santoso (Mitra SPX)"
    val driverPhone = currentOrder?.courierPhone ?: "0812-9876-5432"
    val driverVehicle = currentOrder?.courierVehicle ?: "Honda Vario 160 • B 4821 GBR"
    val estimatedArrival = currentOrder?.estimatedArrival ?: "Hari ini, 16:30 WIB"
    val isInternalCourier = courierServiceType.contains("Internal") || courierName.contains("SPX")

    // Animasi radar berdenyut (pada kurir jika dikirim, atau pada gudang jika sedang dikemas)
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val radarPulseRadius by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 48f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarPulseRadius"
    )
    val radarPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarPulseAlpha"
    )

    // Milestone tahapan pergerakan fisik yang konsisten dengan status pesanan sebenarnya
    val dynamicTrackingSteps = remember(orderStatus, courierName, resiNumber, driverName, estimatedArrival) {
        when {
            isPacked -> listOf(
                TrackingStep(
                    title = "Pesanan Dibuat & Pembayaran Terkonfirmasi",
                    time = currentOrder?.date ?: "25 Okt, 11:00 WIB",
                    description = "Pembayaran telah berhasil diverifikasi oleh sistem Geara Payments.",
                    location = "Jakarta",
                    isCompleted = true,
                    isCurrent = false
                ),
                TrackingStep(
                    title = "Penjual Menyiapkan & Mengemas Paket",
                    time = "25 Okt, 11:30 WIB",
                    description = "Gudang Geara Official Parts memeriksa part number OEM, segel hologram garansi, dan membungkus proteksi bubble wrap ganda.",
                    location = "Gudang Pusat Geara (Cakung DC)",
                    isCompleted = true,
                    isCurrent = true
                ),
                TrackingStep(
                    title = "Menunggu Penjemputan oleh $courierName",
                    time = "Estimasi Hari ini, 17:00 WIB",
                    description = "Nomor booking resi $resiNumber telah diterbitkan dan menunggu kurir pick-up mengambil paket dari gudang.",
                    location = "Drop Point DC Cakung",
                    isCompleted = false,
                    isCurrent = false
                ),
                TrackingStep(
                    title = "Paket Dalam Pengantaran ke Alamat Tujuan",
                    time = "Estimasi Besok",
                    description = "Paket suku cadang akan dibawa oleh armada kurir $courierName menuju alamat penerima.",
                    location = "Dalam Perjalanan",
                    isCompleted = false,
                    isCurrent = false
                ),
                TrackingStep(
                    title = "Pesanan Diterima Pembeli",
                    time = "Estimasi $estimatedArrival",
                    description = "Paket tiba di alamat pembeli (${currentOrder?.deliveryAddress?.take(25) ?: "Alamat Pembeli"}...).",
                    location = "Tujuan Pengiriman",
                    isCompleted = false,
                    isCurrent = false
                )
            )

            isShipped -> listOf(
                TrackingStep(
                    title = "Pesanan Dibuat & Pembayaran Terkonfirmasi",
                    time = "25 Okt, 09:15 WIB",
                    description = "Sistem pembayaran Geara telah menerima dan memverifikasi pesanan suku cadang mobil.",
                    location = "Jakarta",
                    isCompleted = true,
                    isCurrent = false
                ),
                TrackingStep(
                    title = "Penjual Menyiapkan & Mengemas Paket",
                    time = "25 Okt, 10:30 WIB",
                    description = "Gudang resmi Geara Official Parts memeriksa part number, hologram OEM, dan membungkus proteksi ganda.",
                    location = "Gudang Pusat Geara (Cakung DC)",
                    isCompleted = true,
                    isCurrent = false
                ),
                TrackingStep(
                    title = "Paket Diserahkan ke $courierName",
                    time = "25 Okt, 11:45 WIB",
                    description = "Paket telah diterima drop point logistik dan nomor resi $resiNumber aktif.",
                    location = "Drop Point DC Cakung",
                    isCompleted = true,
                    isCurrent = false
                ),
                TrackingStep(
                    title = "Paket Tiba di Hub Transit Jakarta Timur",
                    time = "25 Okt, 13:20 WIB",
                    description = "Sortir paket suku cadang selesai dan diteruskan ke kurir armada pengantaran terakhir.",
                    location = "Hub Transit Jaktim",
                    isCompleted = true,
                    isCurrent = false
                ),
                TrackingStep(
                    title = "Kurir Sedang Bergerak Menuju Alamat Tujuan",
                    time = "25 Okt, 15:10 WIB",
                    description = "Paket sedang dibawa mitra kurir $driverName ($driverVehicle). Jarak ~1.8 km lagi.",
                    location = "Dalam Perjalanan (1.8 km)",
                    isCompleted = true,
                    isCurrent = true
                ),
                TrackingStep(
                    title = "Pesanan Diterima Pembeli",
                    time = "Estimasi $estimatedArrival",
                    description = "Paket tiba di alamat pembeli (${currentOrder?.deliveryAddress?.take(25) ?: "Alamat Pembeli"}...).",
                    location = "Alamat Pembeli",
                    isCompleted = false,
                    isCurrent = false
                )
            )

            isPending -> listOf(
                TrackingStep(
                    title = "Pesanan Dibuat (Menunggu Pembayaran)",
                    time = currentOrder?.date ?: "Hari ini",
                    description = "Silakan selesaikan pembayaran sebelum batas waktu 24 jam agar pesanan segera diproses penjual.",
                    location = "Jakarta",
                    isCompleted = false,
                    isCurrent = true
                ),
                TrackingStep(
                    title = "Penjual Menyiapkan & Mengemas Paket",
                    time = "Estimasi setelah bayar",
                    description = "Penjual akan menyiapkan dan memeriksa suku cadang mobil setelah pembayaran diverifikasi.",
                    location = "Gudang Pusat Geara",
                    isCompleted = false,
                    isCurrent = false
                ),
                TrackingStep(
                    title = "Pengiriman via $courierName",
                    time = "Estimasi setelah dikemas",
                    description = "Paket akan diantar oleh armada kurir $courierName.",
                    location = "Dalam Pengantaran",
                    isCompleted = false,
                    isCurrent = false
                )
            )

            else -> listOf(
                TrackingStep(
                    title = "Pesanan Dibuat & Pembayaran Terkonfirmasi",
                    time = currentOrder?.date ?: "20 Okt 2026",
                    description = "Pembayaran lunas dan diverifikasi.",
                    location = "Jakarta",
                    isCompleted = true,
                    isCurrent = false
                ),
                TrackingStep(
                    title = "Paket Telah Dikemas Penjual",
                    time = "20 Okt, 15:00 WIB",
                    description = "Pemeriksaan part dan packing telah selesai.",
                    location = "Gudang Pusat Geara",
                    isCompleted = true,
                    isCurrent = false
                ),
                TrackingStep(
                    title = "Paket Telah Dikirim via $courierName",
                    time = "21 Okt, 09:00 WIB",
                    description = "Paket diantar oleh armada kurir ke alamat pembeli.",
                    location = "Dalam Pengiriman",
                    isCompleted = true,
                    isCurrent = false
                ),
                TrackingStep(
                    title = "Pesanan Diterima Pembeli (Selesai)",
                    time = "22 Okt, 14:15 WIB",
                    description = "Paket telah diterima langsung oleh pembeli dalam kondisi baik dan tersegel.",
                    location = currentOrder?.deliveryAddress?.take(25) ?: "Alamat Pembeli",
                    isCompleted = true,
                    isCurrent = true
                )
            )
        }
    }

    Scaffold(
        bottomBar = { GearaBottomNavBar(navController, "orders") },
        containerColor = GearaYellow
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Bar Konsisten Berdasarkan Status Pesanan
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Kembali
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = GearaOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Judul Header Sesuai Status Pesanan
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = when {
                            isPacked -> "Detail Pengemasan"
                            isShipped -> "Lacak Pengiriman"
                            isPending -> "Status Pesanan"
                            else -> "Riwayat Pengiriman"
                        },
                        color = GearaDarkBrown,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when {
                            isPacked -> "Sedang Dikemas Penjual • #${currentOrder?.id ?: "ORD-004"}"
                            isShipped -> "Resi & Logistik Real-Time • #${currentOrder?.id ?: "ORD-002"}"
                            isPending -> "Menunggu Pembayaran • #${currentOrder?.id ?: "ORD-003"}"
                            else -> "Pesanan Telah Selesai • #${currentOrder?.id ?: "ORD-001"}"
                        },
                        color = GearaDarkBrown.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Tombol Bagikan Status Resi
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            clipboardManager.setText(
                                AnnotatedString("Lacak Pesanan Geara #${currentOrder?.id} - Ekspedisi: $courierName - Resi: $resiNumber")
                            )
                            Toast.makeText(context, "Tautan pelacakan pesanan disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Bagikan",
                        tint = GearaOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // White Body Container Sheet
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                color = Color(0xFFF9F9FB),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 32.dp)
                ) {
                    Spacer(modifier = Modifier.height(18.dp))

                    // 1. KARTU RESI & INFORMASI EKSPEDISI TUNGGAL (1 Order = 1 Ekspedisi Pilihan Customer)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header Resi: Icon Ekspedisi + Nama Kurir Pilihan Customer + Badge Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isPacked -> Color(0xFFE3F2FD)
                                                    isInternalCourier -> Color(0xFFFFF0E6)
                                                    isPending -> Color(0xFFFFF3E0)
                                                    else -> Color(0xFFE1F5FE)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                isPacked -> Icons.Default.Inventory2
                                                isInternalCourier -> Icons.Default.ElectricScooter
                                                isPending -> Icons.Default.Payment
                                                isCompleted -> Icons.Default.CheckCircle
                                                else -> Icons.Default.LocalShipping
                                            },
                                            contentDescription = null,
                                            tint = when {
                                                isPacked -> Color(0xFF1565C0)
                                                isInternalCourier -> GearaOrange
                                                isPending -> Color(0xFFE65100)
                                                isCompleted -> Color(0xFF2E7D32)
                                                else -> Color(0xFF0277BD)
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = courierName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = GearaDarkBrown,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = when {
                                                isPacked -> "$courierServiceType • Siap Dijemput Kurir"
                                                isShipped -> "$courierServiceType • Mitra Pengantar Resmi"
                                                isPending -> "$courierServiceType • Ekspedisi Pilihan"
                                                else -> "$courierServiceType • Pengiriman Sukses"
                                            },
                                            fontSize = 11.sp,
                                            color = when {
                                                isPacked -> Color(0xFF1565C0)
                                                isInternalCourier -> GearaOrange
                                                isPending -> Color(0xFFE65100)
                                                else -> Color(0xFF0277BD)
                                            },
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // Status Badge Dinamis Sesuai Tahapan Order
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when {
                                                isPacked -> Color(0xFFE3F2FD)
                                                isShipped -> Color(0xFFE8F5E9)
                                                isPending -> Color(0xFFFFF3E0)
                                                else -> Color(0xFFE8F5E9)
                                            }
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = when {
                                            isPacked -> "Dikemas Penjual"
                                            isShipped -> "Dalam Pengiriman"
                                            isPending -> "Belum Bayar"
                                            else -> "Pesanan Selesai"
                                        },
                                        color = when {
                                            isPacked -> Color(0xFF1565C0)
                                            isShipped -> Color(0xFF2E7D32)
                                            isPending -> Color(0xFFE65100)
                                            else -> Color(0xFF2E7D32)
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Nomor Resi & Tombol Salin
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF8F9FA))
                                    .border(1.dp, Color(0xFFEBEBEB), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isPacked) "NOMOR BOOKING RESI PENJUAL" else "NOMOR RESI PENGIRIMAN",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = resiNumber,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GearaDarkBrown,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                // Tombol Salin Resi
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(resiNumber))
                                        Toast.makeText(
                                            context,
                                            "Nomor resi $resiNumber berhasil disalin!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GearaOrange),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Salin",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Salin",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Estimasi Waktu & Keterangan Pengiriman
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isPacked) Icons.Default.HourglassTop else Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = if (isPacked) Color(0xFF1565C0) else Color(0xFF2E7D32),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = when {
                                            isPacked -> "Status: QC & Pengemasan"
                                            isShipped -> "Estimasi: $estimatedArrival"
                                            isPending -> "Batas Bayar: 24 Jam"
                                            else -> "Diterima: ${currentOrder?.date?.take(11) ?: "Selesai"}"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isPacked) Color(0xFF1565C0) else Color(0xFF2E7D32),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = when {
                                        isPacked -> "Siap Pick-up"
                                        isShipped -> "1.8 km lagi"
                                        isPending -> currentOrder?.formattedTotal ?: ""
                                        else -> "Lengkap"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GearaOrange,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. VISUALISASI STATUS: PETA GUDANG PENJUAL (JIKA DIKEMAS) VS PETA GOOGLE MAPS LIVE TRAFFIC (JIKA DIKIRIM)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(340.dp)
                                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                    .background(Color(0xFFE5EBEF))
                            ) {
                                // Background Map Gambar Asli
                                Image(
                                    painter = painterResource(id = R.drawable.img_map_tracking),
                                    contentDescription = "Peta Google Maps Pengiriman",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Custom Canvas Sesuai Status Pesanan (Gudang Penjual vs Kurir Bergerak)
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height

                                    val originWarehouse = Offset(w * 0.35f, h * 0.32f) // Gudang Pusat Geara (Cakung DC)
                                    val transitHub = Offset(w * 0.50f, h * 0.45f) // Hub Transit
                                    val courierPos = Offset(w * 0.62f, h * 0.52f) // Kurir Posisi Saat Ini
                                    val destination = Offset(w * 0.82f, h * 0.78f) // Rumah Pembeli

                                    if (isPacked) {
                                        // --- TAMPILAN MAP KHUSUS STATUS DIKEMAS PENJUAL ---
                                        // Garis putus-putus pendek dari gudang ke drop point kurir
                                        val pickupPath = Path().apply {
                                            moveTo(originWarehouse.x, originWarehouse.y)
                                            cubicTo(w * 0.40f, h * 0.32f, w * 0.45f, h * 0.38f, transitHub.x, transitHub.y)
                                        }
                                        drawPath(
                                            path = pickupPath,
                                            color = Color(0xFF1976D2),
                                            style = Stroke(
                                                width = 6f,
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
                                            )
                                        )

                                        // Animated Radar Pulse di Gudang Pusat Geara (Menandakan Staf Gudang Sedang Memproses Pesanan)
                                        drawCircle(
                                            color = Color(0xFF1976D2).copy(alpha = radarPulseAlpha),
                                            radius = radarPulseRadius * 2.2f,
                                            center = originWarehouse
                                        )
                                        drawCircle(
                                            color = Color.White,
                                            radius = 20f,
                                            center = originWarehouse
                                        )
                                        drawCircle(
                                            color = Color(0xFF1565C0),
                                            radius = 14f,
                                            center = originWarehouse
                                        )

                                        // Pin Drop Point Kurir Penjemputan
                                        drawCircle(color = Color.White, radius = 10f, center = transitHub)
                                        drawCircle(color = GearaOrange, radius = 7f, center = transitHub)

                                        // Pin Alamat Tujuan Pembeli
                                        drawCircle(color = Color.White, radius = 10f, center = destination)
                                        drawCircle(color = Color(0xFF757575), radius = 6f, center = destination)

                                    } else {
                                        // --- TAMPILAN MAP STATUS SEDANG DIKIRIM (SHIPPED) ---
                                        val routePathCompleted = Path().apply {
                                            moveTo(originWarehouse.x, originWarehouse.y)
                                            cubicTo(w * 0.40f, h * 0.32f, w * 0.45f, h * 0.40f, transitHub.x, transitHub.y)
                                            cubicTo(w * 0.52f, h * 0.46f, w * 0.57f, h * 0.49f, courierPos.x, courierPos.y)
                                        }
                                        val routePathUpcoming = Path().apply {
                                            moveTo(courierPos.x, courierPos.y)
                                            cubicTo(w * 0.70f, h * 0.58f, w * 0.75f, h * 0.68f, destination.x, destination.y)
                                        }

                                        // Outer Shadow & Solid Blue Line
                                        drawPath(path = routePathCompleted, color = Color(0x331A73E8), style = Stroke(width = 14f))
                                        drawPath(path = routePathCompleted, color = Color(0xFF1A73E8), style = Stroke(width = 8f))

                                        // Dashed Line ke alamat tujuan
                                        drawPath(
                                            path = routePathUpcoming,
                                            color = Color(0xFF1A73E8),
                                            style = Stroke(width = 6f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f))
                                        )

                                        // Waypoint pins
                                        drawCircle(color = Color.White, radius = 10f, center = originWarehouse)
                                        drawCircle(color = Color(0xFF1565C0), radius = 7f, center = originWarehouse)

                                        drawCircle(color = Color.White, radius = 9f, center = transitHub)
                                        drawCircle(color = Color(0xFF0288D1), radius = 6f, center = transitHub)

                                        drawCircle(color = Color.White, radius = 12f, center = destination)
                                        drawCircle(color = Color(0xFFD32F2F), radius = 8f, center = destination)

                                        // Animated Radar Kurir Sedang Bergerak
                                        drawCircle(
                                            color = GearaOrange.copy(alpha = radarPulseAlpha),
                                            radius = radarPulseRadius * 2.2f,
                                            center = courierPos
                                        )
                                        drawCircle(color = Color.White, radius = 18f, center = courierPos)
                                        drawCircle(color = GearaOrange, radius = 14f, center = courierPos)
                                    }
                                }

                                // Top Status Watermark Chip
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(14.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White.copy(alpha = 0.95f))
                                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isPacked) Color(0xFF1976D2) else Color(0xFF2E7D32))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isPacked) "Gudang Pusat Geara (Cakung DC)" else "Google Maps™ Live Traffic",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GearaDarkBrown
                                    )
                                }

                                // Top Right Control Button (Recenter)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(14.dp)
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .clickable {
                                            if (isPacked) {
                                                Toast.makeText(context, "Posisi Gudang Pusat Geara Parts (Cakung DC) terpusat", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Posisi kurir $driverName terpusat di peta", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "Pusatkan",
                                        tint = GearaOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Overlay Floating Actions di Bagian Bawah Map (Konsisten Antara Pengemasan vs Pengiriman)
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isPacked) {
                                        // --- AKSI KETIKA PESANAN MASIH DIKEMAS (KONSISTEN: HUBUNGI PENJUAL / GUDANG) ---
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(22.dp))
                                                .background(Color(0xFF1565C0))
                                                .clickable { navController.navigate("support") }
                                                .padding(start = 18.dp, end = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Storefront,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Hubungi Penjual (Geara Official Store)",
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                                    contentDescription = null,
                                                    tint = Color(0xFF1565C0),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        // Status Banner Pengemasan Aman
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color.White.copy(alpha = 0.94f))
                                                .border(1.dp, Color(0xFFBBDEFB), RoundedCornerShape(14.dp))
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.VerifiedUser,
                                                    contentDescription = null,
                                                    tint = Color(0xFF1565C0),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Standar QC: Pengecekan Nomor Seri OEM & Bubble Wrap Tebal",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF0D47A1),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }

                                    } else {
                                        // --- AKSI KETIKA PESANAN SUDAH DIKIRIM (TELEPON KURIR & KIRIM PESAN) ---
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(22.dp))
                                                .background(GearaOrange)
                                                .clickable {
                                                    try {
                                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                                            data = Uri.parse("tel:${driverPhone.replace("-", "")}")
                                                        }
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "Menghubungi $driverName: $driverPhone", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                .padding(start = 20.dp, end = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Telepon kurir ($driverName)",
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Phone,
                                                    contentDescription = null,
                                                    tint = GearaOrange,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(22.dp))
                                                .background(GearaOrange)
                                                .clickable { navController.navigate("support") }
                                                .padding(start = 20.dp, end = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Kirim pesan ke kurir",
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                                    contentDescription = null,
                                                    tint = GearaOrange,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Info Pelaksana (Staf Logistik Gudang vs Kurir Pengantar)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(if (isPacked) Color(0xFFE3F2FD) else Color(0xFFFFF0E6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPacked) Icons.Default.Storefront else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isPacked) Color(0xFF1565C0) else GearaOrange,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isPacked) "Tim Logistik Geara Parts Center" else driverName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = GearaDarkBrown
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified",
                                            tint = Color(0xFF1976D2),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isPacked) "Pusat Distribusi Suku Cadang • Cakung DC" else driverVehicle,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFFF8E1))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFA000),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = if (isPacked) "5.0★ Toko Resmi" else "4.9★",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFA000)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3. GRADIENT PROGRESS BAR SESUAI POSISI STATUS SEBENARNYA
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Gradient Bar Red -> Yellow -> Green
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFFFF2A1A),
                                                    Color(0xFFFFB800),
                                                    Color(0xFF3CD124)
                                                )
                                            )
                                        )
                                )

                                // Posisi ikon progress indikator: Di awal jika Dikemas, di tengah jika Dikirim, di ujung jika Selesai
                                val indicatorFraction = when {
                                    isPending -> 0.05f
                                    isPacked -> 0.28f
                                    isShipped -> 0.65f
                                    else -> 0.95f
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(indicatorFraction)
                                        .wrapContentWidth(Alignment.End)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .border(
                                                2.dp,
                                                if (isPacked) Color(0xFF1565C0) else GearaOrange,
                                                CircleShape
                                            )
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                isPacked -> Icons.Default.Inventory2
                                                isShipped -> Icons.Default.TwoWheeler
                                                isCompleted -> Icons.Default.Check
                                                else -> Icons.Default.Payment
                                            },
                                            contentDescription = null,
                                            tint = if (isPacked) Color(0xFF1565C0) else GearaOrange,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Status dan Tanggal Tiba yang Konsisten
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = when {
                                            isPacked -> "Paket sedang dikemas\noleh Penjual"
                                            isShipped -> "Kurir menuju lokasi\nAnda"
                                            isPending -> "Menunggu\nPembayaran"
                                            else -> "Pesanan Telah\nDikirim"
                                        },
                                        color = if (isPacked) Color(0xFF1565C0) else GearaOrange,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentOrder?.date?.take(11) ?: "Hari ini",
                                        color = GearaDarkBrown,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isPacked) "11:30 WIB" else "15:10 WIB",
                                        color = Color(0xFFA8A8A8),
                                        fontSize = 11.sp
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = when {
                                            isPacked -> "Estimasi penjemputan\noleh $courierName"
                                            isShipped -> "Estimasi terima\npesanan Anda"
                                            isPending -> "Batas Waktu\nPembayaran"
                                            else -> "Pesanan Diterima\nSelesai"
                                        },
                                        color = Color(0xFF2E7D32),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isPacked) "Hari ini" else (currentOrder?.date?.take(11) ?: "25 Okt 2026"),
                                        color = GearaDarkBrown,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isPacked) "17:00 WIB" else estimatedArrival,
                                        color = Color(0xFFA8A8A8),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 4. MILESTONE LOGISTIK / PERGERAKAN FISIK PAKET (STEPPER)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timeline,
                                    contentDescription = null,
                                    tint = if (isPacked) Color(0xFF1565C0) else GearaOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Riwayat Pelacakan Fisik Paket",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = GearaDarkBrown
                                    )
                                    Text(
                                        text = "Terintegrasi sistem logistik $courierName",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Vertical Stepper
                            dynamicTrackingSteps.forEachIndexed { index, step ->
                                val isLast = index == dynamicTrackingSteps.lastIndex

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    // Kolom Kiri: Waktu
                                    Column(
                                        modifier = Modifier.width(80.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        val timeParts = step.time.split(",")
                                        Text(
                                            text = timeParts.firstOrNull()?.trim() ?: "",
                                            fontSize = 11.sp,
                                            fontWeight = if (step.isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            color = if (step.isCurrent) {
                                                if (isPacked) Color(0xFF1565C0) else GearaOrange
                                            } else GearaDarkBrown,
                                            textAlign = TextAlign.End
                                        )
                                        if (timeParts.size > 1) {
                                            Text(
                                                text = timeParts.getOrNull(1)?.trim() ?: "",
                                                fontSize = 10.sp,
                                                color = Color.Gray,
                                                textAlign = TextAlign.End
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // Kolom Tengah: Node & Connecting Line
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(24.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        step.isCurrent -> if (isPacked) Color(0xFF1565C0) else GearaOrange
                                                        step.isCompleted -> Color(0xFF2E7D32)
                                                        else -> Color(0xFFE0E0E0)
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (step.isCurrent) {
                                                Icon(
                                                    imageVector = if (isPacked) Icons.Default.Inventory2 else Icons.Default.DirectionsBike,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                            } else if (step.isCompleted) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                            }
                                        }

                                        if (!isLast) {
                                            Box(
                                                modifier = Modifier
                                                    .width(2.dp)
                                                    .height(48.dp)
                                                    .background(
                                                        if (step.isCompleted) Color(0xFF81C784) else Color(0xFFE0E0E0)
                                                    )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // Kolom Kanan: Judul, Deskripsi & Lokasi
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(bottom = if (!isLast) 18.dp else 0.dp)
                                    ) {
                                        Text(
                                            text = step.title,
                                            fontSize = 13.sp,
                                            fontWeight = if (step.isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (step.isCurrent) {
                                                if (isPacked) Color(0xFF1565C0) else GearaOrange
                                            } else GearaDarkBrown
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = step.description,
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            lineHeight = 15.sp
                                        )
                                        if (step.location.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Place,
                                                    contentDescription = null,
                                                    tint = if (step.isCurrent) (if (isPacked) Color(0xFF1565C0) else GearaOrange) else Color.Gray,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = step.location,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (step.isCurrent) (if (isPacked) Color(0xFF1565C0) else GearaOrange) else Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 5. RINCIAN SUKU CADANG DALAM PAKET
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Isi Paket Suku Cadang",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = GearaDarkBrown
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val firstProduct = currentOrder?.items?.firstOrNull()?.product
                            val qty = currentOrder?.items?.firstOrNull()?.quantity ?: 1

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GearaImage(
                                    imageUrl = firstProduct?.imageUrl ?: "",
                                    fallbackRes = firstProduct?.imageRes ?: R.drawable.ic_geara_logo,
                                    contentDescription = firstProduct?.name ?: "Suku Cadang",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFE5EBEF)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = firstProduct?.name ?: "Suku Cadang Mobil Geara",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = GearaDarkBrown,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Part Number: ${firstProduct?.partNumber ?: "GEARA-OEM-PARTS"}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "$qty item • ${firstProduct?.formattedPrice ?: "Rp 135.000"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GearaOrange
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFF2F2F2))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Alamat Pengiriman
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = GearaOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Alamat Pengiriman:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GearaDarkBrown
                                    )
                                    Text(
                                        text = currentOrder?.deliveryAddress ?: "Jl. Sudirman No. 123, Jakarta Selatan",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
