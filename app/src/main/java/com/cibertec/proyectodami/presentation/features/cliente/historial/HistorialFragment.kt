package com.cibertec.proyectodami.presentation.features.cliente.historial

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.databinding.FragmentHistorialBinding
import com.cibertec.proyectodami.domain.model.dtos.PedidoClienteDTO
import com.cibertec.proyectodami.presentation.features.cliente.historial.filtros.FiltrosFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HistorialFragment : Fragment() {

    private var _binding: FragmentHistorialBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HistorialPedidoAdapter
    private lateinit var userPreferences: UserPreferences

    private val viewModel: HistorialViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistorialBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()


        setupViews()
        observeViewModel()
        cargarDatosUsuario()
    }

    private fun setupRecyclerView() {
        adapter = HistorialPedidoAdapter(
            pedidos = emptyList(),
            onDetalleClick = { pedido ->
                // Ejemplo:
                // findNavController().navigate(R.id.action_historialFragment_to_detalleFragment, bundleOf("pedido" to pedido))
            }
        )
        binding.recyclerViewPedidosHistorial.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPedidosHistorial.adapter = adapter
    }

    private fun setupViews() {
        binding.btnFiltros.setOnClickListener {
            mostrarFiltros()
        }
    }

    private fun observeViewModel() {
        viewModel.historialDePedidos.observe(viewLifecycleOwner) { pedidos ->
            Log.d("HistorialFragment", "Observer activado: ${pedidos?.size} pedidos")

            if (pedidos.isNullOrEmpty()) {
                Log.w("HistorialFragment", "Lista vacía o null")
                mostrarEstadoVacio(true)
            } else {
                Log.i("HistorialFragment", "Mostrando ${pedidos.size} pedidos")
                mostrarEstadoVacio(false)
                actualizarLista(pedidos)
            }
        }
    }

    private fun cargarDatosUsuario() {
        viewLifecycleOwner.lifecycleScope.launch {
            val idCliente = userPreferences.idUsuario.first()
            if (idCliente != -1) {
                viewModel.cargarPedidos(idCliente)
            } else {
                mostrarEstadoVacio(true)
            }
        }
    }

    private fun actualizarLista(lista: List<PedidoClienteDTO>) {
        if (lista.isEmpty()) {
            mostrarEstadoVacio(true)
        } else {
            mostrarEstadoVacio(false)
            binding.recyclerViewPedidosHistorial.adapter =
                HistorialPedidoAdapter(lista) { pedido ->
                }
        }
    }

    private fun mostrarEstadoVacio(mostrar: Boolean) {
        binding.emptyStateHistorial.visibility = if (mostrar) View.VISIBLE else View.GONE
        binding.recyclerViewPedidosHistorial.visibility = if (mostrar) View.GONE else View.VISIBLE
    }

    private fun mostrarFiltros() {
        val filtrosFragment = FiltrosFragment.newInstance()
        filtrosFragment.show(childFragmentManager, FiltrosFragment.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
