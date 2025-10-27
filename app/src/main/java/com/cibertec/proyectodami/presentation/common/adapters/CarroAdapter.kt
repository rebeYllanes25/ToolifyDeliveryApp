package com.cibertec.proyectodami.presentation.common.adapters

import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cibertec.proyectodami.databinding.ItemCarritoBinding
import com.cibertec.proyectodami.domain.model.dtos.CarroItem
import com.cibertec.proyectodami.R

class CarroAdapter(
    private val onIncrementar: (CarroItem) -> Unit,
    private val onDecrementar: (CarroItem) -> Unit,
    private val onEliminar: (CarroItem) -> Unit
) : ListAdapter<CarroItem, CarroAdapter.CarritoViewHolder>(CarritoDiffCallback()) {

    private lateinit var drawableMap: Map<Int, Int>

    inner class CarritoViewHolder(private val binding: ItemCarritoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CarroItem) {
            binding.apply {
                tvNombre.text = item.producto.nombre
                tvCategoria.text = item.producto.categoria?.descripcion ?: ""
                tvPrecio.text = "S/ ${String.format("%.2f", item.producto.precio)}"
                tvCantidad.text = item.cantidad.toString()
                tvSubtotal.text = "S/ ${String.format("%.2f", item.subtotal)}"

                // Limpiar Glide anterior
                Glide.with(imgProducto.context).clear(imgProducto)

                // Usar solo URL de imagen
                val imageUrl = item.producto.imagen.takeIf { !it.isNullOrEmpty() }
                    ?: "https://res.cloudinary.com/dheqy208f/image/upload/v1761518343/TooLifyWeb/Products/qun2e14i1zkmahdyyung.png"

                Glide.with(imgProducto.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.no_imagen)
                    .error(R.drawable.no_imagen)
                    .into(imgProducto)

                // Configurar botones
                btnIncrementar.isEnabled = item.cantidad < item.producto.stock
                btnIncrementar.alpha = if (item.cantidad < item.producto.stock) 1f else 0.5f

                btnIncrementar.setOnClickListener { onIncrementar(item) }
                btnDecrementar.setOnClickListener { onDecrementar(item) }
                btnEliminar.setOnClickListener { onEliminar(item) }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        if (!::drawableMap.isInitialized) {
            drawableMap = (1..53).mapNotNull { i ->
                val resId = parent.context.resources.getIdentifier(
                    "producto_$i", "drawable", parent.context.packageName
                )
                if (resId != 0) i to resId else null
            }.toMap()
        }

        val binding = ItemCarritoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CarritoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CarritoDiffCallback : DiffUtil.ItemCallback<CarroItem>() {
        override fun areItemsTheSame(oldItem: CarroItem, newItem: CarroItem): Boolean {
            return oldItem.producto.idProducto == newItem.producto.idProducto
        }

        override fun areContentsTheSame(oldItem: CarroItem, newItem: CarroItem): Boolean {
            return oldItem == newItem
        }
    }
}
