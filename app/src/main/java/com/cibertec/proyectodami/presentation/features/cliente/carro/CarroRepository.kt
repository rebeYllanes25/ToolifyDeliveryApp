package com.cibertec.proyectodami.presentation.features.cliente.carro

import com.cibertec.proyectodami.domain.model.dtos.CarroItem
import com.cibertec.proyectodami.domain.model.entities.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CarroRepository {
    private val _items = MutableStateFlow<List<CarroItem>>(emptyList())
    val items: StateFlow<List<CarroItem>> = _items.asStateFlow()

    private val _cantidadTotal = MutableStateFlow(0)
    val cantidadTotal: StateFlow<Int> = _cantidadTotal.asStateFlow()

    private val _total = MutableStateFlow(0.0)
    val total: StateFlow<Double> = _total.asStateFlow()

    fun agregarProducto(producto: Producto): Boolean {
        val currentItems = _items.value.toMutableList()

        val existingItem = currentItems.find { it.producto.idProducto == producto.idProducto }

        return if (existingItem != null) {
            if (existingItem.incrementar()) {
                _items.value = currentItems
                actualizarTotales()
                true
            } else {
                false
            }
        } else {
            if (producto.stock > 0) {
                currentItems.add(CarroItem(producto, 1))
                _items.value = currentItems
                actualizarTotales()
                true
            } else {
                false
            }
        }
    }

    fun incrementarCantidad(idProducto: Int): Boolean {
        val currentItems = _items.value.toMutableList()
        val index = currentItems.indexOfFirst { it.producto.idProducto == idProducto }

        if (index != -1) {
            val item = currentItems[index]
            if (item.cantidad < item.producto.stock) {
                currentItems[index] = item.copy(cantidad = item.cantidad + 1)
                _items.value = currentItems
                actualizarTotales()
                return true
            }
        }
        return false
    }


    fun decrementarCantidad(idProducto: Int) {
        val currentItems = _items.value.toMutableList()
        val index = currentItems.indexOfFirst { it.producto.idProducto == idProducto }

        if (index != -1) {
            val item = currentItems[index]
            if (item.cantidad > 1) {
                currentItems[index] = item.copy(cantidad = item.cantidad - 1)
            } else {
                currentItems.removeAt(index)
            }

            _items.value = currentItems
            actualizarTotales()
        }
    }


    fun eliminarProducto(idProducto: Int) {
        val currentItems = _items.value.toMutableList()
        currentItems.removeAll { it.producto.idProducto == idProducto }
        _items.value = currentItems
        actualizarTotales()
    }

    fun limpiarCarrito() {
        _items.value = emptyList()
        actualizarTotales()
    }

    private fun actualizarTotales() {
        val items = _items.value
        _cantidadTotal.value = items.sumOf { it.cantidad }
        _total.value = items.sumOf { it.subtotal }
    }

    fun getItem(idProducto: Int): CarroItem? {
        return _items.value.find { it.producto.idProducto == idProducto }
    }

    fun getCantidadProducto(idProducto: Int): Int {
        return _items.value.find { it.producto.idProducto == idProducto }?.cantidad ?: 0
    }
}