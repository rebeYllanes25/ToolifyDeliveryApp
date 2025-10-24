package com.cibertec.proyectodami.presentation.features.repartidor.activo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.databinding.FragmentActivoRepartidorBinding
import com.cibertec.proyectodami.presentation.common.adapters.ActivoPedidoAdapter
import kotlinx.coroutines.runBlocking

class ActivoRepartidorFragment : Fragment() {

    private var _binding: FragmentActivoRepartidorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ActivoViewModel by viewModels()
    private lateinit var adapter: ActivoPedidoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivoRepartidorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupEmptyView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ActivoPedidoAdapter(
            requireActivity(),
            mutableListOf()
        ) { pedido ->
            val idRepartidor = obtenerIdRepartidor()
            viewModel.marcarEnCamino(pedido.idPedido, idRepartidor)

            Toast.makeText(
                requireContext(),
                "Pedido marcado como En Camino",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.recyclerViewActivo.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ActivoRepartidorFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupEmptyView() {
        binding.buttonView.setOnClickListener {
            navegarAPestanaDisponibles()
        }
    }

    private fun obtenerIdRepartidor(): Int {
        val userPreferences = UserPreferences(requireContext())
        return runBlocking() {
            userPreferences.obtenerIdUsuario()
        }
    }

    private fun observeViewModel() {
        viewModel.pedidoActivo.observe(viewLifecycleOwner) { pedido ->
            if (pedido != null) {
                // Mostrar el pedido activo
                adapter.setData(pedido)
                binding.recyclerViewActivo.visibility = View.VISIBLE
                binding.emptyView.visibility = View.GONE
            } else {
                // Mostrar vista vacía
                adapter.clear()
                binding.recyclerViewActivo.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.pedidoCompletado.observe(viewLifecycleOwner) { completado ->
            if (completado) {
                Toast.makeText(
                    requireContext(),
                    "Pedido completado exitosamente",
                    Toast.LENGTH_SHORT
                ).show()

                // Navegar a disponibles
                navegarAPestanaDisponibles()
                viewModel.navegarADisponibles()
            }
        }
    }

    private fun navegarAPestanaDisponibles() {
        val mainActivity = activity as? androidx.appcompat.app.AppCompatActivity
        mainActivity?.findViewById<com.google.android.material.tabs.TabLayout>(
            R.id.tab_layout
        )?.getTabAt(0)?.select()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}