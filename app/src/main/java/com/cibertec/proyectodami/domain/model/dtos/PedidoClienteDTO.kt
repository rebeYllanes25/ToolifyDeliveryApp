package com.cibertec.proyectodami.domain.model.dtos

import com.google.gson.annotations.SerializedName

data class PedidoClienteDTO(

    val idPedido: Int,
    val numPedido: String,
    val idCliente: Int,
    val nomCliente: String,
    val fecha: String?,
    val total: Double,
    val direccionEntrega: String,
    val latitud: Double,
    val longitud: Double,
    val movilidad: String,
    val qrVerificationCode: String,

    @SerializedName("detalles")
    val productos: List<ProductoPedidoDTO>,

    val nomRepartidor: String?,
    val especificaciones: String?,
    val estado: String, // PE, AS, EC, EN, FA


    val tiempoEntregaMinutos: Int?
    )
