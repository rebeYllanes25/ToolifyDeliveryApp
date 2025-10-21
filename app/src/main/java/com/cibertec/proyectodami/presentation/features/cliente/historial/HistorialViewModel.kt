package com.cibertec.proyectodami.presentation.features.cliente.historial

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cibertec.proyectodami.domain.model.dtos.FiltrosData
import com.cibertec.proyectodami.domain.model.dtos.PedidoClienteDTO
import com.cibertec.proyectodami.domain.repository.PedidoClienteRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PedidoClienteRepository(application)

    // ✅ USA DIRECTAMENTE EL LIVEDATA DEL REPOSITORY:
    val historialDePedidos: LiveData<List<PedidoClienteDTO>> = repository.pedidosHistorial

    private val _historialFiltrado = MutableLiveData<List<PedidoClienteDTO>>(emptyList())
    val historialFiltrado: LiveData<List<PedidoClienteDTO>> = _historialFiltrado

    private val _hayFiltrosAplicados = MutableLiveData(false)
    val hayFiltrosAplicados: LiveData<Boolean> = _hayFiltrosAplicados

    private val _filtrosActivos = MutableLiveData<FiltrosData?>(null)
    val filtrosActivos: LiveData<FiltrosData?> = _filtrosActivos

    init {
        historialDePedidos.observeForever { pedidos ->
            if (_hayFiltrosAplicados.value == false) {
                _historialFiltrado.value = pedidos
            }
        }
    }

    fun cargarPedidos(idCliente: Int) {
        viewModelScope.launch {
            repository.obtenerPedidosHistorial(idCliente)
        }
    }

    fun aplicarFiltros(filtros: FiltrosData) {
        _filtrosActivos.value = filtros
        _hayFiltrosAplicados.value = true

        val listaOriginal = historialDePedidos.value ?: emptyList()  // ✅ Ahora sí tiene datos

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaInicio = filtros.fechaInicio.takeIf { it.isNotBlank() }?.let { dateFormat.parse(it) }
        val fechaFin = filtros.fechaFin.takeIf { it.isNotBlank() }?.let { dateFormat.parse(it) }

        val listaFiltrada = listaOriginal.filter { pedido ->
            val coincidePrecio = pedido.total in filtros.precioMin.toDouble()..filtros.precioMax.toDouble()

            val coincideFecha = try {
                val fechaPedido = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    .parse(pedido.fecha)
                when {
                    fechaInicio != null && fechaFin != null -> fechaPedido in fechaInicio..fechaFin
                    fechaInicio != null -> fechaPedido?.after(fechaInicio) == true
                    fechaFin != null -> fechaPedido?.before(fechaFin) == true
                    else -> true
                }
            } catch (e: Exception) {
                true
            }

            coincidePrecio && coincideFecha
        }

        _historialFiltrado.value = listaFiltrada
    }

    fun limpiarFiltros() {
        _hayFiltrosAplicados.value = false
        _filtrosActivos.value = null
        _historialFiltrado.value = historialDePedidos.value
    }

    override fun onCleared() {
        super.onCleared()
        historialDePedidos.removeObserver { }
    }
}