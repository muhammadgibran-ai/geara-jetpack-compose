package id.ac.stmi.aplikasiutsgibran

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GearaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Firebase Crashlytics: Pemantauan otomatis force close & non-fatal exception
        try {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.setCustomKey("app_name", "GEARA CRM Suku Cadang")
            crashlytics.setCustomKey("app_version", "1.0")
            crashlytics.setCustomKey("environment", "Production")
            crashlytics.log("GearaApplication initialized successfully with Crashlytics monitoring")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
