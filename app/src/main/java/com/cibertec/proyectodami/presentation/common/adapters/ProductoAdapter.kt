package com.cibertec.proyectodami.presentation.common.adapters

import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.databinding.ItemProductoBinding
import com.cibertec.proyectodami.domain.model.entities.Producto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductoAdapter(
    private val onProductoClick: (Producto) -> Unit,
    private val onAgregarClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    private val productos = mutableListOf<Producto>()
    private lateinit var drawableMap: Map<Int, Int>
// Esto consumo la base64 y drawables
    /*inner class ProductoViewHolder(private val binding: ItemProductoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(producto: Producto) {
            binding.apply {
                tvNombre.text = producto.nombre
                tvCategoria.text = producto.categoria?.descripcion ?: ""
                tvPrecio.text = "S/ ${String.format("%.2f", producto.precio)}"
                tvStockBajo.visibility = if (producto.stock < 10) View.VISIBLE else View.GONE
                btnAgregar.isEnabled = producto.stock > 0
                root.alpha = if (producto.stock > 0) 1f else 0.5f

                root.setOnClickListener { onProductoClick(producto) }
                btnAgregar.setOnClickListener { onAgregarClick(producto) }

                // Limpiar Glide anterior
                Glide.with(imgProducto.context).clear(imgProducto)

                // Cargar imagen de manera eficiente
                val imageToLoad: Any = producto.decodedImage
                    ?: producto.imagenBytes
                    ?: drawableMap[producto.idProducto]
                    ?: R.drawable.no_imagen

                Glide.with(imgProducto.context)
                    .load(imageToLoad)
                    .placeholder(R.drawable.no_imagen)
                    .error(R.drawable.no_imagen)
                    .into(imgProducto)
            }
        }
    }*/
inner class ProductoViewHolder(private val binding: ItemProductoBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(producto: Producto) {
        binding.apply {
            tvNombre.text = producto.nombre
            tvCategoria.text = producto.categoria?.descripcion ?: ""
            tvPrecio.text = "S/ ${String.format("%.2f", producto.precio)}"
            tvStockBajo.visibility = if (producto.stock < 10) View.VISIBLE else View.GONE
            btnAgregar.isEnabled = producto.stock > 0
            root.alpha = if (producto.stock > 0) 1f else 0.5f

            // Eventos de click
            root.setOnClickListener { onProductoClick(producto) }
            btnAgregar.setOnClickListener { onAgregarClick(producto) }

            // Limpiar Glide anterior (importante al reciclar vistas)
            Glide.with(imgProducto.context).clear(imgProducto)

            // Obtener URL de imagen (usa campo 'imagen' del modelo)
            val imageUrl = producto.imagen.takeIf { !it.isNullOrEmpty() }
                ?: "https://res.cloudinary.com/dheqy208f/image/upload/v1761518343/TooLifyWeb/Products/qun2e14i1zkmahdyyung.png"

            Log.d("ProductoViewHolder", "Cargando imagen: $imageUrl")

            Glide.with(imgProducto.context)
                .load(imageUrl)
                .placeholder(R.drawable.no_imagen)
                .error(R.drawable.no_imagen)
                .into(imgProducto)
        }
    }
}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        if (!::drawableMap.isInitialized) {
            drawableMap = (1..53).mapNotNull { i ->
                val resId = parent.context.resources.getIdentifier(
                    "producto_$i", "drawable", parent.context.packageName
                )
                if (resId != 0) i to resId else null
            }.toMap()
        }

        val binding = ItemProductoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(productos[position])
    }

    override fun getItemCount() = productos.size

    fun submitList(newProductos: List<Producto>) {
        productos.clear()
        productos.addAll(newProductos)
        notifyDataSetChanged()
    }
}
