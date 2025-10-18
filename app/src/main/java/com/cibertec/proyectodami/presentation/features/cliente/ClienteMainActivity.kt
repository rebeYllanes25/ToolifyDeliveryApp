package com.cibertec.proyectodami.presentation.features.cliente

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.databinding.ActivityClienteMainBinding
import com.cibertec.proyectodami.presentation.features.cliente.inicio.InicioFragment
import com.cibertec.proyectodami.presentation.features.cliente.historial.HistorialFragment
import com.cibertec.proyectodami.presentation.features.cliente.notificaciones.NotificacionesFragment
import com.cibertec.proyectodami.presentation.features.cliente.perfil.PerfilFragment
import kotlinx.coroutines.launch

class ClienteMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClienteMainBinding
    private var apartadoActual: ApartadoType = ApartadoType.INICIO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClienteMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarSaludo()
        configurarApartados()

        if (savedInstanceState == null) {
            cargarFragment(InicioFragment(), ApartadoType.INICIO)
        }
    }

    private fun configurarSaludo() {

        val userPreferences = UserPreferences(applicationContext)

        lifecycleScope.launch {
            userPreferences.nombreUsuario.collect { nombre ->
                binding.tvSaludo.text = nombre ?: getString(R.string.user_name)
            }
        }

    }

    private fun configurarApartados() {
        binding.apartadoInicio.setOnClickListener {
            if (apartadoActual != ApartadoType.INICIO) {
                cargarFragment(InicioFragment(), ApartadoType.INICIO)
            }
        }

        binding.apartadoHistorial.setOnClickListener {
            if (apartadoActual != ApartadoType.HISTORIAL) {
                cargarFragment(HistorialFragment(), ApartadoType.HISTORIAL)
            }
        }

        binding.apartadoPerfil.setOnClickListener {
            if (apartadoActual != ApartadoType.PERFIL) {
                cargarFragment(PerfilFragment(), ApartadoType.PERFIL)
            }
        }

        binding.btnNotificaciones.setOnClickListener {
            cargarFragment(NotificacionesFragment(), ApartadoType.NOTIFICACIONES)
        }
    }

    private fun cargarFragment(fragment: Fragment, tipo: ApartadoType) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedorFragmento, fragment)
            .commit()

        actualizarEstiloApartados(tipo)
        apartadoActual = tipo
    }

    private fun actualizarEstiloApartados(apartadoSeleccionado: ApartadoType) {

        if (apartadoSeleccionado == ApartadoType.NOTIFICACIONES) {
            binding.layoutApartados.visibility = android.view.View.GONE
            resetearApartado(binding.apartadoInicio)
            resetearApartado(binding.apartadoHistorial)
            resetearApartado(binding.apartadoPerfil)
        } else {
            binding.layoutApartados.visibility = android.view.View.VISIBLE
            resetearApartado(binding.apartadoInicio)
            resetearApartado(binding.apartadoHistorial)
            resetearApartado(binding.apartadoPerfil)

            when (apartadoSeleccionado) {
                ApartadoType.INICIO -> activarApartado(binding.apartadoInicio)
                ApartadoType.HISTORIAL -> activarApartado(binding.apartadoHistorial)
                ApartadoType.PERFIL -> activarApartado(binding.apartadoPerfil)
                else -> {}
            }
        }
    }

    private fun resetearApartado(apartado: LinearLayout) {
        val imageView = apartado.getChildAt(0) as ImageView
        val textView = apartado.getChildAt(1) as TextView

        imageView.setColorFilter(
            ContextCompat.getColor(this, R.color.color_subtitulos),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        textView.setTextColor(ContextCompat.getColor(this, R.color.color_subtitulos))
        textView.setTypeface(null, android.graphics.Typeface.NORMAL)
    }

    private fun activarApartado(apartado: LinearLayout) {
        val imageView = apartado.getChildAt(0) as ImageView
        val textView = apartado.getChildAt(1) as TextView

        imageView.setColorFilter(
            ContextCompat.getColor(this, R.color.color_principal),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        textView.setTextColor(ContextCompat.getColor(this, R.color.color_principal))
        textView.setTypeface(null, android.graphics.Typeface.BOLD)
    }

    override fun onBackPressed() {
        if (apartadoActual == ApartadoType.NOTIFICACIONES) {
            cargarFragment(InicioFragment(), ApartadoType.INICIO)
        } else {
            super.onBackPressed()
        }
    }

    enum class ApartadoType {
        INICIO, HISTORIAL, PERFIL, NOTIFICACIONES
    }
}