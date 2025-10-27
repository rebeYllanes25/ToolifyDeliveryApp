package com.cibertec.proyectodami.domain.model.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Categoria(
    val idCategoria: Int,
    val descripcion: String
) : Parcelable