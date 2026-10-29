# ==============================================================================
# ProGuard / R8 Rules for GEARA CRM Suku Cadang Android Application
# Optimasi, Minifikasi, dan Obfuscation untuk Perlindungan Kode & Reverse Engineering
# ==============================================================================

# --- Kotlin Coroutines & Flow ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# --- Jetpack Compose ---
-keepclassmembers class * extends androidx.compose.runtime.State {
    <methods>;
}

# --- Dagger-Hilt Dependency Injection ---
-keep,allowobfuscation,allowshrinking class * extends dagger.hilt.internal.GeneratedComponentManager
-keep,allowobfuscation,allowshrinking class * extends dagger.hilt.internal.ComponentManager
-keep class * implements dagger.hilt.internal.GeneratedComponentManager
-keep class * implements dagger.hilt.internal.ComponentManager
-keep class **_HiltModules* { *; }
-keep class dagger.hilt.** { *; }

# --- Room Database ---
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class id.ac.stmi.aplikasiutsgibran.data.local.** { *; }
-keepclassmembers class * {
    @androidx.room.Dao *;
    @androidx.room.Database *;
    @androidx.room.Entity *;
    @androidx.room.PrimaryKey *;
    @androidx.room.ColumnInfo *;
}

# --- Retrofit & OkHttp ---
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# --- Gson & Serialization (Preserve API DTOs) ---
-keepattributes *Annotation*
-keepclassmembers enum * { *; }
-keep class com.google.gson.** { *; }
-keep class id.ac.stmi.aplikasiutsgibran.data.remote.** { *; }
-keepclassmembers class id.ac.stmi.aplikasiutsgibran.data.remote.** { <fields>; }

# --- Domain Data Models ---
-keep class id.ac.stmi.aplikasiutsgibran.data.Product { *; }
-keep class id.ac.stmi.aplikasiutsgibran.data.CartItem { *; }
-keep class id.ac.stmi.aplikasiutsgibran.data.Order { *; }
-keep class id.ac.stmi.aplikasiutsgibran.data.OrderStatus { *; }
-keep class id.ac.stmi.aplikasiutsgibran.data.TrackingStep { *; }
-keep class id.ac.stmi.aplikasiutsgibran.data.NotificationItem { *; }
-keep class id.ac.stmi.aplikasiutsgibran.data.ChatMessage { *; }
-keep class id.ac.stmi.aplikasiutsgibran.data.CheckoutState { *; }

# --- Coil Image Loader ---
-keep class coil.** { *; }
-dontwarn coil.**

# --- Firebase & Crashlytics ---
-keepattributes SourceFile, LineNumberTable
-keep public class * extends java.lang.Exception
-keepclassmembers class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# --- DataStore Preferences ---
-keep class androidx.datastore.** { *; }

# --- Utilities & Diagnostics ---
-keep class id.ac.stmi.aplikasiutsgibran.util.** { *; }
