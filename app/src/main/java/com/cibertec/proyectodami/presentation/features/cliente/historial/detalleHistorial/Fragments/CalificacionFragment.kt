package com.cibertec.proyectodami.presentation.features.cliente.historial.detalleHistorial.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.data.api.PedidosCliente
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.data.remote.RetrofitInstance
import com.cibertec.proyectodami.databinding.FragmentCalificacionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CalificacionFragment : Fragment() {

    private var _binding: FragmentCalificacionBinding? = null
    private val binding get() = _binding!!

    private lateinit var  pedidosCliente:PedidosCliente

    private var pedidoID:Int = 0;
    private var puntuacion:Int = 0;
    private var comentario:String? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inicializarApi()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalificacionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recibirDatosIntent()
        cargarInformacionCalificacion()
    }

    private fun inicializarApi(){
        val userPreferences = UserPreferences(requireContext())
        val retrofit = RetrofitInstance.create(userPreferences)
        pedidosCliente = retrofit.create(PedidosCliente::class.java)
    }

    private fun recibirDatosIntent(){
        arguments?.let {
            pedidoID = it.getInt("ID_PEDIDO")
        }
    }

    private fun cargarInformacionCalificacion(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val responseApi = pedidosCliente.buscarCalificacion(pedidoID)

                withContext(Dispatchers.Main){
                   if (responseApi.isSuccessful){
                       val calificacionData = responseApi.body()

                            if (calificacionData!= null){

                                puntuacion = calificacionData.puntuacion
                                comentario = calificacionData.comentario

                                pintarDatosCalificacion()
                            }else{
                                Toast.makeText(requireContext(), "NO SE ENCONTRO CALIFICACION", Toast.LENGTH_SHORT).show()
                            }


                   }else{
                       Toast.makeText(requireContext(), "ERROR AL OBTENER LA CALIFICACION", Toast.LENGTH_SHORT).show()
                   }
                }
            }catch (e:Exception)
            {
                withContext(Dispatchers.Main){
                    Log.e("CALIFAICACION_FRAGMENT","ERROR ${e.message}", e )
                }
            }
        }
    }


    private fun pintarDatosCalificacion(){
        binding.rbCalificacionDetalle.rating = puntuacion.toFloat()
        binding.rbCalificacionDetalle.isEnabled = false

        binding.tvMensajeRating.text = obtenerMensajePuntuacion(puntuacion)
        binding.tvMensajeRating.setTextColor(pintarColorPorPuntuacion(puntuacion))

        binding.tvObtenerCalificacion.text = comentario

        binding.tvMensajeEmpresa.text = obtenerMensajePorPuntuacion(puntuacion)

    }

    private fun obtenerMensajePuntuacion(puntuacion:Int):String{
        return when(puntuacion){
            1 -> getString(R.string.calificacion_msg_una_estrella)
            2 -> getString(R.string.calificacion_msg_dos_estrellas)
            3 -> getString(R.string.calificacion_msg_tres_estrellas)
            4 -> getString(R.string.calificacion_msg_cuatro_estrellas)
            5 -> getString(R.string.calificacion_msg_cinco_estrellas)
            else -> "error"
        }
    }


    private fun pintarColorPorPuntuacion(puntuacion: Int):Int{
        return when(puntuacion){
            1 -> requireContext().getColor(R.color.color_badge_rojo)
            2 -> requireContext().getColor(R.color.color_amarillo)
            3 -> requireContext().getColor(R.color.orange_strong)
            4 -> requireContext().getColor(R.color.color_principal)
            5 -> requireContext().getColor(R.color.verde)
            else -> requireContext().getColor(R.color.white)
        }
    }

    private fun obtenerMensajePorPuntuacion(puntuacion: Int):String{
        return when(puntuacion){
            1 -> "Lamentamos que tu experiencia con esta compra no haya sido satisfactoria. Trabajaremos para mejorar nuestros servicios"
            2 -> "Sentimos que tu experiencia no haya cumplido tus expectativas.Tomarremos en cuenta tus comentarios para seguir mejorando"
            3 -> "Agradecemos tu calificacion. Seguimos esforzandonos para poder cumplir tus espectativas y brindarte un mejor servicio"
            4 -> "¡Gracias por tu calificacion! Nos alegra que hayas disfrutado de nuestro servicio "
            5 -> "¡Muchas gracias por tu calificación Es un placer servirte, nos alegra saber q cumplimos con tus espectativas, explora nuestro catalago de prodcto."
                else -> "ERROR"
        }
    }

    companion object {
        fun newInstance(
            pedidoIdInt: Int,
            pedidoId: String,
            nombreRepartidor: String? = null
        ): CalificacionFragment {
            return CalificacionFragment().apply {
                arguments = Bundle().apply {
                    putInt("ID_PEDIDO", pedidoIdInt)
                    putString("NUM_PEDIDO", pedidoId)
                    putString("NOMBRE_REPARTIDOR", nombreRepartidor)
                }
            }
        }
    }

}