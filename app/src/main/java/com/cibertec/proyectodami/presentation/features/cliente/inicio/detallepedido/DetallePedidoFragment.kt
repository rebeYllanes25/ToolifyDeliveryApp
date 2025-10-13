package com.cibertec.proyectodami.presentation.features.cliente.inicio.detallepedido

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.cibertec.proyectodami.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DetallePedidoFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_NRO_PEDIDO = "nro_pedido"
        private const val ARG_QR_CODE = "qr_code"

        fun newInstance(nroPedido: String, qrCode: String): DetallePedidoFragment {
            val fragment = DetallePedidoFragment()
            val args = Bundle()
            args.putString(ARG_NRO_PEDIDO, nroPedido)
            args.putString(ARG_QR_CODE, qrCode)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalle_pedido, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nroPedido = arguments?.getString(ARG_NRO_PEDIDO) ?: return
        val qrCode = arguments?.getString(ARG_QR_CODE) ?: return

        val tvPedidoId: TextView = view.findViewById(R.id.tvPedidoIdDetalle)
        val tvQrCode: TextView = view.findViewById(R.id.tvQrCode)
        val btnCerrar: CardView = view.findViewById(R.id.btnCerrar)

        tvPedidoId.text = getString(R.string.formato_pedido_id, nroPedido)
        tvQrCode.text = qrCode

        btnCerrar.setOnClickListener {
            dismiss()
        }

        // TODO: Aquí puedes agregar más detalles del pedido
        // - Lista de productos
        // - Información del repartidor
        // - Dirección de entrega
        // - Timeline del pedido
    }
}