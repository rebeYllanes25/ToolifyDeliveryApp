package com.cibertec.proyectodami.domain.model.entities

import java.time.LocalDateTime

data class Calificacion(
    val idCalificacion: Int,
    val idPedido: Int,
    val idCliente: Int,
    val idRepartidor: Int,
    val puntaje: Short?, // 1 a 5 estrellas
    val comentario: String?,
    val fecha: LocalDateTime?
)
