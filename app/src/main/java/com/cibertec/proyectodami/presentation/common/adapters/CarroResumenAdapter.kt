package com.cibertec.proyectodami.presentation.common.adapters

import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.databinding.ItemCarroResumenBinding
import com.cibertec.proyectodami.domain.model.dtos.CarroItem

class CarroResumenAdapter : ListAdapter<CarroItem, CarroResumenAdapter.ViewHolder>(CarritoResumenDiffCallback()) {

    private lateinit var drawableMap: Map<Int, Int>

    inner class ViewHolder(private val binding: ItemCarroResumenBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CarroItem) {
            binding.apply {
                tvNombre.text = item.producto.nombre
                tvCantidad.text = "x${item.cantidad}"
                tvPrecio.text = "S/ ${String.format("%.2f", item.producto.precio)}"
                tvSubtotal.text = "S/ ${String.format("%.2f", item.subtotal)}"

                Glide.with(imgProducto.context).clear(imgProducto)

                // Usar solo URL de imagen
                val imageUrl = item.producto.imagen.takeIf { !it.isNullOrEmpty() }
                    ?: "https://res.cloudinary.com/dheqy208f/image/upload/v1761518343/TooLifyWeb/Products/qun2e14i1zkmahdyyung.png"

                Glide.with(imgProducto.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.no_imagen)
                    .error(R.drawable.no_imagen)
                    .into(imgProducto)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (!::drawableMap.isInitialized) {
            drawableMap = (1..53).mapNotNull { i ->
                val resId = parent.context.resources.getIdentifier(
                    "producto_$i", "drawable", parent.context.packageName
                )
                if (resId != 0) i to resId else null
            }.toMap()
        }

        val binding = ItemCarroResumenBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class CarritoResumenDiffCallback : DiffUtil.ItemCallback<CarroItem>() {
    override fun areItemsTheSame(oldItem: CarroItem, newItem: CarroItem): Boolean {
        return oldItem.producto.idProducto == newItem.producto.idProducto
    }

    override fun areContentsTheSame(oldItem: CarroItem, newItem: CarroItem): Boolean {
        return oldItem == newItem
    }
}