package id.ac.stmi.aplikasiutsgibran.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaDarkBrown
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaYellow
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController: NavController) {
    var pushNotificationEnabled by remember { mutableStateOf(true) }
    var promoNotificationEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }

    var showClearCacheDialog by remember { mutableStateOf(false) }
    var cacheSize by remember { mutableStateOf("24.8 MB") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = GearaYellow
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
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
                    text = "Pengaturan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // White Container
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                color = Color(0xFFF9F9F9),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 22.dp)
                ) {
                    // SECTION 1: Notifikasi & Suara
                    Text(
                        text = "Pengaturan Notifikasi",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaOrange
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsToggleCard(
                        icon = Icons.Default.Notifications,
                        title = "Status Pesanan & Pengiriman",
                        subtitle = "Pemberitahuan kurir dan perubahan status paket",
                        checked = pushNotificationEnabled,
                        onCheckedChange = { pushNotificationEnabled = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsToggleCard(
                        icon = Icons.Default.LocalOffer,
                        title = "Promo & Rekomendasi Suku Cadang",
                        subtitle = "Pemberitahuan diskon untuk suku cadang favorit",
                        checked = promoNotificationEnabled,
                        onCheckedChange = { promoNotificationEnabled = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsToggleCard(
                        icon = Icons.Default.VolumeUp,
                        title = "Suara & Getar Notifikasi",
                        subtitle = "Bunyikan notifikasi saat ada pembaruan pesanan",
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // SECTION 2: Penyimpanan Data
                    Text(
                        text = "Penyimpanan & Cache",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaOrange
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsActionCard(
                        icon = Icons.Default.DeleteSweep,
                        title = "Hapus Cache Aplikasi",
                        subtitle = "Ukuran cache saat ini: $cacheSize",
                        buttonText = "Hapus",
                        onAction = { showClearCacheDialog = true }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // SECTION 3: Informasi Aplikasi
                    Text(
                        text = "Tentang Aplikasi",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaOrange
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsNavigationCard(
                        icon = Icons.Default.Info,
                        title = "Versi Aplikasi",
                        subtitle = "Geara v2.4.1 (Build 2026.10)",
                        onClick = {}
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsNavigationCard(
                        icon = Icons.Default.Policy,
                        title = "Kebijakan Privasi & Ketentuan Layanan",
                        subtitle = "Standar perlindungan data pelanggan Geara",
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Kebijakan privasi: data Anda dienkripsi dan tidak dibagikan ke pihak ketiga.")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = {
                Text(text = "Hapus Cache?", fontWeight = FontWeight.Bold, color = GearaDarkBrown)
            },
            text = {
                Text(
                    text = "Cache sementara sebesar $cacheSize akan dihapus. Akun dan riwayat pesanan Anda tetap tersimpan.",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        cacheSize = "0.0 KB"
                        showClearCacheDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Cache aplikasi berhasil dibersihkan.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GearaOrange)
                ) {
                    Text("Hapus Cache", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun SettingsToggleCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF7ED)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = GearaOrange, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GearaDarkBrown
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 15.sp
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = GearaOrange,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFCCCCCC)
                )
            )
        }
    }
}

@Composable
fun SettingsNavigationCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF7ED)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = GearaOrange, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GearaDarkBrown
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun SettingsActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    buttonText: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF7ED)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = GearaOrange, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GearaDarkBrown
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5E7EB)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(buttonText, fontSize = 12.sp, color = GearaDarkBrown, fontWeight = FontWeight.Bold)
            }
        }
    }
}

