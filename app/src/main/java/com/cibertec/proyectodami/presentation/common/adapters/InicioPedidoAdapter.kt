import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.domain.model.dtos.PedidoClienteDTO

class InicioPedidoAdapter(
    private val pedidos: List<PedidoClienteDTO>,
    private val onRastrearClick: (PedidoClienteDTO) -> Unit,
    private val onDetalleClick: (PedidoClienteDTO) -> Unit
) : RecyclerView.Adapter<InicioPedidoAdapter.PedidoViewHolder>() {

    inner class PedidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPedidoId: TextView = itemView.findViewById(R.id.tvPedidoId)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val statusChip: CardView = itemView.findViewById(R.id.statusChip)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotal)
        val btnRastrear: CardView = itemView.findViewById(R.id.btnRastrear)
        val btnDetalle: CardView = itemView.findViewById(R.id.btnDetalle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pedido_inicio, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]
        val context = holder.itemView.context

        holder.tvPedidoId.text = context.getString(
            R.string.formato_pedido_id,
            pedido.numPedido
        )

        holder.tvFecha.text = pedido.fecha?.let { fechaStr ->
            try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", java.util.Locale.getDefault())

                val date = inputFormat.parse(fechaStr)

                val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                outputFormat.format(date!!)
            } catch (e: Exception) {
                "Fecha inválida"
            }
        } ?: "Sin fecha"

        holder.tvTotal.text = context.getString(
            R.string.formato_total,
            pedido.total
        )

        // Configurar estado con colores
        val estadoInfo = obtenerEstadoInfo(pedido.estado)
        holder.tvStatus.text = estadoInfo.texto
        holder.tvStatus.setTextColor(ContextCompat.getColor(context, estadoInfo.colorTexto))
        holder.statusChip.setCardBackgroundColor(
            ContextCompat.getColor(context, estadoInfo.colorFondo)
        )

        // Click listeners
        holder.btnRastrear.setOnClickListener {
            onRastrearClick(pedido)
        }

        holder.btnDetalle.setOnClickListener {
            onDetalleClick(pedido)
        }
    }

    override fun getItemCount() = pedidos.size

    private fun obtenerEstadoInfo(estado: String?): EstadoInfo {
        return when (estado) {
            "PE" -> EstadoInfo(
                texto = "Pendiente",
                colorTexto = R.color.color_status_texto,
                colorFondo = R.color.orange_light
            )
            "AS" -> EstadoInfo(
                texto = "Asignado",
                colorTexto = R.color.color_principal,
                colorFondo = R.color.color_fondo
            )
            "EC" -> EstadoInfo(
                texto = "En camino",
                colorTexto = R.color.color_amarillo,
                colorFondo = R.color.amarillo_light
            )
            "EN" -> EstadoInfo(
                texto = "Entregado",
                colorTexto = R.color.verde,
                colorFondo = R.color.green_light
            )
            "FA" -> EstadoInfo(
                texto = "Fallido",
                colorTexto = R.color.color_badge_rojo,
                colorFondo = R.color.rojo_light
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