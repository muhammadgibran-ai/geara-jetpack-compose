package id.ac.stmi.aplikasiutsgibran.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "geara_secure_session")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_USER_PHONE = stringPreferencesKey("user_phone")
        val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        val KEY_FCM_TOKEN = stringPreferencesKey("fcm_token")
        val KEY_PRIMARY_PAYMENT_METHOD = stringPreferencesKey("primary_payment_method")
        val KEY_IS_DANA_CONNECTED = booleanPreferencesKey("is_dana_connected")
        val KEY_DANA_PHONE = stringPreferencesKey("dana_phone")
    }

    val primaryPaymentMethod: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_PRIMARY_PAYMENT_METHOD] ?: "gopay"
    }

    val isDanaConnected: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_DANA_CONNECTED] ?: false
    }

    val danaPhone: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_DANA_PHONE] ?: "081234567806"
    }

    suspend fun savePrimaryPaymentMethod(method: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PRIMARY_PAYMENT_METHOD] = method
        }
    }

    suspend fun setDanaConnected(connected: Boolean, phone: String = "") {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_DANA_CONNECTED] = connected
            if (phone.isNotBlank()) {
                preferences[KEY_DANA_PHONE] = phone
            }
        }
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_LOGGED_IN] ?: false
    }

    val userId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_ID] ?: "user_default"
    }

    val userName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_NAME] ?: "Gibran Rakabuming"
    }

    val userEmail: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_EMAIL] ?: "gibran@geara.co.id"
    }

    val userPhone: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_PHONE] ?: "0812-3456-7890"
    }

    val authToken: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTH_TOKEN] ?: ""
    }

    val fcmToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_FCM_TOKEN]
    }

    suspend fun saveSession(
        isLoggedIn: Boolean,
        userId: String = "user_default",
        name: String = "Gibran Rakabuming",
        email: String = "gibran@geara.co.id",
        phone: String = "0812-3456-7890",
        token: String = "JWT-GEARA-CRM-SECURE-TOKEN"
    ) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_LOGGED_IN] = isLoggedIn
            preferences[KEY_USER_ID] = userId
            preferences[KEY_USER_NAME] = name
            preferences[KEY_USER_EMAIL] = email
            preferences[KEY_USER_PHONE] = phone
            preferences[KEY_AUTH_TOKEN] = token
        }
    }

    suspend fun saveFcmToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FCM_TOKEN] = token
        }
    }

    suspend fun savePhoneEmailMapping(phone: String, email: String) {
        val cleanPhone = phone.filter { it.isDigit() }
        if (cleanPhone.isNotBlank()) {
            context.dataStore.edit { preferences ->
                preferences[stringPreferencesKey("phone_map_$cleanPhone")] = email
            }
        }
    }

    suspend fun getEmailByPhone(phone: String): String? {
        val cleanPhone = phone.filter { it.isDigit() }
        val preferences = context.dataStore.data.first()
        return preferences[stringPreferencesKey("phone_map_$cleanPhone")]
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_LOGGED_IN] = false
            preferences.remove(KEY_AUTH_TOKEN)
        }
    }
}
