package com.cibertec.proyectodami.domain.model.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Proveedor(
    val idProveedor: Int? = null,
    val ruc: String? = null,
    val razonSocial: String,
    val telefono: String? = null,
    val direccion: String,
    val distrito: Distrito? = null,
    val fechaRegistro: String? = null,
    val estado: Boolean? = null
) : Parcelable