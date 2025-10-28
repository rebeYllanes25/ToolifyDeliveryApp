package com.cibertec.proyectodami.domain.model.entities

import com.cibertec.proyectodami.domain.model.dtos.PedidoCompra

data class Venta(
    val idVenta: Int? = null,
    val usuario: UsuarioVentaDTO,
    val fechaRegistro: String? = null,
    val total: Double,
    val estado: String,
    val tipoVenta: String,
    val metodoEntrega: String,
    val especificaciones: String?,
    val detalles: List<DetalleVenta>,
    val pedido: PedidoCompra? = null
)