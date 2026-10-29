package id.ac.stmi.aplikasiutsgibran.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
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
                text = "Masuk",
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
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Selamat\nDatang",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = GearaDarkBrown,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Masuk untuk belanja suku cadang kendaraan Anda.",
                    fontSize = 13.sp,
                    color = GearaDarkBrown.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Field 1: Email atau Nomor Ponsel
                Text(
                    text = "Email atau Nomor Ponsel",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GearaDarkBrown
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = emailOrPhone,
                    onValueChange = { 
                        emailOrPhone = it
                        if (emailError != null) emailError = null
                    },
                    isError = emailError != null,
                    placeholder = { Text("example@example.com", color = Color(0xFFA8A8A8), fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF7E5C8),
                        unfocusedContainerColor = Color(0xFFF7E5C8),
                        errorContainerColor = Color(0xFFFFEBEE),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Red,
                        focusedTextColor = GearaDarkBrown,
                        unfocusedTextColor = GearaDarkBrown
                    ),
                    singleLine = true
                )
                if (emailError != null) {
                    Text(
                        text = emailError!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Field 2: Kata Sandi
                Text(
                    text = "Kata Sandi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GearaDarkBrown
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        if (passwordError != null) passwordError = null
                    },
                    isError = passwordError != null,
                    placeholder = { Text("************", color = Color(0xFFA8A8A8)) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = if (passwordError != null) Color.Red else GearaOrange
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
                        errorContainerColor = Color(0xFFFFEBEE),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Red,
                        focusedTextColor = GearaDarkBrown,
                        unfocusedTextColor = GearaDarkBrown
                    ),
                    singleLine = true
                )
                if (passwordError != null) {
                    Text(
                        text = passwordError!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                    )
                }

                // Lupa Kata Sandi?
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Lupa Kata Sandi?",
                        fontSize = 13.sp,
                        color = GearaOrange,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            forgotPasswordEmail = if (emailOrPhone.contains("@")) emailOrPhone.trim() else ""
                            showForgotPasswordDialog = true
                        }
                    )
                }

                if (showForgotPasswordDialog) {
                    AlertDialog(
                        onDismissRequest = { showForgotPasswordDialog = false },
                        title = {
                            Text("Reset Kata Sandi", fontWeight = FontWeight.Bold, color = GearaDarkBrown)
                        },
                        text = {
                            Column {
                                Text(
                                    "Masukkan alamat email akun Anda untuk menerima tautan resmi pemulihan kata sandi.",
                                    fontSize = 13.sp,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = forgotPasswordEmail,
                                    onValueChange = { forgotPasswordEmail = it },
                                    placeholder = { Text("contoh: nama@email.com") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (forgotPasswordEmail.isNotBlank()) {
                                        authViewModel.resetPassword(forgotPasswordEmail)
                                        showForgotPasswordDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GearaOrange)
                            ) {
                                Text("Kirim Tautan")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showForgotPasswordDialog = false }) {
                                Text("Batal", color = Color.Gray)
                            }
                        }
                    )
                }



                Spacer(modifier = Modifier.height(28.dp))

                // Tombol Masuk
                Button(
                    onClick = {
                        var isValid = true

                        // Validasi Email / Phone
                        if (emailOrPhone.isBlank()) {
                            emailError = "Email atau Nomor Ponsel wajib diisi"
                            isValid = false
                        } else if (emailOrPhone.contains("@") && !android.util.Patterns.EMAIL_ADDRESS.matcher(emailOrPhone).matches()) {
                            emailError = "Format email tidak valid"
                            isValid = false
                        } else if (!emailOrPhone.contains("@") && !emailOrPhone.all { it.isDigit() }) {
                            emailError = "Gunakan angka untuk nomor ponsel"
                            isValid = false
                        } else if (!emailOrPhone.contains("@") && emailOrPhone.length < 10) {
                            emailError = "Nomor ponsel minimal 10 angka"
                            isValid = false
                        }

                        // Validasi Kata Sandi
                        if (password.isBlank()) {
                            passwordError = "Kata sandi wajib diisi"
                            isValid = false
                        } else if (password.length < 6) {
                            passwordError = "Kata sandi minimal 6 karakter"
                            isValid = false
                        }

                        // Jika lolos validasi, navigasi ke Beranda
                        if (isValid) {
                            authViewModel.login(emailOrPhone, password)
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
                            text = "Masuk",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Belum punya akun? Daftar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Belum punya akun? ",
                        fontSize = 13.sp,
                        color = GearaDarkBrown
                    )
                    Text(
                        text = "Daftar",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GearaOrange,
                        modifier = Modifier.clickable { navController.navigate("signup") }
                    )
                }
            }
            }
        }
    }
}
