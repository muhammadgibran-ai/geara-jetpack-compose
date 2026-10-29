package id.ac.stmi.aplikasiutsgibran.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import id.ac.stmi.aplikasiutsgibran.R
import id.ac.stmi.aplikasiutsgibran.auth.AuthViewModel
import id.ac.stmi.aplikasiutsgibran.data.GearaRepository
import id.ac.stmi.aplikasiutsgibran.data.GearaViewModel
import id.ac.stmi.aplikasiutsgibran.ui.components.GearaBottomNavBar
import id.ac.stmi.aplikasiutsgibran.ui.components.GearaImage
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaDarkBrown
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaYellow

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: GearaViewModel
) {
    var activeDrawer by remember { mutableStateOf<String?>(null) } // "cart", "notif", "profile", null
    val cartItems by viewModel.cartItems.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = { GearaBottomNavBar(navController, "home") },
            containerColor = GearaYellow
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header on Yellow Background
                Column(
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    // Top Bar: Search Pill + 3 Round Action Icons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Search Pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color.White)
                                .clickable { navController.navigate("search") }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Cari",
                                    color = Color(0xFFA8A8A8),
                                    fontSize = 14.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(GearaOrange),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Filter",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // 3 Action Buttons (Cart, Bell, Profile)
                        HeaderCircleButton(
                            icon = Icons.Default.ShoppingCart,
                            badgeCount = cartItems.sumOf { it.quantity },
                            onClick = { activeDrawer = "cart" }
                        )
                        HeaderCircleButton(
                            icon = Icons.Default.Notifications,
                            badgeCount = 3,
                            onClick = { activeDrawer = "notif" }
                        )
                        HeaderCircleButton(
                            icon = Icons.Default.Person,
                            onClick = { activeDrawer = "profile" }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title Greetings: "Halo, Sobat Geara!" & "After-Sales & Suku Cadang Resmi Mobil Geara"
                    Text(
                        text = "Halo, Sobat Geara!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "After-Sales & Suku Cadang Resmi Mobil Geara",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GearaOrange
                    )
                }

                // Main Content Area with Curved Top
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(Color(0xFFF7F7F7))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                    ) {
                        // 1. 5 Categories in Cream Circles: Rem, Mesin, Filter, Lampu, Oli
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CategoryCircleItem(label = "Rem", iconRes = R.drawable.ic_cat_rem) {
                                viewModel.setSelectedCategory("Rem")
                                navController.navigate("catalog")
                            }
                            CategoryCircleItem(label = "Mesin", icon = Icons.Default.Build) {
                                viewModel.setSelectedCategory("Mesin")
                                navController.navigate("catalog")
                            }
                            CategoryCircleItem(label = "Filter", iconRes = R.drawable.ic_cat_filter) {
                                viewModel.setSelectedCategory("Filter")
                                navController.navigate("catalog")
                            }
                            CategoryCircleItem(label = "Lampu", icon = Icons.Default.Lightbulb) {
                                viewModel.setSelectedCategory("Lampu")
                                navController.navigate("catalog")
                            }
                            CategoryCircleItem(label = "Oli", icon = Icons.Default.WaterDrop) {
                                viewModel.setSelectedCategory("Oli")
                                navController.navigate("catalog")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 2. Terlaris Section with "Semua >"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Terlaris",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = GearaDarkBrown
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { navController.navigate("catalog") }
                            ) {
                                Text(
                                    text = "Semua",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GearaOrange
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = GearaOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Terlaris Horizontal Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                TerlarisProductCard(
                                    imageRes = R.drawable.img_part_busi,
                                    imageUrl = "${GearaRepository.CLOUD_STORAGE_BASE_URL}prod_1_busi.png",
                                    priceText = "Rp103rb",
                                    onClick = { navController.navigate("product_detail/prod_1") }
                                )
                            }
                            item {
                                TerlarisProductCard(
                                    imageRes = R.drawable.img_part_rem,
                                    imageUrl = "${GearaRepository.CLOUD_STORAGE_BASE_URL}prod_5_rem.png",
                                    priceText = "Rp50rb",
                                    onClick = { navController.navigate("product_detail/prod_5") }
                                )
                            }
                            item {
                                TerlarisProductCard(
                                    imageRes = R.drawable.img_part_filter,
                                    imageUrl = "${GearaRepository.CLOUD_STORAGE_BASE_URL}prod_7_filter.png",
                                    priceText = "Rp12,99rb",
                                    onClick = { navController.navigate("product_detail/prod_7") }
                                )
                            }
                            item {
                                TerlarisProductCard(
                                    imageRes = R.drawable.img_part_lampu,
                                    imageUrl = "${GearaRepository.CLOUD_STORAGE_BASE_URL}prod_8_lampu.png",
                                    priceText = "Rp8,2rb",
                                    onClick = { navController.navigate("product_detail/prod_8") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 3. Advertising Promo Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(GearaOrange)
                                .clickable {
                                    viewModel.applyVoucher("GEARAPROMO")
                                    activeDrawer = "cart"
                                }
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                // Left text
                                Column(
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Perawatan mobil Geara,\njaminan servis resmi",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "DISKON\n30%",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        lineHeight = 26.sp
                                    )
                                }

                                // Right Image
                                Image(
                                    painter = painterResource(id = R.drawable.img_onboarding_1),
                                    contentDescription = "Promo Banner",
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        // Banner Dot Indicators
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(5) { index ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .size(width = if (index == 2) 20.dp else 16.dp, height = 4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (index == 2) GearaOrange else Color(0xFFF7E5C8))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 4. Rekomendasi Section for Geara Cars
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Rekomendasi Mobil Geara",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GearaDarkBrown
                                )
                                Text(
                                    text = "Suku cadang OEM rekomendasi teknisi resmi Geara",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2 Cards Side-by-Side with Heart and Price Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RekomendasiProductCard(
                                modifier = Modifier.weight(1f),
                                imageRes = R.drawable.img_part_busi,
                                imageUrl = "${GearaRepository.CLOUD_STORAGE_BASE_URL}prod_1_busi.png",
                                priceText = "Rp10rb",
                                onClick = { navController.navigate("product_detail/prod_1") }
                            )
                            RekomendasiProductCard(
                                modifier = Modifier.weight(1f),
                                imageRes = R.drawable.img_part_rem,
                                imageUrl = "${GearaRepository.CLOUD_STORAGE_BASE_URL}prod_5_rem.png",
                                priceText = "Rp25rb",
                                onClick = { navController.navigate("product_detail/prod_5") }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // Overlay Slide-In Drawers matching Figma: Cart, Notification, Profile
        AnimatedVisibility(
            visible = activeDrawer != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { activeDrawer = null }
            )
        }

        AnimatedVisibility(
            visible = activeDrawer == "cart",
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            FigmaCartDrawer(
                viewModel = viewModel,
                onCheckout = {
                    activeDrawer = null
                    navController.navigate("checkout")
                },
                onClose = { activeDrawer = null }
            )
        }

        AnimatedVisibility(
            visible = activeDrawer == "notif",
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            FigmaNotificationDrawer(
                onClose = { activeDrawer = null },
                onNavigate = { route ->
                    activeDrawer = null
                    navController.navigate(route)
                }
            )
        }

        AnimatedVisibility(
            visible = activeDrawer == "profile",
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            FigmaProfileDrawer(
                onClose = { activeDrawer = null },
                onNavigate = { route ->
                    activeDrawer = null
                    navController.navigate(route)
                }
            )
        }
    }
}

@Composable
fun HeaderCircleButton(
    icon: ImageVector,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GearaOrange,
            modifier = Modifier.size(20.dp)
        )
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(GearaOrange)
            )
        }
    }
}

@Composable
fun CategoryCircleItem(
    label: String,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFF7E5C8)),
            contentAlignment = Alignment.Center
        ) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    tint = GearaOrange,
                    modifier = Modifier.size(26.dp)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = GearaOrange,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = GearaDarkBrown
        )
    }
}

@Composable
fun TerlarisProductCard(
    imageRes: Int,
    imageUrl: String? = null,
    priceText: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(82.dp)
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE5EBEF))
            .clickable { onClick() }
    ) {
        GearaImage(
            imageUrl = imageUrl,
            fallbackRes = imageRes,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Orange price pill at bottom right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 6.dp)
                .background(GearaOrange, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = priceText,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RekomendasiProductCard(
    modifier: Modifier = Modifier,
    imageRes: Int,
    imageUrl: String? = null,
    priceText: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(150.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFE5EBEF))
            .clickable { onClick() }
    ) {
        GearaImage(
            imageUrl = imageUrl,
            fallbackRes = imageRes,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Top Left rating pill
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "5.0", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Icon(Icons.Default.Favorite, contentDescription = null, tint = GearaOrange, modifier = Modifier.size(12.dp))
            }
        }

        // Bottom right price pill
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp)
                .background(GearaOrange, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = priceText,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ----------------- FIGMA SLIDE-IN DRAWERS -----------------

@Composable
fun FigmaCartDrawer(
    viewModel: GearaViewModel,
    onCheckout: () -> Unit,
    onClose: () -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(310.dp)
            .clip(RoundedCornerShape(topStart = 40.dp))
            .background(GearaOrange)
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = GearaOrange)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Keranjang",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = Color.White.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            if (cartItems.isEmpty()) {
                // Empty Cart (Figma 4.3 - A)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Keranjang Anda kosong",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.Transparent)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Cari Suku\nCadang Untuk\nKendaraanmu",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Cart with Items (Figma 4.3 - B)
                Text(
                    text = "Ada ${cartItems.size} produk di keranjang",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    cartItems.take(2).forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GearaImage(
                                imageUrl = item.product.imageUrl,
                                fallbackRes = item.product.imageRes,
                                contentDescription = item.product.name,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product.name,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = item.product.formattedPrice,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                            // Stepper
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .clickable { viewModel.updateQuantity(item.product.id, -1) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = null, tint = GearaOrange, modifier = Modifier.size(12.dp))
                                }
                                Text(
                                    text = " ${item.quantity} ",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .clickable { viewModel.updateQuantity(item.product.id, 1) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = GearaOrange, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                        Divider(color = Color.White.copy(alpha = 0.3f))
                    }

                    // Price Breakdown exactly from Figma 4.3 - B
                    Spacer(modifier = Modifier.height(10.dp))
                    DrawerPriceRow("Subtotal", "Rp32.000")
                    DrawerPriceRow("Pajak & Biaya", "Rp5.000")
                    DrawerPriceRow("Ongkir", "Rp3.000")
                    Divider(color = Color.White.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
                    DrawerPriceRow("Total", "Rp40.000", isBold = true)
                }

                // Checkout button
                Button(
                    onClick = onCheckout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GearaYellow)
                ) {
                    Text("Checkout", color = GearaDarkBrown, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DrawerPriceRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = if (isBold) 15.sp else 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = if (isBold) 15.sp else 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun FigmaNotificationDrawer(
    onClose: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(310.dp)
            .clip(RoundedCornerShape(topStart = 40.dp))
            .background(GearaOrange)
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = GearaOrange)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Notifikasi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = Color.White.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Exactly 4 items from Figma 4.2 - A
            DrawerNotifItem(
                icon = Icons.Default.GridView,
                text = "Suku cadang baru untuk kendaraan Anda.",
                onClick = { onNavigate("catalog") }
            )
            DrawerNotifItem(
                icon = Icons.Default.FavoriteBorder,
                text = "Suku cadang favorit Anda sedang promo.",
                onClick = { onNavigate("favorites") }
            )
            DrawerNotifItem(
                icon = Icons.Default.ShoppingBag,
                text = "Pesanan Anda telah sampai",
                onClick = { onNavigate("orders") }
            )
            DrawerNotifItem(
                icon = Icons.Default.TwoWheeler,
                text = "Kurir sedang dalam perjalanan",
                onClick = { onNavigate("live_tracking/GR-8842109") }
            )
        }
    }
}

@Composable
fun DrawerNotifItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = GearaOrange, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 17.sp,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Divider(color = Color.White.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
fun FigmaProfileDrawer(
    onClose: () -> Unit,
    onNavigate: (String) -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val userName by authViewModel.sessionManager.userName.collectAsState(initial = "Gibran Rakabuming")
    val userEmail by authViewModel.sessionManager.userEmail.collectAsState(initial = "gibran@geara.co.id")

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(310.dp)
            .clip(RoundedCornerShape(topStart = 40.dp))
            .background(GearaOrange)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header: User Name & Email dynamically loaded from SessionManager
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp, bottom = 20.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_profile_avatar),
                    contentDescription = null,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = userName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userEmail,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }

            // 8 Menu Items exactly from Figma 4.1 - A
            DrawerProfileMenuItem(icon = Icons.Default.ShoppingBag, title = "Pesanan Saya") { onNavigate("orders") }
            DrawerProfileMenuItem(icon = Icons.Default.Person, title = "Profil Saya") { onNavigate("profile") }
            DrawerProfileMenuItem(icon = Icons.Default.LocationOn, title = "Alamat Pengiriman") { onNavigate("checkout") }
            DrawerProfileMenuItem(icon = Icons.Default.CreditCard, title = "Metode Pembayaran") { onNavigate("checkout") }
            DrawerProfileMenuItem(icon = Icons.Default.Phone, title = "Hubungi Kami") { onNavigate("support") }
            DrawerProfileMenuItem(icon = Icons.Default.ChatBubbleOutline, title = "Bantuan & FAQ") { onNavigate("support") }
            DrawerProfileMenuItem(icon = Icons.Default.Settings, title = "Pengaturan") { onNavigate("profile") }
            DrawerProfileMenuItem(icon = Icons.Default.Logout, title = "Keluar") {
                authViewModel.logout {
                    onNavigate("login")
                }
            }
        }
    }
}

@Composable
fun DrawerProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = GearaOrange, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Divider(color = Color.White.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(10.dp))
    }
}
