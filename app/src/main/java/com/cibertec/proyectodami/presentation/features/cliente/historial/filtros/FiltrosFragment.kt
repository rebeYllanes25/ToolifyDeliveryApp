package com.cibertec.proyectodami.presentation.features.cliente.historial.filtros

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.cibertec.proyectodami.databinding.FragmentFiltrosBinding
import com.cibertec.proyectodami.presentation.features.cliente.historial.HistorialViewModel
import androidx.fragment.app.activityViewModels
import com.cibertec.proyectodami.domain.model.dtos.FiltrosData
import java.text.SimpleDateFormat
import java.util.*

class FiltrosFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFiltrosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistorialViewModel by activityViewModels()

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFiltrosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDatePickers()
        setupPriceSlider()
        setupButtons()
        observeViewModel()
    }

    // ---------------- FECHAS ---------------- //
    private fun setupDatePickers() {
        binding.etFechaInicio.setOnClickListener {
            showDatePicker { date ->
                binding.etFechaInicio.setText(dateFormat.format(date))
            }
        }

        binding.etFechaFin.setOnClickListener {
            showDatePicker { date ->
                binding.etFechaFin.setText(dateFormat.format(date))
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // ---------------- SLIDER PRECIO ---------------- //
    private fun setupPriceSlider() {
        binding.rangeSliderPrecio.apply {
            valueFrom = 0f
            valueTo = 1000f
            setValues(0f, 1000f)

            addOnChangeListener { slider, _, _ ->
                val values = slider.values
                binding.tvPrecioMin.text = "S/ ${values[0].toInt()}"
                binding.tvPrecioMax.text = "S/ ${values[1].toInt()}"
            }
        }
    }

    // ---------------- BOTONES ---------------- //
    private fun setupButtons() {
        binding.btnAplicarFiltros.setOnClickListener { aplicarFiltros() }
        binding.btnLimpiar.setOnClickListener { limpiarFiltros() }
    }

    // ---------------- OBSERVADORES ---------------- //
    private fun observeViewModel() {
        viewModel.filtrosActivos.observe(viewLifecycleOwner) { filtros ->
            filtros?.let {
                binding.etFechaInicio.setText(it.fechaInicio)
                binding.etFechaFin.setText(it.fechaFin)
                binding.rangeSliderPrecio.setValues(
                    it.precioMin.toFloat(),
                    it.precioMax.toFloat()
                )
                binding.tvPrecioMin.text = "S/ ${it.precioMin}"
                binding.tvPrecioMax.text = "S/ ${it.precioMax}"
            }
        }
    }

    // ---------------- LÓGICA ---------------- //
    private fun aplicarFiltros() {
        val fechaInicio = binding.etFechaInicio.text.toString()
        val fechaFin = binding.etFechaFin.text.toString()
        val precioMin = binding.rangeSliderPrecio.values[0].toInt()
        val precioMax = binding.rangeSliderPrecio.values[1].toInt()

        // Llamada correcta al ViewModel
        val filtros = FiltrosData(
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            precioMin = precioMin,
            precioMax = precioMax
        )

        viewModel.aplicarFiltros(filtros)
        dismiss()
    }

    private fun limpiarFiltros() {
        binding.etFechaInicio.text?.clear()
        binding.etFechaFin.text?.clear()
        binding.rangeSliderPrecio.setValues(0f, 1000f)
        binding.tvPrecioMin.text = "S/ 0"
        binding.tvPrecioMax.text = "S/ 1000"
        viewModel.limpiarFiltros()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FiltrosFragment"
        fun newInstance() = FiltrosFragment()
    }
}
