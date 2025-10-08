package com.cibertec.proyectodami.ui.repartidor.activo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.proyectodami.models.dtos.PedidoRepartidorDTO
import com.cibertec.proyectodami.ui.repartidor.repository.PedidoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ActivoViewModel : ViewModel() {
    val pedidoActivo: LiveData<PedidoRepartidorDTO?> = PedidoRepository.pedidoActivo

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _pedidoCompletado = MutableLiveData<Boolean>()
    val pedidoCompletado: LiveData<Boolean> = _pedidoCompletado

    fun completarPedido() {
        viewModelScope.launch() {
            _loading.value = true

            delay(1000) // Simular llamada a API

            // TODO: Llamar a la API para completar el pedido
            // api.completarPedido(pedidoActivo.value?.nroPedido)

            PedidoRepository.completarPedido()

            _loading.value = false
            _pedidoCompletado.value = true
        }
    }

    fun navegarADisponibles() {
        _pedidoCompletado.value = false
    }
}