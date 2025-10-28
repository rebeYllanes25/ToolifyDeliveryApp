package com.cibertec.proyectodami.presentation.features.cliente.inicio

import InicioPedidoAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class InicioFragment : Fragment() {

    companion object {
        private const val TAG = "InicioFragment"
    }

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: InicioPedidoAdapter
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
        Log.d(TAG, " Fragment creado")

        setupRecyclerView()
        observarPedidos()
        cargarDatosUsuario()

    }

    private fun cargarDatosUsuario() {
        viewLifecycleOwner.lifecycleScope.launch {
            val idCliente = userPreferences.idUsuario.first()
            Log.d(TAG, "ID Cliente obtenido: $idCliente")

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
            Log.d(TAG, "DATOS RECIBIDOS EN FRAGMENT")
            Log.d(TAG, "Pedidos: ${pedidos?.size ?: 0}")
            pedidos?.forEach { pedido ->
                Log.d(TAG, "  - Pedido #${pedido.numPedido}")
                Log.d(TAG, "    Estado: ${pedido.estado}")
                Log.d(TAG, "    Total: ${pedido.total}")
                Log.d(TAG, "    Repartidor: ${pedido.nomRepartidor}")
            }

            if (pedidos.isNullOrEmpty()) {
                mostrarEstadoVacio(true)
            } else {
                mostrarEstadoVacio(false)
                configurarAdapter(pedidos)
            }
        }
    }

    private fun configurarAdapter(pedidos: List<PedidoClienteDTO>) {
        Log.d(TAG, "Creando adapter con ${pedidos.size} items")
        adapter = InicioPedidoAdapter(
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
        Log.d(TAG, "Abriendo rastreo para pedido #${pedido.numPedido}")
        val intent = Intent(requireContext(), RastreoActivity::class.java).apply {
            putExtra("PEDIDO_ID_INT",pedido.idPedido)
            putExtra("PEDIDO_ID", pedido.numPedido)
            putExtra("ESTADO", pedido.estado)
            putExtra("REPARTIDOR", pedido.nomRepartidor)
            putExtra("APE_PATERNO", pedido.apePaternoRepartidor)
            putExtra("TELEFONO_REPARTIDOR", pedido.telefonoRepartidor)
            putExtra("TIEMPO_ENTREGA", pedido.tiempoEntregaMinutos)
            putExtra("QR_VERIFICATE_PEDIDO",pedido.qrVerificationCode)
            putExtra("PRODUCTOS", Gson().toJson(pedido.productos))
            putExtra("TOTAL", pedido.total)
            putExtra("ID_REPARTIDOR",pedido.idRepartidor)

        }
        startActivity(intent)
    }

    private fun abrirDetalle(pedido: PedidoClienteDTO) {
        Log.d(TAG, "Abriendo detalle para pedido #${pedido.numPedido}")
        val detalleFragment = DetallePedidoFragment.newInstance(
            nroPedido = pedido.numPedido,
            qrCode = pedido.qrVerificationCode
        )
        detalleFragment.show(childFragmentManager, "DetallePedidoFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}