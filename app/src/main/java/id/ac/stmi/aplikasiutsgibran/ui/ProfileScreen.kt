package id.ac.stmi.aplikasiutsgibran.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.ac.stmi.aplikasiutsgibran.R
import id.ac.stmi.aplikasiutsgibran.auth.AuthViewModel
import id.ac.stmi.aplikasiutsgibran.data.GearaViewModel
import id.ac.stmi.aplikasiutsgibran.ui.components.GearaBottomNavBar
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaDarkBrown
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaYellow
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: GearaViewModel? = null,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUserName by authViewModel.sessionManager.userName.collectAsState(initial = "Gibran Rakabuming")
    val currentUserEmail by authViewModel.sessionManager.userEmail.collectAsState(initial = "gibran@geara.co.id")
    val currentUserPhone by authViewModel.sessionManager.userPhone.collectAsState(initial = "0812-3456-7890")
    val scope = rememberCoroutineScope()

    var nama by remember(currentUserName) { mutableStateOf(currentUserName) }
    var tglLahir by remember { mutableStateOf("09 / 10 /1991") }
    var email by remember(currentUserEmail) { mutableStateOf(currentUserEmail) }
    var phone by remember(currentUserPhone) { mutableStateOf(currentUserPhone) }

    Scaffold(
        bottomBar = {
            GearaBottomNavBar(navController = navController, currentRoute = "home")
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
                    text = "Profil Saya",
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
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Avatar with Camera badge
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_profile_avatar),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(22.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .offset(x = 6.dp, y = 6.dp)
                                .size(32.dp)
                                .background(GearaOrange, CircleShape)
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Ubah Foto",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Field 1: Nama Lengkap
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Nama Lengkap",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GearaDarkBrown
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFF3E9B5))
                                .padding(horizontal = 18.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = nama,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = GearaDarkBrown
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 2: Tanggal Lahir
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Tanggal Lahir",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GearaDarkBrown
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFF3E9B5))
                                .padding(horizontal = 18.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = tglLahir,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = GearaDarkBrown
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 3: Email
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Email",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GearaDarkBrown
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFF3E9B5))
                                .padding(horizontal = 18.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = email,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = GearaDarkBrown
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 4: Nomor Telepon
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Nomor Telepon",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GearaDarkBrown
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFF3E9B5))
                                .padding(horizontal = 18.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = phone,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = GearaDarkBrown
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Simpan Profil Button
                    Button(
                        onClick = {
                            scope.launch {
                                authViewModel.sessionManager.saveSession(
                                    isLoggedIn = true,
                                    name = nama,
                                    email = email,
                                    phone = phone
                                )
                            }
                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .width(200.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GearaOrange),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = "Simpan Profil",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
