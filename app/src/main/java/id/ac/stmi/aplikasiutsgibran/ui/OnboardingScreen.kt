package id.ac.stmi.aplikasiutsgibran.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.ac.stmi.aplikasiutsgibran.R
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaDarkBrown
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaYellow

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val imageRes: Int
)

@Composable
fun OnboardingScreen(navController: NavController) {
    val pages = listOf(
        OnboardingPageData(
            title = "Welcome to Geara",
            subtitle = "Pusat suku cadang resmi mobil Geara terlengkap, 100% original dengan dukungan CRM After-Sales terpadu.",
            imageRes = R.drawable.img_onboarding_1
        ),
        OnboardingPageData(
            title = "Pencarian Cepat & Tepat",
            subtitle = "Temukan suku cadang yang 100% kompatibel dengan tipe mobil Geara Anda (Terra SUV, Prime EV, City, Apex).",
            imageRes = R.drawable.img_onboarding_2
        ),
        OnboardingPageData(
            title = "Live Tracking & Garansi",
            subtitle = "Pantau kurir pengiriman secara real-time dan nikmati garansi resmi pabrikan mobil Geara.",
            imageRes = R.drawable.img_onboarding_3
        )
    )

    var currentPage by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GearaYellow),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Skip button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, end = 20.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { navController.navigate("login") }) {
                Text(
                    text = "Lewati",
                    color = GearaDarkBrown,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Onboarding Illustration Image
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = pages[currentPage].imageRes),
                contentDescription = pages[currentPage].title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Bottom sheet card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Indicator Dots
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                repeat(pages.size) { index ->
                    val isSelected = index == currentPage
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (isSelected) 28.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) GearaOrange else Color(0xFFD6D6D6))
                    )
                    if (index < pages.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Text(
                text = pages[currentPage].title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = GearaDarkBrown,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = pages[currentPage].subtitle,
                fontSize = 14.sp,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.height(60.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentPage > 0) {
                    Button(
                        onClick = { currentPage-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF0F0F0),
                            contentColor = GearaDarkBrown
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = "Kembali", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Button(
                    onClick = {
                        if (currentPage < pages.size - 1) {
                            currentPage++
                        } else {
                            navController.navigate("login")
                        }
                    },
                    modifier = Modifier
                        .weight(if (currentPage > 0) 1.5f else 1f)
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GearaOrange),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (currentPage == pages.size - 1) "Mulai Sekarang" else "Lanjut",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
