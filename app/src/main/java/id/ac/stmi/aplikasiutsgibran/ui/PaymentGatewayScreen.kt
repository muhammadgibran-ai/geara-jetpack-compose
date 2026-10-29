package id.ac.stmi.aplikasiutsgibran.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.ac.stmi.aplikasiutsgibran.data.GearaViewModel
import id.ac.stmi.aplikasiutsgibran.data.OrderStatus
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaDarkBrown
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PaymentGatewayScreen(
    orderId: String,
    navController: NavController,
    viewModel: GearaViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val order = viewModel.getOrderById(orderId)

    // State metode pembayaran aktif: "QRIS", "VA", "EWALLET", "CC"
    var selectedChannel by remember { mutableStateOf("QRIS") }
    var selectedBank by remember { mutableStateOf("BCA") }
    var isSimulating by remember { mutableStateOf(false) }
    var isCheckingStatus by remember { mutableStateOf(false) }

    // Hitung mundur waktu pembayaran (24 Jam)
    var remainingSeconds by remember { mutableLongStateOf(86395L) }
    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
    }

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60
    val timerString = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    // Nomor Virtual Account sesuai bank yang dipilih
    val vaNumber = when (selectedBank) {
        "BCA" -> order?.vaNumber ?: "1280808123456789"
        "Mandiri" -> "8890" + (order?.vaNumber?.takeLast(12) ?: "808123456789")
        "BRI" -> "1020" + (order?.vaNumber?.takeLast(12) ?: "808123456789")
        else -> "8808" + (order?.vaNumber?.takeLast(12) ?: "808123456789")
    }

    val totalAmountFormatted = order?.formattedTotal ?: "Rp 205.000"

    Scaffold(
        containerColor = GearaYellow
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "<",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = GearaOrange,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable { navController.popBackStack() }
                        .padding(8.dp)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Pembayaran Geara Pay",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Midtrans & Xendit Gateway Sandbox",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // White Content Sheet
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                color = Color(0xFFF9F9F9),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    // Tagihan & Countdown Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Total Tagihan Pembayaran",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = totalAmountFormatted,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = GearaOrange
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Order ID: #$orderId",
                                fontSize = 12.sp,
                                color = GearaDarkBrown.copy(alpha = 0.7f),
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Selesaikan dalam: ",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = timerString,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD32F2F)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Channel Selector Tabs
                    Text(
                        text = "Pilih Metode Pembayaran",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaDarkBrown
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentChannelTab(
                            title = "QRIS",
                            icon = Icons.Default.QrCode,
                            isSelected = selectedChannel == "QRIS",
                            modifier = Modifier.weight(1f)
                        ) { selectedChannel = "QRIS" }

                        PaymentChannelTab(
                            title = "Virtual Account",
                            icon = Icons.Default.AccountBalance,
                            isSelected = selectedChannel == "VA",
                            modifier = Modifier.weight(1.3f)
                        ) { selectedChannel = "VA" }

                        PaymentChannelTab(
                            title = "E-Wallet",
                            icon = Icons.Default.AccountBalanceWallet,
                            isSelected = selectedChannel == "EWALLET",
                            modifier = Modifier.weight(1f)
                        ) { selectedChannel = "EWALLET" }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Channel Detail Content
                    when (selectedChannel) {
                        "QRIS" -> {
                            QrisPaymentView(
                                totalAmount = totalAmountFormatted,
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("QRIS Data", "ID.CO.GEARA.PAY.$orderId"))
                                    Toast.makeText(context, "Data QRIS disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        "VA" -> {
                            VirtualAccountView(
                                selectedBank = selectedBank,
                                vaNumber = vaNumber,
                                onBankSelected = { selectedBank = it },
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("VA Number", vaNumber))
                                    Toast.makeText(context, "Nomor Virtual Account disalin: $vaNumber", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        "EWALLET" -> {
                            EWalletPaymentView(
                                onSelect = { walletName ->
                                    Toast.makeText(context, "Menghubungkan ke aplikasi $walletName...", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- TOMBOL SIMULASI WEBHOOK GATEWAY (FITUR UTAMA) ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E5)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = GearaOrange, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Simulasi Webhook Sandbox",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GearaDarkBrown
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Menembak Webhook konfirmasi pembayaran ke server untuk mengubah status pesanan dari PENDING ke ACTIVE secara instan.",
                                fontSize = 11.sp,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    isSimulating = true
                                    viewModel.simulatePaymentSuccess(orderId) {
                                        isSimulating = false
                                        Toast.makeText(context, "🎉 Webhook Berhasil: Pembayaran Lunas!", Toast.LENGTH_LONG).show()
                                        navController.navigate("order_confirmed/$orderId") {
                                            popUpTo("payment_gateway/$orderId") { inclusive = true }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GearaOrange),
                                enabled = !isSimulating
                            ) {
                                if (isSimulating) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Memproses Webhook...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Simulasikan Pembayaran Sukses", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tombol Cek Status Pembayaran (Polling)
                    OutlinedButton(
                        onClick = {
                            isCheckingStatus = true
                            viewModel.checkPaymentStatus(orderId) {
                                isCheckingStatus = false
                                Toast.makeText(context, "Pembayaran terkonfirmasi!", Toast.LENGTH_SHORT).show()
                                navController.navigate("order_confirmed/$orderId") {
                                    popUpTo("payment_gateway/$orderId") { inclusive = true }
                                }
                            }
                            scope.launch {
                                delay(1500)
                                isCheckingStatus = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GearaDarkBrown)
                    ) {
                        if (isCheckingStatus) {
                            CircularProgressIndicator(color = GearaOrange, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mengecek Server...", fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cek Status Pembayaran", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tombol Bayar Nanti
                    TextButton(
                        onClick = {
                            navController.navigate("orders") {
                                popUpTo("home")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Bayar Nanti (Lihat di Pesanan Saya)",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

// --- SUB-COMPONENTS ---

@Composable
fun PaymentChannelTab(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = if (isSelected) GearaOrange else Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color.White else GearaDarkBrown,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else GearaDarkBrown,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun QrisPaymentView(
    totalAmount: String,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // QRIS Header Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE53935), RoundedCornerShape(8.dp))
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = "QRIS STANDAR PEMBAYARAN NASIONAL",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas QR Code Generator Visual (Sharp Vector 21x21 matrix simulation)
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.White)
                    .border(2.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                QrisCanvasPattern(modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "NMID: ID10202610210005",
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "GEARA MOTORS AFTER-SALES CRM",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GearaDarkBrown
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Mendukung: GoPay, OVO, Dana, ShopeePay, LinkAja, BCA, Mandiri, BRImo",
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun VirtualAccountView(
    selectedBank: String,
    vaNumber: String,
    onBankSelected: (String) -> Unit,
    onCopy: () -> Unit
) {
    val banks = listOf("BCA", "Mandiri", "BRI", "BNI")

    Column(modifier = Modifier.fillMaxWidth()) {
        // Bank Selection Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            banks.forEach { bank ->
                val isSelected = selectedBank == bank
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0xFFFFE0D3) else Color.White)
                        .border(1.dp, if (isSelected) GearaOrange else Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                        .clickable { onBankSelected(bank) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = bank,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) GearaOrange else GearaDarkBrown
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // VA Display Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Nomor Virtual Account $selectedBank",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF7F7F7), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = vaNumber,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaDarkBrown,
                        letterSpacing = 1.sp
                    )

                    Button(
                        onClick = onCopy,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE0D3)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "Salin",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GearaOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Petunjuk Pembayaran $selectedBank:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GearaDarkBrown
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "1. Buka aplikasi m-Banking atau ATM ${selectedBank}.\n2. Pilih menu Transfer > Virtual Account.\n3. Masukkan nomor VA di atas dan pastikan nominal tagihan sesuai.\n4. Konfirmasi transaksi dan simpan bukti pembayaran.",
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun EWalletPaymentView(
    onSelect: (String) -> Unit
) {
    val wallets = listOf(
        Pair("GoPay", Color(0xFF00AED6)),
        Pair("ShopeePay", Color(0xFFEE4D2D)),
        Pair("DANA", Color(0xFF118EEA)),
        Pair("OVO", Color(0xFF4C2A86))
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Pilih Aplikasi E-Wallet",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GearaDarkBrown
            )

            wallets.forEach { (name, color) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF9F9F9))
                        .clickable { onSelect(name) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(color.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.take(1),
                                color = color,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GearaDarkBrown
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * Menggambar pola QRIS 21x21 matrix yang presisi dan tajam secara native di Compose Canvas
 */
@Composable
fun QrisCanvasPattern(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val n = 21
        val cellSize = size.width / n

        // Gambar Finder Patterns (3 sudut besar)
        drawRect(Color.Black, Offset(0f, 0f), Size(7 * cellSize, 7 * cellSize))
        drawRect(Color.White, Offset(cellSize, cellSize), Size(5 * cellSize, 5 * cellSize))
        drawRect(Color.Black, Offset(2 * cellSize, 2 * cellSize), Size(3 * cellSize, 3 * cellSize))

        drawRect(Color.Black, Offset((n - 7) * cellSize, 0f), Size(7 * cellSize, 7 * cellSize))
        drawRect(Color.White, Offset((n - 6) * cellSize, cellSize), Size(5 * cellSize, 5 * cellSize))
        drawRect(Color.Black, Offset((n - 5) * cellSize, 2 * cellSize), Size(3 * cellSize, 3 * cellSize))

        drawRect(Color.Black, Offset(0f, (n - 7) * cellSize), Size(7 * cellSize, 7 * cellSize))
        drawRect(Color.White, Offset(cellSize, (n - 6) * cellSize), Size(5 * cellSize, 5 * cellSize))
        drawRect(Color.Black, Offset(2 * cellSize, (n - 5) * cellSize), Size(3 * cellSize, 3 * cellSize))

        // Timing patterns
        for (i in 8..(n - 9) step 2) {
            drawRect(Color.Black, Offset(i * cellSize, 6 * cellSize), Size(cellSize, cellSize))
            drawRect(Color.Black, Offset(6 * cellSize, i * cellSize), Size(cellSize, cellSize))
        }

        // Data modules simulation matrix
        val dataCoords = listOf(
            Pair(9, 2), Pair(11, 2), Pair(9, 4), Pair(11, 4), Pair(8, 8), Pair(10, 8),
            Pair(12, 8), Pair(14, 8), Pair(8, 10), Pair(10, 10), Pair(13, 10), Pair(15, 10),
            Pair(9, 12), Pair(11, 12), Pair(14, 12), Pair(8, 14), Pair(12, 14), Pair(16, 14),
            Pair(9, 16), Pair(11, 16), Pair(13, 16), Pair(15, 16), Pair(10, 18), Pair(12, 18)
        )
        for ((r, c) in dataCoords) {
            drawRect(Color.Black, Offset(c * cellSize, r * cellSize), Size(cellSize, cellSize))
        }
    }
}
