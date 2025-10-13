package com.cibertec.proyectodami.presentation.features.repartidor.disponibles

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.proyectodami.domain.model.dtos.PedidoRepartidorDTO
import com.cibertec.proyectodami.domain.repository.PedidoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DisponiblesViewModel : ViewModel() {

    // Observar desde el repository
    val pedidosDisponibles: LiveData<List<PedidoRepartidorDTO>> =
        PedidoRepository.pedidosDisponibles

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    // NUEVO: Para navegar a la pestaña de activos
    private val _navegarAActivos = MutableLiveData<Boolean>()
    val navegarAActivos: LiveData<Boolean> = _navegarAActivos

    // NUEVO: Para bloquear la interfaz
    private val _pedidoAceptado = MutableLiveData<Boolean>()
    val pedidoAceptado: LiveData<Boolean> = _pedidoAceptado

    init {
        cargarPedidosDisponibles()
    }

    fun cargarPedidosDisponibles() {
        viewModelScope.launch {
            _loading.value = true
            delay(1000) // Simular carga

            // TODO: Reemplazar con llamada real a API/Repository
            val pedidos = obtenerPedidosMock()

            // NUEVO: Filtrar el pedido activo si existe
            val pedidoActivo = PedidoRepository.pedidoActivo.value
            val pedidosFiltrados = if (pedidoActivo != null) {
                pedidos.filter { it.nroPedido != pedidoActivo.nroPedido }
            } else {
                pedidos
            }

            PedidoRepository.setPedidosDisponibles(pedidosFiltrados)

            _loading.value = false
        }
    }

    fun ordenarPorDistancia() {
        val pedidos = pedidosDisponibles.value ?: return
        PedidoRepository.setPedidosDisponibles(pedidos.sortedBy { it.distanciaKM })
    }

    fun ordenarPorValor() {
        val pedidos = pedidosDisponibles.value ?: return
        PedidoRepository.setPedidosDisponibles(pedidos.sortedByDescending { it.total })
    }

    fun aceptarPedido(pedido: PedidoRepartidorDTO) {
        viewModelScope.launch {
            _loading.value = true
            _pedidoAceptado.value = true // Bloquear UI

            delay(500) // Simular llamada a API

            // Aceptar pedido en el repository
            PedidoRepository.aceptarPedido(pedido)

            _loading.value = false
            _navegarAActivos.value = true // Señal para navegar
        }
    }

    fun navegacionCompletada() {
        _navegarAActivos.value = false
        _pedidoAceptado.value = false
    }

    private fun obtenerPedidosMock(): List<PedidoRepartidorDTO> {
        return listOf(
            PedidoRepartidorDTO(
                nroPedido = "1001",
                nomCliente = "María González",
                direccion = "Calle Mayor 45, 2º B\n28013 Madrid, España",
                fecha = null,
                movilidad = "Moto",
                total = 45.80,
                distanciaKM = 2.3,
                especificaciones = null,
                estado = "PE",
                tiempoEntrega = 15
            ),
            PedidoRepartidorDTO(
                nroPedido = "1002",
                nomCliente = "Juan Pérez",
                direccion = "Av. Principal 123\nSan Isidro, Lima",
                fecha = null,
                movilidad = "Moto",
                total = 32.50,
                distanciaKM = 1.5,
                especificaciones = "Sin azúcar",
                estado = "PE",
                tiempoEntrega = 10
            ),
            PedidoRepartidorDTO(
                nroPedido = "1003",
                nomCliente = "Ana Torres",
                direccion = "Jr. Los Olivos 890\nMiraflores, Lima",
                fecha = null,
                movilidad = "Moto",
                total = 78.90,
                distanciaKM = 4.2,
                especificaciones = null,
                estado = "PE",
                tiempoEntrega = 25
            ),
            PedidoRepartidorDTO(
                nroPedido = "1004",
                nomCliente = "Carlos Ruiz",
                direccion = "Calle Las Flores 456\nSurco, Lima",
                fecha = null,
                movilidad = "Moto",
                total = 56.20,
                distanciaKM = 3.1,
                especificaciones = "Entregar antes de las 7pm",
                estado = "PE",
                tiempoEntrega = 18
            )
        )
    }
}