package com.cibertec.proyectodami.domain.model.dtos

import java.time.LocalDateTime

data class PedidoRepartidorDTO(
    val idPedido: Int,
    val numPedido: String,
    val nomCliente: String,
    val direccionEntrega: String,
    val fecha: String?,
    val total: Double,
    val movilidad: String,
    val distanciaKM: Double,
    val especificaciones: String?,
    val estado: String,
    val tiempoEntrega: Int?,
    val latitud: Double,
    val longitud: Double
)
