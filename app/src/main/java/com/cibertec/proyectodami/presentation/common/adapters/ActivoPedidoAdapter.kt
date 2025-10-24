package com.cibertec.proyectodami.presentation.common.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.databinding.ItemPedidoActivoBinding
import com.cibertec.proyectodami.domain.model.dtos.PedidoRepartidorDTO
import com.cibertec.proyectodami.presentation.features.repartidor.activo.PedidoDetailBottom
import com.cibertec.proyectodami.presentation.features.repartidor.localizacion.LocalizacionActivity

class ActivoPedidoAdapter(
    private val activity: FragmentActivity,
    private val items: MutableList<PedidoRepartidorDTO>,
    private val onNavigateClick: ((PedidoRepartidorDTO) -> Unit)? = null
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
        b.textAddress.text = pedido.direccionEntrega

        val tiempo = pedido.tiempoEntrega?.toString() ?: "N/A"
        b.textEta.text = ctx.getString(R.string.time_label_active, tiempo)

        // Calcular progreso basado en tiempo (ejemplo)
        val progress = ((pedido.tiempoEntrega ?: 0) * 100 / 30).coerceIn(0, 100)
        b.progressBar.progress = progress

        // Botón para navegar
        b.buttonNavigate.setOnClickListener {
            // Primero actualizar el estado a "En Camino"
            onNavigateClick?.invoke(pedido)

            // Luego abrir la actividad de localización
            val intent = Intent(ctx, LocalizacionActivity::class.java)
            intent.putExtra("PEDIDO_ID", pedido.idPedido)
            intent.putExtra("DIRECCION", pedido.direccionEntrega)
            intent.putExtra("NOMBRE", pedido.nomCliente)
            intent.putExtra("TOTAL", pedido.total)
            intent.putExtra("LATITUD", pedido.latitud)
            intent.putExtra("LONGITUD", pedido.longitud)
            ctx.startActivity(intent)
        }

        // Botón para ver detalles
        b.buttonDetail.setOnClickListener {
            val bottomSheet = PedidoDetailBottom(pedido)
            bottomSheet.show(activity.supportFragmentManager, "PedidoDetailBottom")
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