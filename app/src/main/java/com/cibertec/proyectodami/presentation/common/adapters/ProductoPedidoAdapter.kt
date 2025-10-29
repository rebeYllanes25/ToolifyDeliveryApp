package com.cibertec.proyectodami.presentation.common.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.databinding.CardSeguimientoItemsBinding
import com.cibertec.proyectodami.domain.model.dtos.ProductoPedidoDTO

class ProductoPedidoAdapter(
    private val productos: List<ProductoPedidoDTO>
) : RecyclerView.Adapter<ProductoPedidoAdapter.VH>() {
    inner class VH(private val binding: CardSeguimientoItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(producto: ProductoPedidoDTO) {
            binding.apply {
                Log.d(
                    "Adapter",
                    "Producto: ${producto.nombreProducto}, Cantidad: ${producto.cantidad}, Subtotal: ${producto.subTotal}"
                )
                tvNombreProducto.text = producto.nombreProducto
                imgProducts
                tvDescripcion.text = producto.descripcionProducto
                tvCantidadTotal.text = itemView.context.getString(
                    R.string.seguimiento_card_cantidad,
                    producto.cantidad
                )
                tvPrecioTotal.text = itemView.context.getString(
                    R.string.seguimiento_card_precio_total,
                    producto.subTotal.toDouble()
                )

                Glide.with(itemView.context)
                    .load(producto.imagen)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgProducts)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = CardSeguimientoItemsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(productos[position])
    }

    override fun getItemCount(): Int = productos.size
}