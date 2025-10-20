package com.cibertec.proyectodami.domain.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cibertec.proyectodami.data.api.PedidosCliente
import com.cibertec.proyectodami.domain.model.dtos.CalificacionDTO
import com.cibertec.proyectodami.domain.model.dtos.PedidoClienteDTO
import com.cibertec.proyectodami.domain.model.dtos.requests.CalificarRequest


object PedidoClienteRepository {

    private val _pedidoSeguimiento = MutableLiveData<PedidoClienteDTO?>(null)
    val pedidoSeguimiento: LiveData<PedidoClienteDTO?> = _pedidoSeguimiento

    private val _pedidoCalificado = MutableLiveData<CalificacionDTO?>(null)
    val pedidoCalificado: LiveData<CalificacionDTO?> = _pedidoCalificado

    private val _pedidosEnCamino = MutableLiveData<List<PedidoClienteDTO>>(emptyList())
    val pedidosEnCamino: LiveData<List<PedidoClienteDTO>> = _pedidosEnCamino

    private val _pedidosHistorial = MutableLiveData<List<PedidoClienteDTO>>(emptyList())
    val pedidosHistorial: LiveData<List<PedidoClienteDTO>> = _pedidosHistorial

    private lateinit var pedidoApi: PedidosCliente

    fun init(api: PedidosCliente) {
        this.pedidoApi = api
    }

    fun setPedidosEnCamino(pedidos: List<PedidoClienteDTO>) {
        _pedidosEnCamino.value = pedidos
    }

    fun setPedidosHistorial(pedidos: List<PedidoClienteDTO>) {
        _pedidosHistorial.value = pedidos
    }

    suspend fun obtenerPedidosEnCamino(idCliente: Int) {
        try {
            val pedidos = pedidoApi.obtenerPedidosEC(idCliente, "EC")
            _pedidosEnCamino.postValue(pedidos)
        } catch (e: Exception) {
            e.printStackTrace()
            _pedidosEnCamino.postValue(emptyList())
        }
    }

    suspend fun obtenerPedidosHistorial(idCliente: Int) {
        try {
            val pedidos = pedidoApi.obtenerPedidosHistorial(idCliente)
            _pedidosHistorial.postValue(pedidos)
        } catch (e: Exception) {
            e.printStackTrace()
            _pedidosHistorial.postValue(emptyList())
        }
    }

    suspend fun obtenerPedidoPorId(idPedido: Int) {
        try {
            val pedido = pedidoApi.obtenerPedidoPorId(idPedido)
            _pedidoSeguimiento.postValue(pedido)
        } catch (e: Exception) {
            e.printStackTrace()
            _pedidoSeguimiento.postValue(null)
        }
    }

    suspend fun registrarCalificacionPedido(calificacion: CalificarRequest, idPedido: Int) {
        try {
            val response = pedidoApi.registrarCalificacion(idPedido, calificacion)
            if (response.isSuccessful) {
                _pedidoCalificado.postValue(response.body())
            } else {
                throw Exception("Error al registrar calificación: ${response.code()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
