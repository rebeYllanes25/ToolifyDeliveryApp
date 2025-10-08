package com.cibertec.proyectodami.ui.repartidor.disponibles

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.proyectodami.databinding.ItemPedidoRepartidorBinding
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.models.dtos.PedidoRepartidorDTO

class DisponiblesPedidoAdapter(
    private val items: MutableList<PedidoRepartidorDTO>,
    private val onAceptarClick: (PedidoRepartidorDTO) -> Unit
) : RecyclerView.Adapter<DisponiblesPedidoAdapter.VH>() {

    private var bloqueado = false // NUEVO: Flag para bloquear interacción

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

        b.tvPedidoId.text = ctx.getString(R.string.order_id_prefix, pedido.nroPedido)
        b.tvCliente.text = pedido.nomCliente
        b.tvDireccion.text = pedido.direccion
        b.tvTotal.text = ctx.getString(R.string.value_price, pedido.total)
        b.tvDistancia.text = ctx.getString(R.string.distance_km, pedido.distanciaKM.toString())
        val tiempo = pedido.tiempoEntrega?.toString() ?: "N/A"
        b.tvTiempo.text = ctx.getString(R.string.time_label, tiempo)

        // MODIFICADO: Deshabilitar botón si está bloqueado
        b.btnAceptarPedido.isEnabled = !bloqueado

        if (bloqueado) {
            // Botón bloqueado - gris
            b.btnAceptarPedido.setBackgroundColor(
                ctx.getColor(android.R.color.darker_gray)
            )
            b.btnAceptarPedido.setTextColor(
                ctx.getColor(android.R.color.white)
            )
            b.btnAceptarPedido.alpha = 0.9f
            b.root.alpha = 0.9f
        } else {
            // Botón activo - color original (naranja)
            b.btnAceptarPedido.setBackgroundColor(
                ctx.getColor(R.color.orange_strong)
            )
            b.btnAceptarPedido.setTextColor(
                ctx.getColor(android.R.color.white)
            )
            b.btnAceptarPedido.alpha = 1f
            b.root.alpha = 1f
        }

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

    // NUEVO: Método para bloquear todos los pedidos
    fun bloquearPedidos() {
        if (bloqueado) return // Ya está bloqueado, evitar múltiples notificaciones
        bloqueado = true
        notifyDataSetChanged()
    }

    // NUEVO: Método para desbloquear (por si se cancela)
    fun desbloquearPedidos() {
        if (!bloqueado) return // Ya está desbloqueado
        bloqueado = false
        notifyDataSetChanged()
    }
}