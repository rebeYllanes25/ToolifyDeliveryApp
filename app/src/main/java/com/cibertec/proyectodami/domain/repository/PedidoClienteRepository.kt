package com.cibertec.proyectodami.domain.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cibertec.proyectodami.data.api.PedidosCliente
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.data.remote.RetrofitInstance
import com.cibertec.proyectodami.domain.model.dtos.CalificacionDTO
import com.cibertec.proyectodami.domain.model.dtos.PedidoClienteDTO
import com.cibertec.proyectodami.domain.model.dtos.requests.CalificarRequest
import retrofit2.HttpException

class PedidoClienteRepository(context: Context) {

    companion object {
        private const val TAG = "PedidoClienteRepo"
    }

    // DataStore del usuario
    private val userPreferences = UserPreferences(context)

    // Retrofit con interceptor (token incluido)
    private val pedidoApi: PedidosCliente by lazy {
        val retrofit = RetrofitInstance.create(userPreferences)
        retrofit.create(PedidosCliente::class.java)
    }

    // LiveData observables
    private val _pedidoSeguimiento = MutableLiveData<PedidoClienteDTO?>(null)
    val pedidoSeguimiento: LiveData<PedidoClienteDTO?> = _pedidoSeguimiento

    private val _pedidoCalificado = MutableLiveData<CalificacionDTO?>(null)
    val pedidoCalificado: LiveData<CalificacionDTO?> = _pedidoCalificado

    private val _pedidosEnCamino = MutableLiveData<List<PedidoClienteDTO>>(emptyList())
    val pedidosEnCamino: LiveData<List<PedidoClienteDTO>> = _pedidosEnCamino

    private val _pedidosHistorial = MutableLiveData<List<PedidoClienteDTO>>(emptyList())
    val pedidosHistorial: LiveData<List<PedidoClienteDTO>> = _pedidosHistorial


    // -------------------- PETICIONES --------------------

    suspend fun obtenerPedidosEnCamino(idCliente: Int) {
        Log.d(TAG, "═══════════════════════════════")
        Log.d(TAG, "📡 obtenerPedidosEnCamino - ID Cliente: $idCliente")
        Log.d(TAG, "═══════════════════════════════")

        try {
            val pedidos = pedidoApi.obtenerPedidosEC(idCliente, "EC")
            Log.i(TAG, "✅ Pedidos en camino obtenidos: ${pedidos.size}")
            _pedidosEnCamino.postValue(pedidos)
        } catch (e: HttpException) {
            Log.e(TAG, "❌ ERROR HTTP ${e.code()} - ${e.message()}")
            _pedidosEnCamino.postValue(emptyList())
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR GENERAL", e)
            _pedidosEnCamino.postValue(emptyList())
        }
    }

    suspend fun obtenerPedidosHistorial(idCliente: Int) {
        Log.d(TAG, "📡 obtenerPedidosHistorial - ID Cliente: $idCliente")
        try {
            val pedidos = pedidoApi.obtenerPedidosHistorial(idCliente)
            Log.i(TAG, "✅ Historial obtenido: ${pedidos.size} pedidos")
            _pedidosHistorial.postValue(pedidos)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener historial", e)
            _pedidosHistorial.postValue(emptyList())
        }
    }

    suspend fun obtenerPedidoPorId(idPedido: Int) {
        Log.d(TAG, "📡 obtenerPedidoPorId - ID Pedido: $idPedido")
        try {
            val pedido = pedidoApi.obtenerPedidoPorId(idPedido)
            Log.i(TAG, "✅ Pedido obtenido: ${pedido.nroPedido}")
            _pedidoSeguimiento.postValue(pedido)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener pedido", e)
            _pedidoSeguimiento.postValue(null)
        }
    }

    suspend fun registrarCalificacionPedido(calificacion: CalificarRequest, idPedido: Int) {
        Log.d(TAG, "📡 registrarCalificacion - Pedido: $idPedido")
        try {
            val response = pedidoApi.registrarCalificacion(idPedido, calificacion)
            if (response.isSuccessful) {
                Log.i(TAG, "✅ Calificación registrada correctamente")
                _pedidoCalificado.postValue(response.body())
            } else {
                Log.e(TAG, "❌ Error ${response.code()} al registrar calificación")
                throw Exception("Error HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al calificar pedido", e)
            throw e
        }
    }
}
