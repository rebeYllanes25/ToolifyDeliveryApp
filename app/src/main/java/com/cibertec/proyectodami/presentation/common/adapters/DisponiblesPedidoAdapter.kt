package com.cibertec.proyectodami.presentation.common.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.proyectodami.databinding.ItemPedidoRepartidorBinding
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.domain.model.dtos.PedidoRepartidorDTO
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt


class DisponiblesPedidoAdapter(
    private val items: MutableList<PedidoRepartidorDTO>,
    private val onAceptarClick: (PedidoRepartidorDTO) -> Unit
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

        b.tvPedidoId.text = ctx.getString(R.string.order_id_prefix, pedido.numPedido)
        b.tvCliente.text = pedido.nomCliente
        b.tvDireccion.text = pedido.direccionEntrega
        b.tvTotal.text = ctx.getString(R.string.value_price, pedido.total)

        val repartidorLatitud = -12.10
        val repartidorLongitud = -77.05

        val destinoLatitud = pedido.latitud
        val destinoLongitud = pedido.longitud

        val distanciaKm = calcularDistancia(
            repartidorLatitud, repartidorLongitud,
            destinoLatitud, destinoLongitud
        )

        val tiempoEstimadoHoras = distanciaKm / VELOCIDAD_PROMEDIO_KMH
        val tiempoEstimadoMinutos = (tiempoEstimadoHoras * 60).roundToInt()

        val distanciaFormateada = String.format("%.1f", distanciaKm)
        b.tvDistancia.text = ctx.getString(R.string.distance_km, distanciaFormateada)

        val tiempoFormateado = tiempoEstimadoMinutos.toString()
        b.tvTiempo.text = ctx.getString(R.string.time_label, tiempoFormateado)
        b.btnAceptarPedido.isEnabled = !bloqueado

        b.btnAceptarPedido.setOnClickListener {
            if (!bloqueado) {
                onAceptarClick(pedido)
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