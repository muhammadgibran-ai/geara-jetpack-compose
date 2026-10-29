package id.ac.stmi.aplikasiutsgibran.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import kotlinx.coroutines.launch
import java.util.Calendar

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

    // Error states for user feedback
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var birthDateError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val authState by authViewModel.authState.collectAsState()

    // Native DatePickerDialog untuk kemudahan memilih tanggal lahir
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedDay = String.format("%02d", dayOfMonth)
                val formattedMonth = String.format("%02d", month + 1)
                birthDate = "$formattedDay/$formattedMonth/$year"
                birthDateError = null
            },
            calendar.get(Calendar.YEAR) - 18,
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                navController.navigate("home") {
                    popUpTo(0) { inclusive = true }
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
                IconButton(onClick = {
                    val prevRoute = navController.previousBackStackEntry?.destination?.route
                    if (prevRoute != null && prevRoute != "home" && prevRoute != "profile" && prevRoute != "launch") {
                        navController.popBackStack()
                    } else {
                        navController.navigate("login") {
                            popUpTo("signup") { inclusive = true }
                        }
                    }
                }) {
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
                        onValueChange = {
                            fullName = it
                            fullNameError = null
                        },
                        placeholder = "Nama Anda (contoh: Brnnboy)",
                        isError = fullNameError != null,
                        errorMessage = fullNameError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
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
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        placeholder = { Text("Minimal 6 karakter", color = Color(0xFFA8A8A8)) },
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
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
                    if (passwordError != null) {
                        Text(
                            text = passwordError ?: "",
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Field 3: Email
                    SignUpField(
                        label = "Email",
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        placeholder = "contoh: nama@mail.com",
                        isError = emailError != null,
                        errorMessage = emailError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Field 4: Nomor Ponsel (HANYA BOLEH ANGKA & TANDA '+')
                    SignUpField(
                        label = "Nomor Ponsel",
                        value = phone,
                        onValueChange = { input ->
                            // FILTER: Dilarang keras huruf, hanya terima digit angka dan optional '+' di awal
                            val filtered = input.filterIndexed { index, char ->
                                char.isDigit() || (index == 0 && char == '+')
                            }
                            if (filtered.length <= 15) {
                                phone = filtered
                                phoneError = null
                            }
                        },
                        placeholder = "081234567890",
                        isError = phoneError != null,
                        errorMessage = phoneError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Field 5: Tanggal Lahir (HANYA BOLEH ANGKA & FORMAT TANGGAL ATAU PILIH DARI KALENDER)
                    Text(
                        text = "Tanggal Lahir",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GearaDarkBrown
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = birthDate,
                        onValueChange = { input ->
                            // FILTER: Hanya angka yang boleh dimasukkan manual
                            val digitsOnly = input.filter { it.isDigit() }
                            if (digitsOnly.length <= 8) {
                                // Format otomatis menjadi DD/MM/YYYY
                                birthDate = when {
                                    digitsOnly.length > 4 -> "${digitsOnly.substring(0, 2)}/${digitsOnly.substring(2, 4)}/${digitsOnly.substring(4)}"
                                    digitsOnly.length > 2 -> "${digitsOnly.substring(0, 2)}/${digitsOnly.substring(2)}"
                                    else -> digitsOnly
                                }
                                birthDateError = null
                            }
                        },
                        placeholder = { Text("DD/MM/YYYY (contoh: 20/10/2006)", color = Color(0xFFA8A8A8), fontSize = 13.sp) },
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Pilih Tanggal Lahir",
                                    tint = GearaOrange
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
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
                    if (birthDateError != null) {
                        Text(
                            text = birthDateError ?: "",
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

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

                    // Tombol Daftar dengan Validasi Lengkap
                    Button(
                        onClick = {
                            var hasError = false

                            if (fullName.trim().isBlank()) {
                                fullNameError = "Nama lengkap wajib diisi."
                                hasError = true
                            } else {
                                fullNameError = null
                            }

                            if (password.length < 6) {
                                passwordError = if (password.isBlank()) {
                                    "Kata sandi wajib diisi."
                                } else {
                                    "Kata sandi minimal 6 karakter (saat ini baru ${password.length} karakter)."
                                }
                                hasError = true
                            } else {
                                passwordError = null
                            }

                            if (email.trim().isBlank() || !email.contains("@") || !email.contains(".")) {
                                emailError = "Format email tidak valid (contoh: nama@mail.com)."
                                hasError = true
                            } else {
                                emailError = null
                            }

                            val phoneDigits = phone.filter { it.isDigit() }
                            if (phoneDigits.length < 10) {
                                phoneError = "Nomor ponsel minimal 10 digit angka."
                                hasError = true
                            } else {
                                phoneError = null
                            }

                            if (birthDate.trim().isBlank()) {
                                birthDateError = "Tanggal lahir wajib diisi (contoh: 20/10/2006)."
                                hasError = true
                            } else {
                                birthDateError = null
                            }

                            if (hasError) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        when {
                                            password.length in 1..5 -> "Kata sandi minimal 6 karakter."
                                            phoneDigits.length < 10 -> "Nomor ponsel minimal 10 digit."
                                            fullName.isBlank() -> "Nama lengkap tidak boleh kosong."
                                            else -> "Silakan lengkapi semua data pendaftaran dengan benar."
                                        }
                                    )
                                }
                            } else {
                                authViewModel.register(
                                    email = email.trim(),
                                    pass = password,
                                    fullName = fullName.trim(),
                                    phone = phone.trim(),
                                    birthDate = birthDate.trim()
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

                    Spacer(modifier = Modifier.height(16.dp))
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
    placeholder: String,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
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
        keyboardOptions = keyboardOptions,
        isError = isError,
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
    if (errorMessage != null) {
        Text(
            text = errorMessage,
            color = Color(0xFFD32F2F),
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
        )
    }
}
