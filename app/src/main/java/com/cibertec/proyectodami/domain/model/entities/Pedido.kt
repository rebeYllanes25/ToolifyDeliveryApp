package com.cibertec.proyectodami.domain.model.entities

import java.time.LocalDateTime

data class Pedido(
    val idPedido: Int,
    val idVenta: Int,

    val numPedido: String,
    val fecha: LocalDateTime,
    val direccion: String,
    var latitud: Double,
    var longitud: Double,

    val qrVerificationCode: String, // codigo QR para validar la entrega
    val idRepartidor: Int?,

    val fechaAsignacion: LocalDateTime?,
    val fechaEnCamino: LocalDateTime?,
    val fechaEntrega: LocalDateTime?,
    val tiempoEntrega: Int?, // minutos de dif entre fechaEnCamino y fechaEntrega

    var estado: String, // PE,AS,EC,EN,FA
    val observaciones: String?
)
