package com.cibertec.proyectodami.presentation.features.repartidor.disponibles

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.proyectodami.domain.model.dtos.PedidoRepartidorDTO
import com.cibertec.proyectodami.domain.repository.PedidoRepartidorRepository
import kotlinx.coroutines.launch

class DisponiblesViewModel : ViewModel() {

    val pedidosDisponibles: LiveData<List<PedidoRepartidorDTO>> =
        PedidoRepartidorRepository.pedidosDisponibles

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _navegarAActivos = MutableLiveData<Boolean>()
    val navegarAActivos: LiveData<Boolean> = _navegarAActivos

    private val _pedidoAceptado = MutableLiveData<Boolean>()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        cargarPedidosDisponibles()
    }

    fun cargarPedidosDisponibles() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                PedidoRepartidorRepository.cargarPedidosDisponibles()
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Error al cargar pedidos: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun ordenarPorDistancia() {
        val pedidos = pedidosDisponibles.value ?: return
        PedidoRepartidorRepository.setPedidosDisponibles(pedidos.sortedBy { it.distanciaKM })
    }

    fun ordenarPorValor() {
        val pedidos = pedidosDisponibles.value ?: return
        PedidoRepartidorRepository.setPedidosDisponibles(pedidos.sortedByDescending { it.total })
    }

    fun aceptarPedido(pedido: PedidoRepartidorDTO, idRepartidor: Int) {
        viewModelScope.launch {
            _loading.value = true
            _pedidoAceptado.value = true
            _error.value = null

            try {
                PedidoRepartidorRepository.caminoPedido(pedido.idPedido, idRepartidor)

                PedidoRepartidorRepository.cargarPedidosDisponibles()

                _navegarAActivos.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Error al aceptar pedido: ${e.message}"
            } finally {
                _loading.value = false
                _pedidoAceptado.value = false
            }
        }
    }

    fun navegacionCompletada() {
        _navegarAActivos.value = false
        _pedidoAceptado.value = false
    }
}