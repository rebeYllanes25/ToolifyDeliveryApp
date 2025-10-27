package com.cibertec.proyectodami.domain.model.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Producto(
    val idProducto: Int,
    val nombre: String,
    val descripcion: String,
    val proveedor: Proveedor? = null,
    val categoria: Categoria? = null,
    val precio: Double,
    val stock: Int,
    val imagen: String? = null,
    val fechaRegistro: String? = null,
    val estado: Boolean? = null,
) : Parcelable