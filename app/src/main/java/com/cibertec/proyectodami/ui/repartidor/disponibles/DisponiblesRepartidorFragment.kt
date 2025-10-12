package com.cibertec.proyectodami.ui.repartidor.disponibles

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.proyectodami.databinding.ButtonOptionsBinding
import com.cibertec.proyectodami.databinding.FragmentDisponiblesRepartidorBinding
import com.cibertec.proyectodami.listener.OptionsMenuListener
import com.cibertec.proyectodami.ui.repartidor.RepartidorMainActivity
import com.cibertec.proyectodami.ui.repartidor.repository.PedidoRepository
import com.google.android.material.bottomsheet.BottomSheetDialog

class DisponiblesRepartidorFragment : Fragment(), OptionsMenuListener {
    private var _binding: FragmentDisponiblesRepartidorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DisponiblesViewModel by viewModels()
    private lateinit var adapter: DisponiblesPedidoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisponiblesRepartidorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    // NUEVO: Verificar bloqueo cada vez que el fragment sea visible
    override fun onResume() {
        super.onResume()
        binding.recyclerViewPedidos.postDelayed({
            verificarPedidoActivo()
        }, 100)
    }

    private fun setupRecyclerView() {
        adapter = DisponiblesPedidoAdapter(mutableListOf()) { pedido ->
            if (PedidoRepository.tienePedidoActivo()) {
                Toast.makeText(
                    context,
                    "Ya tienes un pedido activo. Complétalo primero.",
                    Toast.LENGTH_LONG
                ).show()
                return@DisponiblesPedidoAdapter
            }

            viewModel.aceptarPedido(pedido)
            Toast.makeText(
                context,
                "Aceptando pedido #${pedido.nroPedido}...",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.recyclerViewPedidos.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@DisponiblesRepartidorFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewModel.pedidosDisponibles.observe(viewLifecycleOwner) { pedidos ->
            adapter.addAll(pedidos)

            binding.recyclerViewPedidos.visibility =
                if (pedidos.isEmpty()) View.GONE else View.VISIBLE
            binding.emptyView.visibility =
                if (pedidos.isEmpty()) View.VISIBLE else View.GONE

            // NUEVO: Verificar bloqueo después de cargar datos
            if (PedidoRepository.tienePedidoActivo()) {
                adapter.bloquearPedidos()
                binding.recyclerViewPedidos.alpha = 0.5f
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }

        // NUEVO: Observar cuando se acepta un pedido
        /*viewModel.pedidoAceptado.observe(viewLifecycleOwner) { aceptado ->
            if (aceptado) {
                adapter.bloquearPedidos()
                binding.recyclerViewPedidos.alpha = 0.5f
            } else {
                adapter.desbloquearPedidos()
                binding.recyclerViewPedidos.alpha = 1f
            }
        }*/

        viewModel.navegarAActivos.observe(viewLifecycleOwner) { navegar ->
            if (navegar) {
                Toast.makeText(
                    context,
                    "Pedido aceptado exitosamente",
                    Toast.LENGTH_SHORT
                ).show()

                navegarAPestanaActivos()

                viewModel.navegacionCompletada()
            }
        }

        PedidoRepository.pedidoActivo.observe(viewLifecycleOwner) { pedidoActivo ->
            if (pedidoActivo != null) {
                adapter.bloquearPedidos()
                binding.recyclerViewPedidos.alpha = 0.5f
                mostrarMensajePedidoActivo()
            } else {
                // No hay pedido activo, desbloquear
                adapter.desbloquearPedidos()
                binding.recyclerViewPedidos.alpha = 1f
            }
        }
    }

    override fun onOptionsMenuClicked() {
        showSortOptionsBottomSheet()
    }

    private fun showSortOptionsBottomSheet() {
        val bottomSheetBinding = ButtonOptionsBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.ordenarDistancia.setOnClickListener {
            viewModel.ordenarPorDistancia()
            Toast.makeText(context, "Ordenado por distancia", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        bottomSheetBinding.ordenarValor.setOnClickListener {
            viewModel.ordenarPorValor()
            Toast.makeText(context, "Ordenado por valor", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        bottomSheetBinding.actualizarLista.setOnClickListener {
            viewModel.cargarPedidosDisponibles()
            Toast.makeText(context, "Actualizando...", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun navegarAPestanaActivos() {
        val mainActivity = activity as? RepartidorMainActivity
        mainActivity?.mainBinding?.tabLayout?.getTabAt(1)?.select()
    }

    // NUEVO: Verificar si hay pedido activo y aplicar bloqueo
    private fun verificarPedidoActivo() {
        if (PedidoRepository.tienePedidoActivo()) {
            adapter.bloquearPedidos()
            binding.recyclerViewPedidos.alpha = 0.5f
            Log.d("DisponiblesFragment", "Pedido activo detectado - bloqueando UI")
        } else {
            adapter.desbloquearPedidos()
            binding.recyclerViewPedidos.alpha = 1f
            Log.d("DisponiblesFragment", "No hay pedido activo - desbloqueando UI")
        }
    }

    // NUEVO: Mostrar mensaje cuando hay pedido activo
    private fun mostrarMensajePedidoActivo() {
        // Solo mostrar si el fragmento está visible
        if (isResumed) {
            Toast.makeText(
                context,
                "Completa tu pedido activo antes de aceptar otro",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}