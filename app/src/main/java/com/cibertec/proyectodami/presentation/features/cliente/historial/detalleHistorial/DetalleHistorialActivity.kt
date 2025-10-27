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
import com.cibertec.proyectodami.domain.repository.PedidoClienteRepository
import com.cibertec.proyectodami.presentation.common.adapters.ProductoPedidoAdapter
import com.cibertec.proyectodami.presentation.features.cliente.historial.detalleHistorial.Fragments.CalificacionFragment
import com.google.gson.Gson
import com.cibertec.proyectodami.presentation.features.cliente.historial.detalleHistorial.Fragments.DetalleHistorialFragment

class DetalleHistorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleHistorialBinding
    private var apartadoActual: ApartadoTypeDT = ApartadoTypeDT.DETALLE

    lateinit var pedidoRepository: PedidoClienteRepository


    private var pedidoIdInt: Int = 0;
    private var pedidoId:String? = null;
    private var estado:String? = null;
    private var nombreRepartidor:String? = null
    private var apePaternoRepartidor:String? = null
    private var tiempoEntrega: Int  = 0;
    private var productos: List<ProductoPedidoDTO>? = null
    private var total: Double = 0.0
    private var direccion: String? = null
    private var stringqr:String? = null
    private var idRepartidor:Int = 0;
    //agregar
    private var fecha:String? = null;
    private var idCliente:Int = 0;

    private lateinit var productosAdapter : ProductoPedidoAdapter

    private var yaCalificado: Boolean = false;
    private var verificacionCompletada: Boolean = false

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

        pedidoId = intent.getStringExtra("PEDIDO_ID")
        pedidoIdInt = intent.getIntExtra("PEDIDO_ID_INT",0)
        estado = intent.getStringExtra("ESTADO")
        nombreRepartidor = intent.getStringExtra("REPARTIDOR")
        apePaternoRepartidor = intent.getStringExtra("APE_PATERNO")
        tiempoEntrega = intent.getIntExtra("TIEMPO_ENTREGA",0)
        stringqr = intent.getStringExtra("QR_VERIFICATE_PEDIDO")
        val productosJson = intent.getStringExtra("PRODUCTOS")
        total = intent.getDoubleExtra("TOTAL",0.0)
        idRepartidor = intent.getIntExtra("ID_REPARTIDOR",0)

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
            putInt("ID_CLIENTE", idCliente)
            idRepartidor?.let { putInt("ID_REPARTIDOR", it) }
        }
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedorFragmentoDetalle, fragment)
            .commit()


        actualizarEstiloApartados(tipo)
        apartadoActual = tipo
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

