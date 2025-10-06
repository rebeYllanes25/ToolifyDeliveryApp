package com.cibertec.proyectodami.models.dtos

data class UsuarioDTO(
    val idUsuario: Int,
    val nombres: String,
    val apePaterno: String,
    val apeMaterno: String,
    val correo: String,
    val rol: Int,
    val descripcionRol: String,
    val tokenSesion: String
)
