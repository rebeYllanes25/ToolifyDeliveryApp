package com.cibertec.proyectodami.presentation.features.repartidor.estadistica


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cibertec.proyectodami.databinding.FragmentEstadisticaRepartidorBinding
import com.cibertec.proyectodami.presentation.features.repartidor.escaner.EscanerActivity
import com.cibertec.proyectodami.presentation.features.repartidor.localizacion.LocalizacionActivity

class EstadisticaRepartidorFragment : Fragment() {


    private var _binding: FragmentEstadisticaRepartidorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEstadisticaRepartidorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.fabMenu.setOnClickListener {
            val intent = Intent(requireContext(), EscanerActivity::class.java)
            startActivity(intent)
        }


        binding.fabLocacion.setOnClickListener {
            val intent = Intent(requireContext(), LocalizacionActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
