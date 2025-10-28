package com.cibertec.proyectodami.presentation.common.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.proyectodami.databinding.ItemPedidoRepartidorBinding
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.domain.model.dtos.PedidoRepartidorDTO
import com.cibertec.proyectodami.domain.repository.PedidoRepartidorRepository
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt


class DisponiblesPedidoAdapter(
    private val items: MutableList<PedidoRepartidorDTO>,
    private val onAceptarClick: (PedidoRepartidorDTO) -> Unit,
    private val lifecycleOwner: LifecycleOwner, // <-- agregamos
    private val idRepartidor: Int,

    ) : RecyclerView.Adapter<DisponiblesPedidoAdapter.VH>() {
    private val RADIO_TIERRA_KM = 6371.0
    private val VELOCIDAD_PROMEDIO_KMH = 20.0
    private var bloqueado = false

    inner class VH(val binding: ItemPedidoRepartidorBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPedidoRepartidorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val pedido = items[position]
        val b = holder.binding
        val ctx = b.root.context

        // Asignación de datos básicos
        b.tvPedidoId.text = ctx.getString(R.string.order_id_prefix, pedido.numPedido)
        b.tvCliente.text = pedido.nomCliente
        b.tvDireccion.text = pedido.direccionEntrega
        b.tvTotal.text = ctx.getString(R.string.value_price, pedido.total)

        // LÓGICA DE CÁLCULO DE DISTANCIA Y TIEMPO
        // 1. Coordenadas del Repartidor (DEBE SER DINÁMICO/REAL)
        // **IMPORTANTE: Reemplaza estos valores fijos con la ubicación actual del usuario.**
        val repartidorLatitud = -12.10
        val repartidorLongitud = -77.05

        // 2. Coordenadas de Destino (Asumimos que están en el DTO)
        val destinoLatitud = pedido.latitud // Necesitas este campo en PedidoRepartidorDTO
        val destinoLongitud = pedido.longitud // Necesitas este campo en PedidoRepartidorDTO

        val distanciaKm = calcularDistancia(
            repartidorLatitud, repartidorLongitud,
            destinoLatitud, destinoLongitud
        )

        // 4. Calcular Tiempo Estimado (min)
        val tiempoEstimadoHoras = distanciaKm / VELOCIDAD_PROMEDIO_KMH
        val tiempoEstimadoMinutos = (tiempoEstimadoHoras * 60).roundToInt()

        // 5. Asignar al TextView
        val distanciaFormateada = String.format("%.1f", distanciaKm)
        b.tvDistancia.text = ctx.getString(R.string.distance_km, distanciaFormateada) // Muestra el valor en el formato de tu recurso string

        val tiempoFormateado = tiempoEstimadoMinutos.toString()
        b.tvTiempo.text = ctx.getString(R.string.time_label, tiempoFormateado) // Muestra el valor en el formato de tu recurso string

        b.btnAceptarPedido.isEnabled = !bloqueado



       b.btnAceptarPedido.setOnClickListener {
            if (!bloqueado) {
                bloqueado = true
                b.btnAceptarPedido.isEnabled = false

                // Usamos lifecycleScope del Activity/Fragment
                lifecycleOwner.lifecycleScope.launch {
                    try {
                        // Llamada a tu repository para asignar el pedido al repartidor
                        PedidoRepartidorRepository.asignarRepartidor(pedido.idPedido, pedido.idRepartidor)

                        // Notificar al Activity/Fragment si quieres actualizar UI
                        onAceptarClick(pedido)

                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Error al aceptar pedido: ${e.message}", Toast.LENGTH_LONG).show()
                        bloqueado = false
                        b.btnAceptarPedido.isEnabled = true
                    }
                }
            }
        }
    }

    fun addAll(nuevos: List<PedidoRepartidorDTO>) {
        items.clear()
        items.addAll(nuevos)
        notifyDataSetChanged()
    }

    fun remove(pedido: PedidoRepartidorDTO) {
        val idx = items.indexOf(pedido)
        if (idx != -1) {
            items.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }

    fun bloquearPedidos() {
        if (bloqueado) return
        bloqueado = true
        notifyDataSetChanged()
    }

    fun desbloquearPedidos() {
        if (!bloqueado) return
        bloqueado = false
        notifyDataSetChanged()
    }

    fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return RADIO_TIERRA_KM * c
    }
}