package id.ac.stmi.aplikasiutsgibran.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import id.ac.stmi.aplikasiutsgibran.BuildConfig
import id.ac.stmi.aplikasiutsgibran.data.remote.GearaApiService
import id.ac.stmi.aplikasiutsgibran.util.network.ConnectivityObserver
import id.ac.stmi.aplikasiutsgibran.util.network.NetworkConnectivityObserver
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Dinamis sesuai Build Type (Debug: 10.0.2.2:3000 / Release: https://api.geara.co.id/)
    private const val BASE_URL_DEBUG: String = "http://10.0.2.2:3000/"
    private const val BASE_URL_RELEASE: String = "https://api.geara.co.id/"
    private val BASE_URL: String = if (BuildConfig.DEBUG) BASE_URL_DEBUG else BASE_URL_RELEASE

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGearaApiService(retrofit: Retrofit): GearaApiService {
        return retrofit.create(GearaApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideConnectivityObserver(
        @ApplicationContext context: Context
    ): ConnectivityObserver {
        return NetworkConnectivityObserver(context)
    }
}
