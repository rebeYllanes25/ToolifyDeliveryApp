package com.cibertec.proyectodami.domain.model.dtos

data class UsuarioDTO(
    val idUsuario: Int,
    val nombres: String,
    val apePaterno: String,
    val apeMaterno: String,
    val correo: String,
    val telefono: String,
    val direccion:String,
    val rol: Rol,
    val tokenSesion: String
) {
    val descripcionRol: String
        get() = when (rol.idRol) {
            1 -> "Administrador"
            2 -> "Cliente"
            3 -> "Vendedor"
            4 -> "Repartidor"
            else -> rol.descripcion
        }
}

data class Rol(
    val idRol: Int,
    val descripcion: String
)

