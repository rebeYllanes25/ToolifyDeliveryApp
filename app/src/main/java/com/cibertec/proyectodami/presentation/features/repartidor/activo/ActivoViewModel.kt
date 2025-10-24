package com.cibertec.proyectodami.presentation.features.repartidor.activo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.proyectodami.domain.model.dtos.PedidoRepartidorDTO
import com.cibertec.proyectodami.domain.repository.PedidoRepartidorRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ActivoViewModel : ViewModel() {
    val pedidoActivo: LiveData<PedidoRepartidorDTO?> = PedidoRepartidorRepository.pedidoActivo

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _pedidoCompletado = MutableLiveData<Boolean>()
    val pedidoCompletado: LiveData<Boolean> = _pedidoCompletado

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun marcarEnCamino(idPedido: Int, idRepartidor: Int) {
        viewModelScope.launch {
            try {
                _loading.value = true
                PedidoRepartidorRepository.caminoPedido(idPedido, idRepartidor)

                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al actualizar el estado: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun completarPedido() {
        viewModelScope.launch {
            _loading.value = true

            delay(1000) // Simular llamada a API

            // TODO: Llamar a la API para completar el pedido
            // api.completarPedido(pedidoActivo.value?.nroPedido)

            PedidoRepartidorRepository.completarPedido()

            _loading.value = false
            _pedidoCompletado.value = true
        }
    }

    fun navegarADisponibles() {
        _pedidoCompletado.value = false
    }
}