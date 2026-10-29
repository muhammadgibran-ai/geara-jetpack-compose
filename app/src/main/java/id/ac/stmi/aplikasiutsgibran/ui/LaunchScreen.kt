package id.ac.stmi.aplikasiutsgibran.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.ac.stmi.aplikasiutsgibran.R
import id.ac.stmi.aplikasiutsgibran.auth.AuthViewModel
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaDarkBrown
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun LaunchScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var showWelcomeScreen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1200)
        val isLoggedIn = try {
            authViewModel.sessionManager.isLoggedIn.first()
        } catch (_: Exception) {
            false
        }

        if (isLoggedIn) {
            navController.navigate("home") {
                popUpTo("launch") { inclusive = true }
            }
        } else {
            showWelcomeScreen = true
        }
    }

    if (!showWelcomeScreen) {
        // Geara 0-A: Launch First Screen (Yellow Background)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GearaYellow),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_geara_logo),
                contentDescription = "GEARA Logo",
                modifier = Modifier.size(220.dp)
            )
        }
    } else {
        // Geara 0-B: Launch Welcome Screen (Orange Background with Masuk & Daftar buttons)
        AnimatedVisibility(
            visible = true,
            enter = fadeIn()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GearaOrange)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Image(
                    painter = painterResource(id = R.drawable.ic_geara_logo_yellow),
                    contentDescription = "GEARA Logo",
                    modifier = Modifier.size(240.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Suku cadang tepat, perjalanan lebih mantap.",
                    fontSize = 15.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.weight(1f))

                // Tombol Masuk
                Button(
                    onClick = { navController.navigate("login") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GearaYellow),
                    shape = RoundedCornerShape(27.dp)
                ) {
                    Text(
                        text = "Masuk",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaDarkBrown
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tombol Daftar
                Button(
                    onClick = { navController.navigate("signup") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF7E5C8)),
                    shape = RoundedCornerShape(27.dp)
                ) {
                    Text(
                        text = "Daftar",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaDarkBrown
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Lewati ke Onboarding jika ingin melihat tur
                androidx.compose.material3.TextButton(
                    onClick = { navController.navigate("onboarding") }
                ) {
                    Text(
                        text = "Lihat Panduan Aplikasi >",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
