package com.cibertec.proyectodami.presentation.features.repartidor.estadistica


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cibertec.proyectodami.data.api.PedidosRepartidor
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.data.remote.RetrofitInstance
import com.cibertec.proyectodami.databinding.FragmentEstadisticaRepartidorBinding
import com.cibertec.proyectodami.domain.model.dtos.responses.EstadisticaRepartidorDTO
import com.cibertec.proyectodami.presentation.features.repartidor.escaner.EscanerActivity
import com.cibertec.proyectodami.presentation.features.repartidor.localizacion.LocalizacionActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class EstadisticaRepartidorFragment : Fragment() {

    private var _binding: FragmentEstadisticaRepartidorBinding? = null
    private val binding get() = _binding!!

    private lateinit var pedidosRepartidorApi: PedidosRepartidor
    private lateinit var userPreferences: UserPreferences
    private var idRepartidor: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEstadisticaRepartidorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inicializarApi()

        obtenerIdRepartidor()
    }

    private fun inicializarApi() {
        userPreferences = UserPreferences(requireContext())
        val retrofit = RetrofitInstance.create(userPreferences)
        pedidosRepartidorApi = retrofit.create(PedidosRepartidor::class.java)
    }



    private fun obtenerIdRepartidor() {
        lifecycleScope.launch {
            userPreferences.idUsuario.collect { id ->
                if (id != -1) {
                    idRepartidor = id
                    cargarEstadisticas()
                }
            }
        }
    }

    private fun cargarEstadisticas() {
        lifecycleScope.launch {
            try {
                mostrarEstadosIniciales()

                val estadisticas = withContext(Dispatchers.IO) {
                    pedidosRepartidorApi.obtenerEstadisticasRepartidor(idRepartidor)
                }

                actualizarEstadisticas(estadisticas)

            } catch (e: Exception) {
                Log.e("EstadisticasFragment", "Error: ${e.message}", e)

                Toast.makeText(
                    requireContext(),
                    "Error al cargar estadísticas: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

                // Mostrar valores por defecto
                mostrarEstadisticasVacias()
            }
        }
    }

    private fun actualizarEstadisticas(stats: EstadisticaRepartidorDTO) {
        // 1. Total de entregas
        binding.tvNumEntregas.text = stats.totalEntregas.toString()

        // 2. Tiempo promedio (en minutos)
        binding.tiempoPromedio.text = if (stats.tiempoPromedio > 0) {
            "${stats.tiempoPromedio.roundToInt()} min"
        } else {
            "Sin datos"
        }

        // 3. Calificación promedio (con 1 decimal)
        binding.calificacion.text = if (stats.calificacionPromedio > 0) {
            String.format("%.1f", stats.calificacionPromedio)
        } else {
            "Sin calificaciones"
        }

        // Animar los números
        animarEstadisticas()
    }

    private fun animarEstadisticas() {
        // Animación de fade-in
        binding.tvNumEntregas.alpha = 0f
        binding.tiempoPromedio.alpha = 0f
        binding.calificacion.alpha = 0f

        binding.tvNumEntregas.animate()
            .alpha(1f)
            .setDuration(800)
            .start()

        binding.tiempoPromedio.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(200)
            .start()

        binding.calificacion.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(400)
            .start()
    }

    private fun mostrarEstadosIniciales() {
        binding.tvNumEntregas.text = "..."
        binding.tiempoPromedio.text = "..."
        binding.calificacion.text = "..."
    }

    private fun mostrarEstadisticasVacias() {
        binding.tvNumEntregas.text = "0"
        binding.tiempoPromedio.text = "Sin datos"
        binding.calificacion.text = "0.0"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
