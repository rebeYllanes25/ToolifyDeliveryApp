package com.cibertec.proyectodami.presentation.features.cliente

import InicioFragment
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.databinding.ActivityClienteMainBinding
import com.cibertec.proyectodami.presentation.features.cliente.historial.HistorialFragment
import com.cibertec.proyectodami.presentation.features.cliente.perfil.PerfilFragment

class ClienteMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClienteMainBinding

    // Referencias a los apartados
    private var apartadoActual: ApartadoType = ApartadoType.INICIO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClienteMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarSaludo()
        configurarApartados()

        // Cargar fragment inicial
        if (savedInstanceState == null) {
            cargarFragment(InicioFragment(), ApartadoType.INICIO)
        }
    }

    private fun configurarSaludo() {
        // TODO: Obtener nombre del usuario desde SharedPreferences o ViewModel
        val nombreUsuario = "María"
        binding.tvSaludo.text = getString(R.string.saludo_usuario, nombreUsuario)
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
            // TODO: Abrir fragment o activity de notificaciones
            abrirNotificaciones()
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
        resetearApartado(binding.apartadoInicio)
        resetearApartado(binding.apartadoHistorial)
        resetearApartado(binding.apartadoPerfil)

        when (apartadoSeleccionado) {
            ApartadoType.INICIO -> activarApartado(binding.apartadoInicio)
            ApartadoType.HISTORIAL -> activarApartado(binding.apartadoHistorial)
            ApartadoType.PERFIL -> activarApartado(binding.apartadoPerfil)
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

    private fun abrirNotificaciones() {
        // Opción 1: Abrir como BottomSheet
        //val notificacionesFragment = NotificacionesFragment()
        //notificacionesFragment.show(supportFragmentManager, "NotificacionesFragment")

        // Opción 2: Navegar a un nuevo Fragment
        // cargarFragment(NotificacionesFragment(), null)

        // Ocultar badge después de abrir
        binding.insigniaNotificacion.visibility = android.view.View.GONE
    }

    enum class ApartadoType {
        INICIO, HISTORIAL, PERFIL
    }
}