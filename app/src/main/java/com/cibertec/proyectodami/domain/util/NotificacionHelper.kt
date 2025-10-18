package com.cibertec.proyectodami.domain.util

import android.content.Context
import com.cibertec.proyectodami.domain.model.entities.Notificacion
import com.cibertec.proyectodami.domain.model.enums.TipoNotificacion
import com.cibertec.proyectodami.domain.repository.NotificacionesRepository
import java.util.Date
import java.util.UUID

class NotificacionHelper(private val context: Context) {

    private val repository = NotificacionesRepository(context)

    fun notificarPedidoConfirmado(pedidoId: String) {
        val notificacion = Notificacion(
            id = UUID.randomUUID().toString(),
            tipo = TipoNotificacion.PEDIDO_CONFIRMADO,
            titulo = "Pedido confirmado",
            mensaje = "Tu pedido #$pedidoId ha sido confirmado exitosamente",
            fechaCreacion = Date(),
            leida = false,
            pedidoId = pedidoId
        )
        repository.insertar(notificacion)
    }

    fun notificarRepartidorAsignado(pedidoId: String, nombreRepartidor: String) {
        val notificacion = Notificacion(
            id = UUID.randomUUID().toString(),
            tipo = TipoNotificacion.PEDIDO_ASIGNADO,
            titulo = "Repartidor asignado",
            mensaje = "$nombreRepartidor fue asignado a tu pedido #$pedidoId",
            fechaCreacion = Date(),
            leida = false,
            pedidoId = pedidoId
        )
        repository.insertar(notificacion)
    }

    fun notificarPedidoEnCamino(pedidoId: String, tiempoEstimado: Int) {
        val notificacion = Notificacion(
            id = UUID.randomUUID().toString(),
            tipo = TipoNotificacion.PEDIDO_EN_CAMINO,
            titulo = "Tu pedido está en camino",
            mensaje = "El repartidor está de camino a tu ubicación. Tiempo estimado: $tiempoEstimado minutos",
            fechaCreacion = Date(),
            leida = false,
            pedidoId = pedidoId
        )
        repository.insertar(notificacion)
    }

    fun notificarPedidoEntregado(pedidoId: String) {
        val notificacion = Notificacion(
            id = UUID.randomUUID().toString(),
            tipo = TipoNotificacion.PEDIDO_ENTREGADO,
            titulo = "Pedido entregado",
            mensaje = "Tu pedido #$pedidoId fue entregado exitosamente. ¡Esperamos que lo disfrutes!",
            fechaCreacion = Date(),
            leida = false,
            pedidoId = pedidoId
        )
        repository.insertar(notificacion)
    }

    fun notificarPedidoFallido(pedidoId: String, motivo: String) {
        val notificacion = Notificacion(
            id = UUID.randomUUID().toString(),
            tipo = TipoNotificacion.PEDIDO_FALLIDO,
            titulo = "Problema con tu pedido",
            mensaje = "No se pudo completar la entrega del pedido #$pedidoId. Motivo: $motivo",
            fechaCreacion = Date(),
            leida = false,
            pedidoId = pedidoId
        )
        repository.insertar(notificacion)
    }

    fun notificarPromocion(titulo: String, mensaje: String) {
        val notificacion = Notificacion(
            id = UUID.randomUUID().toString(),
            tipo = TipoNotificacion.PROMOCION,
            titulo = titulo,
            mensaje = mensaje,
            fechaCreacion = Date(),
            leida = false
        )
        repository.insertar(notificacion)
    }

    fun notificarSistema(titulo: String, mensaje: String) {
        val notificacion = Notificacion(
            id = UUID.randomUUID().toString(),
            tipo = TipoNotificacion.SISTEMA,
            titulo = titulo,
            mensaje = mensaje,
            fechaCreacion = Date(),
            leida = false
        )
        repository.insertar(notificacion)
    }

    fun crearNotificacionesDePrueba() {
        notificarPedidoConfirmado("12345")
        Thread.sleep(100)

        notificarRepartidorAsignado("12345", "Rebu")
        Thread.sleep(100)

        notificarPedidoEnCamino("12345", 15)
        Thread.sleep(100)

        notificarPedidoEntregado("12344")
        Thread.sleep(100)

        notificarPromocion(
            "El pedido fue enviado",
            "Perame we ya llegoooooo"
        )
    }
}