package com.cibertec.proyectodami.presentation.features.cliente.productos

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.data.api.CompraCliente
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.data.remote.RetrofitInstance
import com.cibertec.proyectodami.databinding.FragmentProductosBinding
import com.cibertec.proyectodami.domain.model.entities.Categoria
import com.cibertec.proyectodami.domain.model.entities.Producto
import com.cibertec.proyectodami.presentation.common.adapters.ProductoAdapter
import com.cibertec.proyectodami.presentation.features.cliente.carro.CarroRepository
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class ProductosFragment : Fragment() {

    private var _binding: FragmentProductosBinding? = null
    private val binding get() = _binding!!

    private lateinit var userPreferences: UserPreferences
    private lateinit var clienteAPI: CompraCliente

    private lateinit var productosAdapter: ProductoAdapter
    private val categorias = mutableListOf<Categoria>()

    // Paginación
    private var currentPage = 0
    private var totalPages = 1
    private var isLoading = false

    // Filtros
    private var selectedCategoriasIds = mutableListOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPreferences()
        setupRecyclerView()
        setupSwipeRefresh()
        setupPaginationButtons()

        cargarProductos()
    }

    private fun setupPreferences() {
        userPreferences = UserPreferences(requireContext())
        clienteAPI = RetrofitInstance.create(userPreferences).create(CompraCliente::class.java)
    }

    private fun setupRecyclerView() {
        productosAdapter = ProductoAdapter(
            onProductoClick = { producto ->
                Toast.makeText(requireContext(), "Ver ${producto.nombre}", Toast.LENGTH_SHORT).show()
            },
            onAgregarClick = { producto ->
                agregarAlCarrito(producto)
            }
        )

        binding.rvProductos.apply {
            adapter = productosAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            resetAndReload()
        }
    }

    private fun setupPaginationButtons() {
        binding.apply {
            btnFirstPage.setOnClickListener {
                if (currentPage > 0) {
                    currentPage = 0
                    cargarProductos()
                    scrollToTop()
                }
            }

            btnPrevious.setOnClickListener {
                if (currentPage > 0) {
                    currentPage--
                    cargarProductos()
                    scrollToTop()
                }
            }

            btnNext.setOnClickListener {
                if (currentPage < totalPages - 1) {
                    currentPage++
                    cargarProductos()
                    scrollToTop()
                }
            }

            btnLastPage.setOnClickListener {
                if (currentPage < totalPages - 1) {
                    currentPage = totalPages - 1
                    cargarProductos()
                    scrollToTop()
                }
            }
        }
    }

    private fun setupCategoriaChips() {
        _binding?.let { binding ->
            binding.chipGroupCategorias.removeAllViews()

            val chipTodos = Chip(requireContext()).apply {
                text = "Todos"
                isCheckable = true
                isChecked = selectedCategoriasIds.isEmpty()
                setOnClickListener {
                    selectedCategoriasIds.clear()
                    resetAndReload()
                }
            }
            binding.chipGroupCategorias.addView(chipTodos)

            categorias.forEach { categoria ->
                val chip = Chip(requireContext()).apply {
                    text = categoria.descripcion
                    isCheckable = true
                    setOnClickListener {
                        if (isChecked) {
                            selectedCategoriasIds.clear()
                            selectedCategoriasIds.add(categoria.idCategoria)
                        } else {
                            selectedCategoriasIds.remove(categoria.idCategoria)
                        }
                        resetAndReload()
                    }
                }
                binding.chipGroupCategorias.addView(chip)
            }
        }
    }

    private fun cargarProductos() {
        if (isLoading) return
        isLoading = true
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = clienteAPI.listProductosYCategorias(
                    idCategorias = if (selectedCategoriasIds.isEmpty()) null else selectedCategoriasIds,
                    page = currentPage,
                    size = 12,
                    order = "asc"
                )

                totalPages = response.productos.totalPages
                val productos = response.productos.content
                productosAdapter.submitList(productos)

                if (categorias.isEmpty()) {
                    categorias.addAll(response.categorias)
                    setupCategoriaChips()
                }

                updatePaginationUI()
                showEmptyState(productos.isEmpty())

            } catch (e: Exception) {
                Log.e("ProductosFragment", "Error cargando productos", e)
                Toast.makeText(
                    requireContext(),
                    "Error al cargar productos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                isLoading = false
                hideLoading()
            }
        }
    }

    private fun updatePaginationUI() {
        _binding?.apply {
            tvPageInfo.text = "Página ${currentPage + 1} de $totalPages"

            btnFirstPage.isEnabled = currentPage > 0
            btnPrevious.isEnabled = currentPage > 0
            btnNext.isEnabled = currentPage < totalPages - 1
            btnLastPage.isEnabled = currentPage < totalPages - 1

            btnFirstPage.alpha = if (currentPage > 0) 1f else 0.5f
            btnPrevious.alpha = if (currentPage > 0) 1f else 0.5f
            btnNext.alpha = if (currentPage < totalPages - 1) 1f else 0.5f
            btnLastPage.alpha = if (currentPage < totalPages - 1) 1f else 0.5f
        }
    }

    private fun scrollToTop() {
        _binding?.rvProductos?.smoothScrollToPosition(0)
    }

    private fun resetAndReload() {
        currentPage = 0
        cargarProductos()
    }

    private fun agregarAlCarrito(producto: Producto) {
        if (CarroRepository.agregarProducto(producto)) {
            Toast.makeText(
                requireContext(),
                "${producto.nombre} agregado al carrito",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                "No hay stock disponible",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showLoading(show: Boolean) {
        _binding?.let { binding ->
            binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
            binding.rvProductos.visibility = if (show) View.GONE else View.VISIBLE
            binding.paginationLayout.visibility = if (show) View.GONE else View.VISIBLE
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun hideLoading() {
        _binding?.let { binding ->
            binding.progressBar.visibility = View.GONE
            binding.rvProductos.visibility = View.VISIBLE
            binding.paginationLayout.visibility = View.VISIBLE
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun showEmptyState(show: Boolean) {
        _binding?.let { binding ->
            binding.layoutEmpty.visibility = if (show) View.VISIBLE else View.GONE
            binding.rvProductos.visibility = if (show) View.GONE else View.VISIBLE
            binding.paginationLayout.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
