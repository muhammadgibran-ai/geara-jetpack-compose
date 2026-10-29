package id.ac.stmi.aplikasiutsgibran.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.ac.stmi.aplikasiutsgibran.R
import id.ac.stmi.aplikasiutsgibran.data.GearaRepository
import id.ac.stmi.aplikasiutsgibran.data.GearaViewModel
import id.ac.stmi.aplikasiutsgibran.ui.components.GearaBottomNavBar
import id.ac.stmi.aplikasiutsgibran.ui.components.GearaImage
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaDarkBrown
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaYellow

@Composable
fun CatalogScreen(
    navController: NavController,
    viewModel: GearaViewModel
) {
    val context = LocalContext.current
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val allProducts by viewModel.availableProducts.collectAsState()
    val favoriteIds by viewModel.favoriteProductIds.collectAsState()
    val cartCount = viewModel.cartItems.collectAsState().value.sumOf { it.quantity }

    val categories = listOf("Rem", "Mesin", "Filter", "Lampu", "Oli")

    // Android Hardware & Gesture Back Handler: Kembali ke Home dengan aman & bersihkan filter
    BackHandler {
        viewModel.setSelectedCategory("Semua")
        val popped = navController.popBackStack("home", inclusive = false)
        if (!popped) {
            navController.navigate("home") { launchSingleTop = true }
        }
    }

    // State scroll list dan auto-reset ke item teratas saat kategori diubah
    val listState = rememberLazyListState()
    LaunchedEffect(selectedCategory) {
        listState.scrollToItem(0)
    }

    // Filter produk dinamis berdasarkan kategori yang dipilih pengguna
    val filteredProducts = remember(allProducts, selectedCategory) {
        if (selectedCategory.isBlank() || selectedCategory.equals("Semua", ignoreCase = true)) {
            allProducts
        } else {
            allProducts.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    Scaffold(
        bottomBar = { GearaBottomNavBar(navController, "catalog") },
        containerColor = GearaYellow
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header: Search pill, back button, and action icons
            Column(
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tombol Navigasi Kembali ke Home
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable {
                                viewModel.setSelectedCategory("Semua")
                                val popped = navController.popBackStack("home", inclusive = false)
                                if (!popped) {
                                    navController.navigate("home") { launchSingleTop = true }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Beranda",
                            tint = GearaOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }

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
                            Text(text = "Cari", color = Color(0xFFA8A8A8), fontSize = 14.sp)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(GearaOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    HeaderCircleButton(icon = Icons.Default.ShoppingCart, badgeCount = cartCount) {
                        navController.navigate("cart")
                    }
                    HeaderCircleButton(icon = Icons.Default.Notifications) {
                        navController.navigate("notifications")
                    }
                    HeaderCircleButton(icon = Icons.Default.Person) {
                        navController.navigate("profile")
                    }
                }
            }

            // Category Bar on Orange background (matching Figma 9.2-A / Rem.png)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GearaOrange)
                    .padding(top = 10.dp, start = 12.dp, end = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                .background(if (isSelected) Color.White else Color.Transparent)
                                .clickable {
                                    if (isSelected) {
                                        viewModel.setSelectedCategory("Semua")
                                    } else {
                                        viewModel.setSelectedCategory(cat)
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) GearaYellow else Color(0xFFF7E5C8)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (cat) {
                                        "Rem" -> Icon(
                                            painter = painterResource(id = R.drawable.ic_cat_rem),
                                            contentDescription = cat,
                                            tint = GearaOrange,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        "Filter" -> Icon(
                                            painter = painterResource(id = R.drawable.ic_cat_filter),
                                            contentDescription = cat,
                                            tint = GearaOrange,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        "Mesin" -> Icon(
                                            imageVector = Icons.Default.Build,
                                            contentDescription = cat,
                                            tint = GearaOrange,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        "Lampu" -> Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = cat,
                                            tint = GearaOrange,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        else -> Icon(
                                            imageVector = Icons.Default.WaterDrop,
                                            contentDescription = cat,
                                            tint = GearaOrange,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) GearaDarkBrown else Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Products List Container on White background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    // "Urutkan Populer" & Kategori Info Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Kategori: ", fontSize = 13.sp, color = GearaDarkBrown)
                            Text(
                                text = if (selectedCategory.isBlank() || selectedCategory.equals("Semua", ignoreCase = true)) "Semua Produk" else selectedCategory,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GearaOrange
                            )
                            Text(
                                text = " • ${filteredProducts.size} item",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        if (selectedCategory.isNotBlank() && !selectedCategory.equals("Semua", ignoreCase = true)) {
                            Text(
                                text = "Reset Filter",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GearaOrange,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setSelectedCategory("Semua") }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(GearaOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dynamic list of items based on filteredProducts
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (filteredProducts.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = Color.LightGray
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Belum ada produk untuk kategori $selectedCategory",
                                            fontSize = 14.sp,
                                            color = GearaDarkBrown.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredProducts, key = { it.id }) { product ->
                                FigmaKatalogCard(
                                    title = product.name,
                                    rating = "%.1f".format(product.rating),
                                    price = "Rp" + "%,d".format(product.price).replace(',', '.'),
                                    description = product.description,
                                    imageRes = product.imageRes,
                                    imageUrl = product.imageUrl,
                                    isFavorite = favoriteIds.contains(product.id),
                                    onFavoriteClick = {
                                        val isFav = viewModel.toggleFavorite(product.id)
                                        Toast.makeText(
                                            context,
                                            if (isFav) "${product.name} ditambahkan ke Favorit ❤️" else "${product.name} dihapus dari Favorit",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onClick = { navController.navigate("product_detail/${product.id}") }
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
fun FigmaKatalogCard(
    title: String,
    rating: String,
    price: String,
    description: String,
    imageRes: Int,
    imageUrl: String? = null,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        // Large rounded image with grey background & Favorite button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE5EBEF)),
            contentAlignment = Alignment.Center
        ) {
            if (imageRes != 0) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                GearaImage(
                    imageUrl = imageUrl,
                    fallbackRes = imageRes,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Tombol Favorit Interaktif di Pojok Kanan Atas Gambar Produk
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.92f))
                    .clickable { onFavoriteClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Hapus dari Favorit" else "Tambah ke Favorit",
                    tint = if (isFavorite) GearaOrange else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Title with Orange Rating Pill & Price on Right (Anti-Clipped Layout)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kolom Judul & Badge Rating dengan Modifier.weight(1f)
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = GearaDarkBrown,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = " • ",
                    fontSize = 15.sp,
                    color = GearaOrange,
                    fontWeight = FontWeight.Bold
                )
                // Badge Rating: Tidak menggunakan weight agar ukurannya paten & tidak pernah terpotong
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GearaOrange)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = rating,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = GearaYellow,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = price,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = GearaOrange,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            fontSize = 13.sp,
            color = Color(0xFF757575),
            lineHeight = 18.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color(0xFFF0F0F0))
    }
}
