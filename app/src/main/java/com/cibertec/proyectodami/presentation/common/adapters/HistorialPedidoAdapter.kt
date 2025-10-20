package com.cibertec.proyectodami.presentation.features.cliente.historial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.domain.model.dtos.PedidoClienteDTO
import java.text.SimpleDateFormat
import java.util.*

class HistorialPedidoAdapter(
    private val pedidos: List<PedidoClienteDTO>,
    private val onDetalleClick: (PedidoClienteDTO) -> Unit
) : RecyclerView.Adapter<HistorialPedidoAdapter.HistorialViewHolder>() {

    inner class HistorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPedidoId: TextView = itemView.findViewById(R.id.tvPedidoId)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val statusChip: CardView = itemView.findViewById(R.id.statusChip)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotal)
        val btnDetalle: CardView = itemView.findViewById(R.id.btnDetalle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pedido_historial, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val pedido = pedidos[position]
        val context = holder.itemView.context

        holder.tvPedidoId.text = context.getString(
            R.string.formato_pedido_id,
            pedido.numPedido
        )

        holder.tvFecha.text = pedido.fecha?.let { fechaStr ->
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
                val date = inputFormat.parse(fechaStr)
                val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                outputFormat.format(date!!)
            } catch (e: Exception) {
                "Fecha inválida"
            }
        } ?: "Sin fecha"

        holder.tvTotal.text = context.getString(
            R.string.formato_total,
            pedido.total
        )

        val estadoInfo = obtenerEstadoInfo(pedido.estado)
        holder.tvStatus.text = estadoInfo.texto
        holder.tvStatus.setTextColor(ContextCompat.getColor(context, estadoInfo.colorTexto))
        holder.statusChip.setCardBackgroundColor(
            ContextCompat.getColor(context, estadoInfo.colorFondo)
        )

        holder.btnDetalle.setOnClickListener {
            onDetalleClick(pedido)
        }
    }

    override fun getItemCount() = pedidos.size

    private fun obtenerEstadoInfo(estado: String?): EstadoInfo {
        return when (estado) {
            "PE" -> EstadoInfo(
                texto = "Pendiente",
                colorTexto = R.color.orange,
                colorFondo = R.color.orange_light
            )
            "AS" -> EstadoInfo(
                texto = "Asignado",
                colorTexto = R.color.color_principal,
                colorFondo = R.color.color_fondo
            )
            "EC" -> EstadoInfo(
                texto = "En camino",
                colorTexto = R.color.orange,
                colorFondo = R.color.orange_light
            )
            "EN" -> EstadoInfo(
                texto = "Entregado",
                colorTexto = R.color.green,
                colorFondo = R.color.green_light
            )
            "FA" -> EstadoInfo(
                texto = "Fallido",
                colorTexto = R.color.orange_strong,
                colorFondo = R.color.orange_light_alt
            )
            else -> EstadoInfo(
                texto = "Desconocido",
                colorTexto = R.color.color_subtitulos,
                colorFondo = R.color.color_fondo
            )
        }
    }

    private data class EstadoInfo(
        val texto: String,
        val colorTexto: Int,
        val colorFondo: Int
    )
}
