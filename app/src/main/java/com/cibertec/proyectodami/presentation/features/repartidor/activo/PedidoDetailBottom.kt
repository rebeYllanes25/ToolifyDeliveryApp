package com.cibertec.proyectodami.presentation.features.repartidor.activo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cibertec.proyectodami.databinding.ButtonDetallePedidoBinding
import com.cibertec.proyectodami.domain.model.dtos.PedidoRepartidorDTO
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.cibertec.proyectodami.R

class PedidoDetailBottom(
    private val pedido: PedidoRepartidorDTO
) : BottomSheetDialogFragment() {

    private var _binding: ButtonDetallePedidoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ButtonDetallePedidoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        // Configurar datos del pedido
        binding.tvCliente.text = pedido.nomCliente
        binding.tvDireccion.text = pedido.direccionEntrega
        binding.tvMonto.text = getString(R.string.value_price, pedido.total)
        binding.tvNumeroPedido.text = getString(R.string.order_id_prefix, pedido.numPedido)
        val specs = pedido.especificaciones
        binding.tvEspecifiaciones.text = if (specs.isNullOrBlank()) {
            "No hay especificaciones"
        } else {
            specs
        }

        // TODO: Generar QR Code si es necesario
        // binding.ivQrCode.setImageBitmap(generateQRCode(pedido.idPedido.toString()))

        // Botón cerrar
        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
