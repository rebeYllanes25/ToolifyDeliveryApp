package com.cibertec.proyectodami.presentation.features.cliente.carro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.proyectodami.databinding.FragmentCarroBinding
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.presentation.common.adapters.CarroAdapter
import com.cibertec.proyectodami.presentation.features.cliente.finalizarCompra.FinalizarFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class CarroFragment : Fragment() {
    private var _binding: FragmentCarroBinding? = null
    private val binding get() = _binding!!

    private lateinit var carritoAdapter: CarroAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupButtons()
        observeCarrito()
    }

    private fun setupRecyclerView() {
        carritoAdapter = CarroAdapter(
            onIncrementar = { item ->
                if (!CarroRepository.incrementarCantidad(item.producto.idProducto)) {
                    Toast.makeText(
                        requireContext(),
                        "No hay más stock disponible",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onDecrementar = { item ->
                CarroRepository.decrementarCantidad(item.producto.idProducto)
            },
            onEliminar = { item ->
                mostrarDialogoEliminar(item.producto.nombre, item.producto.idProducto)
            }
        )

        binding.rvCarrito.apply {
            adapter = carritoAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupButtons() {
        binding.btnLimpiarCarrito.setOnClickListener {
            mostrarDialogoLimpiar()
        }

        binding.btnProcesarCompra.setOnClickListener {
            procesarCompra()
        }

        binding.btnSeguirComprando.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun observeCarrito() {
        viewLifecycleOwner.lifecycleScope.launch {
            CarroRepository.items.collect { items ->
                carritoAdapter.submitList(items)
                actualizarEstadoVacio(items.isEmpty())
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            CarroRepository.cantidadTotal.collect { cantidad ->
                binding.tvCantidadItems.text = "$cantidad item${if (cantidad != 1) "s" else ""}"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            CarroRepository.total.collect { total ->
                binding.tvTotal.text = "S/ ${String.format("%.2f", total)}"
            }
        }
    }

    private fun actualizarEstadoVacio(isEmpty: Boolean) {
        binding.apply {
            layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            rvCarrito.visibility = if (isEmpty) View.GONE else View.VISIBLE
            layoutResumen.visibility = if (isEmpty) View.GONE else View.VISIBLE
            btnLimpiarCarrito.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    private fun mostrarDialogoEliminar(nombreProducto: String, idProducto: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar producto")
            .setMessage("¿Deseas eliminar '$nombreProducto' del carrito?")
            .setPositiveButton("Eliminar") { _, _ ->
                CarroRepository.eliminarProducto(idProducto)
                Toast.makeText(
                    requireContext(),
                    "Producto eliminado",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoLimpiar() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Vaciar carrito")
            .setMessage("¿Estás seguro de que deseas vaciar el carrito?")
            .setPositiveButton("Vaciar") { _, _ ->
                CarroRepository.limpiarCarrito()
                Toast.makeText(
                    requireContext(),
                    "Carrito vaciado",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun procesarCompra() {
        if (CarroRepository.items.value.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "El carrito está vacío",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val finalizarCompraFragment = FinalizarFragment()

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.contenedorFragmento, finalizarCompraFragment)
            .addToBackStack(null)
            .commit()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}