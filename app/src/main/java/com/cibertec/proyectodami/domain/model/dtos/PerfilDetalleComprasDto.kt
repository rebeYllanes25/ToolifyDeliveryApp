package com.cibertec.proyectodami.domain.model.dtos

data class PerfilDetalleComprasDto(
    val idUser:Int,
    val imagenUsuario:String,
    val nombresCompletos:String,
    val correo:String,
    val nroDoc:String,
    val direccion:String,
    val distrito:String,
    val telefono:String,
    val fechaRegistro:String,
    val productoMasComprado:String,
    val fechaMayorCompras:String,
    val totalDeProductosComprados:Int,
    val gastoTotal:Double,
    val categoriaMasComprada:String,
    val totalVentas:Long
)
