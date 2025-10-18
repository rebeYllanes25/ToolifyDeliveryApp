package com.cibertec.proyectodami.presentation.common.adapters

import com.cibertec.proyectodami.domain.model.entities.Notificacion
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.databinding.ItemNotificacionBinding
import java.util.Date
import java.util.concurrent.TimeUnit

class NotificacionesAdapter(
    private val onNotificacionClick: (Notificacion) -> Unit,
    private val onMarcarLeida: (Notificacion) -> Unit,
    private val onEliminar: (Notificacion) -> Unit
) : ListAdapter<Notificacion, NotificacionesAdapter.NotificacionViewHolder>(NotificacionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacionViewHolder {
        val binding = ItemNotificacionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificacionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificacionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificacionViewHolder(
        private val binding: ItemNotificacionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notificacion: Notificacion) {
            with(binding) {
                indicadorNoLeida.visibility = if (notificacion.leida) View.GONE else View.VISIBLE

                val tipo = notificacion.tipo
                ivIconoTipo.setImageResource(tipo.getIconoResId())
                cardIcono.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.context, tipo.getColorFondo())
                )
                ivIconoTipo.setColorFilter(
                    ContextCompat.getColor(itemView.context, tipo.getColorIcono())
                )

                tvTitulo.text = notificacion.titulo
                tvMensaje.text = notificacion.mensaje
                tvFechaHora.text = formatearTiempoTranscurrido(notificacion.fechaCreacion)

                val alphaTexto = if (notificacion.leida) 0.6f else 1.0f
                tvTitulo.alpha = alphaTexto
                tvMensaje.alpha = alphaTexto

                if (notificacion.pedidoId != null) {
                    tvBadgeEstado.visibility = View.VISIBLE
                    tvBadgeEstado.text = itemView.context.getString(tipo.getTituloString())
                } else {
                    tvBadgeEstado.visibility = View.GONE
                }

                cardNotificacion.setOnClickListener {
                    onNotificacionClick(notificacion)
                }

                btnOpciones.setOnClickListener {
                    mostrarMenuOpciones(it, notificacion)
                }
            }
        }

        private fun mostrarMenuOpciones(view: View, notificacion: Notificacion) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_notificacion_opciones, popup.menu)

            val itemMarcar = popup.menu.findItem(R.id.action_marcar_leida)
            if (notificacion.leida) {
                itemMarcar.title = view.context.getString(R.string.notificaciones_marcar_no_leida)
            } else {
                itemMarcar.title = view.context.getString(R.string.notificaciones_marcar_leida)
            }

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_marcar_leida -> {
                        onMarcarLeida(notificacion)
                        true
                    }
                    R.id.action_eliminar -> {
                        onEliminar(notificacion)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        private fun formatearTiempoTranscurrido(fecha: Date): String {
            val ahora = Date()
            val diff = ahora.time - fecha.time

            val minutos = TimeUnit.MILLISECONDS.toMinutes(diff)
            val horas = TimeUnit.MILLISECONDS.toHours(diff)
            val dias = TimeUnit.MILLISECONDS.toDays(diff)

            return when {
                minutos < 1 -> itemView.context.getString(R.string.notificaciones_tiempo_ahora)
                minutos < 60 -> itemView.context.getString(R.string.notificaciones_tiempo_minutos, minutos)
                horas < 24 -> itemView.context.getString(R.string.notificaciones_tiempo_horas, horas)
                dias < 7 -> itemView.context.getString(R.string.notificaciones_tiempo_dias, dias)
                else -> itemView.context.getString(R.string.notificaciones_tiempo_semanas, dias / 7)
            }
        }
    }

    class NotificacionDiffCallback : DiffUtil.ItemCallback<Notificacion>() {
        override fun areItemsTheSame(oldItem: Notificacion, newItem: Notificacion): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Notificacion, newItem: Notificacion): Boolean {
            return oldItem == newItem
        }
    }
}