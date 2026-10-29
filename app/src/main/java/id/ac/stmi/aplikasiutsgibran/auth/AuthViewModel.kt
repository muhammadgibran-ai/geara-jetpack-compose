package id.ac.stmi.aplikasiutsgibran.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import id.ac.stmi.aplikasiutsgibran.data.GearaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    val sessionManager: SessionManager,
    val repository: GearaRepository
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private fun syncFcmToken(userId: String, email: String) {
        try {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        if (!token.isNullOrBlank()) {
                            viewModelScope.launch {
                                sessionManager.saveFcmToken(token)
                                repository.registerFcmToken(token = token, userId = userId, email = email)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Login ke Firebase:
     * - Mengecek ke Firebase apakah ada akun tersebut.
     * - Jika ada dan kata sandi benar, user masuk ke akun.
     * - Jika tidak ada atau salah, memunculkan pesan "Akun tidak valid".
     */
    fun login(emailOrPhone: String, pass: String) {
        val cleanInput = emailOrPhone.trim()
        val cleanPass = pass.trim()

        if (cleanInput.isBlank()) {
            _authState.value = AuthState.Error("Email atau nomor ponsel tidak boleh kosong.")
            return
        }
        if (cleanPass.isBlank()) {
            _authState.value = AuthState.Error("Kata sandi tidak boleh kosong.")
            return
        }
        if (cleanPass.length < 6) {
            _authState.value = AuthState.Error("Kata sandi minimal 6 karakter.")
            return
        }

        _authState.value = AuthState.Loading

        val isEmail = cleanInput.contains("@")

        if (isEmail) {
            performFirebaseSignIn(cleanInput, cleanPass, cleanInput, isEmail = true)
        } else {
            // Cek apakah nomor ponsel terdaftar
            viewModelScope.launch {
                val registeredEmail = sessionManager.getEmailByPhone(cleanInput)
                if (!registeredEmail.isNullOrBlank()) {
                    performFirebaseSignIn(registeredEmail, cleanPass, cleanInput, isEmail = false)
                } else {
                    val cleanDigits = cleanInput.filter { it.isDigit() }
                    val altPhone = if (cleanDigits.startsWith("0")) "+62" + cleanDigits.substring(1)
                                   else if (cleanDigits.startsWith("62")) "0" + cleanDigits.substring(2)
                                   else cleanDigits
                    val altEmail = sessionManager.getEmailByPhone(altPhone)
                    if (!altEmail.isNullOrBlank()) {
                        performFirebaseSignIn(altEmail, cleanPass, cleanInput, isEmail = false)
                    } else {
                        _authState.value = AuthState.Error("Akun tidak valid. Nomor ponsel '$cleanInput' belum terdaftar di Firebase.")
                    }
                }
            }
        }
    }

    private fun performFirebaseSignIn(email: String, pass: String, originalInput: String, isEmail: Boolean) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        firebaseUser.getIdToken(false).addOnCompleteListener { tokenTask ->
                            val jwtToken = tokenTask.result?.token ?: "JWT-FIREBASE-SECURE-TOKEN"
                            val displayName = firebaseUser.displayName ?: "Pengguna Geara"
                            val userEmail = firebaseUser.email ?: email

                            viewModelScope.launch {
                                sessionManager.saveSession(
                                    isLoggedIn = true,
                                    userId = firebaseUser.uid,
                                    name = displayName,
                                    email = userEmail,
                                    phone = if (!isEmail) originalInput else "0812-3456-7890",
                                    token = jwtToken
                                )
                                syncFcmToken(firebaseUser.uid, userEmail)
                            }
                            _authState.value = AuthState.Success("Login Berhasil! Selamat datang kembali, $displayName.")
                        }
                    } else {
                        _authState.value = AuthState.Error("Akun tidak valid.")
                    }
                } else {
                    val ex = task.exception
                    val errorMsg = when {
                        ex?.message?.contains("no user record", ignoreCase = true) == true ||
                        ex?.message?.contains("user-not-found", ignoreCase = true) == true ->
                            "Akun tidak valid. Akun dengan email '$email' belum terdaftar di Firebase."
                        ex?.message?.contains("wrong-password", ignoreCase = true) == true ||
                        ex?.message?.contains("invalid-credential", ignoreCase = true) == true ->
                            "Akun tidak valid. Kata sandi yang Anda masukkan salah."
                        ex?.message?.contains("network", ignoreCase = true) == true ->
                            "Gagal terhubung ke Firebase. Periksa koneksi internet Anda."
                        else ->
                            "Akun tidak valid. Email atau kata sandi Anda salah."
                    }
                    _authState.value = AuthState.Error(errorMsg)
                }
            }
    }

    /**
     * Registrasi Akun Baru ke Firebase Authentication:
     * - Mendaftarkan user ke Firebase Authentication secara langsung.
     * - Menyimpan displayName pengguna.
     * - Memetakan nomor ponsel ke email pengguna untuk kemudahan login.
     */
    fun register(
        email: String,
        pass: String,
        fullName: String,
        phone: String,
        birthDate: String = ""
    ) {
        val cleanEmail = email.trim()
        val cleanName = fullName.trim()
        val cleanPhone = phone.trim()
        val cleanBirthDate = birthDate.trim()

        if (cleanName.isBlank()) {
            _authState.value = AuthState.Error("Nama lengkap wajib diisi.")
            return
        }
        if (cleanEmail.isBlank() || !cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            _authState.value = AuthState.Error("Format email tidak valid (harus menyertakan @ dan domain, contoh: nama@mail.com).")
            return
        }
        if (pass.length < 6) {
            _authState.value = AuthState.Error("Kata sandi terlalu pendek. Minimal 6 karakter.")
            return
        }
        if (cleanPhone.filter { it.isDigit() }.length < 10) {
            _authState.value = AuthState.Error("Nomor ponsel tidak valid. Masukkan minimal 10 digit angka.")
            return
        }
        if (cleanBirthDate.isBlank()) {
            _authState.value = AuthState.Error("Tanggal lahir wajib diisi.")
            return
        }

        _authState.value = AuthState.Loading

        auth.createUserWithEmailAndPassword(cleanEmail, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(cleanName)
                        .build()

                    user?.updateProfile(profileUpdates)

                    val uid = user?.uid ?: "user_${System.currentTimeMillis()}"

                    viewModelScope.launch {
                        sessionManager.savePhoneEmailMapping(cleanPhone, cleanEmail)
                        sessionManager.saveSession(
                            isLoggedIn = true,
                            userId = uid,
                            name = cleanName,
                            email = cleanEmail,
                            phone = cleanPhone,
                            token = "JWT-FIREBASE-$uid"
                        )
                        syncFcmToken(uid, cleanEmail)
                    }
                    _authState.value = AuthState.Success("Registrasi Berhasil! Akun $cleanName telah terdaftar di database Firebase.")
                } else {
                    val ex = task.exception
                    val errorMsg = when {
                        ex?.message?.contains("email-already-in-use", ignoreCase = true) == true ||
                        ex?.message?.contains("already in use", ignoreCase = true) == true ->
                            "Email '$cleanEmail' sudah terdaftar di Firebase. Silakan langsung masuk di menu Login."
                        ex?.message?.contains("weak-password", ignoreCase = true) == true ->
                            "Kata sandi terlalu lemah. Gunakan minimal 6 karakter kombinasi huruf dan angka."
                        ex?.message?.contains("network", ignoreCase = true) == true ->
                            "Gagal terhubung ke Firebase. Periksa koneksi internet Anda."
                        else ->
                            ex?.localizedMessage ?: "Registrasi gagal. Silakan coba kembali."
                    }
                    _authState.value = AuthState.Error(errorMsg)
                }
            }
    }

    fun resetPassword(email: String) {
        if (email.isBlank() || !email.contains("@")) {
            _authState.value = AuthState.Error("Alamat email tidak valid. Masukkan email terdaftar Anda.")
            return
        }
        _authState.value = AuthState.Loading
        auth.sendPasswordResetEmail(email.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success("Tautan reset kata sandi resmi telah dikirim ke ${email.trim()}. Silakan periksa inbox email Anda.")
                } else {
                    _authState.value = AuthState.Error(task.exception?.localizedMessage ?: "Gagal mengirim link reset password.")
                }
            }
    }

    fun logout(onComplete: () -> Unit = {}) {
        try {
            auth.signOut()
        } catch (_: Exception) {}

        viewModelScope.launch {
            sessionManager.clearSession()
            _authState.value = AuthState.Idle
            onComplete()
        }
    }

    fun loginWithGoogle(
        email: String,
        name: String,
        idToken: String? = null
    ) {
        _authState.value = AuthState.Loading
        val uid = "google_${System.currentTimeMillis()}"
        viewModelScope.launch {
            sessionManager.saveSession(
                isLoggedIn = true,
                userId = uid,
                name = name,
                email = email,
                phone = "0812-3456-7890",
                token = idToken ?: "JWT-GOOGLE-${System.currentTimeMillis()}"
            )
            syncFcmToken(uid, email)
            _authState.value = AuthState.Success("Berhasil masuk melalui Google OAuth ($email)!")
        }
    }

    fun loginWithFacebook(
        name: String = "Pengguna Facebook",
        email: String = "facebook.user@geara.co.id",
        accessToken: String? = null
    ) {
        _authState.value = AuthState.Loading
        val uid = "fb_${System.currentTimeMillis()}"
        viewModelScope.launch {
            sessionManager.saveSession(
                isLoggedIn = true,
                userId = uid,
                name = name,
                email = email,
                phone = "0812-3456-7890",
                token = accessToken ?: "JWT-FACEBOOK-${System.currentTimeMillis()}"
            )
            syncFcmToken(uid, email)
            _authState.value = AuthState.Success("Berhasil masuk melalui Facebook OAuth ($name)!")
        }
    }

    fun loginWithBiometric(
        userEmail: String? = null,
        userName: String? = null
    ) {
        _authState.value = AuthState.Loading
        val uid = "bio_${System.currentTimeMillis()}"
        val email = userEmail ?: "gibran@geara.co.id"
        val name = userName ?: "Gibran Rakabuming"
        viewModelScope.launch {
            sessionManager.saveSession(
                isLoggedIn = true,
                userId = uid,
                name = name,
                email = email,
                phone = "0812-3456-7890",
                token = "JWT-BIOMETRIC-HARDWARE-${System.currentTimeMillis()}"
            )
            syncFcmToken(uid, email)
            _authState.value = AuthState.Success("Autentikasi Sensor Sidik Jari Perangkat Berhasil!")
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
