package com.cibertec.proyectodami.presentation.features.cliente.calificacion

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.databinding.FragmentCalificacionBinding

class CalificacionFragment : Fragment() {

    private lateinit var binding: FragmentCalificacionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalificacionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rbCalificacion.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                actualizarMensajeRating(rating.toInt())
            }
        }
    }

    private fun actualizarMensajeRating(rating: Int) {
        val mensaje = when (rating) {
            0 -> getString(R.string.calificacion_msg_default)
            1 -> getString(R.string.calificacion_msg_una_estrella)
            2 -> getString(R.string.calificacion_msg_dos_estrellas)
            3 -> getString(R.string.calificacion_msg_tres_estrellas)
            4 -> getString(R.string.calificacion_msg_cuatro_estrellas)
            5 -> getString(R.string.calificacion_msg_cinco_estrellas)
            else -> getString(R.string.calificacion_msg_default)
        }

        val colorResId = when (rating) {
            0 -> R.color.color_subtitulos
            1 -> R.color.color_badge_rojo
            2 -> R.color.color_amarillo
            3 -> R.color.orange_strong
            4 -> R.color.color_principal
            5 -> R.color.verde
            else -> R.color.color_subtitulos
        }

        binding.tvMensajeRating.text = mensaje
        binding.tvMensajeRating.setTextColor(requireContext().getColor(colorResId))
    }
}