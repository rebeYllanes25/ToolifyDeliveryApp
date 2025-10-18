package com.cibertec.proyectodami.domain.model.dtos

data class PedidoClienteDTO(
    val nroPedido: String,
    val fechaPedido: String?,
    val estadoDelivery: String, // PE, AS, EC, EN, FA
    val tiempoEntregaMinutos: Int?,
    val nombreRepartidor: String?,
    val movilidad: String,
    val qrVerificationCode: String,

    val productos: List<ProductoPedidoDTO>,

    val subtotalProductos: Double,
    val costoEnvio: Double,
    val totalPagar: Double
)
