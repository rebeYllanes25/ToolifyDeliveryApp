package com.cibertec.proyectodami.presentation.features.cliente.finalizarCompra

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.cibertec.proyectodami.databinding.DialogFullscreenMapBinding
import com.cibertec.proyectodami.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapaFullscreenDialogFragment(
    private val ubicacionInicial: LatLng?,
    private val onUbicacionConfirmada: (LatLng, String) -> Unit
) : DialogFragment(), OnMapReadyCallback {

    private var _binding: DialogFullscreenMapBinding? = null
    private val binding get() = _binding!!
    private var map: GoogleMap? = null
    private var ubicacionSeleccionada: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFullscreenMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentFullscreen)
                as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnCerrarFullscreen.setOnClickListener { dismiss() }
        binding.btnConfirmarUbicacion.setOnClickListener {
            ubicacionSeleccionada?.let { ubicacion ->
                onUbicacionConfirmada(ubicacion, binding.tvDireccionDialog.text.toString())
                dismiss()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.uiSettings?.isZoomControlsEnabled = true

        val limaDefault = LatLng(-12.0464, -77.0428)
        val initialPos = ubicacionInicial ?: limaDefault

        // 1. Inicializar ubicación y marcador
        seleccionarUbicacion(initialPos, false)

        // 2. Click Listener
        map?.setOnMapClickListener { latLng ->
            seleccionarUbicacion(latLng, true)
        }

        // 3. Drag Listener
        map?.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {
                binding.tvDireccionDialog.text = "Moviendo ubicación..."
            }
            override fun onMarkerDrag(marker: Marker) {}
            override fun onMarkerDragEnd(marker: Marker) {
                seleccionarUbicacion(marker.position, false)
            }
        })
    }

    // FUNCIÓN NUEVA: Abstrae la lógica de selección y llama al Fragmento Padre para el geocoding
    private fun seleccionarUbicacion(latLng: LatLng, animate: Boolean) {
        map?.clear()
        map?.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("📍 Nueva ubicación")
                .draggable(true)
        )?.showInfoWindow()

        ubicacionSeleccionada = latLng

        if (animate) {
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        } else {
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        }

        // Llama a la función pública del Fragmento Padre para obtener la dirección real
        (parentFragment as? FinalizarFragment)?.obtenerDireccionParaDialogo(latLng) { direccion ->
            binding.tvDireccionDialog.text = direccion
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
