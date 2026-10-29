package id.ac.stmi.aplikasiutsgibran.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.ac.stmi.aplikasiutsgibran.auth.SessionManager
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaDarkBrown
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaYellow
import kotlinx.coroutines.launch

@Composable
fun PaymentMethodsScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context.applicationContext) }

    val savedPrimaryMethod by sessionManager.primaryPaymentMethod.collectAsState(initial = "gopay")
    val savedIsDanaConnected by sessionManager.isDanaConnected.collectAsState(initial = false)
    val savedDanaPhone by sessionManager.danaPhone.collectAsState(initial = "081234567806")

    // Label 'Utama' dan status seleksi berpindah sesuai item yang diklik
    var primaryMethod by remember { mutableStateOf("gopay") }
    var selectedMethod by remember { mutableStateOf("gopay") }

    // Status akun DANA
    var isDanaConnected by remember { mutableStateOf(false) }
    var showDanaDialog by remember { mutableStateOf(false) }
    var danaPhoneNumber by remember { mutableStateOf("081234567806") }

    LaunchedEffect(savedPrimaryMethod) {
        primaryMethod = savedPrimaryMethod
        selectedMethod = savedPrimaryMethod
    }
    LaunchedEffect(savedIsDanaConnected) {
        isDanaConnected = savedIsDanaConnected
    }
    LaunchedEffect(savedDanaPhone) {
        danaPhoneNumber = savedDanaPhone
    }

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
                    text = "Metode Pembayaran",
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
                    Text(
                        text = "Metode Pembayaran",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaDarkBrown
                    )
                    Text(
                        text = "Pilih metode yang digunakan sebagai pembayaran utama.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // SECTION 1: Dompet Digital (E-Wallet)
                    Text(
                        text = "Dompet Digital",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaOrange
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. GoPay
                    PaymentOptionCard(
                        title = "GoPay",
                        subtitle = "Terhubung • Saldo: Rp 850.000",
                        isPrimary = primaryMethod == "gopay",
                        isSelected = selectedMethod == "gopay",
                        onClick = {
                            selectedMethod = "gopay"
                            primaryMethod = "gopay"
                            coroutineScope.launch {
                                sessionManager.savePrimaryPaymentMethod("gopay")
                                snackbarHostState.showSnackbar("GoPay dipilih sebagai metode pembayaran utama.")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. OVO
                    PaymentOptionCard(
                        title = "OVO",
                        subtitle = "Terhubung • 0812-3456-7890",
                        isPrimary = primaryMethod == "ovo",
                        isSelected = selectedMethod == "ovo",
                        onClick = {
                            selectedMethod = "ovo"
                            primaryMethod = "ovo"
                            coroutineScope.launch {
                                sessionManager.savePrimaryPaymentMethod("ovo")
                                snackbarHostState.showSnackbar("OVO dipilih sebagai metode pembayaran utama.")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. DANA (Ada Pop-up Hubungkan Akun jika belum terhubung)
                    PaymentOptionCard(
                        title = "DANA",
                        subtitle = if (isDanaConnected) "Terhubung • Saldo: Rp 450.000" else "Klik untuk menghubungkan akun",
                        isPrimary = primaryMethod == "dana",
                        isSelected = selectedMethod == "dana",
                        onClick = {
                            if (!isDanaConnected) {
                                showDanaDialog = true
                            } else {
                                selectedMethod = "dana"
                                primaryMethod = "dana"
                                coroutineScope.launch {
                                    sessionManager.savePrimaryPaymentMethod("dana")
                                    snackbarHostState.showSnackbar("DANA dipilih sebagai metode pembayaran utama.")
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // SECTION 2: Virtual Account (Bank Transfer)
                    Text(
                        text = "Virtual Account",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaOrange
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. BCA Virtual Account
                    PaymentOptionCard(
                        title = "BCA Virtual Account",
                        subtitle = "Verifikasi instan otomatis 24 Jam",
                        isPrimary = primaryMethod == "va_bca",
                        isSelected = selectedMethod == "va_bca",
                        onClick = {
                            selectedMethod = "va_bca"
                            primaryMethod = "va_bca"
                            coroutineScope.launch {
                                sessionManager.savePrimaryPaymentMethod("va_bca")
                                snackbarHostState.showSnackbar("BCA Virtual Account dipilih sebagai metode utama.")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5. Mandiri Virtual Account
                    PaymentOptionCard(
                        title = "Mandiri Virtual Account",
                        subtitle = "Verifikasi instan otomatis 24 Jam",
                        isPrimary = primaryMethod == "va_mandiri",
                        isSelected = selectedMethod == "va_mandiri",
                        onClick = {
                            selectedMethod = "va_mandiri"
                            primaryMethod = "va_mandiri"
                            coroutineScope.launch {
                                sessionManager.savePrimaryPaymentMethod("va_mandiri")
                                snackbarHostState.showSnackbar("Mandiri Virtual Account dipilih sebagai metode utama.")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Info Keamanan
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Transaksi diproses melalui standar keamanan enkripsi 256-bit SSL Bank Indonesia.",
                                fontSize = 11.sp,
                                color = Color(0xFF166534),
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
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
                            isDanaConnected = true
                            selectedMethod = "dana"
                            primaryMethod = "dana"
                            showDanaDialog = false
                            coroutineScope.launch {
                                sessionManager.setDanaConnected(true, danaPhoneNumber)
                                sessionManager.savePrimaryPaymentMethod("dana")
                                snackbarHostState.showSnackbar("Akun DANA ($danaPhoneNumber) terhubung sebagai metode utama.")
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Nomor ponsel DANA minimal 10 digit.")
                            }
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

@Composable
fun PaymentOptionCard(
    title: String,
    subtitle: String,
    isPrimary: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFF7ED) else Color.White
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(1.5.dp, GearaOrange)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
        },
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
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) GearaOrange.copy(alpha = 0.15f) else Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (title.contains("Virtual Account", ignoreCase = true)) {
                            Icons.Default.AccountBalance
                        } else {
                            Icons.Default.CreditCard
                        },
                        contentDescription = null,
                        tint = if (isSelected) GearaOrange else Color.DarkGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
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
                // Label 'Utama' berpindah dinamis ke item yang dipilih
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
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
