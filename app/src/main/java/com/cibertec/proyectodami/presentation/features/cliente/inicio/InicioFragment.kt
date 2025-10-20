package com.cibertec.proyectodami.presentation.features.cliente.inicio

import PedidoInicioAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.proyectodami.databinding.FragmentInicioBinding
import com.cibertec.proyectodami.domain.model.dtos.PedidoClienteDTO
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.presentation.features.cliente.inicio.detallepedido.DetallePedidoFragment
import com.cibertec.proyectodami.presentation.features.cliente.rastreo.RastreoActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PedidoInicioAdapter
    private val viewModel: InicioViewModel by viewModels()
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observarPedidos()
        cargarDatosUsuario()
    }

    private fun cargarDatosUsuario() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Obtener el ID del usuario desde DataStore
            val idCliente = userPreferences.idUsuario.first()

            if (idCliente != -1) {
                viewModel.cargarPedidos(idCliente)
            } else {
                mostrarEstadoVacio(true)
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewPedidosInicio.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observarPedidos() {
        viewModel.pedidosEnCamino.observe(viewLifecycleOwner) { pedidos ->
            if (pedidos.isNullOrEmpty()) {
                mostrarEstadoVacio(true)
            } else {
                mostrarEstadoVacio(false)
                configurarAdapter(pedidos)
            }
        }
    }

    private fun configurarAdapter(pedidos: List<PedidoClienteDTO>) {
        adapter = PedidoInicioAdapter(
            pedidos = pedidos,
            onRastrearClick = { pedido -> abrirRastreo(pedido) },
            onDetalleClick = { pedido -> abrirDetalle(pedido) }
        )
        binding.recyclerViewPedidosInicio.adapter = adapter
    }

    private fun mostrarEstadoVacio(mostrar: Boolean) {
        binding.emptyState.visibility = if (mostrar) View.VISIBLE else View.GONE
        binding.recyclerViewPedidosInicio.visibility = if (mostrar) View.GONE else View.VISIBLE
    }

    private fun abrirRastreo(pedido: PedidoClienteDTO) {
        val intent = Intent(requireContext(), RastreoActivity::class.java).apply {
            putExtra("PEDIDO_ID", pedido.nroPedido)
            putExtra("ESTADO", pedido.estadoDelivery)
            putExtra("REPARTIDOR", pedido.nombreRepartidor)
            putExtra("TIEMPO_ENTREGA", pedido.tiempoEntregaMinutos)
        }
        startActivity(intent)
    }

    private fun abrirDetalle(pedido: PedidoClienteDTO) {
        val detalleFragment = DetallePedidoFragment.newInstance(
            nroPedido = pedido.nroPedido,
            qrCode = pedido.qrVerificationCode
        )
        detalleFragment.show(childFragmentManager, "DetallePedidoFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}