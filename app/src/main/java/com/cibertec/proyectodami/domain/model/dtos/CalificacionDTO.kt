package com.cibertec.proyectodami.domain.model.dtos

data class CalificacionDTO(
    val idCalificacion: Int,
    val idPedido: Int,
    val idCliente: Int,
    val nombreCliente: String?,
    val idRepartidor: Int,
    val nombreRepartidor: String?,
    val puntuacion: Short?,
    val comentario: String?,
    val fechaCalificacion: String
)

