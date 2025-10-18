package com.cibertec.proyectodami.domain.model.dtos

data class FiltrosData(
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val precioMin: Int = 0,
    val precioMax: Int = 1000
)
