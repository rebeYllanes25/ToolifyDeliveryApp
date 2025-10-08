package com.cibertec.proyectodami.models.dtos

import java.time.LocalDateTime

data class PedidoRepartidorDTO(
    val nroPedido: String,
    val nomCliente: String,
    val direccion: String,
    val fecha: LocalDateTime?,
    val total: Double,
    val distanciaKM: Double,
    val especificaciones: String?,
    val estado: String,
    val tiempoEntrega: Int?
)
