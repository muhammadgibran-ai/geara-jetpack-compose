package id.ac.stmi.aplikasiutsgibran.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

/**
 * Manajer Google Sign-In berbasis protokol OAuth 2.0 resmi (Google Play Services Auth).
 * Membuka bottom sheet akun Google asli dari sistem Android untuk otentikasi nyata.
 */
object GoogleAuthManager {

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(context: Context): Intent {
        val client = getGoogleSignInClient(context)
        return client.signInIntent
    }

    fun parseSignInResult(intent: Intent?): Result<GoogleSignInAccount> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                Result.success(account)
            } else {
                Result.failure(Exception("Akun Google tidak ditemukan."))
            }
        } catch (e: ApiException) {
            val statusMessage = when (e.statusCode) {
                12501 -> "Pemilihan akun Google dibatalkan pengguna."
                12500 -> "Terjadi kesalahan internal saat menghubungkan ke akun Google."
                7 -> "Koneksi jaringan terputus saat login Google."
                else -> "Gagal otentikasi Google (Kode: ${e.statusCode})"
            }
            Result.failure(Exception(statusMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getLastSignedInAccount(context: Context): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    fun signOut(context: Context, onComplete: () -> Unit = {}) {
        getGoogleSignInClient(context).signOut().addOnCompleteListener {
            onComplete()
        }
    }
}
