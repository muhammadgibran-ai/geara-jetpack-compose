package id.ac.stmi.aplikasiutsgibran.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.ac.stmi.aplikasiutsgibran.data.GearaViewModel
import id.ac.stmi.aplikasiutsgibran.ui.components.GearaBottomNavBar
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaDarkBrown
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaYellow

data class NotificationItemData(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val tag: String,
    val subtitle: String,
    val description: String,
    val actionText: String,
    val destinationRoute: String
)

@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: GearaViewModel? = null
) {
    // ID notifikasi yang sedang dibuka dalam mode dropdown
    var expandedNotifId by remember { mutableStateOf<String?>(null) }

    val notifications = remember {
        listOf(
            NotificationItemData(
                id = "catalog",
                icon = Icons.Default.GridView,
                title = "Suku cadang baru untuk kendaraan Anda.",
                tag = "Produk Baru • 10 mnt lalu",
                subtitle = "Katalog Geara Original 2026",
                description = "Suku cadang resmi untuk tipe mobil Geara Anda (Terra SUV, Prime EV, City, Apex) kini tersedia lengkap dengan jaminan kualitas pabrikan dan garansi 12 bulan.",
                actionText = "Lihat Katalog Suku Cadang",
                destinationRoute = "catalog"
            ),
            NotificationItemData(
                id = "promo",
                icon = Icons.Default.FavoriteBorder,
                title = "Suku cadang favorit Anda sedang promo.",
                tag = "Promo Kilat • 1 jam lalu",
                subtitle = "Diskon Spesial Hingga 30%",
                description = "Busi Iridium, Filter Udara High-Flow, dan Oli Mesin di daftar favorit Anda sedang diskon terbatas. Klaim promonya sebelum masa berlaku habis!",
                actionText = "Buka Menu Favorit",
                destinationRoute = "favorites"
            ),
            NotificationItemData(
                id = "order",
                icon = Icons.Default.ShoppingBag,
                title = "Pesanan Anda telah sampai",
                tag = "Pengiriman Selesai • 3 jam lalu",
                subtitle = "Pesanan #ORD-20261129-01 Selesai",
                description = "Paket suku cadang Geara Anda telah sampai di alamat tujuan dan diterima dengan aman. Silakan konfirmasi penerimaan barang dan berikan ulasan Anda.",
                actionText = "Lihat Rincian Pesanan",
                destinationRoute = "orders"
            ),
            NotificationItemData(
                id = "tracking",
                icon = Icons.Default.TwoWheeler,
                title = "Kurir sedang dalam perjalanan",
                tag = "Live Tracking • Baru saja",
                subtitle = "Driver: Budi Santoso (Honda Vario)",
                description = "Kurir Geara Express sedang menuju ke lokasi pengiriman Anda dengan nomor resi GR-8842109. Estimasi waktu kedatangan kurir sekitar 12 menit.",
                actionText = "Lacak Posisi Kurir Sekarang",
                destinationRoute = "live_tracking/ORD-20261129-01"
            )
        )
    }

    Scaffold(
        bottomBar = {
            GearaBottomNavBar(navController = navController, currentRoute = "home")
        },
        containerColor = GearaYellow
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = GearaOrange,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Text(
                    text = "Notifikasi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Orange Container
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                color = GearaOrange,
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 22.dp)
                ) {
                    // Header inside orange panel
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifikasi",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Notifikasi",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Ketuk untuk melihat detail informasi",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.4f), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // List of Notification with Dropdown Expand
                    notifications.forEachIndexed { index, notif ->
                        val isExpanded = expandedNotifId == notif.id

                        NotificationDropdownItem(
                            item = notif,
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                expandedNotifId = if (isExpanded) null else notif.id
                            },
                            onActionClick = { route ->
                                navController.navigate(route)
                            }
                        )

                        if (index < notifications.size - 1) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.3f), thickness = 0.8.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun NotificationDropdownItem(
    item: NotificationItemData,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onActionClick: (String) -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "dropdown_rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isExpanded) Color.White.copy(alpha = 0.12f) else Color.Transparent)
            .padding(6.dp)
    ) {
        // Baris Notifikasi Utama (Klik untuk membuka/menutup Dropdown)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = GearaOrange,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                lineHeight = 19.sp,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Icon Dropdown Arrow (Animasi Berputar)
            IconButton(
                onClick = { onToggleExpand() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Tutup" else "Buka Detail",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationAngle)
                )
            }
        }

        // Dropdown Informasi Lengkap
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Tag / Waktu Notifikasi
                    Text(
                        text = item.tag,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaOrange
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Judul Sub
                    Text(
                        text = item.subtitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaDarkBrown
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Deskripsi Detail
                    Text(
                        text = item.description,
                        fontSize = 12.sp,
                        color = Color(0xFF4A4A4A),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tombol Aksi Menuju Halaman
                    Button(
                        onClick = { onActionClick(item.destinationRoute) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GearaOrange
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = item.actionText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
