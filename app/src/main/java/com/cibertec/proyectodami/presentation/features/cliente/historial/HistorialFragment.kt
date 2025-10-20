package com.cibertec.proyectodami.presentation.features.cliente.historial

import ClientePedidoAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.databinding.FragmentHistorialBinding
import com.cibertec.proyectodami.presentation.features.cliente.historial.filtros.FiltrosFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class HistorialFragment : Fragment() {

//    private var _binding: FragmentHistorialBinding? = null
//    private val binding get() = _binding!!
//
//    private lateinit var adapter: ClientePedidoAdapter
//    private val viewModel: HistorialViewModel by activityViewModels()
//    private lateinit var userPreferences: UserPreferences
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentHistorialBinding.inflate(inflater, container, false)
//        userPreferences = UserPreferences(requireContext())
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        setupRecyclerView()
//        setupViews()
//        observeViewModel()
//        cargarDatosUsuario()
//    }
//
//    private fun cargarDatosUsuario() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            val idCliente = userPreferences.idUsuario.first()
//
//            if (idCliente != -1) {
//                viewModel.cargarPedidos(idCliente)
//            } else {
//                mostrarEstadoVacio(true)
//            }
//        }
//    }
//    private fun mostrarEstadoVacio(mostrar: Boolean) {
//        binding.emptyState.visibility = if (mostrar) View.VISIBLE else View.GONE
//        binding.recyclerViewPedidosInicio.visibility = if (mostrar) View.GONE else View.VISIBLE
//    }
//
//    private fun setupRecyclerView() {
//        binding.recycle.layoutManager = LinearLayoutManager(requireContext())
//    }
//
//    private fun setupViews() {
//        // Botón para abrir filtros
//        binding.btnFiltros.setOnClickListener {
//            mostrarFiltros()
//        }
//    }
//
//
//    private fun observeViewModel() {
//        viewModel.hayFiltrosAplicados.observe(viewLifecycleOwner) { hayFiltros ->
//            binding.indicadorFiltros.visibility = if (hayFiltros) View.VISIBLE else View.GONE
//        }
//
//        viewModel.historialFiltrado.observe(viewLifecycleOwner) { lista ->
//            actualizarLista(lista)
//        }
//    }
//
//    private fun mostrarFiltros() {
//        val filtrosFragment = FiltrosFragment.newInstance()
//        filtrosFragment.show(childFragmentManager, FiltrosFragment.TAG)
//    }
//
//    private fun actualizarLista(lista: List<Any>) {
//        // Tu lógica para actualizar el RecyclerView
//        // adapter.submitList(lista)
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
}