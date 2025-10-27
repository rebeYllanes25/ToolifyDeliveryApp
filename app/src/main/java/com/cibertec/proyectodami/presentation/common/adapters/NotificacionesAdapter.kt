package com.cibertec.proyectodami.presentation.common.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.databinding.ItemNotificacionBinding
import com.cibertec.proyectodami.domain.model.dtos.NotificacionDTO
import java.util.Date
import java.util.concurrent.TimeUnit

class NotificacionesAdapter(
    private var notificaciones: MutableList<NotificacionDTO>,
    private val onNotificacionClick: (NotificacionDTO) -> Unit,
    private val onMarcarLeida: (NotificacionDTO) -> Unit,
    private val onDescartar: (NotificacionDTO) -> Unit
) : RecyclerView.Adapter<NotificacionesAdapter.NotificacionViewHolder>() {

    inner class NotificacionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardNotificacion: CardView = view.findViewById(R.id.cardNotificacion)
        val indicadorNoLeida: View = view.findViewById(R.id.indicadorNoLeida)
        val ivIcono: ImageView = view.findViewById(R.id.ivIconoNotificacion)
        val tvTitulo: TextView = view.findViewById(R.id.tvTituloNotificacion)
        val tvMensaje: TextView = view.findViewById(R.id.tvMensaje)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val btnOpciones: ImageButton = view.findViewById(R.id.btnOpciones)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notificacion, parent, false)
        return NotificacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificacionViewHolder, position: Int) {
        val notificacion = notificaciones[position]
        val context = holder.itemView.context

        // Mostrar indicador de no leída
        holder.indicadorNoLeida.visibility = if (notificacion.leida) View.GONE else View.VISIBLE

        // Estilo de texto según si está leída o no
        if (!notificacion.leida) {
            holder.tvTitulo.setTypeface(null, Typeface.BOLD)
            holder.tvMensaje.setTypeface(null, Typeface.NORMAL)
            holder.cardNotificacion.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.white)
            )
        } else {
            holder.tvTitulo.setTypeface(null, Typeface.NORMAL)
            holder.tvMensaje.setTypeface(null, Typeface.NORMAL)
            holder.cardNotificacion.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.gris_claro)
            )
        }

        val (icono, colorFondo) = obtenerIconoYColor(notificacion.tipo)
        holder.ivIcono.setImageResource(icono)
        holder.ivIcono.setBackgroundResource(colorFondo)

        holder.tvTitulo.text = notificacion.titulo
        holder.tvMensaje.text = notificacion.mensaje
        holder.tvFecha.text = notificacion.getTiempoRelativo()

        holder.cardNotificacion.setOnClickListener {
            onNotificacionClick(notificacion)
        }

        holder.btnOpciones.setOnClickListener {
            mostrarMenu(holder.btnOpciones, notificacion, position)
        }
    }

    override fun getItemCount(): Int = notificaciones.size

    private fun obtenerIconoYColor(tipo: String): Pair<Int, Int> {
        return when (tipo) {
            "PEDIDO_PENDIENTE" -> Pair(R.drawable.ic_notification, R.drawable.circle_background_orange)
            "PEDIDO_ACEPTADO" -> Pair(R.drawable.ic_person_assigned, R.drawable.circle_background_blue)
            "PEDIDO_EN_CAMINO" -> Pair(R.drawable.ic_delivery_truck, R.drawable.circle_background_purple)
            "PEDIDO_CERCA" -> Pair(R.drawable.ic_gift, R.drawable.circle_background_yellow)
            "PEDIDO_ENTREGADO" -> Pair(R.drawable.ic_check_circle, R.drawable.circle_background_green)
            "PEDIDO_FALLIDO" -> Pair(R.drawable.ic_error, R.drawable.circle_background_red)
            else -> Pair(R.drawable.ic_notification, R.drawable.circle_background)
        }
    }

    private fun mostrarMenu(view: View, notificacion: NotificacionDTO, position: Int) {
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_notificacion_opciones)

        // Ocultar opción "Marcar como leída" si ya está leída
        if (notificacion.leida) {
            popup.menu.findItem(R.id.menu_marcar_leida)?.isVisible = false
        }

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_marcar_leida -> {
                    onMarcarLeida(notificacion)
                    notificaciones[position] = notificacion.copy(leida = true)
                    notifyItemChanged(position)
                    true
                }
                R.id.menu_descartar -> {
                    onDescartar(notificacion)
                    notificaciones.removeAt(position)
                    notifyItemRemoved(position)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    fun actualizarNotificaciones(nuevasNotificaciones: List<NotificacionDTO>) {
        notificaciones.clear()
        notificaciones.addAll(nuevasNotificaciones)
        notifyDataSetChanged()
    }

    fun agregarNotificacion(notificacion: NotificacionDTO) {
        notificaciones.add(0, notificacion)
        notifyItemInserted(0)
    }
}