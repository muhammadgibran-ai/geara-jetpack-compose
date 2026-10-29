package id.ac.stmi.aplikasiutsgibran.ui

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.ac.stmi.aplikasiutsgibran.R
import id.ac.stmi.aplikasiutsgibran.data.CheckoutState
import id.ac.stmi.aplikasiutsgibran.data.GearaViewModel
import id.ac.stmi.aplikasiutsgibran.ui.components.GearaBottomNavBar
import id.ac.stmi.aplikasiutsgibran.ui.components.GearaImage
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaDarkBrown
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaYellow

data class CheckoutCourierChoice(
    val courierName: String,
    val servicePackage: String,
    val badgeText: String,
    val eta: String,
    val fee: Int,
    val isInternal: Boolean
) {
    val fullName: String
        get() = "$courierName - $servicePackage"
}

@Composable
fun CheckoutScreen(
    navController: NavController,
    viewModel: GearaViewModel
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val checkoutState by viewModel.checkoutState.collectAsState()
    val selectedItems = cartItems.filter { it.selected }

    var address by remember { mutableStateOf("Jl. Merdeka No. 78, Bandung") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Opsi ekspedisi pengiriman (1 order dilayani oleh 1 ekspedisi)
    val courierChoices = remember {
        listOf(
            CheckoutCourierChoice("SPX Express", "Sameday", "Kurir Internal", "Tiba Hari Ini (3-6 jam)", 15000, true),
            CheckoutCourierChoice("JNE Express", "YES", "Pihak Ketiga", "Tiba Besok (1x24 jam)", 18000, false),
            CheckoutCourierChoice("J&T Express", "EZ", "Pihak Ketiga", "1-2 Hari Kerja", 15000, false),
            CheckoutCourierChoice("SiCepat Ekspres", "BEST", "Pihak Ketiga", "1-2 Hari Kerja", 16000, false)
        )
    }
    var selectedCourier by remember { mutableStateOf(courierChoices[0]) }

    LaunchedEffect(checkoutState) {
        when (checkoutState) {
            is CheckoutState.Success -> {
                val orderId = (checkoutState as CheckoutState.Success).orderId
                navController.navigate("payment_gateway/$orderId") {
                    popUpTo("cart") { inclusive = true }
                }
                viewModel.resetCheckoutState()
            }
            is CheckoutState.Error -> {
                val message = (checkoutState as CheckoutState.Error).message
                snackbarHostState.showSnackbar(message)
                viewModel.resetCheckoutState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            GearaBottomNavBar(navController = navController, currentRoute = "cart")
        },
        containerColor = GearaYellow
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header bar seragam dengan gaya visual aplikasi Geara
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Kembali lingkaran putih
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

                // Judul Header
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp)
                ) {
                    Text(
                        text = "Konfirmasi Pesanan",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaDarkBrown
                    )
                    Text(
                        text = "Checkout & Pengiriman Suku Cadang",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = GearaDarkBrown.copy(alpha = 0.75f)
                    )
                }
            }

            // White content sheet with curved top corners
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                color = Color(0xFFF8F8F8),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    // Alamat Pengiriman
                    Text(
                        text = "Alamat Pengiriman",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaDarkBrown
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { newValue: String -> address = newValue },
                        placeholder = { Text(text = "Masukkan alamat lengkap pengiriman", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Alamat",
                                tint = GearaOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GearaOrange,
                            unfocusedBorderColor = Color(0xFFD6D6D6),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Pilihan Ekspedisi Pengiriman (1 Pesanan = 1 Ekspedisi)
                    Text(
                        text = "Pilih Ekspedisi Pengiriman",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaDarkBrown
                    )
                    Text(
                        text = "Setiap pesanan dilayani khusus oleh 1 ekspedisi logistik resmi",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        courierChoices.forEach { courier ->
                            val isChosen = selectedCourier == courier
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(
                                        width = if (isChosen) 2.dp else 1.dp,
                                        color = if (isChosen) GearaOrange else Color(0xFFE5E7EB),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedCourier = courier },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isChosen) Color(0xFFFFF7F2) else Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isChosen) 1.5.dp else 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isChosen,
                                        onClick = { selectedCourier = courier },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = GearaOrange,
                                            unselectedColor = Color(0xFFBDBDBD)
                                        )
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        // Baris 1: Nama Ekspedisi Lengkap & Tarif Ongkir
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = courier.fullName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GearaDarkBrown
                                            )
                                            Text(
                                                text = "Rp " + "%,d".format(courier.fee).replace(',', '.'),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isChosen) GearaOrange else GearaDarkBrown
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(5.dp))

                                        // Baris 2: Badge Tipe Kurir & Estimasi Waktu Tiba
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                color = if (courier.isInternal) Color(0xFFFFECE0) else Color(0xFFE1F5FE),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = courier.badgeText,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (courier.isInternal) GearaOrange else Color(0xFF0277BD),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    maxLines = 1
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Schedule,
                                                    contentDescription = null,
                                                    tint = Color(0xFF9E9E9E),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "Estimasi: ${courier.eta}",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF757575)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Ringkasan Pesanan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ringkasan Pesanan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GearaDarkBrown
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFE2D2))
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                                .clickable { navController.popBackStack() }
                        ) {
                            Text(
                                text = "Ubah",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GearaOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color(0xFFFFD5C0), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // List Item
                    if (selectedItems.isEmpty()) {
                        Text("Tidak ada barang yang dipilih.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                    } else {
                        selectedItems.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFE8E8E8)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    GearaImage(
                                        imageUrl = item.product.imageUrl,
                                        fallbackRes = item.product.imageRes,
                                        contentDescription = item.product.name,
                                        modifier = Modifier.size(56.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.product.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GearaDarkBrown,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = item.product.brand,
                                        fontSize = 11.sp,
                                        color = GearaDarkBrown.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = item.product.formattedPrice,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GearaOrange
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus",
                                        tint = GearaOrange,
                                        modifier = Modifier.size(20.dp).clickable {
                                            viewModel.removeFromCart(item.product.id)
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .background(Color(0xFFFFD5C0), CircleShape)
                                                .clickable { viewModel.updateQuantity(item.product.id, -1) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("-", color = GearaDarkBrown, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Text(
                                            text = item.quantity.toString(),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GearaDarkBrown,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .background(GearaOrange, CircleShape)
                                                .clickable { viewModel.updateQuantity(item.product.id, 1) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("+", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(color = Color(0xFFFFD5C0), thickness = 0.8.dp)
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    // Ekspedisi Terpilih
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ekspedisi Pengiriman", fontSize = 14.sp, color = GearaDarkBrown)
                        Text(selectedCourier.fullName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GearaDarkBrown)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subtotal breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal Produk", fontSize = 14.sp, color = GearaDarkBrown)
                        Text("Rp " + "%,d".format(viewModel.selectedSubtotal).replace(',', '.'), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = GearaDarkBrown)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Biaya Pengiriman", fontSize = 14.sp, color = GearaDarkBrown)
                        Text("Rp " + "%,d".format(selectedCourier.fee).replace(',', '.'), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = GearaDarkBrown)
                    }

                    if (viewModel.discountAmount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Diskon Promo", fontSize = 14.sp, color = Color(0xFF4CAF50))
                            Text("- Rp " + "%,d".format(viewModel.discountAmount).replace(',', '.'), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4CAF50))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFFFD5C0), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Total
                    val grandTotalWithCourier = (viewModel.selectedSubtotal + selectedCourier.fee - viewModel.discountAmount).coerceAtLeast(0)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GearaDarkBrown)
                        Text("Rp " + "%,d".format(grandTotalWithCourier).replace(',', '.'), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GearaOrange)
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Buat Pesanan button
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                if (checkoutState !is CheckoutState.Loading) {
                                    viewModel.placeOrder(address, selectedCourier.fullName, "BCA Virtual Account")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(27.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (checkoutState is CheckoutState.Loading) Color.Gray else GearaOrange
                            ),
                            enabled = checkoutState !is CheckoutState.Loading
                        ) {
                            if (checkoutState is CheckoutState.Loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Text(
                                    text = "Buat Pesanan",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
