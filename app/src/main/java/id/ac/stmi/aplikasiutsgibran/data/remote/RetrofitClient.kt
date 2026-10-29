package id.ac.stmi.aplikasiutsgibran.data.remote

import id.ac.stmi.aplikasiutsgibran.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Alamat server backend nyata (Dinamis dari BuildConfig: 10.0.2.2 untuk Debug, HTTPS untuk Release)
    var baseUrl: String = BuildConfig.BASE_URL
        private set

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private var retrofit: Retrofit = buildRetrofit(baseUrl)

    private fun buildRetrofit(url: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(if (url.endsWith("/")) url else "$url/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    var apiService: GearaApiService = retrofit.create(GearaApiService::class.java)
        private set

    fun updateBaseUrl(newUrl: String) {
        baseUrl = newUrl
        retrofit = buildRetrofit(newUrl)
        apiService = retrofit.create(GearaApiService::class.java)
    }
}
