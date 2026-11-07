package id.ac.stmi.aplikasiutsgibran.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.ac.stmi.aplikasiutsgibran.data.GearaRepository
import id.ac.stmi.aplikasiutsgibran.data.remote.FirestoreSyncService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
    data class AccountAlreadyExists(val email: String, val name: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    val sessionManager: SessionManager,
    val repository: GearaRepository,
    val firestoreSyncService: FirestoreSyncService,
    @ApplicationContext private val context: Context
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
                                val phoneNum = if (!isEmail) originalInput else "0812-3456-7890"
                                sessionManager.saveSession(
                                    isLoggedIn = true,
                                    userId = firebaseUser.uid,
                                    name = displayName,
                                    email = userEmail,
                                    phone = phoneNum,
                                    token = jwtToken
                                )
                                firestoreSyncService.syncUserProfile(
                                    userId = firebaseUser.uid,
                                    name = displayName,
                                    email = userEmail,
                                    phone = phoneNum
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
                        ex?.message?.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) == true ->
                            "Layanan Email/Password belum diaktifkan di Firebase Console. Silakan buka Firebase Console -> Authentication -> Sign-in method -> aktifkan Email/Password."
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
                        firestoreSyncService.syncUserProfile(
                            userId = uid,
                            name = cleanName,
                            email = cleanEmail,
                            phone = cleanPhone
                        )
                        syncFcmToken(uid, cleanEmail)
                    }
                    _authState.value = AuthState.Success("Registrasi Berhasil! Akun $cleanName telah terdaftar di database Firebase.")
                } else {
                    val ex = task.exception
                    val isEmailInUse = ex?.message?.contains("email-already-in-use", ignoreCase = true) == true ||
                                       ex?.message?.contains("already in use", ignoreCase = true) == true

                    if (isEmailInUse) {
                        _authState.value = AuthState.AccountAlreadyExists(cleanEmail, cleanName)
                        return@addOnCompleteListener
                    }

                    val errorMsg = when {
                        ex?.message?.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) == true ->
                            "Layanan Email/Password belum diaktifkan di Firebase Console. Silakan buka Firebase Console -> Authentication -> Sign-in method -> aktifkan Email/Password."
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

    /**
     * Logout / Keluar dari akun:
     * - Mengeluarkan sesi dari Firebase Authentication.
     * - Menghapus cache sesi dan kredensial login Google (GoogleAuthManager.signOut)
     *   sehingga pengguna harus memilih/memasukkan akun Google baru dan tidak tersimpan otomatis.
     * - Menghapus sesi lokal di SessionManager (DataStore).
     */
    fun logout(callerContext: Context? = null, onComplete: () -> Unit = {}) {
        try {
            auth.signOut()
        } catch (_: Exception) {}

        val ctx = callerContext ?: context
        GoogleAuthManager.signOut(ctx) {
            viewModelScope.launch {
                sessionManager.clearSession()
                _authState.value = AuthState.Idle
                onComplete()
            }
        }
    }

    /**
     * Mendaftar akun baru melalui Google Sign-In:
     * - Jika akun Google yang dipilih sudah pernah terdaftar di Firebase atau Firestore,
     *   sistem membatalkan sesi dan memancarkan AuthState.AccountAlreadyExists agar
     *   menampilkan dialog pop-up "Akun Sudah Terdaftar".
     * - Jika belum terdaftar, akun baru dibuat dan langsung masuk ke aplikasi.
     */
    fun registerWithGoogle(
        email: String,
        name: String,
        idToken: String? = null,
        callerContext: Context? = null
    ) {
        _authState.value = AuthState.Loading
        val ctx = callerContext ?: context

        if (!idToken.isNullOrBlank()) {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val authResult = task.result
                    val firebaseUser = authResult?.user
                    val additionalUserInfo = authResult?.additionalUserInfo
                    val isNewUser = additionalUserInfo?.isNewUser == true
                    val finalEmail = firebaseUser?.email ?: email
                    val finalName = firebaseUser?.displayName ?: name
                    val finalUid = firebaseUser?.uid ?: ""

                    viewModelScope.launch {
                        // Cek apakah akun ini sudah pernah terdaftar sebelumnya
                        var alreadyExists = !isNewUser

                        if (!alreadyExists && finalUid.isNotBlank()) {
                            try {
                                if (firestoreSyncService.isUserRegistered(finalUid, finalEmail)) {
                                    alreadyExists = true
                                }
                            } catch (_: Exception) {}
                        }

                        if (alreadyExists) {
                            // Akun sudah terdaftar!
                            // Batalkan sesi Firebase dan Google agar kredensial tidak tertinggal
                            try {
                                auth.signOut()
                            } catch (_: Exception) {}
                            GoogleAuthManager.signOut(ctx)

                            _authState.value = AuthState.AccountAlreadyExists(
                                email = finalEmail,
                                name = finalName
                            )
                        } else {
                            // Akun baru, proses pendaftaran
                            val jwtToken = try {
                                firebaseUser?.getIdToken(false)?.await()?.token ?: idToken
                            } catch (_: Exception) {
                                idToken
                            }

                            sessionManager.saveSession(
                                isLoggedIn = true,
                                userId = finalUid,
                                name = finalName,
                                email = finalEmail,
                                phone = "0812-3456-7890",
                                token = jwtToken
                            )
                            firestoreSyncService.syncUserProfile(
                                userId = finalUid,
                                name = finalName,
                                email = finalEmail,
                                phone = "0812-3456-7890"
                            )
                            syncFcmToken(finalUid, finalEmail)
                            _authState.value = AuthState.Success("Pendaftaran Berhasil! Selamat datang di Geara, $finalName.")
                        }
                    }
                } else {
                    val ex = task.exception
                    val msg = ex?.message ?: ""
                    if (ex is com.google.firebase.auth.FirebaseAuthUserCollisionException ||
                        msg.contains("already in use", ignoreCase = true) ||
                        msg.contains("email-already-in-use", ignoreCase = true) ||
                        msg.contains("account-exists-with-different-credential", ignoreCase = true)
                    ) {
                        try {
                            auth.signOut()
                        } catch (_: Exception) {}
                        GoogleAuthManager.signOut(ctx)

                        _authState.value = AuthState.AccountAlreadyExists(
                            email = email,
                            name = name
                        )
                    } else {
                        _authState.value = AuthState.Error(ex?.localizedMessage ?: "Pendaftaran dengan Google gagal.")
                    }
                }
            }
        } else {
            // Jalur fallback simulasi offline/lokal jika idToken kosong
            viewModelScope.launch {
                val exists = firestoreSyncService.isUserRegistered("", email)
                if (exists) {
                    GoogleAuthManager.signOut(ctx)
                    _authState.value = AuthState.AccountAlreadyExists(
                        email = email,
                        name = name
                    )
                } else {
                    val uid = "google_${System.currentTimeMillis()}"
                    sessionManager.saveSession(
                        isLoggedIn = true,
                        userId = uid,
                        name = name,
                        email = email,
                        phone = "0812-3456-7890",
                        token = "JWT-GOOGLE-${System.currentTimeMillis()}"
                    )
                    firestoreSyncService.syncUserProfile(
                        userId = uid,
                        name = name,
                        email = email,
                        phone = "0812-3456-7890"
                    )
                    syncFcmToken(uid, email)
                    _authState.value = AuthState.Success("Pendaftaran Berhasil! Selamat datang di Geara, $name.")
                }
            }
        }
    }

    fun loginWithGoogle(
        email: String,
        name: String,
        idToken: String? = null
    ) {
        _authState.value = AuthState.Loading
        if (!idToken.isNullOrBlank()) {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    val finalUid = firebaseUser?.uid ?: "google_${System.currentTimeMillis()}"
                    val finalEmail = firebaseUser?.email ?: email
                    val finalName = firebaseUser?.displayName ?: name

                    firebaseUser?.getIdToken(false)?.addOnCompleteListener { tokenTask ->
                        val jwtToken = tokenTask.result?.token ?: idToken
                        viewModelScope.launch {
                            sessionManager.saveSession(
                                isLoggedIn = true,
                                userId = finalUid,
                                name = finalName,
                                email = finalEmail,
                                phone = "0812-3456-7890",
                                token = jwtToken
                            )
                            firestoreSyncService.syncUserProfile(
                                userId = finalUid,
                                name = finalName,
                                email = finalEmail,
                                phone = "0812-3456-7890"
                            )
                            syncFcmToken(finalUid, finalEmail)
                            _authState.value = AuthState.Success("Berhasil masuk melalui Google ($finalEmail).")
                        }
                    } ?: run {
                        viewModelScope.launch {
                            sessionManager.saveSession(
                                isLoggedIn = true,
                                userId = finalUid,
                                name = finalName,
                                email = finalEmail,
                                phone = "0812-3456-7890",
                                token = idToken
                            )
                            firestoreSyncService.syncUserProfile(
                                userId = finalUid,
                                name = finalName,
                                email = finalEmail,
                                phone = "0812-3456-7890"
                            )
                            syncFcmToken(finalUid, finalEmail)
                            _authState.value = AuthState.Success("Berhasil masuk melalui Google ($finalEmail).")
                        }
                    }
                } else {
                    val errorMsg = task.exception?.localizedMessage ?: "Autentikasi Firebase dengan Google gagal."
                    _authState.value = AuthState.Error(errorMsg)
                }
            }
        } else {
            val uid = "google_${System.currentTimeMillis()}"
            viewModelScope.launch {
                sessionManager.saveSession(
                    isLoggedIn = true,
                    userId = uid,
                    name = name,
                    email = email,
                    phone = "0812-3456-7890",
                    token = "JWT-GOOGLE-${System.currentTimeMillis()}"
                )
                firestoreSyncService.syncUserProfile(
                    userId = uid,
                    name = name,
                    email = email,
                    phone = "0812-3456-7890"
                )
                syncFcmToken(uid, email)
                _authState.value = AuthState.Success("Berhasil masuk melalui Google ($email).")
            }
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
