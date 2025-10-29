package com.cibertec.proyectodami.domain.model.dtos.responses

data class EstadisticaRepartidorDTO (
    val totalEntregas: Long,
    val tiempoPromedio: Double,
    val calificacionPromedio: Double
)