package com.cibertec.proyectodami.domain.model.entities

data class DetalleVenta(
    val idDetalleVenta: Int? = null,
    val venta: Venta? = null,
    val producto: ProductoVenta,
    val cantidad: Int,
    val precio: Double = 0.0
)

data class ProductoVenta(
    val idProducto: Int
)

data class UsuarioVentaDTO(
    val idUsuario: Int
)
