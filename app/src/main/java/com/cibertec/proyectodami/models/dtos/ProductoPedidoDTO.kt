package com.cibertec.proyectodami.models.dtos

data class ProductoPedidoDTO(
    val idDetalleVenta: Int,
    val nombreProducto: String,
    val descripcionProducto: String,
    val imagen: String?,
    val precioUnitario: Double,
    val cantidad: Short,
    val subTotal: Double,
)
