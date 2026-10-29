package id.ac.stmi.aplikasiutsgibran.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.ac.stmi.aplikasiutsgibran.R
import id.ac.stmi.aplikasiutsgibran.data.GearaViewModel
import id.ac.stmi.aplikasiutsgibran.data.Order
import id.ac.stmi.aplikasiutsgibran.data.OrderStatus
import id.ac.stmi.aplikasiutsgibran.ui.components.GearaBottomNavBar
import id.ac.stmi.aplikasiutsgibran.ui.components.GearaImage
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaDarkBrown
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaYellow

enum class UserRole {
    PEMBELI,
    PENJUAL
}

@Composable
fun OrdersScreen(
    navController: NavController,
    viewModel: GearaViewModel
) {
    val context = LocalContext.current
    var currentRole by remember { mutableStateOf(UserRole.PEMBELI) }
    var selectedStage by remember { mutableStateOf("Semua") }

    val allOrders by viewModel.orders.collectAsState()

    // 4 Tahapan Berkala Status Pesanan sesuai permintaan: Belum Bayar, Dikemas, Dikirim, Selesai (+ Semua)
    val stages = listOf("Semua", "Belum Bayar", "Dikemas", "Dikirim", "Selesai")

    // Filter order berdasarkan tahapan berkala
    val filteredOrders = remember(allOrders, selectedStage) {
        when (selectedStage) {
            "Belum Bayar" -> allOrders.filter { it.status == OrderStatus.PENDING }
            "Dikemas" -> allOrders.filter { it.status == OrderStatus.PACKED }
            "Dikirim" -> allOrders.filter { it.status == OrderStatus.SHIPPED || it.status == OrderStatus.ACTIVE }
            "Selesai" -> allOrders.filter { it.status == OrderStatus.COMPLETED }
            else -> allOrders.filter { it.status != OrderStatus.CANCELED }
        }
    }

    // Hitung counter untuk tiap tahapan
    val countUnpaid = allOrders.count { it.status == OrderStatus.PENDING }
    val countPacked = allOrders.count { it.status == OrderStatus.PACKED }
    val countShipped = allOrders.count { it.status == OrderStatus.SHIPPED || it.status == OrderStatus.ACTIVE }
    val countCompleted = allOrders.count { it.status == OrderStatus.COMPLETED }

    Scaffold(
        bottomBar = { GearaBottomNavBar(navController, "orders") },
        containerColor = GearaYellow
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Bar dengan Kontras Tinggi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button in white circle
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = if (currentRole == UserRole.PEMBELI) "Pesanan Saya" else "Seller Center Geara",
                        color = GearaDarkBrown,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (currentRole == UserRole.PEMBELI) "Pantau status pesanan belanjaan Anda" else "Kelola & proses pesanan toko resmi",
                        color = GearaDarkBrown.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Role Switcher Pill: Pembeli vs Penjual (Seller Center)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, GearaOrange.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .clickable {
                            currentRole = if (currentRole == UserRole.PEMBELI) UserRole.PENJUAL else UserRole.PEMBELI
                            Toast.makeText(
                                context,
                                if (currentRole == UserRole.PENJUAL) "Beralih ke Mode Seller Center (Penjual)" else "Beralih ke Mode Pembeli",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (currentRole == UserRole.PEMBELI) Icons.Default.ShoppingBag else Icons.Default.Storefront,
                            contentDescription = null,
                            tint = GearaOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentRole == UserRole.PEMBELI) "Pembeli" else "Penjual",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GearaOrange
                        )
                    }
                }
            }

            // White Container Sheet
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
                        .padding(top = 16.dp)
                ) {
                    // Seller Center Metrics Card (Khusus Mode Penjual)
                    if (currentRole == UserRole.PENJUAL) {
                        SellerCenterDashboardCard(
                            totalOrders = allOrders.size,
                            needPacked = countPacked,
                            needShipped = countShipped,
                            totalRevenue = allOrders.filter { it.status == OrderStatus.COMPLETED }.sumOf { it.totalAmount }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // 4 Tahapan Berkala Horizontal Scrollable Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        stages.forEach { stage ->
                            val isSelected = selectedStage == stage
                            val count = when (stage) {
                                "Belum Bayar" -> countUnpaid
                                "Dikemas" -> countPacked
                                "Dikirim" -> countShipped
                                "Selesai" -> countCompleted
                                else -> allOrders.size
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) GearaOrange else Color.White)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) GearaOrange else Color(0xFFFFD5C0),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { selectedStage = stage }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stage,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else GearaDarkBrown
                                    )
                                    if (count > 0 && stage != "Semua") {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.White else GearaOrange)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "$count",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) GearaOrange else Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Daftar Pesanan
                    if (filteredOrders.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Inbox,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Tidak ada pesanan di tahap \"$selectedStage\"",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(filteredOrders, key = { it.id }) { order ->
                                OrderCardItem(
                                    order = order,
                                    role = currentRole,
                                    onPayNow = {
                                        navController.navigate("payment_gateway/${order.id}")
                                    },
                                    onTrackShipment = {
                                        navController.navigate("live_tracking/${order.id}")
                                    },
                                    onProcessShipment = { courierName, resi ->
                                        viewModel.processShipment(order.id, courierName, resi)
                                        Toast.makeText(
                                            context,
                                            "Pesanan #${order.id} berhasil dikirim via $courierName! Resi: $resi",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    },
                                    onCompleteOrder = {
                                        viewModel.updateOrderStatus(order.id, OrderStatus.COMPLETED)
                                        Toast.makeText(context, "Pesanan #${order.id} selesai! Terima kasih.", Toast.LENGTH_SHORT).show()
                                    },
                                    onBuyAgain = {
                                        val first = order.items.firstOrNull()?.product
                                        if (first != null) {
                                            viewModel.addToCart(first, 1)
                                            navController.navigate("cart")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SellerCenterDashboardCard(
    totalOrders: Int,
    needPacked: Int,
    needShipped: Int,
    totalRevenue: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF0E6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = GearaOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Geara Official Store",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GearaDarkBrown
                        )
                        Text(
                            text = "Verified OEM Partner • Toko Aktif",
                            fontSize = 11.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Performa 4.9★",
                        color = Color(0xFF2E7D32),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(12.dp))

            // 3 Performance Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn(title = "Perlu Dikemas", value = "$needPacked", highlight = needPacked > 0)
                MetricColumn(title = "Perlu Dikirim", value = "$needShipped", highlight = needShipped > 0)
                MetricColumn(title = "Total Pesanan", value = "$totalOrders", highlight = false)
                MetricColumn(title = "Pendapatan", value = "Rp " + "%,d".format(totalRevenue / 1000).replace(',', '.') + " rb", highlight = true)
            }
        }
    }
}

@Composable
fun MetricColumn(title: String, value: String, highlight: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (highlight) GearaOrange else GearaDarkBrown
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun OrderCardItem(
    order: Order,
    role: UserRole,
    onPayNow: () -> Unit,
    onTrackShipment: () -> Unit,
    onProcessShipment: (courier: String, resi: String) -> Unit,
    onCompleteOrder: () -> Unit,
    onBuyAgain: () -> Unit
) {
    val firstItem = order.items.firstOrNull()?.product
    val totalQty = order.items.sumOf { it.quantity }

    var showShippingDialog by remember { mutableStateOf(false) }

    // Dialog Input Resi & Kurir untuk Seller Center
    if (showShippingDialog) {
        var selectedCourier by remember { mutableStateOf("SPX Express (Kurir Internal)") }
        var inputResi by remember { mutableStateOf("SPXID" + System.currentTimeMillis().toString().takeLast(9)) }
        val courierOptions = listOf(
            "SPX Express (Kurir Internal)",
            "JNE Express (Reguler/YES)",
            "J&T Express (EZ Pick-Up)",
            "SiCepat Express"
        )

        AlertDialog(
            onDismissRequest = { showShippingDialog = false },
            title = {
                Text(
                    text = "Atur Pengiriman Pesanan #${order.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GearaDarkBrown
                )
            },
            text = {
                Column {
                    Text(
                        text = "Pilih mitra kurir logistik dan konfirmasi nomor resi pengiriman suku cadang:",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Pilihan Kurir:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GearaDarkBrown)
                    Spacer(modifier = Modifier.height(6.dp))

                    courierOptions.forEach { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedCourier = opt
                                    val prefix = when {
                                        opt.contains("SPX") -> "SPXID"
                                        opt.contains("JNE") -> "JNE"
                                        opt.contains("J&T") -> "JT"
                                        else -> "SCP"
                                    }
                                    inputResi = prefix + System.currentTimeMillis().toString().takeLast(9)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCourier == opt,
                                onClick = {
                                    selectedCourier = opt
                                    val prefix = when {
                                        opt.contains("SPX") -> "SPXID"
                                        opt.contains("JNE") -> "JNE"
                                        opt.contains("J&T") -> "JT"
                                        else -> "SCP"
                                    }
                                    inputResi = prefix + System.currentTimeMillis().toString().takeLast(9)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = GearaOrange)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = opt, fontSize = 12.sp, color = GearaDarkBrown)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = inputResi,
                        onValueChange = { inputResi = it },
                        label = { Text("Nomor Resi Pengiriman") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showShippingDialog = false
                        onProcessShipment(selectedCourier, inputResi)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GearaOrange)
                ) {
                    Text("Konfirmasi & Cetak Resi", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showShippingDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header card: Order ID, Date & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = GearaOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "#${order.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = GearaDarkBrown
                    )
                    Text(
                        text = " • ${order.date.take(11)}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                // Badge Status Berdasarkan 4 Tahapan
                val (badgeBg, badgeText, badgeColor) = when (order.status) {
                    OrderStatus.PENDING -> Triple(Color(0xFFFFF3E0), "Belum Bayar", Color(0xFFE65100))
                    OrderStatus.PACKED -> Triple(Color(0xFFE3F2FD), "Dikemas Penjual", Color(0xFF1565C0))
                    OrderStatus.SHIPPED, OrderStatus.ACTIVE -> Triple(Color(0xFFE8F5E9), "Sedang Dikirim", Color(0xFF2E7D32))
                    OrderStatus.COMPLETED -> Triple(Color(0xFFF5F5F5), "Selesai", Color(0xFF616161))
                    OrderStatus.CANCELED -> Triple(Color(0xFFFFEBEE), "Dibatalkan", Color(0xFFC62828))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF2F2F2))
            Spacer(modifier = Modifier.height(12.dp))

            // Body: Product Image & Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GearaImage(
                    imageUrl = firstItem?.imageUrl ?: "",
                    fallbackRes = firstItem?.imageRes ?: R.drawable.ic_geara_logo,
                    contentDescription = firstItem?.name ?: "Suku Cadang",
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE5EBEF)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = firstItem?.name ?: "Suku Cadang Mobil Geara",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = GearaDarkBrown,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$totalQty item suku cadang • ${order.paymentMethod}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Info Logistik Ringkas
                    if (order.status == OrderStatus.SHIPPED || order.status == OrderStatus.ACTIVE) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = GearaOrange,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${order.courierName} (${order.resiNumber})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GearaOrange,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else if (order.status == OrderStatus.PACKED) {
                        Text(
                            text = "Gudang sedang memeriksa spesifikasi part",
                            fontSize = 11.sp,
                            color = Color(0xFF1565C0),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Price total
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Belanja",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = order.formattedTotal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = GearaDarkBrown
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF2F2F2))
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Sesuai Role & Tahapan Berkala
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (role == UserRole.PEMBELI) {
                    // --- AKSI MODE PEMBELI ---
                    when (order.status) {
                        OrderStatus.PENDING -> {
                            Button(
                                onClick = onPayNow,
                                colors = ButtonDefaults.buttonColors(containerColor = GearaOrange),
                                shape = RoundedCornerShape(18.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Bayar Sekarang", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        OrderStatus.PACKED -> {
                            OutlinedButton(
                                onClick = onTrackShipment,
                                shape = RoundedCornerShape(18.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GearaOrange),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = GearaOrange, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Detail Pengemasan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GearaOrange)
                            }
                        }
                        OrderStatus.SHIPPED, OrderStatus.ACTIVE -> {
                            Button(
                                onClick = onTrackShipment,
                                colors = ButtonDefaults.buttonColors(containerColor = GearaOrange),
                                shape = RoundedCornerShape(18.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Lacak Pengiriman", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = onCompleteOrder,
                                shape = RoundedCornerShape(18.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Pesanan Tiba", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                        OrderStatus.COMPLETED -> {
                            Button(
                                onClick = onBuyAgain,
                                colors = ButtonDefaults.buttonColors(containerColor = GearaOrange),
                                shape = RoundedCornerShape(18.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Beli Lagi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        OrderStatus.CANCELED -> {
                            Text("Pesanan telah dibatalkan", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                } else {
                    // --- AKSI MODE SELLER CENTER (PENJUAL) ---
                    when (order.status) {
                        OrderStatus.PENDING -> {
                            OutlinedButton(
                                onClick = { /* ingatkan pembeli */ },
                                shape = RoundedCornerShape(18.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE65100)),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Ingatkan Pembeli", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            }
                        }
                        OrderStatus.PACKED -> {
                            Button(
                                onClick = { showShippingDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GearaOrange),
                                shape = RoundedCornerShape(18.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Atur Pengiriman & Cetak Resi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        OrderStatus.SHIPPED, OrderStatus.ACTIVE -> {
                            Button(
                                onClick = onTrackShipment,
                                colors = ButtonDefaults.buttonColors(containerColor = GearaOrange),
                                shape = RoundedCornerShape(18.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PinDrop, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pantau Posisi Kurir", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        OrderStatus.COMPLETED -> {
                            Text(
                                text = "Dana berhasil dicairkan ke Rekening Toko",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                        OrderStatus.CANCELED -> {
                            Text("Stok suku cadang telah dikembalikan", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
