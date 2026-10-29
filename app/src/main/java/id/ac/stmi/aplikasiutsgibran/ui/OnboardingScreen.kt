package id.ac.stmi.aplikasiutsgibran.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val imageRes: Int,
    val iconRes: Int,
    val buttonText: String
)

@Composable
fun OnboardingScreen(navController: NavController) {
    val pages = remember {
        listOf(
            OnboardingPageData(
                title = "Cari Suku Cadang",
                subtitle = "Temukan suku cadang untuk kendaraan Anda.\nCek spesifikasi dan pilih produk yang sesuai.",
                imageRes = R.drawable.img_onboarding_1,
                iconRes = R.drawable.ic_onboarding_search,
                buttonText = "Lanjut"
            ),
            OnboardingPageData(
                title = "Pembayaran Mudah",
                subtitle = "Belanja suku cadang dengan pilihan pembayaran praktis dan ringkasan biaya yang jelas.",
                imageRes = R.drawable.img_onboarding_2,
                iconRes = R.drawable.ic_onboarding_payment,
                buttonText = "Lanjut"
            ),
            OnboardingPageData(
                title = "Pengiriman Andal",
                subtitle = "Pantau pengiriman dari gudang hingga tiba di alamat Anda.",
                imageRes = R.drawable.img_onboarding_3,
                iconRes = R.drawable.ic_onboarding_delivery,
                buttonText = "Mulai Sekarang"
            )
        )
    }

    var currentPage by remember { mutableIntStateOf(0) }
    val page = pages[currentPage]

    val brandOrange = Color(0xFFE95322)
    val indicatorInactive = Color(0xFFF3E9B5)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Bagian Atas: Foto Full-Width (Sesuai Desain Figma)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.15f)
            ) {
                AnimatedContent(
                    targetState = page.imageRes,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "OnboardingImage"
                ) { targetImage ->
                    Image(
                        painter = painterResource(id = targetImage),
                        contentDescription = page.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Tombol "Lewati" di Pojok Kanan Atas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 16.dp, end = 20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                navController.navigate("login")
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Lewati",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Bagian Bawah: Card Putih dengan Rounded Corners atas
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-20).dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color.White)
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon Fitur Oranye dari Desain Asli Figma
                Box(
                    modifier = Modifier
                        .size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = page.iconRes,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "OnboardingIcon"
                    ) { targetIcon ->
                        Icon(
                            painter = painterResource(id = targetIcon),
                            contentDescription = null,
                            tint = brandOrange,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Judul Utama (Merah/Oranye Bold)
                Text(
                    text = page.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = brandOrange,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subtitle / Deskripsi
                Text(
                    text = page.subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF555555),
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 42.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Page Indicator Bars (3 strip kapsul oranye & krem)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = index == currentPage
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(if (isSelected) 22.dp else 20.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) brandOrange else indicatorInactive)
                        )
                        if (index < pages.size - 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tombol Lanjut / Mulai Sekarang
                Button(
                    onClick = {
                        if (currentPage < pages.size - 1) {
                            currentPage++
                        } else {
                            navController.navigate("login")
                        }
                    },
                    modifier = Modifier
                        .width(180.dp)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                    shape = RoundedCornerShape(22.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = page.buttonText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
