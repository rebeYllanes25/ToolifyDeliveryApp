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
    val pedidoAceptado: LiveData<Boolean> = _pedidoAceptado

    init {
        cargarPedidosDisponibles()
    }

    fun cargarPedidosDisponibles() {
        viewModelScope.launch {
            _loading.value = true
            try {
                PedidoRepartidorRepository.cargarPedidosDisponibles()
            } catch (e: Exception) {
                e.printStackTrace()
                // Aquí podrías mostrar un mensaje de error si deseas
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

            try {
                PedidoRepartidorRepository.aceptarPedido(pedido, idRepartidor)
                _navegarAActivos.value = true
            } catch (e: Exception) {
                e.printStackTrace()
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
