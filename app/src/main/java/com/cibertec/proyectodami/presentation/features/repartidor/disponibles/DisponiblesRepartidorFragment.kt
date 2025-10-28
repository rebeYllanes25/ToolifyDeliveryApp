package com.cibertec.proyectodami.presentation.features.repartidor.disponibles

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.proyectodami.data.api.PedidosRepartidor
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.data.remote.RetrofitInstance
import com.cibertec.proyectodami.databinding.ButtonOptionsBinding
import com.cibertec.proyectodami.databinding.FragmentDisponiblesRepartidorBinding
import com.cibertec.proyectodami.domain.model.dtos.PedidoRepartidorDTO
import com.cibertec.proyectodami.listener.OptionsMenuListener
import com.cibertec.proyectodami.presentation.common.adapters.DisponiblesPedidoAdapter
import com.cibertec.proyectodami.presentation.features.repartidor.RepartidorMainActivity
import com.cibertec.proyectodami.domain.repository.PedidoRepartidorRepository
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DisponiblesRepartidorFragment : Fragment(), OptionsMenuListener {
    private var _binding: FragmentDisponiblesRepartidorBinding? = null
    private val binding get() = _binding!!

    private val userPreferences: UserPreferences by lazy {
        UserPreferences(requireContext().applicationContext)
    }

    private val viewModel: DisponiblesViewModel by viewModels()
    private lateinit var adapter: DisponiblesPedidoAdapter
    private var idRepartidor: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = requireContext().applicationContext
        val userPreferences = UserPreferences(context)
        val pedidoApi = RetrofitInstance.create(userPreferences).create(PedidosRepartidor::class.java)
        PedidoRepartidorRepository.init(pedidoApi, userPreferences)
    }

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

    override fun onResume() {
        super.onResume()
        binding.recyclerViewPedidos.postDelayed({
            verificarPedidoActivo()
        }, 100)
    }

    private fun setupRecyclerView() {
        // Inicializar el adaptador inmediatamente
        adapter = DisponiblesPedidoAdapter(
            items = mutableListOf(),
            onAceptarClick = { pedido ->
                handleAceptarPedido(pedido)
            }
        )

        binding.recyclerViewPedidos.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@DisponiblesRepartidorFragment.adapter
            setHasFixedSize(true)
        }

        // Obtener el ID del repartidor (solo el primer valor)
        lifecycleScope.launch {
            userPreferences.idUsuario.first().let { id ->
                if (id != -1) {
                    idRepartidor = id
                } else {
                    Toast.makeText(
                        context,
                        "No se pudo obtener el ID del repartidor.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun handleAceptarPedido(pedido: PedidoRepartidorDTO) {
        // Verificar si hay pedido activo
        if (PedidoRepartidorRepository.tienePedidoActivo()) {
            Toast.makeText(
                context,
                "Ya tienes un pedido activo. Complétalo primero.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Bloquear UI inmediatamente para evitar doble clic
        adapter.bloquearPedidos()
        binding.recyclerViewPedidos.alpha = 0.5f

        // Llamar al ViewModel para procesar la aceptación
        viewModel.aceptarPedido(pedido, idRepartidor)
    }

    private fun observeViewModel() {
        viewModel.pedidosDisponibles.observe(viewLifecycleOwner) { pedidos ->
            Log.d("DisponiblesFragment", "Pedidos recibidos: ${pedidos.size}")
            adapter.addAll(pedidos)

            binding.recyclerViewPedidos.visibility =
                if (pedidos.isEmpty()) View.GONE else View.VISIBLE
            binding.emptyView.visibility =
                if (pedidos.isEmpty()) View.VISIBLE else View.GONE

            // Verificar bloqueo después de cargar datos
            if (PedidoRepartidorRepository.tienePedidoActivo()) {
                adapter.bloquearPedidos()
                binding.recyclerViewPedidos.alpha = 0.5f
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }


        viewModel.navegarAActivos.observe(viewLifecycleOwner) { navegar ->
            if (navegar) {
                Toast.makeText(
                    context,
                    "Pedido aceptado exitosamente",
                    Toast.LENGTH_SHORT
                ).show()

                // Navegar a la pestaña de activos
                navegarAPestanaActivos()

                // Notificar al ViewModel que la navegación se completó
                viewModel.navegacionCompletada()
            }
        }

        PedidoRepartidorRepository.pedidoActivo.observe(viewLifecycleOwner) { pedidoActivo ->
            if (pedidoActivo != null) {
                adapter.bloquearPedidos()
                binding.recyclerViewPedidos.alpha = 0.5f
                mostrarMensajePedidoActivo()
            } else {
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

    private fun verificarPedidoActivo() {
        if (PedidoRepartidorRepository.tienePedidoActivo()) {
            adapter.bloquearPedidos()
            binding.recyclerViewPedidos.alpha = 0.5f
            Log.d("DisponiblesFragment", "Pedido activo detectado - bloqueando UI")
        } else {
            adapter.desbloquearPedidos()
            binding.recyclerViewPedidos.alpha = 1f
            Log.d("DisponiblesFragment", "No hay pedido activo - desbloqueando UI")
        }
    }

    private fun mostrarMensajePedidoActivo() {
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