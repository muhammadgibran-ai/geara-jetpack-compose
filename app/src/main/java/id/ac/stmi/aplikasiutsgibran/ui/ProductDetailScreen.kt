package id.ac.stmi.aplikasiutsgibran.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.ac.stmi.aplikasiutsgibran.data.GearaRepository
import id.ac.stmi.aplikasiutsgibran.data.GearaViewModel
import id.ac.stmi.aplikasiutsgibran.ui.components.GearaImage
import id.ac.stmi.aplikasiutsgibran.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    navController: NavController,
    viewModel: GearaViewModel
) {
    val availableProducts by viewModel.availableProducts.collectAsState()
    val product = availableProducts.firstOrNull { it.id == productId } ?: GearaRepository.products.firstOrNull { it.id == productId } ?: GearaRepository.products.first()
    val cartCount = viewModel.cartItems.collectAsState().value.sumOf { it.quantity }
    val favoriteIds by viewModel.favoriteProductIds.collectAsState()
    val isFavorite = favoriteIds.contains(productId)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var quantity by remember { mutableIntStateOf(1) }

    Scaffold(
        containerColor = GearaBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detail Suku Cadang", fontWeight = FontWeight.Bold, color = GearaDarkBrown) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = GearaDarkBrown)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val added = viewModel.toggleFavorite(productId)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (added) "${product.name} ditambahkan ke Favorit ❤️" else "${product.name} dihapus dari Favorit"
                            )
                        }
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Hapus dari Favorit" else "Tambah ke Favorit",
                            tint = if (isFavorite) GearaOrange else GearaDarkBrown
                        )
                    }
                    IconButton(onClick = { navController.navigate("cart") }) {
                        Box {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = GearaOrange)
                            if (cartCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 6.dp, y = (-4).dp)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(GearaRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cartCount.toString(),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GearaYellow)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 16.dp,
                color = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Chat button
                        OutlinedButton(
                            onClick = { navController.navigate("messages") },
                            modifier = Modifier.size(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(0.dp),
                            border = BorderStroke(1.dp, GearaOrange.copy(alpha = 0.35f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = GearaOrange
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Chat CRM",
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Add to cart button
                        Button(
                            onClick = {
                                viewModel.addToCart(product, quantity)
                                scope.launch {
                                    snackbarHostState.showSnackbar("$quantity x ${product.name} masuk ke keranjang!")
                                }
                            },
                            modifier = Modifier
                                .weight(1.05f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GearaLightOrange,
                                contentColor = GearaOrange
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(19.dp),
                                    tint = GearaOrange
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+ Keranjang",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    color = GearaOrange
                                )
                            }
                        }

                        // Buy now button
                        Button(
                            onClick = {
                                viewModel.addToCart(product, quantity)
                                navController.navigate("cart")
                            },
                            modifier = Modifier
                                .weight(1.15f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GearaOrange)
                        ) {
                            Text(
                                text = "Beli Sekarang",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                maxLines = 1,
                                softWrap = false,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(GearaBackground)
        ) {
            // Large Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                GearaImage(
                    imageUrl = product.imageUrl,
                    fallbackRes = product.imageRes,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
                if (product.discountPercent > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .background(GearaRed, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Diskon ${product.discountPercent}%",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Main Product Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Brand & Part Number Header with flexible weights & overflow protection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = GearaOrange.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = product.brand,
                                color = GearaOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "PN: ${product.partNumber}",
                                color = GearaTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GearaDarkBrown,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Price Section
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = product.formattedPrice,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = GearaOrange
                        )
                        if (product.originalPrice > 0) {
                            Text(
                                text = product.formattedOriginalPrice,
                                fontSize = 14.sp,
                                color = GearaGray,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rating & Sold
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF9F9F9), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${product.rating} / 5.0", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = " (142 ulasan)", color = GearaTextSecondary, fontSize = 12.sp)
                        }
                        Text(text = "Terjual ${product.soldCount}+ unit", color = GearaTextSecondary, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quantity Stepper & Stock
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Jumlah Pembelian", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = GearaDarkBrown)
                            Text(
                                text = if (product.stock > 0) "Stok: Tersisa ${product.stock} unit" else "Stok Habis",
                                fontSize = 12.sp,
                                color = if (product.stock > 10) Color(0xFF2E7D32) else GearaRed,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = quantity.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            IconButton(
                                onClick = { if (quantity < product.stock) quantity++ },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(GearaLightOrange, RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Tambah", tint = GearaOrange, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // CRM Compatibility Box for Geara Cars
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = GearaGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Garansi Kompatibilitas Mobil Geara (CRM)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GearaDarkBrown
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Suku cadang resmi ini telah diverifikasi oleh tim teknisi After-Sales Geara Service Center dan 100% plug & play bergaransi resmi untuk lini mobil Geara berikut:",
                        fontSize = 12.sp,
                        color = GearaTextSecondary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    product.motorCompatibility.forEach { car ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = GearaOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = car, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GearaDarkBrown)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Deskripsi Suku Cadang",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GearaDarkBrown
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.description,
                        fontSize = 13.sp,
                        color = Color(0xFF616161),
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0xFFEEEEEE))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Garansi Toko", fontSize = 13.sp, color = GearaTextSecondary)
                        Text(text = "6 Bulan Ganti Baru", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GearaGreen)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Kondisi", fontSize = 13.sp, color = GearaTextSecondary)
                        Text(text = "100% Baru & Original", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GearaDarkBrown)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
