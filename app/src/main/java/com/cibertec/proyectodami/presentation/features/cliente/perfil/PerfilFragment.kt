package com.cibertec.proyectodami.presentation.features.cliente.perfil

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.cibertec.proyectodami.LoginActivity
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.data.api.UserAuth
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.data.remote.RetrofitInstance
import com.cibertec.proyectodami.databinding.FragmentPerfilBinding
import kotlinx.coroutines.launch


class PerfilFragment : Fragment() {

    private var _binding : FragmentPerfilBinding?= null;
    private val binding get() = _binding!!;

    private lateinit var userPreferences: UserPreferences
    private lateinit var authApi: UserAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences = UserPreferences(requireContext())
        authApi = RetrofitInstance.create(userPreferences).create(UserAuth::class.java)

        binding.btnLogout.setOnClickListener {
            lifecycleScope.launch {
                userPreferences.limpiarDatos()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        activarEfectoMarquee()
    }


    fun activarEfectoMarquee(){

        //DATOS PERSONALES
        binding.tvCorreoCliente.isSelected = true;
        binding.tvDocumentoCliente.isSelected = true;
        binding.tvDireccionCliente.isSelected = true;
        binding.tvDistritoCliente.isSelected = true;
        binding.tvTelefonoCliente.isSelected = true;
        binding.tvUnidoDesde.isSelected = true;

        //DATOS COMPRAS
        binding.tvProductoMasCompradoCliente.isSelected = true;
        binding.tvFechaMasComproCliente.isSelected = true;
        binding.tvTotalProductosCliente.isSelected = true;
        binding.tvInversionTotalCliente.isSelected = true;
        binding.tvCategoriaCliente.isSelected = true;



    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}