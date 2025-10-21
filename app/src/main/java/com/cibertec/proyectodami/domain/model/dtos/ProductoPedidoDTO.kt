package com.cibertec.proyectodami.domain.model.dtos

data class ProductoPedidoDTO(
    val idDetalleVenta: Int,
    val idProducto: Int,
    val nombreProducto: String,
    val descripcionProducto: String,
    val imagen: String?,
    val precio: Double,
    val cantidad: Int,
    val subTotal: Double
)
