package com.cibertec.proyectodami.domain.util

import android.content.Context
import android.util.Log
import com.cibertec.proyectodami.data.api.Notificaciones
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.data.remote.RetrofitInstance
import com.cibertec.proyectodami.domain.model.dtos.requests.FcmTokenRequest
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FcmTokenHelper {

    fun obtenerYEnviarToken(context: Context, userPreferences: UserPreferences) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val usuarioId = userPreferences.obtenerIdUsuario()
                val rolUsuario = userPreferences.obtenerRol()

                if (usuarioId == -1 || rolUsuario == null) {
                    Log.e("FCM", "No se pudo obtener el usuario o rol desde DataStore")
                    return@launch
                }

                if (rolUsuario != 2) {
                    Log.d("FCM", "Usuario no es cliente, no se envía token")
                    return@launch
                }

                val token = FirebaseMessaging.getInstance().token.await()
                Log.d("FCM", "Token FCM obtenido: $token")

                guardarTokenLocal(context, token)

                val retrofit = RetrofitInstance.create(userPreferences)
                val api = retrofit.create(Notificaciones::class.java)

                val response = api.registrarTokenFcm(
                    usuarioId = usuarioId,
                    tokenRequest = FcmTokenRequest(token)
                )

                if (response.isSuccessful) {
                    Log.d("FCM", "Token registrado exitosamente en backend")
                } else {
                    Log.e("FCM", "Error al registrar token: ${response.code()} ${response.message()}")
                }

            } catch (e: Exception) {
                Log.e("FCM", "Error al obtener/enviar token: ${e.message}")
            }
        }
    }

    fun eliminarToken(context: Context, userPreferences: UserPreferences) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val usuarioId = userPreferences.obtenerIdUsuario()
                if (usuarioId == -1) {
                    Log.e("FCM", "No se encontró un ID de usuario válido para eliminar token")
                    return@launch
                }

                val retrofit = RetrofitInstance.create(userPreferences)
                val api = retrofit.create(Notificaciones::class.java)

                val response = api.eliminarTokenFcm(usuarioId)
                if (response.isSuccessful) {
                    Log.d("FCM", "Token eliminado del backend correctamente")
                    borrarTokenLocal(context)
                } else {
                    Log.e("FCM", "Error al eliminar token: ${response.code()} ${response.message()}")
                }

            } catch (e: Exception) {
                Log.e("FCM", "Error al eliminar token: ${e.message}")
            }
        }
    }

    // Guarda el token FCM en SharedPreferences (solo visible por la app)
    private fun guardarTokenLocal(context: Context, token: String) {
        val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
    }

    private fun borrarTokenLocal(context: Context) {
        val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        prefs.edit().remove("fcm_token").apply()
    }
}