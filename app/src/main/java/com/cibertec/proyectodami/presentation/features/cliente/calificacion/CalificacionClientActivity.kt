package com.cibertec.proyectodami.presentation.features.cliente.calificacion

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.data.api.PedidosCliente
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.data.remote.RetrofitInstance
import com.cibertec.proyectodami.databinding.ActivityCalificacionClientBinding
import com.cibertec.proyectodami.domain.repository.CalificacionRepository

class CalificacionClientActivity : AppCompatActivity() {

    private lateinit var  binding: ActivityCalificacionClientBinding

    private val pedidosService: PedidosCliente by lazy {
        val userPreferences = UserPreferences(this)
        RetrofitInstance.create(userPreferences).create(PedidosCliente::class.java)
    }

    private val viewModel : CalificacionViewModel by viewModels {
        CalificacionViewModelFactory(
            CalificacionRepository(apiService = pedidosService)
        )
    }

    private var idPedido: Int = 0
    private var numPedido: String = ""
    private var total: Double = 0.0
    private var nombreRepartidor: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCalificacionClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idPedido = intent.getIntExtra("ID_PEDIDO", 0)
        numPedido = intent.getStringExtra("NUM_PEDIDO") ?: ""
        total = intent.getDoubleExtra("TOTAL", 0.0)
        nombreRepartidor = intent.getStringExtra("NOMBRE_REPARTIDOR") ?: ""


        cargarDatosFragment()
        calificarPedido()
        observarViewModel()
    }

    private fun calificarPedido(){
        binding.btnEnviarCalificacion.setOnClickListener {
            enviarCalificacion()
        }

        ratingBar()
    }

    private fun cargarDatosFragment(){
        binding.apply {
            tvCalificaNameRepartidor.text = nombreRepartidor
            tvCalificaCodigoPedido.text = numPedido
            tvCalificaTotal.text = getString(R.string.seguimiento_fmt_total, total)
        }
    }

    private fun ratingBar(){
        binding.rbCalificacion.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                actualizarMensajeRating(rating.toInt())
            }
        }
    }

    private fun enviarCalificacion() {
        val rating = binding.rbCalificacion.rating.toInt()

        if (rating == 0) {
            Toast.makeText(
                this,
                "Por favor selecciona una calificación",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val comentario = binding.tilExperiencia.editText?.text.toString().trim()

        viewModel.registrarCalificacion(
            idPedido = idPedido,
            puntuacion = rating.toShort(),
            comentario = comentario.ifEmpty { null }
        )
    }

    private fun observarViewModel(){
        viewModel.calificacionState.observe(this){state ->
            when(state){
                is CalificacionState.Loading->{
                    mostrarCargando(true)
                }

                is CalificacionState.Success->{
                    mostrarCargando(false)
                    Toast.makeText(this, "Se envio correctamente tu calificacion", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is CalificacionState.Error ->{
                    mostrarCargando(false)
                    Toast.makeText(this, "Error ${state.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun mostrarCargando(mostrar: Boolean) {
        binding.btnEnviarCalificacion.isEnabled = !mostrar
        binding.btnEnviarCalificacion.text = if (mostrar) {
            "Enviando..."
        } else {
            getString(R.string.btn_enviar_calificacion)
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
        binding.tvMensajeRating.setTextColor(getColor(colorResId))
    }

}