package com.cibertec.proyectodami.models.entities

data class Calificacion(
    val idCalificacion: Int,
    val idPedido: Int,
    val idCliente: Int,
    val idRepartidor: Int,
    val puntaje: Short,
    val comentario: String?,
    val fecha: String
)
