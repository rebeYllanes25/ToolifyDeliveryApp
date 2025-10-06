package com.cibertec.proyectodami.models.dtos

import java.time.LocalDateTime

data class PedidoClienteDTO(
    val nroPedido: String,
    val fechaPedido: LocalDateTime,
    val estadoDelivery: String, // PE, AS, EC, EN, FA
    val tiempoEntregaMinutos: Int?,
    val nombreRepartidor: String?,
    val qrVerificationCode: String,

    val productos: List<ProductoPedidoDTO>,

    val subtotalProductos: Double,
    val costoEnvio: Double,
    val totalPagar: Double
)
