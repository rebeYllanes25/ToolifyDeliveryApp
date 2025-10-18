package com.cibertec.proyectodami.domain.model.entities

import com.cibertec.proyectodami.domain.model.enums.TipoNotificacion
import java.util.Date

data class Notificacion(
    val id: String,
    val tipo: TipoNotificacion,
    val titulo: String,
    val mensaje: String,
    val fechaCreacion: Date,
    val leida: Boolean = false,
    val pedidoId: String? = null,
    val datos: String? = null
)