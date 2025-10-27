package com.cibertec.proyectodami.presentation.features.cliente.perfil

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.cibertec.proyectodami.LoginActivity
import com.cibertec.proyectodami.data.api.PedidosCliente
import com.cibertec.proyectodami.data.api.UserAuth
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.data.remote.RetrofitInstance
import com.cibertec.proyectodami.databinding.FragmentPerfilBinding
import com.cibertec.proyectodami.domain.model.dtos.PerfilDetalleComprasDto
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale


class PerfilFragment : Fragment() {

    private var _binding : FragmentPerfilBinding?= null;
    private val binding get() = _binding!!;

    private lateinit var userPreferences: UserPreferences
    private lateinit var authApi: UserAuth
    private lateinit var pedidosApi: PedidosCliente

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater,container,false)
        return binding.root
    }

    companion object {
        private const val TAG = "PERFIL_FRAGMENT"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences = UserPreferences(requireContext())
        authApi = RetrofitInstance.create(userPreferences).create(UserAuth::class.java)
        pedidosApi = RetrofitInstance.create(userPreferences).create(PedidosCliente::class.java)


        cargarDatosRetornadosDelApi()

        binding.btnLogout.setOnClickListener {
            lifecycleScope.launch {
                userPreferences.limpiarDatos()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

    private fun cargarDatosRetornadosDelApi(){
        lifecycleScope.launch {
            try {

                val idUsuarioLogeado = userPreferences.obtenerIdUsuario()
                val pefilPrint = pedidosApi.perfilDetalle(idUsuarioLogeado)

                actualizaPerfil(pefilPrint)
            }catch (e:Exception)
            {
                Toast.makeText(
                    requireContext(),
                    "Error al cargar el perfil: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun actualizaPerfil(perfil:PerfilDetalleComprasDto){
        Log.d(TAG, "NOMBRES COMPLETOS #${perfil.nombresCompletos}")
        Log.d(TAG, "CORREO #${perfil.correo}")
        Log.d(TAG, "DOCUMENTO #${perfil.nroDoc}")
        Log.d(TAG, "DIRECCION #${perfil.direccion}")
        Log.d(TAG, "DISTRITO #${perfil.distrito}")
        Log.d(TAG, "TELEFONO #${perfil.telefono}")
        Log.d(TAG, "FECHA REGISTRO #${perfil.fechaRegistro}")
        Log.d(TAG, "productoMasComprado #${perfil.productoMasComprado}")
        Log.d(TAG, "fechaMayorCompras #${perfil.fechaMayorCompras}")
        Log.d(TAG, "totalDeProductosComprados #${perfil.totalDeProductosComprados}")
        Log.d(TAG, "gastoTotal #${perfil.gastoTotal}")
        Log.d(TAG, "categoriaMasComprada #${perfil.categoriaMasComprada}")
        Log.d(TAG, "totalVentas #${perfil.totalVentas}")
        //DATOS PERSONALES
        binding.tvNombresCompletos.text = perfil.nombresCompletos
        binding.tvCorreoCliente.text     = perfil.correo
        binding.tvDocumentoCliente.text        = perfil.nroDoc
        binding.tvDireccionCliente.text = perfil.direccion
        binding.tvDistritoCliente.text  = perfil.distrito
        binding.tvTelefonoCliente.text  = perfil.telefono
        binding.tvUnidoDesde.text       = perfil.fechaRegistro

        //DATOS DE PEDIDO
        binding.tvProductoMasCompradoCliente.text = perfil.productoMasComprado ?: "No hay informacion"
        binding.tvFechaMasComproCliente.text      = perfil.fechaMayorCompras ?: "No hay informacion"
        binding.tvTotalProductosCliente.text      = perfil.totalDeProductosComprados.toString() ?: "No hay informacion"

        val formatPrecio = NumberFormat.getCurrencyInstance(Locale("es", "PE"))

        binding.tvInversionTotalCliente.text      = formatPrecio.format(perfil.gastoTotal) ?: "No hay informacion"
        binding.tvCategoriaCliente.text           = perfil.categoriaMasComprada ?: "No hay informacion"
        binding.tvPedidosTotales.text             = perfil.totalVentas.toString() ?: "No hay informacion"
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}