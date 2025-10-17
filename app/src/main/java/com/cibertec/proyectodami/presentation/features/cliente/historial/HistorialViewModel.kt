package com.cibertec.proyectodami.presentation.features.cliente.historial

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cibertec.proyectodami.domain.model.dtos.FiltrosData

class HistorialViewModel : ViewModel() {

    private val _filtrosActivos = MutableLiveData<FiltrosData?>()
    val filtrosActivos: LiveData<FiltrosData?> = _filtrosActivos

    private val _hayFiltrosAplicados = MutableLiveData<Boolean>(false)
    val hayFiltrosAplicados: LiveData<Boolean> = _hayFiltrosAplicados

    private val _historialList = MutableLiveData<List<Any>>()
    val historialList: LiveData<List<Any>> = _historialList

    private val _historialFiltrado = MutableLiveData<List<Any>>()
    val historialFiltrado: LiveData<List<Any>> = _historialFiltrado

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
