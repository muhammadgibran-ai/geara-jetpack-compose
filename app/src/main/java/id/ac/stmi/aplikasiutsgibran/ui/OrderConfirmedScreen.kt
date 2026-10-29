package id.ac.stmi.aplikasiutsgibran.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
fun OrderConfirmedScreen(
    orderId: String,
    navController: NavController
) {
    Scaffold(
        bottomBar = {
            GearaBottomNavBar(navController = navController, currentRoute = "orders")
        },
        containerColor = GearaYellow
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top back arrow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "<",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = GearaOrange,
                    modifier = Modifier
                        .clickable {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(0.7f))

            // Large Circular Timer/Status Icon from Figma
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .border(width = 5.dp, color = GearaOrange, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Inner dot indicator
                Box(
                    modifier = Modifier
                        .offset(x = (-18).dp, y = (-8).dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(GearaOrange)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Title
            Text(
                text = "Pesanan Dikonfirmasi!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = GearaDarkBrown,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle
            Text(
                text = "Pesanan suku cadang berhasil\ndibuat",
                fontSize = 16.sp,
                color = GearaDarkBrown,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Estimated Arrival
            Text(
                text = "Estimasi tiba 29 Nov, pukul 16.00",
                fontSize = 15.sp,
                color = GearaDarkBrown,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Lacak Pesanan Link
            Text(
                text = "Lacak Pesanan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GearaOrange,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clickable {
                        navController.navigate("live_tracking/$orderId")
                    }
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Support Prompt
            Text(
                text = "Butuh bantuan? Hubungi tim layanan\npelanggan Geara.",
                fontSize = 14.sp,
                color = GearaDarkBrown,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier
                    .clickable {
                        navController.navigate("support")
                    }
                    .padding(bottom = 20.dp)
            )
        }
    }
}
