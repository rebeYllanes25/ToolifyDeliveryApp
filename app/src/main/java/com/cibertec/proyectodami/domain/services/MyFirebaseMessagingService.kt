package com.cibertec.proyectodami.domain.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.data.api.Notificaciones
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.data.remote.RetrofitInstance
import com.cibertec.proyectodami.domain.model.dtos.requests.FcmTokenRequest
import com.cibertec.proyectodami.presentation.features.cliente.ClienteMainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nuevo token generado: $token")

        guardarTokenLocalmente(token)

        // Enviar token al backend (si el usuario ya está logueado)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userPreferences = UserPreferences(applicationContext)
                val usuarioId = userPreferences.obtenerIdUsuario()
                val rol = userPreferences.obtenerRol()

                if (usuarioId != -1 && rol == 2) { // Tiene q ser cliente
                    enviarTokenAlBackend(token, usuarioId, userPreferences)
                } else {
                    Log.d("FCM", "Usuario no logueado o no es cliente, no se envía token")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Error al obtener usuario/rol: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("FCM", "Mensaje recibido de: ${message.from}")

        // Extraer datos (ahora incluye título y mensaje desde el backend)
        val titulo = message.notification?.title ?: "Nueva Notificación"
        val cuerpo = message.notification?.body ?: ""
        val notificacionId = message.data["notificacionId"]
        val pedidoId = message.data["pedidoId"]
        val tipo = message.data["tipo"]
        val clienteId = message.data["clienteId"]

        // Mostrar notificación
        mostrarNotificacion(titulo, cuerpo, notificacionId, pedidoId)
    }

    private fun guardarTokenLocalmente(token: String) {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
    }

    private fun enviarTokenAlBackend(token: String, usuarioId: Int, userPreferences: UserPreferences) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val retrofit = RetrofitInstance.create(userPreferences)
                val api = retrofit.create(Notificaciones::class.java)

                val response = api.registrarTokenFcm(
                    usuarioId = usuarioId,
                    tokenRequest = FcmTokenRequest(token)
                )

                if (response.isSuccessful) {
                    Log.d("FCM", "Token enviado al backend exitosamente")
                } else {
                    Log.e("FCM", "Error al registrar token: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Error al enviar token: ${e.message}")
            }
        }
    }

    private fun mostrarNotificacion(
        titulo: String,
        cuerpo: String,
        notificacionId: String?,
        pedidoId: String?
    ) {
        val channelId = "pedidos_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificaciones de Pedidos",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, ClienteMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notificacionId", notificacionId)
            putExtra("pedidoId", pedidoId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}