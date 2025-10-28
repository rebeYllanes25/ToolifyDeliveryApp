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

        // Cargar pedido activo al inicializar
        cargarPedidoActivoDesdePreferences()
    }

    private fun cargarPedidoActivoDesdePreferences() {
        CoroutineScope(Dispatchers.IO).launch {
            userPreferences.pedidoActivoJson.collect { json ->
                if (json != null) {
                    try {
                        val pedido = gson.fromJson(json, PedidoRepartidorDTO::class.java)
                        withContext(Dispatchers.Main) {
                            _pedidoActivo.value = pedido
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

            // ✨ GUARDAR EN DATASTORE
            guardarPedidoActivoEnPreferences(pedidoActualizado)

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    // ✨ NUEVA: Establecer pedido activo y guardarlo
    fun setPedidoActivo(pedido: PedidoRepartidorDTO) {
        _pedidoActivo.value = pedido
        guardarPedidoActivoEnPreferences(pedido)
    }

    // ✨ NUEVA: Guardar en DataStore
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

    // ✨ ACTUALIZADA: Limpiar pedido activo
    suspend fun marcarPedidoCerca(idPedido: Int) {
        try {
            val pedidoActualizado = pedidoApi.cercaPedido(idPedido)

            // Actualizar el pedido activo con el nuevo estado
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

            // Actualizamos el pedido activo con el estado de entrega
            _pedidoActivo.postValue(pedidoEntregado)

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }



    fun completarPedido() {
        _pedidoActivo.value = null

        // Limpiar de DataStore
        CoroutineScope(Dispatchers.IO).launch {
            userPreferences.limpiarPedidoActivo()
        }
    }

    fun tienePedidoActivo(): Boolean {
        return _pedidoActivo.value != null
    }
}