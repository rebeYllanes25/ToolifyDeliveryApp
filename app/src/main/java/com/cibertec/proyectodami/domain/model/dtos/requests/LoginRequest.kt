package com.cibertec.proyectodami.domain.model.dtos.requests

data class LoginRequest(
    val correo: String,
    val clave: String
)
