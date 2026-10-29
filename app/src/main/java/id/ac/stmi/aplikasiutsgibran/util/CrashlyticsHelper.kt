package id.ac.stmi.aplikasiutsgibran.util

import com.google.firebase.crashlytics.FirebaseCrashlytics

object CrashlyticsHelper {

    fun logEvent(message: String) {
        try {
            FirebaseCrashlytics.getInstance().log(message)
        } catch (_: Exception) {}
    }

    fun recordException(throwable: Throwable) {
        try {
            FirebaseCrashlytics.getInstance().recordException(throwable)
        } catch (_: Exception) {}
    }

    fun setUserId(userId: String) {
        try {
            FirebaseCrashlytics.getInstance().setUserId(userId)
        } catch (_: Exception) {}
    }

    fun testCrash() {
        throw RuntimeException("Uji Coba Force Close Firebase Crashlytics GEARA CRM Suku Cadang")
    }
}
