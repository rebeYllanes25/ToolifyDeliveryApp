package com.cibertec.proyectodami.presentation.features.cliente.notificaciones


import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.databinding.FragmentNotificacionesBinding
import com.cibertec.proyectodami.domain.model.entities.Notificacion
import com.cibertec.proyectodami.domain.model.enums.FiltroNotificacion
import com.cibertec.proyectodami.presentation.common.adapters.NotificacionesAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout

class NotificacionesFragment : Fragment() {

    private var _binding: FragmentNotificacionesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotificacionesViewModel
    private lateinit var adapter: NotificacionesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificacionesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[NotificacionesViewModel::class.java]

        configurarRecyclerView()
        configurarListeners()
        observarViewModel()
    }

    private fun configurarRecyclerView() {
        adapter = NotificacionesAdapter(
            onNotificacionClick = { notificacion ->
                viewModel.onNotificacionClick(notificacion)
            },
            onMarcarLeida = { notificacion ->
                viewModel.marcarComoLeida(notificacion)
                val mensaje = if (notificacion.leida) {
                    getString(R.string.notificaciones_marcar_no_leida)
                } else {
                    getString(R.string.notificaciones_marcar_leida)
                }
                mostrarSnackbar(mensaje)
            },
            onEliminar = { notificacion ->
                mostrarDialogoEliminar(notificacion)
            }
        )

        binding.rvNotificaciones.adapter = adapter
    }

    private fun configurarListeners() {

        binding.tabsFiltro.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val filtro = when (tab.position) {
                    0 -> FiltroNotificacion.TODAS
                    1 -> FiltroNotificacion.SIN_LEER
                    2 -> FiltroNotificacion.LEIDAS
                    else -> FiltroNotificacion.TODAS
                }
                viewModel.cambiarFiltro(filtro)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.btnMarcarTodasLeidas.setOnClickListener {
            viewModel.marcarTodasComoLeidas()
            mostrarSnackbar(getString(R.string.notificaciones_todas_marcadas_leidas))
        }

        binding.btnEliminarTodas.setOnClickListener {
            mostrarDialogoEliminarTodas()
        }
    }

    private fun observarViewModel() {
        viewModel.notificaciones.observe(viewLifecycleOwner) {
            actualizarLista()
        }

        viewModel.cantidadSinLeer.observe(viewLifecycleOwner) { cantidad ->
            if (cantidad > 0) {
                binding.tvContadorNotificaciones.text = getString(
                    R.string.notificaciones_sin_leer,
                    cantidad
                )
                binding.tvContadorNotificaciones.visibility = View.VISIBLE
            } else {
                binding.tvContadorNotificaciones.visibility = View.GONE
            }

            binding.btnMarcarTodasLeidas.isEnabled = cantidad > 0
        }

        viewModel.filtro.observe(viewLifecycleOwner) {
            actualizarLista()
        }
    }

    private fun actualizarLista() {
        val notificaciones = viewModel.obtenerNotificacionesFiltradas()
        adapter.submitList(notificaciones)

        val mostrarVacio = notificaciones.isEmpty()
        binding.layoutEstadoVacio.visibility = if (mostrarVacio) View.VISIBLE else View.GONE
        binding.rvNotificaciones.visibility = if (mostrarVacio) View.GONE else View.VISIBLE

        binding.btnEliminarTodas.isEnabled = notificaciones.isNotEmpty()
    }

    private fun mostrarDialogoEliminar(notificacion: Notificacion) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.notificaciones_confirmar_eliminar_titulo))
            .setMessage(getString(R.string.notificaciones_confirmar_eliminar_mensaje))
            .setPositiveButton(getString(R.string.notificaciones_btn_confirmar)) { dialog, _ ->
                viewModel.eliminarNotificacion(notificacion)
                mostrarSnackbar(getString(R.string.notificaciones_eliminada))
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.notificaciones_btn_cancelar)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun mostrarDialogoEliminarTodas() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.notificaciones_confirmar_eliminar_todas))
            .setMessage(getString(R.string.notificaciones_confirmar_eliminar_mensaje))
            .setPositiveButton(getString(R.string.notificaciones_btn_confirmar)) { dialog, _ ->
                viewModel.eliminarTodas()
                mostrarSnackbar(getString(R.string.notificaciones_todas_eliminadas))
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.notificaciones_btn_cancelar)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun mostrarSnackbar(mensaje: String) {
        Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = NotificacionesFragment()
    }
}