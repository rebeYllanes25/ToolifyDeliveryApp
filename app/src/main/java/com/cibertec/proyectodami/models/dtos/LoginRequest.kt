package com.cibertec.proyectodami.models.dtos

data class LoginRequest(
    val correo: String,
    val clave: String
)
