package com.cibertec.proyectodami.presentation.features.cliente.historial

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.proyectodami.domain.model.dtos.FiltrosData
import com.cibertec.proyectodami.domain.model.dtos.PedidoClienteDTO
import com.cibertec.proyectodami.domain.repository.PedidoClienteRepository
import kotlinx.coroutines.launch

class HistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PedidoClienteRepository(application)

    val historialDePedidos: LiveData<List<PedidoClienteDTO>> = repository.pedidosHistorial

    private val _filtrosActivos = MutableLiveData<FiltrosData?>()
    val filtrosActivos: LiveData<FiltrosData?> = _filtrosActivos

    private val _hayFiltrosAplicados = MutableLiveData<Boolean>(false)
    val hayFiltrosAplicados: LiveData<Boolean> = _hayFiltrosAplicados

    private val _historialList = MutableLiveData<List<Any>>()
    val historialList: LiveData<List<Any>> = _historialList

    private val _historialFiltrado = MutableLiveData<List<Any>>()
    val historialFiltrado: LiveData<List<Any>> = _historialFiltrado

    fun cargarPedidos(idCliente: Int) {
        Log.d("HistorialViewModel", "🚀 Iniciando carga de pedidos")
        Log.d("HistorialViewModel", "ID Cliente: $idCliente")

        viewModelScope.launch {
            try {
                repository.obtenerPedidosHistorial(idCliente)
                Log.i("HistorialViewModel", "Petición completada correctamente")
            } catch (e: Exception) {
                Log.e("HistorialViewModel", "Error al cargar pedidos", e)
            }
        }
    }

    fun aplicarFiltros(fechaInicio: String, fechaFin: String, precioMin: Int, precioMax: Int) {
        val filtros = FiltrosData(fechaInicio, fechaFin, precioMin, precioMax)
        _filtrosActivos.value = filtros
        _hayFiltrosAplicados.value = true

        filtrarHistorial(filtros)
    }

    fun limpiarFiltros() {
        _filtrosActivos.value = null
        _hayFiltrosAplicados.value = false
        _historialFiltrado.value = _historialList.value
    }

    private fun filtrarHistorial(filtros: FiltrosData) {
        // TO DO: logica del filtrado segun las fechas y precios
        val listaOriginal = _historialList.value ?: return

    }

}
