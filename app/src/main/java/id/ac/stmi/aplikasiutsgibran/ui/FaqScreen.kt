package id.ac.stmi.aplikasiutsgibran.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaDarkBrown
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaYellow

data class FaqItem(
    val id: String,
    val category: String,
    val question: String,
    val answer: String
)

@Composable
fun FaqScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var expandedFaqId by remember { mutableStateOf<String?>(null) }

    val allFaqs = remember {
        listOf(
            FaqItem(
                id = "faq_1",
                category = "Keaslian & Garansi",
                question = "Bagaimana cara memastikan keaslian suku cadang Geara?",
                answer = "Setiap suku cadang resmi Geara memiliki label hologram dan kode QR pada kemasan. Anda dapat memindai kode QR melalui aplikasi untuk memverifikasi nomor seri produk."
            ),
            FaqItem(
                id = "faq_2",
                category = "Keaslian & Garansi",
                question = "Berapa lama masa garansi suku cadang?",
                answer = "Suku cadang Geara bergaransi 12 bulan atau 20.000 km sejak tanggal pembelian. Garansi mencakup kerusakan akibat cacat produksi pabrik."
            ),
            FaqItem(
                id = "faq_3",
                category = "Pengiriman",
                question = "Berapa lama estimasi pengiriman suku cadang?",
                answer = "Wilayah Jabodetabek: kurir instan tiba dalam 1-3 jam, pengiriman reguler membutuhkan 1-2 hari kerja. Pengiriman luar Jabodetabek berkisar 2-4 hari kerja."
            ),
            FaqItem(
                id = "faq_4",
                category = "Pengembalian",
                question = "Apakah barang bisa ditukar jika tidak cocok?",
                answer = "Bisa. Pengajuan penukaran atau pengembalian dana diterima dalam 7 hari setelah barang sampai, asalkan segel belum dibuka dan kemasan asli masih utuh."
            ),
            FaqItem(
                id = "faq_5",
                category = "Pemasangan",
                question = "Apakah bisa sekaligus dipasang di bengkel resmi?",
                answer = "Bisa. Pada halaman checkout, pilih opsi 'Pasang di Bengkel Rekanan' dan tentukan jadwal kunjungan ke bengkel rekanan terdekat."
            ),
            FaqItem(
                id = "faq_6",
                category = "Pembayaran",
                question = "Apa saja metode pembayaran yang didukung?",
                answer = "Tersedia pembayaran melalui QRIS, Virtual Account (BCA, Mandiri), serta dompet digital (GoPay, OVO, DANA)."
            )
        )
    }

    val filteredFaqs = allFaqs.filter {
        it.question.contains(searchQuery, ignoreCase = true) ||
        it.answer.contains(searchQuery, ignoreCase = true) ||
        it.category.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
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
                    text = "Bantuan & FAQ",
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
                        text = "Pertanyaan Umum",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaDarkBrown
                    )
                    Text(
                        text = "Jawaban seputar suku cadang, garansi, dan pengiriman barang.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari topik (misal: garansi, retur)...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = GearaOrange)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = Color.Gray)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = GearaOrange,
                            unfocusedBorderColor = Color(0xFFE5E5E5)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // FAQ Accordion List
                    filteredFaqs.forEach { faq ->
                        val isExpanded = expandedFaqId == faq.id
                        val rotationAngle by animateFloatAsState(
                            targetValue = if (isExpanded) 180f else 0f,
                            label = "faq_rotation"
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clickable {
                                    expandedFaqId = if (isExpanded) null else faq.id
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isExpanded) Color(0xFFFFF7ED) else Color.White
                            ),
                            border = if (isExpanded) {
                                androidx.compose.foundation.BorderStroke(1.2.dp, GearaOrange)
                            } else {
                                androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
                            },
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = faq.category,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GearaOrange
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = faq.question,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GearaDarkBrown,
                                            lineHeight = 18.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = if (isExpanded) GearaOrange else Color.DarkGray,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .rotate(rotationAngle)
                                    )
                                }

                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        HorizontalDivider(color = GearaOrange.copy(alpha = 0.2f), thickness = 0.8.dp)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = faq.answer,
                                            fontSize = 12.sp,
                                            color = Color(0xFF4A4A4A),
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Card Butuh Bantuan Lanjutan
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0EB)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GearaOrange.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Butuh bantuan lebih lanjut?",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GearaDarkBrown
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Hubungi tim layanan pelanggan Geara untuk pertanyaan lain seputar pesanan Anda.",
                                fontSize = 12.sp,
                                color = Color(0xFF555555),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { navController.navigate("contact_us") },
                                colors = ButtonDefaults.buttonColors(containerColor = GearaOrange),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Hubungi Kami", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

