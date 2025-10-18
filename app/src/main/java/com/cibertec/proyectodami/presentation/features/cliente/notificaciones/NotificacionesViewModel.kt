package com.cibertec.proyectodami.presentation.features.cliente.notificaciones

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cibertec.proyectodami.domain.model.entities.Notificacion
import com.cibertec.proyectodami.domain.model.enums.FiltroNotificacion
import com.cibertec.proyectodami.domain.repository.NotificacionesRepository

class NotificacionesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NotificacionesRepository(application)

    private val _notificaciones = MutableLiveData<List<Notificacion>>()
    val notificaciones: LiveData<List<Notificacion>> = _notificaciones

    private val _cantidadSinLeer = MutableLiveData<Int>()
    val cantidadSinLeer: LiveData<Int> = _cantidadSinLeer

    private val _filtro = MutableLiveData(FiltroNotificacion.TODAS)
    val filtro: LiveData<FiltroNotificacion> = _filtro

    init {
        cargarNotificaciones()
    }

    fun cargarNotificaciones() {
        val todasLasNotificaciones = repository.obtenerTodas()
        _notificaciones.value = todasLasNotificaciones
        _cantidadSinLeer.value = repository.contarNoLeidas()
    }

    fun cambiarFiltro(nuevoFiltro: FiltroNotificacion) {
        _filtro.value = nuevoFiltro
    }

    fun obtenerNotificacionesFiltradas(): List<Notificacion> {
        val todas = _notificaciones.value ?: emptyList()
        return when (_filtro.value) {
            FiltroNotificacion.TODAS -> todas
            FiltroNotificacion.SIN_LEER -> todas.filter { !it.leida }
            FiltroNotificacion.LEIDAS -> todas.filter { it.leida }
            null -> todas
        }
    }

    fun marcarComoLeida(notificacion: Notificacion) {
        repository.marcarComoLeida(notificacion.id, !notificacion.leida)
        cargarNotificaciones()
    }

    fun marcarTodasComoLeidas() {
        repository.marcarTodasComoLeidas()
        cargarNotificaciones()
    }

    fun eliminarNotificacion(notificacion: Notificacion) {
        repository.eliminar(notificacion.id)
        cargarNotificaciones()
    }

    fun eliminarTodas() {
        repository.eliminarTodas()
        cargarNotificaciones()
    }

    fun onNotificacionClick(notificacion: Notificacion) {
        if (!notificacion.leida) {
            marcarComoLeida(notificacion)
        }
        // LLevar al detalle del pedido
    }
}