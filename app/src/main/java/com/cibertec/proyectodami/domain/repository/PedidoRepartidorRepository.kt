package com.cibertec.proyectodami.domain.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cibertec.proyectodami.data.api.PedidosRepartidor
import com.cibertec.proyectodami.domain.model.dtos.PedidoRepartidorDTO

object PedidoRepartidorRepository {

    private val _pedidoActivo = MutableLiveData<PedidoRepartidorDTO?>(null)
    val pedidoActivo: LiveData<PedidoRepartidorDTO?> = _pedidoActivo

    private val _pedidosDisponibles = MutableLiveData<List<PedidoRepartidorDTO>>(emptyList())
    val pedidosDisponibles: LiveData<List<PedidoRepartidorDTO>> = _pedidosDisponibles

    private lateinit var pedidoApi: PedidosRepartidor

    fun init(api: PedidosRepartidor) {
        this.pedidoApi = api
    }
    fun setPedidosDisponibles(pedidos: List<PedidoRepartidorDTO>) {
        _pedidosDisponibles.value = pedidos
    }

    suspend fun cargarPedidosDisponibles() {
        try {
            val pedidos = pedidoApi.obtenerPedidosPendientes()

            val pedidoActivo = _pedidoActivo.value
            val pedidosFiltrados = if (pedidoActivo != null) {
                pedidos.filter { it.numPedido != pedidoActivo.numPedido }
            } else {
                pedidos
            }

            _pedidosDisponibles.postValue(pedidosFiltrados)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun aceptarPedido(pedido: PedidoRepartidorDTO, idRepartidor: Int) {
        try {
            val pedidoActualizado = pedidoApi.asignarRepartidor(pedido.idPedido, idRepartidor)
            _pedidoActivo.postValue(pedidoActualizado)

            val disponibles = _pedidosDisponibles.value?.toMutableList() ?: mutableListOf()
            disponibles.remove(pedido)
            _pedidosDisponibles.postValue(disponibles)

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun completarPedido() {
        _pedidoActivo.value = null
    }

    fun tienePedidoActivo(): Boolean {
        return _pedidoActivo.value != null
    }
}
