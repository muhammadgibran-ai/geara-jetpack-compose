package id.ac.stmi.aplikasiutsgibran.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import id.ac.stmi.aplikasiutsgibran.R
import id.ac.stmi.aplikasiutsgibran.data.GearaViewModel
import id.ac.stmi.aplikasiutsgibran.data.Product
import id.ac.stmi.aplikasiutsgibran.ui.components.GearaBottomNavBar
import id.ac.stmi.aplikasiutsgibran.ui.components.GearaImage
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaDarkBrown
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaYellow

@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: GearaViewModel
) {
    val context = LocalContext.current
    val favoriteIds by viewModel.favoriteProductIds.collectAsState()
    val allProducts by viewModel.availableProducts.collectAsState()

    // Ambil daftar produk yang difavoritkan secara reaktif
    val favoriteProducts = remember(allProducts, favoriteIds) {
        allProducts.filter { favoriteIds.contains(it.id) }
    }

    Scaffold(
        bottomBar = { GearaBottomNavBar(navController, "favorites") },
        containerColor = GearaYellow
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header on Yellow Background
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = GearaOrange
                    )
                }
                Text(
                    text = "Favorit",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 48.dp),
                    textAlign = TextAlign.Center
                )
            }

            // White Body Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                if (favoriteProducts.isEmpty()) {
                    // Empty State bila belum ada produk favorit atau semua sudah di-unfavorit
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFDE8D7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = GearaOrange,
                                    modifier = Modifier.size(46.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Belum Ada Produk Favorit",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = GearaDarkBrown
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Simpan suku cadang yang Anda sukai dengan menekan ikon hati pada katalog produk.",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { navController.navigate("catalog") },
                                colors = ButtonDefaults.buttonColors(containerColor = GearaOrange),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(46.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Jelajahi Katalog", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Suku cadang favorit untuk kendaraan Anda (${favoriteProducts.size}).",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GearaOrange,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(favoriteProducts, key = { it.id }) { product ->
                                FavoriteProductCard(
                                    product = product,
                                    onUnfavorite = {
                                        viewModel.toggleFavorite(product.id)
                                        Toast.makeText(context, "${product.name} dihapus dari Favorit", Toast.LENGTH_SHORT).show()
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
fun FavoriteProductCard(
    product: Product,
    onUnfavorite: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Card Box exactly like Figma 9.3-A
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFE5EBEF))
        ) {
            // Image with Cloud Storage async loading & fallback
            GearaImage(
                imageUrl = product.imageUrl,
                fallbackRes = product.imageRes,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentScale = ContentScale.Fit
            )

            // Top Left Category Icon
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                when (product.category) {
                    "Rem" -> Icon(
                        painter = painterResource(id = R.drawable.ic_cat_rem),
                        contentDescription = null,
                        tint = GearaOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    "Filter" -> Icon(
                        painter = painterResource(id = R.drawable.ic_cat_filter),
                        contentDescription = null,
                        tint = GearaOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    "Mesin" -> Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = GearaOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    "Lampu" -> Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = GearaOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    else -> Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = GearaOrange,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Top Right Red Heart Icon (Tombol Unfavorit Interaktif)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onUnfavorite() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Hapus dari Favorit",
                    tint = GearaOrange,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = product.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = GearaOrange,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = product.formattedPrice,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = GearaDarkBrown,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Periksa kecocokan dengan kendaraan Anda.",
            fontSize = 11.sp,
            color = GearaDarkBrown.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
