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

    // Obtiene el token FCM actual y lo envía al backend
    fun obtenerYEnviarToken(context: Context, usuarioId: Int, rolUsuario: String, userPreferences: UserPreferences) {
        if (rolUsuario.uppercase() != "C") {
            Log.d("FCM", "Usuario no es cliente, no se envía token")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                Log.d("FCM", "Token obtenido: $token")

                guardarTokenLocal(context, token)

                val retrofit = RetrofitInstance.create(userPreferences)
                val api = retrofit.create(Notificaciones::class.java)

                val response = api.registrarTokenFcm(
                    usuarioId = usuarioId,
                    tokenRequest = FcmTokenRequest(token)
                )

                if (response.isSuccessful) {
                    Log.d("FCM", "Token registrado en backend exitosamente")
                } else {
                    Log.e("FCM", "Error al registrar token: ${response.code()} ${response.message()}")
                }

            } catch (e: Exception) {
                Log.e("FCM", "Error al obtener/enviar token: ${e.message}")
            }
        }
    }

    fun eliminarToken(context: Context, usuarioId: Int, userPreferences: UserPreferences) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val retrofit = RetrofitInstance.create(userPreferences)
                val api = retrofit.create(com.cibertec.proyectodami.data.api.Notificaciones::class.java)

                val response = api.eliminarTokenFcm(usuarioId)
                if (response.isSuccessful) {
                    Log.d("FCM", "Token eliminado del backend")
                    borrarTokenLocal(context)
                } else {
                    Log.e("FCM", "Error al eliminar token: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e("FCM", "Error al eliminar token: ${e.message}")
            }
        }
    }

    private fun guardarTokenLocal(context: Context, token: String) {
        val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
    }

    private fun borrarTokenLocal(context: Context) {
        val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        prefs.edit().remove("fcm_token").apply()
    }
}