package com.cibertec.proyectodami.presentation.features.cliente.historial

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.databinding.FragmentHistorialBinding
import com.cibertec.proyectodami.presentation.features.cliente.historial.filtros.FiltrosFragment


class HistorialFragment : Fragment() {

    private var _binding: FragmentHistorialBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistorialViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        // Botón para abrir filtros
        binding.btnFiltros.setOnClickListener {
            mostrarFiltros()
        }
    }


    private fun observeViewModel() {
        viewModel.hayFiltrosAplicados.observe(viewLifecycleOwner) { hayFiltros ->
            binding.indicadorFiltros.visibility = if (hayFiltros) View.VISIBLE else View.GONE
        }

        viewModel.historialFiltrado.observe(viewLifecycleOwner) { lista ->
            actualizarLista(lista)
        }
    }

    private fun mostrarFiltros() {
        val filtrosFragment = FiltrosFragment.newInstance()
        filtrosFragment.show(childFragmentManager, FiltrosFragment.TAG)
    }

    private fun actualizarLista(lista: List<Any>) {
        // Tu lógica para actualizar el RecyclerView
        // adapter.submitList(lista)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}