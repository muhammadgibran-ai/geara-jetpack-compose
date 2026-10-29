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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.text.KeyboardOptions
import id.ac.stmi.aplikasiutsgibran.auth.SessionManager
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

    val sessionManager = remember { SessionManager(context.applicationContext) }
    val savedPrimaryMethod by sessionManager.primaryPaymentMethod.collectAsState(initial = "gopay")
    val savedIsDanaConnected by sessionManager.isDanaConnected.collectAsState(initial = false)
    val savedDanaPhone by sessionManager.danaPhone.collectAsState(initial = "081234567806")

    // State metode pembayaran aktif: "QRIS", "VA", "EWALLET", "CC"
    var selectedChannel by remember { mutableStateOf("EWALLET") }
    var selectedWallet by remember { mutableStateOf("GoPay") }
    var selectedBank by remember { mutableStateOf("BCA") }
    var isSimulating by remember { mutableStateOf(false) }
    var isCheckingStatus by remember { mutableStateOf(false) }

    var showDanaDialog by remember { mutableStateOf(false) }
    var danaPhoneNumber by remember { mutableStateOf("081234567806") }

    // Sinkronisasi otomatis dari Metode Pembayaran Utama yang dipilih pengguna
    LaunchedEffect(savedPrimaryMethod) {
        when (savedPrimaryMethod) {
            "gopay" -> {
                selectedChannel = "EWALLET"
                selectedWallet = "GoPay"
            }
            "ovo" -> {
                selectedChannel = "EWALLET"
                selectedWallet = "OVO"
            }
            "dana" -> {
                selectedChannel = "EWALLET"
                selectedWallet = "DANA"
            }
            "va_bca" -> {
                selectedChannel = "VA"
                selectedBank = "BCA"
            }
            "va_mandiri" -> {
                selectedChannel = "VA"
                selectedBank = "Mandiri"
            }
        }
    }

    LaunchedEffect(savedDanaPhone) {
        danaPhoneNumber = savedDanaPhone
    }

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
                                text = "Total Pembayaran",
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
                                    text = "Batas waktu pembayaran: ",
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
                                    Toast.makeText(context, "Kode QRIS disalin.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        "VA" -> {
                            VirtualAccountView(
                                selectedBank = selectedBank,
                                primaryMethod = savedPrimaryMethod,
                                vaNumber = vaNumber,
                                onBankSelected = { bank ->
                                    selectedBank = bank
                                    val methodKey = if (bank == "Mandiri") "va_mandiri" else "va_bca"
                                    scope.launch {
                                        sessionManager.savePrimaryPaymentMethod(methodKey)
                                    }
                                },
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("VA Number", vaNumber))
                                    Toast.makeText(context, "Nomor Virtual Account disalin: $vaNumber", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        "EWALLET" -> {
                            EWalletPaymentView(
                                selectedWallet = selectedWallet,
                                primaryMethod = savedPrimaryMethod,
                                isDanaConnected = savedIsDanaConnected,
                                onSelect = { walletName ->
                                    selectedWallet = walletName
                                    val methodKey = when (walletName) {
                                        "GoPay" -> "gopay"
                                        "OVO" -> "ovo"
                                        "DANA" -> "dana"
                                        else -> "gopay"
                                    }
                                    scope.launch {
                                        sessionManager.savePrimaryPaymentMethod(methodKey)
                                    }
                                    Toast.makeText(context, "$walletName dipilih sebagai metode utama.", Toast.LENGTH_SHORT).show()
                                },
                                onConnectDana = {
                                    showDanaDialog = true
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
                                    text = "Simulasi Pembayaran (Sandbox)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GearaDarkBrown
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Simulasi alur transaksi tanpa potongan dana nyata untuk pengujian status pesanan.",
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
                                        Toast.makeText(context, "Pembayaran berhasil diverifikasi.", Toast.LENGTH_LONG).show()
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
                                    Text("Memproses verifikasi...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Simulasikan Pembayaran Berhasil", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                                Toast.makeText(context, "Pembayaran telah terkonfirmasi.", Toast.LENGTH_SHORT).show()
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
                            Text("Memeriksa status...", fontSize = 13.sp)
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
                            text = "Bayar nanti (simpan ke Pesanan Saya)",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // Pop-up Hubungkan Akun DANA
        if (showDanaDialog) {
            AlertDialog(
                onDismissRequest = { showDanaDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF118EEA).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color(0xFF118EEA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Hubungkan Akun DANA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = GearaDarkBrown
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Masukkan nomor ponsel yang terdaftar di akun DANA untuk menghubungkan pembayaran.",
                            fontSize = 13.sp,
                            color = Color(0xFF555555),
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = danaPhoneNumber,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() || it == '+' }
                                if (filtered.length <= 15) {
                                    danaPhoneNumber = filtered
                                }
                            },
                            label = { Text("Nomor Ponsel DANA") },
                            placeholder = { Text("081234567890") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (danaPhoneNumber.length >= 10) {
                                selectedWallet = "DANA"
                                showDanaDialog = false
                                scope.launch {
                                    sessionManager.setDanaConnected(true, danaPhoneNumber)
                                    sessionManager.savePrimaryPaymentMethod("dana")
                                }
                                Toast.makeText(context, "Akun DANA ($danaPhoneNumber) terhubung sebagai metode utama.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Nomor ponsel DANA minimal 10 digit.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GearaOrange),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Hubungkan Akun", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDanaDialog = false }) {
                        Text("Batal", color = Color.Gray)
                    }
                }
            )
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
    primaryMethod: String,
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
                val isPrimary = (bank == "BCA" && primaryMethod == "va_bca") || (bank == "Mandiri" && primaryMethod == "va_mandiri")

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0xFFFFE0D3) else Color.White)
                        .border(
                            1.dp,
                            if (isSelected) GearaOrange else Color(0xFFE0E0E0),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onBankSelected(bank) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = bank,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) GearaOrange else GearaDarkBrown
                        )
                        if (isPrimary) {
                            Text(
                                text = "Utama",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GearaOrange
                            )
                        }
                    }
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
    selectedWallet: String,
    primaryMethod: String,
    isDanaConnected: Boolean,
    onSelect: (String) -> Unit,
    onConnectDana: () -> Unit
) {
    val wallets = listOf(
        Triple("GoPay", "Terhubung • Saldo: Rp 850.000", Color(0xFF00AED6)),
        Triple("OVO", "Terhubung • 0812-3456-7890", Color(0xFF4C2A86)),
        Triple("DANA", if (isDanaConnected) "Terhubung • Saldo: Rp 450.000" else "Klik untuk menghubungkan akun", Color(0xFF118EEA))
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

            wallets.forEach { (name, subtitle, color) ->
                val isSelected = selectedWallet == name
                val methodKey = when (name) {
                    "GoPay" -> "gopay"
                    "OVO" -> "ovo"
                    "DANA" -> "dana"
                    else -> "gopay"
                }
                val isPrimary = primaryMethod == methodKey

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFFFFF7ED) else Color(0xFFF9F9F9))
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) GearaOrange else Color(0xFFEEEEEE),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            if (name == "DANA" && !isDanaConnected) {
                                onConnectDana()
                            } else {
                                onSelect(name)
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
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
                        Column {
                            Text(
                                text = name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GearaDarkBrown
                            )
                            Text(
                                text = subtitle,
                                fontSize = 11.sp,
                                color = if (subtitle.contains("Klik untuk", ignoreCase = true)) GearaOrange else Color.Gray,
                                fontWeight = if (subtitle.contains("Klik untuk", ignoreCase = true)) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isPrimary) {
                            Surface(
                                color = Color(0xFFFFECE5),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GearaOrange.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "Utama",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GearaOrange,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Terpilih",
                                tint = GearaOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
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
