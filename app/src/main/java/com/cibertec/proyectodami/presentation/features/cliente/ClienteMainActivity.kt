package com.cibertec.proyectodami.presentation.features.cliente

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.data.api.PedidosCliente
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.data.remote.RetrofitInstance
import com.cibertec.proyectodami.databinding.ActivityClienteMainBinding
import com.cibertec.proyectodami.domain.repository.PedidoClienteRepository
import com.cibertec.proyectodami.presentation.features.cliente.carro.CarroFragment
import com.cibertec.proyectodami.presentation.features.cliente.carro.CarroRepository
import com.cibertec.proyectodami.presentation.features.cliente.inicio.InicioFragment
import com.cibertec.proyectodami.presentation.features.cliente.historial.HistorialFragment
import com.cibertec.proyectodami.presentation.features.cliente.notificaciones.NotificacionesFragment
import com.cibertec.proyectodami.presentation.features.cliente.perfil.PerfilFragment
import com.cibertec.proyectodami.presentation.features.cliente.productos.ProductosFragment
import kotlinx.coroutines.launch

class ClienteMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClienteMainBinding
    private var apartadoActual: ApartadoType = ApartadoType.PRODUCTOS

    lateinit var pedidoRepository: PedidoClienteRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClienteMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inicializarRepositorio()
        configurarSaludo()
        configurarApartados()
        setupCarritoButton()
        observeCarrito()

        if (savedInstanceState == null) {
            cargarFragment(ProductosFragment(), ApartadoType.PRODUCTOS)
        }
    }

    private fun inicializarRepositorio() {
        try {
            val userPreferences = UserPreferences(applicationContext)
            val retrofit = RetrofitInstance.create(userPreferences)
            val pedidosApi = retrofit.create(PedidosCliente::class.java)

            pedidoRepository = PedidoClienteRepository(applicationContext)

        } catch (e: Exception) {
            android.util.Log.e("ClienteMainActivity", "Error al inicializar Repository", e)
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
        binding.inicioProducto.setOnClickListener {
            if (apartadoActual != ApartadoType.PRODUCTOS) {
                cargarFragment(ProductosFragment(), ApartadoType.PRODUCTOS)
            }
        }

        binding.apartadoPedido.setOnClickListener {
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
        if (apartadoSeleccionado == ApartadoType.NOTIFICACIONES || apartadoSeleccionado == ApartadoType.CARRO) {
            binding.layoutApartados.visibility = View.GONE
            resetearBotonProducto()
            resetearApartado(binding.apartadoPedido)
            resetearApartado(binding.apartadoHistorial)
            resetearApartado(binding.apartadoPerfil)
        } else {
            binding.layoutApartados.visibility = View.VISIBLE

            resetearBotonProducto()
            resetearApartado(binding.apartadoPedido)
            resetearApartado(binding.apartadoHistorial)
            resetearApartado(binding.apartadoPerfil)

            when (apartadoSeleccionado) {
                ApartadoType.PRODUCTOS -> activarBotonProducto()
                ApartadoType.INICIO -> activarApartado(binding.apartadoPedido)
                ApartadoType.HISTORIAL -> activarApartado(binding.apartadoHistorial)
                ApartadoType.PERFIL -> activarApartado(binding.apartadoPerfil)
                else -> {}
            }
        }
    }

    private fun resetearBotonProducto() {
        val imageView = binding.inicioProducto.getChildAt(0) as? ImageView
        val textView = binding.inicioProducto.getChildAt(1) as? TextView

        imageView?.setColorFilter(
            ContextCompat.getColor(this, R.color.color_subtitulos),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        textView?.setTextColor(ContextCompat.getColor(this, R.color.color_subtitulos))
        textView?.setTypeface(null, android.graphics.Typeface.NORMAL)
    }

    private fun activarBotonProducto() {
        val imageView = binding.inicioProducto.getChildAt(0) as? ImageView
        val textView = binding.inicioProducto.getChildAt(1) as? TextView

        imageView?.setColorFilter(
            ContextCompat.getColor(this, R.color.color_principal),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        textView?.setTextColor(ContextCompat.getColor(this, R.color.color_principal))
        textView?.setTypeface(null, android.graphics.Typeface.BOLD)
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
        if (apartadoActual == ApartadoType.NOTIFICACIONES || apartadoActual == ApartadoType.CARRO) {
            cargarFragment(ProductosFragment(), ApartadoType.PRODUCTOS)
        } else if (apartadoActual != ApartadoType.PRODUCTOS) {
            cargarFragment(ProductosFragment(), ApartadoType.PRODUCTOS)
        } else {
            super.onBackPressed()
        }
    }

    enum class ApartadoType {
        INICIO, PRODUCTOS, HISTORIAL, PERFIL, NOTIFICACIONES, CARRO
    }

    private fun setupCarritoButton() {
        val btnCarrito = binding.btnCarrito

        btnCarrito?.setOnClickListener {
            cargarFragment(CarroFragment(), ApartadoType.CARRO)
        }
    }

    private fun observeCarrito() {
        lifecycleScope.launch {
            CarroRepository.cantidadTotal.collect { cantidad ->
                actualizarInsigniaCarrito(cantidad)
            }
        }
    }

    private fun actualizarInsigniaCarrito(cantidad: Int) {
        val insigniaCarrito = binding.insigniaCarrito
        insigniaCarrito?.visibility = if (cantidad > 0) View.VISIBLE else View.GONE
    }
}
