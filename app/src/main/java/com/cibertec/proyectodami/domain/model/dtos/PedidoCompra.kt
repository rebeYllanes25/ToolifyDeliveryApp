package com.cibertec.proyectodami.domain.model.dtos

import com.cibertec.proyectodami.domain.model.entities.Venta
import java.math.BigDecimal
import java.time.LocalDateTime

data class PedidoCompra(
    var idPedido: Int? = null,
    var venta: Venta? = null,
    var fecha: LocalDateTime? = null,
    var direccionEntrega: String? = null,
    var latitud: BigDecimal? = null,
    var longitud: BigDecimal? = null,
    var numPedido: String? = null,
    var qrVerificacion: String? = null,
    var repartidor: UsuarioDTO? = null,
    var movilidad: String? = null,
    var fechaAsignacion: LocalDateTime? = null,
    var fechaEnCamino: LocalDateTime? = null,
    var fechaEntregado: LocalDateTime? = null,
    var tiempoEntrega: Short? = null,
    var estado: String? = null,
    var observaciones: String? = null
)