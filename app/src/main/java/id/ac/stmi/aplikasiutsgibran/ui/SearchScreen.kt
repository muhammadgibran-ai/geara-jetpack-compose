package id.ac.stmi.aplikasiutsgibran.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.ac.stmi.aplikasiutsgibran.R
import id.ac.stmi.aplikasiutsgibran.data.GearaViewModel
import id.ac.stmi.aplikasiutsgibran.ui.components.GearaBottomNavBar
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaDarkBrown
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaYellow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: GearaViewModel? = null
) {
    val initialCat = viewModel?.selectedCategory?.collectAsState()?.value?.let {
        if (it.isBlank() || it.equals("Semua", ignoreCase = true)) "Rem" else it
    } ?: "Rem"

    var selectedCategory by remember { mutableStateOf(initialCat) }
    var selectedSort by remember { mutableStateOf("Rating Tertinggi") }
    var minRating by remember { mutableIntStateOf(4) }
    var selectedSubcategory by remember { mutableStateOf<String?>(null) }
    var priceSliderPosition by remember { mutableFloatStateOf(0.5f) }

    val categories = listOf("Rem", "Mesin", "Filter", "Lampu", "Oli")
    val sortOptions = listOf("Rating Tertinggi", "Paling Populer", "Harga Terendah", "Harga Tertinggi")

    val subcategoriesMap = mapOf(
        "Rem" to listOf("Semua Rem", "Kampas Depan", "Kampas Tromol", "Piringan Cakram", "Kaliper 4-Piston", "Minyak Rem DOT4", "Master Rem", "Selang Braided", "Sensor ABS"),
        "Mesin" to listOf("Semua Mesin", "Busi Iridium", "Timing Belt", "Piston Kit", "Fan Belt", "Gasket Silinder", "Koil Pengapian", "Pompa Bensin", "Sensor O2"),
        "Filter" to listOf("Semua Filter", "Filter Oli", "Filter Udara", "Filter Bensin", "Filter AC Kabin", "Filter Transmisi", "Filter Solar", "Kasa Tangki"),
        "Lampu" to listOf("Semua Lampu", "Foglamp Bi-Beam", "Bohlam Stop Rem", "Lampu Mundur", "DRL Strip", "Lampu Plafon", "Lampu Senja", "Bohlam Sein", "Lampu Plat"),
        "Oli" to listOf("Semua Oli", "Oli Full Synthetic", "Oli Transmisi CVT", "Minyak Rem", "Radiator Coolant", "Engine Flush", "Cleaner Injektor", "Air Wiper")
    )

    val currentSubcategories = subcategoriesMap[selectedCategory] ?: listOf("Semua $selectedCategory")

    val maxPrice = (25000 + (priceSliderPosition * 1975000)).toInt()
    val formattedMaxPrice = "Rp " + "%,d".format(maxPrice).replace(',', '.')

    val cartCount = viewModel?.cartItems?.collectAsState()?.value?.sumOf { it.quantity } ?: 0

    Scaffold(
        bottomBar = {
            Surface(
                color = GearaYellow,
                modifier = Modifier.fillMaxWidth()
            ) {
                GearaBottomNavBar(navController = navController, currentRoute = "catalog")
            }
        },
        containerColor = GearaYellow
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button in white circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            val popped = navController.popBackStack()
                            if (!popped) {
                                navController.navigate("catalog") { launchSingleTop = true }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = GearaOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Title centered
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Saring & Filter",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaDarkBrown
                    )
                    Text(
                        text = "Katalog Suku Cadang Geara",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = GearaDarkBrown.copy(alpha = 0.75f)
                    )
                }

                // Cart & Profile top actions
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { navController.navigate("cart") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Keranjang",
                            tint = GearaOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        if (cartCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = (-2).dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(GearaOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (cartCount > 9) "9+" else "$cartCount",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { navController.navigate("profile") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil",
                            tint = GearaOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // White content sheet with curved top corners
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                color = Color(0xFFF9F9FB),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    // Section 1: Kategori Utama
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kategori Utama",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GearaDarkBrown
                        )
                        Text(
                            text = "Pilih kategori",
                            fontSize = 12.sp,
                            color = Color(0xFF8E8E93)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5 Category circles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        categories.forEach { cat ->
                            val isSelected = cat == selectedCategory
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        selectedCategory = cat
                                        selectedSubcategory = null
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) GearaOrange else Color(0xFFFFF0E6)
                                        )
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(2.dp, GearaYellow, CircleShape)
                                            } else {
                                                Modifier
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val iconTint = if (isSelected) Color.White else GearaOrange
                                    when (cat) {
                                        "Rem" -> Icon(
                                            painter = painterResource(id = R.drawable.ic_cat_rem),
                                            contentDescription = cat,
                                            tint = iconTint,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        "Filter" -> Icon(
                                            painter = painterResource(id = R.drawable.ic_cat_filter),
                                            contentDescription = cat,
                                            tint = iconTint,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        "Mesin" -> Icon(
                                            imageVector = Icons.Default.Build,
                                            contentDescription = cat,
                                            tint = iconTint,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        "Lampu" -> Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = cat,
                                            tint = iconTint,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        else -> Icon(
                                            imageVector = Icons.Default.WaterDrop,
                                            contentDescription = cat,
                                            tint = iconTint,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) GearaOrange else GearaDarkBrown
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = Color(0xFFFFE2D2), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(18.dp))

                    // Section 2: Urutkan & Rating (Bebas dari bug "Urut\nkan")
                    Text(
                        text = "Urutkan Berdasarkan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaDarkBrown
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        sortOptions.forEach { opt ->
                            val isSelected = opt == selectedSort
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) GearaOrange else Color.White
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) GearaOrange else Color(0xFFFFD5C0),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedSort = opt }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = opt,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else GearaDarkBrown
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Rating Filter bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFFFE0D0), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Filter Rating",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GearaDarkBrown
                            )
                            Text(
                                text = if (minRating > 0) "$minRating.0★ ke atas" else "Semua Rating",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GearaOrange
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            (1..5).forEach { star ->
                                Icon(
                                    imageVector = if (star <= minRating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Bintang $star",
                                    tint = GearaOrange,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            minRating = if (minRating == star) 0 else star
                                        }
                                        .padding(1.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = Color(0xFFFFE2D2), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(18.dp))

                    // Section 3: Subkategori spesifik per kategori
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Subkategori $selectedCategory",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GearaDarkBrown
                        )
                        if (selectedSubcategory != null) {
                            Text(
                                text = "Atur Ulang",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GearaOrange,
                                modifier = Modifier.clickable { selectedSubcategory = null }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pilih jenis suku cadang spesifik untuk $selectedCategory",
                        fontSize = 12.sp,
                        color = Color(0xFF8E8E93)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        currentSubcategories.forEach { sub ->
                            val isDefaultAll = sub.startsWith("Semua")
                            val isSelected = if (selectedSubcategory == null) {
                                isDefaultAll
                            } else {
                                selectedSubcategory == sub
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) GearaOrange else Color.White
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) GearaOrange else Color(0xFFFFD5C0),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        selectedSubcategory = if (isDefaultAll) null else sub
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = sub,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else GearaDarkBrown
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = Color(0xFFFFE2D2), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(18.dp))

                    // Section 4: Rentang Harga (Bebas dari bug "Harg\na" dan "Rp100\n000 +")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rentang Harga",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GearaDarkBrown
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF0E6))
                                .border(1.dp, GearaOrange.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Maks. $formattedMaxPrice",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GearaOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = priceSliderPosition,
                        onValueChange = { priceSliderPosition = it },
                        colors = SliderDefaults.colors(
                            thumbColor = GearaOrange,
                            activeTrackColor = GearaOrange,
                            inactiveTrackColor = Color(0xFFE2E4E8)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Rp25 rb", fontSize = 11.sp, color = Color(0xFF8E8E93), fontWeight = FontWeight.Medium)
                        Text("Rp250 rb", fontSize = 11.sp, color = Color(0xFF8E8E93), fontWeight = FontWeight.Medium)
                        Text("Rp750 rb", fontSize = 11.sp, color = Color(0xFF8E8E93), fontWeight = FontWeight.Medium)
                        Text("Rp2 jt+", fontSize = 11.sp, color = Color(0xFF8E8E93), fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Section 5: Action buttons (Atur Ulang & Terapkan)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedCategory = "Rem"
                                selectedSubcategory = null
                                selectedSort = "Rating Tertinggi"
                                minRating = 4
                                priceSliderPosition = 0.5f
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.5.dp, GearaOrange),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = GearaOrange
                            )
                        ) {
                            Text(
                                text = "Atur Ulang",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GearaOrange
                            )
                        }

                        Button(
                            onClick = {
                                viewModel?.setSelectedCategory(selectedCategory)
                                val popped = navController.popBackStack()
                                if (!popped) {
                                    navController.navigate("catalog") { launchSingleTop = true }
                                }
                            },
                            modifier = Modifier
                                .weight(2f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GearaOrange),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                text = "Terapkan Filter",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
