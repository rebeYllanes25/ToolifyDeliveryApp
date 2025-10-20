package com.cibertec.proyectodami.presentation.features.cliente.inicio

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.cibertec.proyectodami.domain.model.dtos.PedidoClienteDTO
import com.cibertec.proyectodami.domain.repository.PedidoClienteRepository
import kotlinx.coroutines.launch

class InicioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PedidoClienteRepository(application)

    val pedidosEnCamino: LiveData<List<PedidoClienteDTO>> = repository.pedidosEnCamino

    fun cargarPedidos(idCliente: Int) {
        Log.d("InicioViewModel", "🚀 Iniciando carga de pedidos")
        Log.d("InicioViewModel", "ID Cliente: $idCliente")


        viewModelScope.launch {
            try {
                repository.obtenerPedidosEnCamino(idCliente)
                Log.i("InicioViewModel", "✅ Petición completada correctamente")
            } catch (e: Exception) {
                Log.e("InicioViewModel", "❌ Error al cargar pedidos", e)
            }
        }
    }
}
