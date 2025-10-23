package com.cibertec.proyectodami.data.remote

import android.os.Build
import com.cibertec.proyectodami.BuildConfig
import com.cibertec.proyectodami.data.dataStore.AuthInterceptor
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    /* Cambiar el local IP de tu red de wifi*/
    private const val LOCAL_IP = "192.168.1.7"
    private const val PORT = "8080"

    fun create(userPreferences: UserPreferences): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(userPreferences))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val baseUrl = when {
            // Emulador: usar DEV_BASE_URL (10.0.2.2)
            isRunningOnEmulator() -> BuildConfig.DEV_BASE_URL

            // Dispositivo físico en red local: usar IP local
            BuildConfig.DEBUG -> "http://$LOCAL_IP:$PORT/"

            // Producción: usar PROD_BASE_URL
            else -> BuildConfig.PROD_BASE_URL
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    private fun isRunningOnEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.lowercase().contains("vbox")
                || Build.FINGERPRINT.lowercase().contains("test-keys")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86"))
    }
}