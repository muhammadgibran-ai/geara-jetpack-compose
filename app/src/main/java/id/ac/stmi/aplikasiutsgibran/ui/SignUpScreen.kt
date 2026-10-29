package id.ac.stmi.aplikasiutsgibran.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import id.ac.stmi.aplikasiutsgibran.auth.AuthState
import id.ac.stmi.aplikasiutsgibran.auth.AuthViewModel
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaDarkBrown
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaYellow

@Composable
fun SignUpScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                navController.navigate("home") {
                    popUpTo("signup") { inclusive = true }
                }
                authViewModel.resetState()
            }
            is AuthState.Error -> {
                val msg = (authState as AuthState.Error).message
                snackbarHostState.showSnackbar(msg)
                authViewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = GearaYellow
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Yellow Bar
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
                    text = "Buat Akun",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 48.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

                // Main White Curved Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                // Field 1: Nama Lengkap
                SignUpField(
                    label = "Nama Lengkap",
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = "raka pratama"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Field 2: Kata Sandi
                Text(
                    text = "Kata Sandi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GearaDarkBrown
                )
                Spacer(modifier = Modifier.height(6.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("************", color = Color(0xFFA8A8A8)) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = GearaOrange
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF7E5C8),
                        unfocusedContainerColor = Color(0xFFF7E5C8),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = GearaDarkBrown,
                        unfocusedTextColor = GearaDarkBrown
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Field 3: Email
                SignUpField(label = "Email", value = email, onValueChange = { email = it }, placeholder = "example@example.com")

                Spacer(modifier = Modifier.height(14.dp))

                // Field 4: Nomor Ponsel
                SignUpField(label = "Nomor Ponsel", value = phone, onValueChange = { phone = it }, placeholder = "+62 812 3456 789")

                Spacer(modifier = Modifier.height(14.dp))

                // Field 5: Tanggal Lahir
                SignUpField(label = "Tanggal Lahir", value = birthDate, onValueChange = { birthDate = it }, placeholder = "DD / MM /YYY")

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Dengan melanjutkan, Anda menyetujui\nSyarat Penggunaan dan Kebijakan Privasi.",
                    fontSize = 11.sp,
                    color = GearaDarkBrown.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 15.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Tombol Daftar
                Button(
                    onClick = { 
                        var isValid = true
                        if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
                            isValid = false
                            // Di sini kita bisa memberikan pesan error menggunakan state lokal jika mau
                        }
                        if (password.length < 6) {
                            isValid = false
                        }
                        
                        if (isValid) {
                            authViewModel.register(
                                email = email,
                                pass = password,
                                fullName = fullName,
                                phone = phone
                            ) 
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (authState is AuthState.Loading) Color.Gray else GearaOrange
                    ),
                    shape = RoundedCornerShape(27.dp),
                    enabled = authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text(
                            text = "Daftar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))



                Spacer(modifier = Modifier.height(24.dp))

                // Sudah punya akun? Masuk
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sudah punya akun? ",
                        fontSize = 13.sp,
                        color = GearaDarkBrown
                    )
                    Text(
                        text = "Masuk",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaOrange,
                        modifier = Modifier.clickable { navController.navigate("login") }
                    )
                }
            }
        }
    }
}
}


@Composable
fun SignUpField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Text(
        text = label,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = GearaDarkBrown
    )
    Spacer(modifier = Modifier.height(6.dp))
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color(0xFFA8A8A8), fontSize = 14.sp) },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF7E5C8),
            unfocusedContainerColor = Color(0xFFF7E5C8),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = GearaDarkBrown,
            unfocusedTextColor = GearaDarkBrown
        ),
        singleLine = true
    )
}
