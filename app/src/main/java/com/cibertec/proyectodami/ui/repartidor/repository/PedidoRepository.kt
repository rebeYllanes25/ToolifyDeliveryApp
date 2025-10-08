package com.cibertec.proyectodami.ui.repartidor.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cibertec.proyectodami.models.dtos.PedidoRepartidorDTO

object PedidoRepository {
    private val _pedidoActivo = MutableLiveData<PedidoRepartidorDTO?>(null)
    val pedidoActivo: LiveData<PedidoRepartidorDTO?> = _pedidoActivo

    private val _pedidosDisponibles = MutableLiveData<List<PedidoRepartidorDTO>>(emptyList())
    val pedidosDisponibles: LiveData<List<PedidoRepartidorDTO>> = _pedidosDisponibles

    fun setPedidosDisponibles(pedidos: List<PedidoRepartidorDTO>) {
        _pedidosDisponibles.value = pedidos
    }

    fun aceptarPedido(pedido: PedidoRepartidorDTO) {
        // Establecer como pedido activo
        _pedidoActivo.value = pedido

        // Remover de disponibles
        val disponibles = _pedidosDisponibles.value?.toMutableList() ?: mutableListOf()
        disponibles.remove(pedido)
        _pedidosDisponibles.value = disponibles

        // TODO: Aquí harías la llamada a la API
        // api.aceptarPedido(pedido.nroPedido)
    }

    fun completarPedido() {
        _pedidoActivo.value = null
        // TODO: Llamada a API para marcar como completado
    }

    fun tienePedidoActivo(): Boolean {
        return _pedidoActivo.value != null
    }
}