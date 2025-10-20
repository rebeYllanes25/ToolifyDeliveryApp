package com.cibertec.proyectodami.presentation.features.cliente.inicio

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.proyectodami.domain.model.dtos.PedidoClienteDTO
import com.cibertec.proyectodami.domain.repository.PedidoClienteRepository
import kotlinx.coroutines.launch

class InicioViewModel : ViewModel() {

    val pedidosEnCamino: LiveData<List<PedidoClienteDTO>> = PedidoClienteRepository.pedidosEnCamino

    fun cargarPedidos(idCliente: Int) {
        viewModelScope.launch {
            PedidoClienteRepository.obtenerPedidosEnCamino(idCliente)
        }
    }
}