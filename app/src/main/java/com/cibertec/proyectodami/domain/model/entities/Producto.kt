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
    val imagenBytes: String? = null,
    val base64Img: String? = null,
    val fechaRegistro: String? = null,
    val estado: Boolean? = null,
    @Transient
    var decodedImage: ByteArray? = null,
    var url:String? = "https://res.cloudinary.com/dheqy208f/image/upload/v1761518343/TooLifyWeb/Products/qun2e14i1zkmahdyyung.png"
) : Parcelable