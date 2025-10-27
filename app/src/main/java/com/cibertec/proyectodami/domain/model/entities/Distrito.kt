package com.cibertec.proyectodami.domain.model.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Distrito(
    val idDistrito: Int? = null,
    val nombre: String
) : Parcelable