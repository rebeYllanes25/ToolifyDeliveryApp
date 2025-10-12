package com.cibertec.proyectodami.domain.model.dtos

data class LoginRequest(
    val correo: String,
    val clave: String
)
