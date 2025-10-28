package com.cibertec.proyectodami.presentation.features.cliente.historial.detalleHistorial.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.data.api.PedidosCliente
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.data.remote.RetrofitInstance
import com.cibertec.proyectodami.databinding.FragmentDetalleHistorialBinding
import com.cibertec.proyectodami.domain.model.dtos.ProductoPedidoDTO
import com.cibertec.proyectodami.presentation.common.adapters.ProductoPedidoAdapter
import com.cibertec.proyectodami.presentation.features.cliente.calificacion.CalificacionClientActivity
import com.cibertec.proyectodami.presentation.features.cliente.historial.detalleHistorial.DetalleHistorialActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetalleHistorialFragment : Fragment() {

    private var _binding: FragmentDetalleHistorialBinding? = null
    private val binding get() = _binding!!

    private lateinit var pedidosClienteApi: PedidosCliente
    private lateinit var productosAdapter: ProductoPedidoAdapter


    private var pedidoIdInt: Int = 0
    private var pedidoId: String? = null
    private var estado: String? = null
    private var nombreRepartidor: String? = null
    private var apePaternoRepartidor: String? = null
    private var telefonoRepartidor: String? = null
    private var tiempoEntrega: Int = 0
    private var productos: List<ProductoPedidoDTO>? = null
    private var total: Double = 0.0
    private var direccion: String? = null
    private var fechaEmitida: String? = null
    private var costoEnvio: Double = 0.0
    private var propina: Double = 0.0
    private var especificaciones: String? = null
    private var movilidad: String? = null

    private var yaCalificado: Boolean = false
    private var puntuacion: Int = 0
    private var comentario: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleHistorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inicializarApi()
        obtenerDatosArguments()
        cargarDatosPedido()

        configurarBotonCalificacion()
    }

    private fun inicializarApi() {
        val userPreferences = UserPreferences(requireContext())
        val retrofit = RetrofitInstance.create(userPreferences)
        pedidosClienteApi = retrofit.create(PedidosCliente::class.java)
    }

    private fun obtenerDatosArguments() {
        arguments?.let {
            pedidoIdInt = it.getInt("ID_PEDIDO", 0)
            pedidoId = it.getString("NUM_PEDIDO")
            estado = it.getString("ESTADO_PEDIDO")
            total = it.getDouble("TOTAL_PEDIDO", 0.0)
            fechaEmitida = it.getString("FECHA_PEDIDO")

            nombreRepartidor = it.getString("NOMBRE_REPARTIDOR")
            apePaternoRepartidor = it.getString("APE_PAT_REPARTIDOR")
            telefonoRepartidor = it.getString("TELEFONO_REPARTIDOR")
            movilidad = it.getString("TIPO_ENVIO")
            direccion = it.getString("DIRECCION_PEDIDO")
        }
    }

    private fun cargarDatosPedido() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("DETALLE_HISTORIAL", "🔄 Cargando datos del pedido #$pedidoIdInt")

                val datosPedido = pedidosClienteApi.obtenerPedidoPorId(pedidoIdInt)
                val responseCalificacion = pedidosClienteApi.verificarCalificacion(pedidoIdInt)

                    if (responseCalificacion.isSuccessful) {
                        yaCalificado = responseCalificacion.body()?.get("yaCalificado") ?: false
                        Log.d("DETALLE_HISTORIAL", "Ya calificado: $yaCalificado")

                       if(yaCalificado){
                           val calificacionData = pedidosClienteApi.buscarCalificacion(pedidoIdInt)
                           if (calificacionData.isSuccessful){
                               val data = calificacionData.body()
                               if (data != null){
                                   puntuacion = data.puntuacion
                                   comentario = data.comentario
                               }
                           }else{
                               Log.e("DETALLE_HISTORIAL", "NO SE ENCOTRO CALIFICACION")
                           }
                       }
                    }
                withContext(Dispatchers.Main) {

                    estado = datosPedido.estado
                    productos = datosPedido.productos
                    costoEnvio = 15.0
                    propina =  0.0
                    especificaciones = datosPedido.especificaciones
                    movilidad = datosPedido.movilidad

                    pintarDatosVista()
                    actualizarEstadoPedido()
                    configurarVisibilidadCalificacion()
                    configurarBotonCalificacion()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("DETALLE_HISTORIAL", "Error cargando datos: ${e.message}")
                    Toast.makeText(requireContext(), "Error al cargar datos del pedido", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun obtenerCalificacion() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = pedidosClienteApi.buscarCalificacion(pedidoIdInt)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val calificacionData = response.body()
                        if (calificacionData!= null){
                            puntuacion = calificacionData.puntuacion
                            comentario = calificacionData.comentario

                            Log.d("DETALLE_HISTORIAL", "Calificación: $puntuacion estrellas")
                            Log.d("DETALLE_HISTORIAL", "Comentario: $comentario")
                        }

                    }
                }
            } catch (e: Exception) {
                Log.e("DETALLE_HISTORIAL", "Error obteniendo calificación: ${e.message}")
            }
        }
    }

    private fun pintarDatosVista() {

        binding.tvNumeroIdFragment.text = pedidoId
        binding.tvProductoTotalesFragment.text = "${productos?.size ?: 0} productos"
        binding.tvDireccionEntregaFragment.text = direccion
        binding.tvFechEmitida.text = fechaEmitida

        val subTotal = productos?.sumOf { it.subTotal } ?: 0.0
        binding.tvTotalCompra.text = getString(R.string.seguimiento_fmt_subtotal, subTotal)
        binding.tvCosteEnvioFragment.text = getString(R.string.seguimiento_fmt_coste, costoEnvio)
        binding.tvPropinaRepartidor.text = getString(R.string.seguimiento_fmt_propina, propina)
        binding.tvTotalPagadoFragment.text = getString(R.string.seguimiento_fmt_total, total)

        binding.tvNombreCompletoFragment.text = "$nombreRepartidor $apePaternoRepartidor"
        binding.tvTelefonoRepaFragment.text = telefonoRepartidor
        binding.tvEspecificacionesPedido.text = especificaciones
        binding.tvMovilidadRepartidor.text = movilidad



        cargarRecyclerViewProductos()
    }

    private fun cargarRecyclerViewProductos() {
        productos?.let { listaProductos ->
            productosAdapter = ProductoPedidoAdapter(listaProductos)

            binding.rvProductosDetalles.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = productosAdapter
                isNestedScrollingEnabled = false
                setHasFixedSize(true)
            }
        }
    }

    private fun actualizarEstadoPedido() {
        val estadoIndex = when (estado) {
            "PE" -> 0
            "AS" -> 1
            "EC" -> 2
            "CR" -> 3
            "EN" -> 4
            else -> 0
        }

        actualizarUIEstados(estadoIndex)
    }

    private fun actualizarUIEstados(estadoActual: Int) {
        val iconos = listOf(
            binding.icEstadoAceptado,
            binding.icEstadoPreparando,
            binding.icEstadoTransito,
            binding.icEstadoCerca,
            binding.icEstadoEntregado
        )

        val labels = listOf(
            binding.lblAceptado,
            binding.lblPreparando,
            binding.lblEnTransito,
            binding.lblCerca,
            binding.lblEntregado
        )

        for (i in iconos.indices) {
            val icono = iconos[i]
            val label = labels[i]

            if (i <= estadoActual) {

                icono.setColorFilter(requireContext().getColor(R.color.white))
                icono.setBackgroundResource(R.drawable.ic_border_status)
                label.setTextColor(requireContext().getColor(R.color.color_principal))
                label.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {

                icono.setColorFilter(requireContext().getColor(R.color.white))
                icono.setBackgroundResource(R.drawable.ic_border_status_off)
                label.setTextColor(requireContext().getColor(R.color.gris_claro))
                label.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }

     private fun configurarVisibilidadCalificacion() {

        val btnCalificacion = requireActivity().findViewById<View>(R.id.divApartadoCalificacion)
        val textoBtn = requireActivity().findViewById<TextView>(R.id.tvTextoCalificacion)

        when{
            estado == "EN" && yaCalificado ->{
                btnCalificacion.visibility = View.VISIBLE
                btnCalificacion.isEnabled = true
                btnCalificacion.alpha = 1.0f
                textoBtn.text = "Ver calificacion"
            }
            estado == "EN" && !yaCalificado ->{
                btnCalificacion.visibility = View.VISIBLE
                btnCalificacion.isEnabled = true
                btnCalificacion.alpha = 1.0f
                textoBtn.text = "Calificar pedido"
            }else ->{
            btnCalificacion.visibility = View.VISIBLE
            btnCalificacion.isEnabled = true
            btnCalificacion.alpha = 1.0f
            textoBtn.text = "No disponible"
            }
        }
    }

    private fun configurarBotonCalificacion() {

        val btnCalificacion = requireActivity().findViewById<View>(R.id.divApartadoCalificacion)

        btnCalificacion.setOnClickListener {
            if (estado != "EN") {
                Toast.makeText(
                    requireContext(),
                    "El pedido debe estar entregado para poder calificar",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (yaCalificado) {

                fragmentCalificacion()
            } else {

                activityCalificacion()
            }
        }
    }

    private fun fragmentCalificacion() {
        Log.d("DETALLE_HISTORIAL", "👁️ Navegando a CalificacionFragment (ver calificación)")

        val calificacionFragment = CalificacionFragment.newInstance(
            pedidoIdInt = pedidoIdInt,
            pedidoId = pedidoId.toString(),
            nombreRepartidor = nombreRepartidor
        )

        (requireActivity() as? DetalleHistorialActivity)?.let { activity ->
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.contenedorFragmentoDetalle,calificacionFragment)
                .addToBackStack(null)
                .commit()

        activity.actualizarApartados(DetalleHistorialActivity.ApartadoTypeDT.CALIFICACION)
        }
    }

    private fun activityCalificacion(){
        val intent = Intent(requireContext(), CalificacionClientActivity::class.java).apply {
            putExtra("ID_PEDIDO",pedidoIdInt)
            putExtra("NUM_PEDIDO",pedidoId)
            putExtra("TOTAL",total)
            putExtra("NOMBRE_REPARTIDOR",nombreRepartidor)
        }
        startActivity(intent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            pedidoIdInt: Int,
            pedidoId: String,
            estado: String,
            nombreRepartidor: String?,
            apePaternoRepartidor: String?,
            telefonoRepartidor: String?,
            total: Double,
            direccion: String?,
            fechaEmitida: String?
        ): DetalleHistorialFragment {
            return DetalleHistorialFragment().apply {
                arguments = Bundle().apply {
                    putInt("ID_PEDIDO", pedidoIdInt)
                    putString("NUM_PEDIDO", pedidoId)
                    putString("ESTADO_PEDIDO", estado)
                    putDouble("TOTAL_PEDIDO", total)
                    putString("NOMBRE_REPARTIDOR", nombreRepartidor)
                    putString("APE_PAT_REPARTIDOR", apePaternoRepartidor)
                    putString("TELEFONO_REPARTIDOR", telefonoRepartidor)
                    putString("DIRECCION_PEDIDO", direccion)
                    putString("FECHA_PEDIDO", fechaEmitida)
                }
            }
        }
    }
}