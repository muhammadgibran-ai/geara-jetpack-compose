package id.ac.stmi.aplikasiutsgibran.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.ac.stmi.aplikasiutsgibran.data.CartItem
import id.ac.stmi.aplikasiutsgibran.data.GearaViewModel
import id.ac.stmi.aplikasiutsgibran.ui.components.GearaImage
import id.ac.stmi.aplikasiutsgibran.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    viewModel: GearaViewModel
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val appliedVoucher by viewModel.appliedVoucher.collectAsState()
    var voucherInput by remember { mutableStateOf(appliedVoucher ?: "") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val allSelected = cartItems.isNotEmpty() && cartItems.all { it.selected }

    Scaffold(
        containerColor = GearaBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Keranjang Suku Cadang", fontWeight = FontWeight.Bold, color = GearaDarkBrown) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = GearaDarkBrown)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GearaYellow)
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 16.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Total Pembayaran", fontSize = 12.sp, color = GearaTextSecondary)
                                Text(
                                    text = viewModel.formattedGrandTotal,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GearaOrange
                                )
                            }
                            Button(
                                onClick = {
                                    if (viewModel.selectedSubtotal > 0) {
                                        navController.navigate("checkout")
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Pilih minimal 1 suku cadang untuk checkout!")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .height(50.dp)
                                    .padding(start = 16.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GearaOrange)
                            ) {
                                Text("Lanjut Checkout", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(GearaBackground),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.RemoveShoppingCart,
                        contentDescription = null,
                        tint = GearaGray,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Keranjang Belanja Kosong",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaDarkBrown
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Yuk cari suku cadang resmi untuk mobil Geara Anda!",
                        fontSize = 13.sp,
                        color = GearaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { navController.navigate("catalog") },
                        colors = ButtonDefaults.buttonColors(containerColor = GearaOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Buka Katalog Suku Cadang")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(GearaBackground),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Select all row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = allSelected,
                            onCheckedChange = { viewModel.selectAll(it) },
                            colors = CheckboxDefaults.colors(checkedColor = GearaOrange)
                        )
                        Text(
                            text = "Pilih Semua Suku Cadang",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = GearaDarkBrown
                        )
                    }
                }

                // Cart item cards
                items(cartItems) { item ->
                    CartItemCard(
                        item = item,
                        onToggle = { viewModel.toggleSelection(item.product.id) },
                        onIncrement = { viewModel.updateQuantity(item.product.id, 1) },
                        onDecrement = { viewModel.updateQuantity(item.product.id, -1) },
                        onRemove = { viewModel.removeFromCart(item.product.id) }
                    )
                }

                // Voucher Input Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Voucher Diskon & Promo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = GearaDarkBrown
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = voucherInput,
                                    onValueChange = { voucherInput = it },
                                    placeholder = { Text("Ketik GEARAPROMO", fontSize = 13.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GearaOrange,
                                        unfocusedBorderColor = Color(0xFFD6D6D6)
                                    )
                                )
                                Button(
                                    onClick = {
                                        val success = viewModel.applyVoucher(voucherInput)
                                        scope.launch {
                                            if (success) {
                                                snackbarHostState.showSnackbar("Voucher berhasil digunakan! Diskon Rp 25.000")
                                            } else {
                                                snackbarHostState.showSnackbar("Kode voucher tidak valid. Coba: GEARAPROMO")
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GearaOrange)
                                ) {
                                    Text("Terapkan")
                                }
                            }
                            if (appliedVoucher != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Voucher $appliedVoucher aktif (Hemat Rp 25.000)",
                                    fontSize = 12.sp,
                                    color = GearaGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Price Breakdown Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Ringkasan Pembayaran",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = GearaDarkBrown
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Subtotal Produk", fontSize = 13.sp, color = GearaTextSecondary)
                                Text(
                                    text = "Rp " + "%,d".format(viewModel.selectedSubtotal).replace(',', '.'),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Biaya Pengiriman", fontSize = 13.sp, color = GearaTextSecondary)
                                Text(
                                    text = "Rp " + "%,d".format(viewModel.deliveryFee).replace(',', '.'),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (viewModel.discountAmount > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Diskon Voucher", fontSize = 13.sp, color = GearaGreen)
                                    Text(
                                        text = "- Rp " + "%,d".format(viewModel.discountAmount).replace(',', '.'),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GearaGreen
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFEEEEEE))
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Total Bayar", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GearaDarkBrown)
                                Text(
                                    text = viewModel.formattedGrandTotal,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GearaOrange
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
fun CartItemCard(
    item: CartItem,
    onToggle: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.selected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = GearaOrange)
            )

            GearaImage(
                imageUrl = item.product.imageUrl,
                fallbackRes = item.product.imageRes,
                contentDescription = item.product.name,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF7F7F7)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.brand,
                    fontSize = 11.sp,
                    color = GearaOrange,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.product.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GearaDarkBrown,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.product.formattedPrice,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GearaOrange
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFFF0F0F0), RoundedCornerShape(6.dp))
                                .clickable { onDecrement() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = item.quantity.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(GearaLightOrange, RoundedCornerShape(6.dp))
                                .clickable { onIncrement() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah", tint = GearaOrange, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}
