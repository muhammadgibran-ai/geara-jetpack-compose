package id.ac.stmi.aplikasiutsgibran.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.ac.stmi.aplikasiutsgibran.ui.components.GearaBottomNavBar
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaDarkBrown
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaYellow

@Composable
fun PaymentScreen(
    navController: NavController
) {
    Scaffold(
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
            // Header bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Back arrow
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

                // Title
                Text(
                    text = "Pembayaran",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
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
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    // Alamat Pengiriman
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "Alamat\nPengiriman",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GearaDarkBrown,
                            lineHeight = 22.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Alamat",
                            tint = GearaOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Address Pill Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFF3E9B5))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Jl. Merdeka No. 78, Bandung",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = GearaDarkBrown
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Ringkasan Pesanan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ringkasan\nPesanan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GearaDarkBrown,
                            lineHeight = 22.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFE2D2))
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Ubah",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GearaOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Oli Mesin 1L", fontSize = 13.sp, color = GearaDarkBrown)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Filter Udara", fontSize = 13.sp, color = GearaDarkBrown)
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        Column {
                            Text("2 produk", fontSize = 13.sp, color = GearaOrange)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("1 produk", fontSize = 13.sp, color = GearaOrange)
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = "Rp40.000",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GearaDarkBrown
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFFFD5C0), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Metode Pembayaran
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Metode\nPembayaran",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GearaDarkBrown,
                            lineHeight = 22.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFE2D2))
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Ubah",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GearaOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = "Kartu Kredit",
                            tint = GearaOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kartu Kredit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = GearaDarkBrown
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF3E9B5))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "*** *** *** 43 /00 /000",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = GearaDarkBrown
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFFFD5C0), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Waktu Pengiriman
                    Text(
                        text = "Waktu\nPengiriman",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaDarkBrown,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estimasi Tiba",
                            fontSize = 14.sp,
                            color = GearaDarkBrown
                        )
                        Text(
                            text = "1-3 hari",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GearaOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFFFD5C0), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(36.dp))

                    // Bayar Sekarang Button
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                navController.navigate("order_confirmed/ORD-20261129-01") {
                                    popUpTo("home")
                                }
                            },
                            modifier = Modifier
                                .width(200.dp)
                                .height(54.dp),
                            shape = RoundedCornerShape(27.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE2D2)),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = "Bayar\nSekarang",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GearaOrange,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
