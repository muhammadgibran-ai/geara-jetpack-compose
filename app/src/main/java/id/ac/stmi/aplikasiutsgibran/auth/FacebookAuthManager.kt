package id.ac.stmi.aplikasiutsgibran.auth

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

/**
 * Manajer Facebook Authentication berbasis protokol resmi OAuth 2.0 (RFC 6749 & RFC 8252).
 * Membuka Chrome Custom Tabs menuju Authorization Endpoint Facebook resmi:
 * https://www.facebook.com/v19.0/dialog/oauth
 */
object FacebookAuthManager {

    // Default App ID untuk GEARA CRM (dapat disesuaikan di strings.xml atau Developer Portal)
    const val DEFAULT_FB_CLIENT_ID = "1098472918471201"
    const val REDIRECT_URI = "geara://oauth/facebook"

    fun buildOAuthUrl(clientId: String = DEFAULT_FB_CLIENT_ID): String {
        return Uri.parse("https://www.facebook.com/v19.0/dialog/oauth").buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("response_type", "token")
            .appendQueryParameter("scope", "email,public_profile")
            .appendQueryParameter("display", "touch")
            .build()
            .toString()
    }

    fun launchOAuthFlow(context: Context, clientId: String = DEFAULT_FB_CLIENT_ID) {
        val authUrl = buildOAuthUrl(clientId)
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        try {
            customTabsIntent.launchUrl(context, Uri.parse(authUrl))
        } catch (_: Exception) {
            // Fallback ke browser standar jika Custom Tabs tidak tersedia
            val browserIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(authUrl))
            browserIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(browserIntent)
        }
    }

    /**
     * Mem-parsing URI Callback redirect dari Facebook OAuth (misal: geara://oauth/facebook#access_token=... atau ?code=...)
     */
    fun parseOAuthCallback(uri: Uri): Result<String> {
        val fragment = uri.fragment
        if (!fragment.isNullOrEmpty()) {
            val params = fragment.split("&").associate {
                val parts = it.split("=")
                if (parts.size == 2) parts[0] to parts[1] else parts[0] to ""
            }
            val accessToken = params["access_token"]
            if (!accessToken.isNullOrEmpty()) {
                return Result.success(accessToken)
            }
        }

        val code = uri.getQueryParameter("code")
        if (!code.isNullOrEmpty()) {
            return Result.success(code)
        }

        val error = uri.getQueryParameter("error_description") ?: uri.getQueryParameter("error")
        return Result.failure(Exception(error ?: "Otorisasi OAuth Facebook gagal."))
    }
}
