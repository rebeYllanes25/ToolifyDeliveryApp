package com.cibertec.proyectodami.domain.model.dtos

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NotificacionDTO(
    val id: Int,
    val tipo: String,
    val titulo: String,
    val mensaje: String,
    val fecha: String,
    val leida: Boolean,
    val idPedido: Int,
    val idCliente: Int
)
{
    fun getFechaFormateada(): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(fecha)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            fecha
        }
    }

    // obtener tiempo relativo (hace 2 horas, hace 1 día, etc.)
    fun getTiempoRelativo(): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = format.parse(fecha) ?: return fecha
            val now = Date()
            val diff = now.time - date.time

            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            when {
                seconds < 60 -> "Hace un momento"
                minutes < 60 -> "Hace ${minutes}m"
                hours < 24 -> "Hace ${hours}h"
                days < 7 -> "Hace ${days}d"
                else -> getFechaFormateada()
            }
        } catch (e: Exception) {
            fecha
        }
    }
}
