package com.cibertec.proyectodami.presentation.common.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.databinding.ItemPedidoActivoBinding
import com.cibertec.proyectodami.domain.model.dtos.PedidoRepartidorDTO

class ActivoPedidoAdapter(
    private val items: MutableList<PedidoRepartidorDTO>
) : RecyclerView.Adapter<ActivoPedidoAdapter.VH>() {

    inner class VH(val binding: ItemPedidoActivoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPedidoActivoBinding.inflate(
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

        // Configurar datos del pedido
        b.textName.text = pedido.nomCliente
        b.textPrice.text = ctx.getString(R.string.value_price, pedido.total)
        b.textAddress.text = pedido.direccion

        val tiempo = pedido.tiempoEntrega?.toString() ?: "N/A"
        b.textEta.text = ctx.getString(R.string.time_label_active, tiempo)

        // Calcular progreso basado en tiempo (ejemplo)
        val progress = ((pedido.tiempoEntrega ?: 0) * 100 / 30).coerceIn(0, 100)
        b.progressBar.progress = progress

        // Botón para navegar (abrir Google Maps)
        b.buttonNavigate.setOnClickListener {
            // TODO: Reemplazar con coordenadas reales
            val gmmIntentUri = Uri.parse("google.navigation:q=${pedido.direccion}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            if (mapIntent.resolveActivity(ctx.packageManager) != null) {
                ctx.startActivity(mapIntent)
            }
        }

        // Botón para ver detalles (o llamar)
        b.buttonCall.setOnClickListener {
            // TODO: Implementar vista de detalles o llamada
            // Por ahora podría abrir un diálogo con más información
        }
    }

    fun setData(pedido: PedidoRepartidorDTO) {
        items.clear()
        items.add(pedido)
        notifyDataSetChanged()
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }
}