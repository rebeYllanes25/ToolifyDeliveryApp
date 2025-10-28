package com.cibertec.proyectodami.domain.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cibertec.proyectodami.data.api.PedidosRepartidor
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.domain.model.dtos.PedidoRepartidorDTO
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object PedidoRepartidorRepository {

    private val _pedidoActivo = MutableLiveData<PedidoRepartidorDTO?>(null)
    val pedidoActivo: LiveData<PedidoRepartidorDTO?> = _pedidoActivo

    private val _pedidosDisponibles = MutableLiveData<List<PedidoRepartidorDTO>>(emptyList())
    val pedidosDisponibles: LiveData<List<PedidoRepartidorDTO>> = _pedidosDisponibles

    private lateinit var userPreferences: UserPreferences
    private val gson = Gson()
    private lateinit var pedidoApi: PedidosRepartidor

    fun init(api: PedidosRepartidor, preferences: UserPreferences) {
        pedidoApi = api
        userPreferences = preferences

        cargarPedidoActivoDesdePreferences()
    }

    private fun cargarPedidoActivoDesdePreferences() {
        CoroutineScope(Dispatchers.IO).launch {
            userPreferences.pedidoActivoJson.collect { json ->
                if (json != null) {
                    try {
                        val pedido = gson.fromJson(json, PedidoRepartidorDTO::class.java)
                        withContext(Dispatchers.Main) {
                            // Solo mostrar en Activo si está en estado ACEPTADO
                            if (pedido.estado == "AS" || pedido.estado == "CR") {
                                _pedidoActivo.value = pedido
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun setPedidosDisponibles(pedidos: List<PedidoRepartidorDTO>) {
        _pedidosDisponibles.value = pedidos
    }

    suspend fun cargarPedidosDisponibles() {
        try {
            val pedidos = pedidoApi.obtenerPedidosAceptados()

            val pedidoActivo = _pedidoActivo.value
            val pedidosFiltrados = if (pedidoActivo != null) {
                pedidos.filter { it.numPedido != pedidoActivo.numPedido }
            } else {
                pedidos
            }

            _pedidosDisponibles.postValue(pedidosFiltrados)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun caminoPedido(pedido: Int, idRepartidor: Int) {
        try {
            val pedidoActualizado = pedidoApi.caminoPedido(pedido, idRepartidor)
            _pedidoActivo.postValue(pedidoActualizado)

            guardarPedidoActivoEnPreferences(pedidoActualizado)

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun setPedidoActivo(pedido: PedidoRepartidorDTO) {
        _pedidoActivo.value = pedido
        guardarPedidoActivoEnPreferences(pedido)
    }

    private fun guardarPedidoActivoEnPreferences(pedido: PedidoRepartidorDTO) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pedidoJson = gson.toJson(pedido)
                userPreferences.guardarPedidoActivo(
                    idPedido = pedido.idPedido,
                    numPedido = pedido.numPedido,
                    estado = pedido.estado,
                    pedidoJson = pedidoJson
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun marcarPedidoCerca(idPedido: Int) {
        try {
            val pedidoActualizado = pedidoApi.cercaPedido(idPedido)

            _pedidoActivo.postValue(pedidoActualizado)

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun entregarPedido(idPedido: Int, codigoQR: String, idRepartidor: Int) {
        try {
            val pedidoEntregado = pedidoApi.entregadoPedido(
                idPedido = idPedido,
                codigoQr = codigoQR,
                idRepartidor = idRepartidor
            )

            _pedidoActivo.postValue(pedidoEntregado)

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun iniciarEntrega() {
        _pedidoActivo.postValue(null)

        CoroutineScope(Dispatchers.IO).launch {
            userPreferences.limpiarPedidoActivo()
        }
    }

    fun completarPedido() {
        _pedidoActivo.value = null

        CoroutineScope(Dispatchers.IO).launch {
            userPreferences.limpiarPedidoActivo()
        }
    }

    fun tienePedidoActivo(): Boolean {
        return _pedidoActivo.value != null
    }
}