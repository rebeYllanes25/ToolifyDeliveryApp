package com.cibertec.proyectodami.domain.model.enums

import com.cibertec.proyectodami.R

enum class TipoNotificacion {
    PEDIDO_CONFIRMADO,
    PEDIDO_ASIGNADO,
    PEDIDO_EN_CAMINO,
    PEDIDO_ENTREGADO,
    PEDIDO_FALLIDO,
    PROMOCION,
    SISTEMA;

    fun getIconoResId(): Int {
        return when (this) {
            PEDIDO_CONFIRMADO -> R.drawable.ic_check_circle
            PEDIDO_ASIGNADO -> R.drawable.ic_person_assigned
            PEDIDO_EN_CAMINO -> R.drawable.ic_delivery_truck
            PEDIDO_ENTREGADO -> R.drawable.ic_check_circle
            PEDIDO_FALLIDO -> R.drawable.ic_error
            PROMOCION -> R.drawable.ic_gift
            SISTEMA -> R.drawable.ic_notification
        }
    }

    fun getColorFondo(): Int {
        return when (this) {
            PEDIDO_CONFIRMADO -> R.color.green_light
            PEDIDO_ASIGNADO -> R.color.fondo_celeste_claro
            PEDIDO_EN_CAMINO -> R.color.orange_light
            PEDIDO_ENTREGADO -> R.color.green_light
            PEDIDO_FALLIDO -> R.color.orange_light_alt
            PROMOCION -> R.color.orange_light
            SISTEMA -> R.color.light_gray
        }
    }

    fun getColorIcono(): Int {
        return when (this) {
            PEDIDO_CONFIRMADO -> R.color.verde
            PEDIDO_ASIGNADO -> R.color.color_principal
            PEDIDO_EN_CAMINO -> R.color.orange
            PEDIDO_ENTREGADO -> R.color.verde
            PEDIDO_FALLIDO -> R.color.red
            PROMOCION -> R.color.orange_strong
            SISTEMA -> R.color.color_subtitulos
        }
    }

    fun getTituloString(): Int {
        return when (this) {
            PEDIDO_CONFIRMADO -> R.string.notificaciones_tipo_pedido_nuevo
            PEDIDO_ASIGNADO -> R.string.notificaciones_tipo_pedido_asignado
            PEDIDO_EN_CAMINO -> R.string.notificaciones_tipo_pedido_en_camino
            PEDIDO_ENTREGADO -> R.string.notificaciones_tipo_pedido_entregado
            PEDIDO_FALLIDO -> R.string.notificaciones_tipo_pedido_fallido
            PROMOCION -> R.string.notificaciones_tipo_promocion
            SISTEMA -> R.string.notificaciones_tipo_sistema
        }
    }
}