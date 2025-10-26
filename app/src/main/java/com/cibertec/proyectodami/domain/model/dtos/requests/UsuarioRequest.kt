package com.cibertec.proyectodami.domain.model.dtos.requests

data class UsuarioRequest(
    val nombres: String,
    val apePaterno: String,
    val apeMaterno: String,
    val correo: String,
    val clave: String,
    val nroDocumento: String,
    val direccion: String,
    val distrito: Distrito,
    val telefono: String
)

data class Distrito(
    val idDistrito: Int
)