package com.cibertec.proyectodami.presentation.features.cliente.inicio

import PedidoInicioAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.proyectodami.databinding.FragmentInicioBinding
import com.cibertec.proyectodami.domain.model.dtos.PedidoClienteDTO
import com.cibertec.proyectodami.presentation.features.cliente.inicio.detallepedido.DetallePedidoFragment
import com.cibertec.proyectodami.presentation.features.cliente.rastreo.RastreoActivity
import org.threeten.bp.LocalDateTime

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PedidoInicioAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        cargarPedidos()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewPedidosInicio.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun cargarPedidos() {
        // TODO: Reemplazar con llamada real a tu API/ViewModel
        val pedidos = obtenerPedidosEjemplo()

        if (pedidos.isEmpty()) {
            mostrarEstadoVacio(true)
        } else {
            mostrarEstadoVacio(false)
            configurarAdapter(pedidos)
        }
    }

    private fun configurarAdapter(pedidos: List<PedidoClienteDTO>) {
        adapter = PedidoInicioAdapter(
            pedidos = pedidos,
            onRastrearClick = { pedido ->
                // Navegar a Activity de rastreo
                abrirRastreo(pedido)
            },
            onDetalleClick = { pedido ->
                // Abrir fragment de detalle
                abrirDetalle(pedido)
            }
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
        // Abrir BottomSheet o Fragment con detalles del pedido
        val detalleFragment = DetallePedidoFragment.newInstance(
            nroPedido = pedido.nroPedido,
            qrCode = pedido.qrVerificationCode
        )

        detalleFragment.show(childFragmentManager, "DetallePedidoFragment")
    }

    /**
     * DATOS DE EJEMPLO - Reemplazar con datos reales de tu API
     */
    private fun obtenerPedidosEjemplo(): List<PedidoClienteDTO> {
        return listOf(
            PedidoClienteDTO(
                nroPedido = "DEL2024001",
                fechaPedido = LocalDateTime.now().minusHours(2),
                estadoDelivery = "EC",
                tiempoEntregaMinutos = 15,
                nombreRepartidor = "Juan Pérez",
                movilidad = "Moto",
                qrVerificationCode = "QR123ABC",
                productos = emptyList(),
                subtotalProductos = 20.50,
                costoEnvio = 4.00,
                totalPagar = 24.50
            ),
            PedidoClienteDTO(
                nroPedido = "DEL2024002",
                fechaPedido = LocalDateTime.now().minusHours(3),
                estadoDelivery = "AS",
                tiempoEntregaMinutos = 30,
                nombreRepartidor = "María García",
                movilidad = "Bicicleta",
                qrVerificationCode = "QR456DEF",
                productos = emptyList(),
                subtotalProductos = 15.75,
                costoEnvio = 3.00,
                totalPagar = 18.75
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}