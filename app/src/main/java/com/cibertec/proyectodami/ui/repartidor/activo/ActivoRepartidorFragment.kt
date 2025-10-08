package com.cibertec.proyectodami.ui.repartidor.activo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.databinding.FragmentActivoRepartidorBinding

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
        adapter = ActivoPedidoAdapter(mutableListOf())

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

        viewModel.pedidoCompletado.observe(viewLifecycleOwner) { completado ->
            if (completado) {
                Toast.makeText(
                    context,
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