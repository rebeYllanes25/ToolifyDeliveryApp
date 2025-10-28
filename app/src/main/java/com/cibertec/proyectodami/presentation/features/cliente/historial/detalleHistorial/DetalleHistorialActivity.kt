package com.cibertec.proyectodami.presentation.features.cliente.historial.detalleHistorial

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.databinding.ActivityDetalleHistorialBinding
import com.cibertec.proyectodami.domain.model.dtos.ProductoPedidoDTO
import com.cibertec.proyectodami.presentation.features.cliente.historial.detalleHistorial.Fragments.CalificacionFragment
import com.google.gson.Gson
import com.cibertec.proyectodami.presentation.features.cliente.historial.detalleHistorial.Fragments.DetalleHistorialFragment

class DetalleHistorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleHistorialBinding
    private var apartadoActual: ApartadoTypeDT = ApartadoTypeDT.DETALLE


    private var pedidoIdInt: Int = 0;
    private var pedidoId:String? = null;
    private var estado:String? = null;
    private var nombreRepartidor:String? = null
    private var apePaternoRepartidor:String? = null
    private var telefonoRepartidor:String? = null
    private var productos: List<ProductoPedidoDTO>? = null
    private var total: Double = 0.0
    private var movilidad:String? = null
    private var direccion: String? = null
    private var fecha:String? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetalleHistorialBinding.inflate(layoutInflater)
        setContentView(binding.root)


        obtenerDatosIntent()
        setUpListeners()

        if (savedInstanceState == null) {
            cargarFragment(DetalleHistorialFragment(), ApartadoTypeDT.DETALLE)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun obtenerDatosIntent(){

        pedidoIdInt = intent.getIntExtra("ID_PEDIDO",0)
        pedidoId = intent.getStringExtra("NUM_PEDIDO")
        estado = intent.getStringExtra("ESTADO_PEDIDO")
        total = intent.getDoubleExtra("TOTAL_PEDIDO",0.0)
        fecha = intent.getStringExtra("FECHA_PEDIDO")
        nombreRepartidor = intent.getStringExtra("NOMBRE_REPARTIDOR")
        apePaternoRepartidor = intent.getStringExtra("APE_PAT_REPARTIDOR")
        telefonoRepartidor = intent.getStringExtra("TELEFONO_REPARTIDOR")
        movilidad = intent.getStringExtra("TIPO_ENVIO")
        direccion = intent.getStringExtra("DIRECCION_PEDIDO")
        val productosJson = intent.getStringExtra("PRODUCTOS")

        productos = if (!productosJson.isNullOrEmpty()) {
            Gson().fromJson(productosJson, Array<ProductoPedidoDTO>::class.java).toList()
        } else {
            emptyList()
        }
    }

    private fun cargarFragment(fragment: Fragment, tipo: ApartadoTypeDT) {

        val bundle = Bundle().apply{

            putInt("ID_PEDIDO",pedidoIdInt)
            putString("NUM_PEDIDO", pedidoId)
            putString("ESTADO_PEDIDO", estado)
            putDouble("TOTAL_PEDIDO", total)
            putString("FECHA_PEDIDO",fecha)
            putString("NOMBRE_REPARTIDOR",nombreRepartidor)
            putString("APE_PAT_REPARTIDOR",apePaternoRepartidor)
            putString("TELEFONO_REPARTIDOR",telefonoRepartidor)
            putString("TIPO_ENVIO",movilidad)
            putString("DIRECCION_PEDIDO",direccion)
        }
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedorFragmentoDetalle, fragment)
            .commit()


        actualizarEstiloApartados(tipo)
        apartadoActual = tipo
    }

     fun actualizarApartados(apartadoSeleccionado: ApartadoTypeDT){
       actualizarEstiloApartados(apartadoSeleccionado)

    }

    private fun actualizarEstiloApartados(apartadoSeleccionado: ApartadoTypeDT) {

        binding.divApartadoDetalle.visibility = android.view.View.VISIBLE

        resetearApartado(binding.divApartadoDetalle)
        resetearApartado(binding.divApartadoCalificacion)

        when (apartadoSeleccionado) {
            ApartadoTypeDT.DETALLE -> activarApartado(binding.divApartadoDetalle)
            ApartadoTypeDT.CALIFICACION -> activarApartado(binding.divApartadoCalificacion)
            else -> {}
        }
    }

    private fun resetearApartado(apartado: LinearLayout) {
        val imageView = apartado.getChildAt(0) as ImageView
        val textView = apartado.getChildAt(1) as TextView

        imageView.setColorFilter(
            ContextCompat.getColor(this, R.color.color_subtitulos),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        textView.setTextColor(ContextCompat.getColor(this, R.color.color_principal))
        textView.setTypeface(null, android.graphics.Typeface.BOLD)

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

    private fun setUpListeners(){

        binding.divApartadoDetalle.setOnClickListener {
            cargarFragment(DetalleHistorialFragment(), ApartadoTypeDT.DETALLE)
        }

        binding.divApartadoCalificacion.setOnClickListener {
            cargarFragment(CalificacionFragment(), ApartadoTypeDT.CALIFICACION)
        }
    }

    enum class ApartadoTypeDT {
        DETALLE, CALIFICACION
    }

}

