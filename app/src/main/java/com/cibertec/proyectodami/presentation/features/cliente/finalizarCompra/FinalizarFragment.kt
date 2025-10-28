package com.cibertec.proyectodami.presentation.features.cliente.finalizarCompra

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.data.api.CompraCliente
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.data.remote.RetrofitInstance
import com.cibertec.proyectodami.databinding.FragmentFinalizarBinding
import com.cibertec.proyectodami.domain.model.dtos.CarroItem
import com.cibertec.proyectodami.domain.model.dtos.PedidoCompra
import com.cibertec.proyectodami.domain.model.entities.DetalleVenta
import com.cibertec.proyectodami.domain.model.entities.ProductoVenta
import com.cibertec.proyectodami.domain.model.entities.UsuarioVentaDTO
import com.cibertec.proyectodami.domain.model.entities.Venta
import com.cibertec.proyectodami.presentation.common.adapters.CarroResumenAdapter
import com.cibertec.proyectodami.presentation.features.cliente.carro.CarroRepository
import com.cibertec.proyectodami.presentation.features.cliente.historial.HistorialFragment
import com.cibertec.proyectodami.presentation.features.cliente.inicio.InicioFragment
import com.cibertec.proyectodami.presentation.features.cliente.inicio.detallepedido.DetallePedidoFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.*

class FinalizarFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentFinalizarBinding? = null
    private val binding get() = _binding!!

    private lateinit var userPreferences: UserPreferences
    private lateinit var compraApiService: CompraCliente
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var carritoAdapter: CarroResumenAdapter

    private var googleMap: GoogleMap? = null
    private var ubicacionSeleccionada: LatLng? = null
    private var direccionObtenida: String = ""
    private var marcadorSeleccionado: Marker? = null

    // MÉTODO DE ENTREGA FIJO EN DELIVERY ('D')
    private var metodoEntrega: String = "D"
    // TIPO DE MOVILIDAD PREDETERMINADO
    private var tipoMovilidad: String? = "M"

    private var items = listOf<CarroItem>()
    private var total: Double = 0.0

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            obtenerUbicacionActual()
        } else {
            Toast.makeText(
                requireContext(),
                "Permiso de ubicación denegado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinalizarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupServices()
        setupRecyclerView()
        setupSpinners()
        sincronizarEstadoInicial()
        setupRadioButtons()
        setupButtons()
        observeCarrito()

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        setupMapButtons()
    }

    // CORRECCIÓN: Se establece el estado inicial fijo en Delivery con Moto
    private fun sincronizarEstadoInicial() {
        // El método de entrega es fijo: Delivery ("D")
        metodoEntrega = "D"
        mostrarSeccionDelivery(true)

        // Asegurar que 'rbMoto' esté marcado y la variable tipoMovilidad sea 'M' (Por defecto en el XML)
        tipoMovilidad = "M"
    }

    private fun setupServices() {
        userPreferences = UserPreferences(requireContext())
        compraApiService = RetrofitInstance.create(userPreferences).create(CompraCliente::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun setupRecyclerView() {
        carritoAdapter = CarroResumenAdapter()
        binding.rvResumenCarrito.apply {
            adapter = carritoAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSpinners() {
        // Lista de meses
        val meses = (1..12).map { it.toString().padStart(2, '0') }
        val mesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, meses)
        binding.spinnerMes.setAdapter(mesAdapter)

        // Lista de años
        val anioActual = Calendar.getInstance().get(Calendar.YEAR)
        val anios = (anioActual..anioActual + 15).map { it.toString() }
        val anioAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, anios)
        binding.spinnerAnio.setAdapter(anioAdapter)
    }

    // CORRECCIÓN: Se elimina la lógica de rgMetodoEntrega
    private fun setupRadioButtons() {
        // Radio buttons de método de entrega (eliminado del XML, por lo tanto se elimina el listener)

        // Radio buttons de tipo de movilidad
        binding.rgTipoMovilidad.setOnCheckedChangeListener { _, checkedId ->
            tipoMovilidad = when (checkedId) {
                R.id.rbMoto -> "M"
                R.id.rbAuto -> "A"
                else -> null
            }
            calcularTotal()
        }

        // Ya que el Delivery es fijo, solicitamos ubicación apenas se inicia el Fragment
        if (ubicacionSeleccionada == null) {
            solicitarPermisoUbicacion()
        }
    }

    private fun setupButtons() {
        binding.btnConfirmarCompra.setOnClickListener {
            mostrarDialogoPago()
        }

        binding.btnCancelar.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun observeCarrito() {
        viewLifecycleOwner.lifecycleScope.launch {
            CarroRepository.items.collect { itemsList ->
                items = itemsList
                carritoAdapter.submitList(itemsList)
                calcularTotal()
            }
        }
    }

    private fun mostrarSeccionDelivery(mostrar: Boolean) {
        // En este caso, siempre se mostrará, ya que se eliminó la lógica de selección.
        binding.layoutDelivery.visibility = if (mostrar) View.VISIBLE else View.GONE
    }

    // CORRECCIÓN: La lógica de cálculo ya no considera 'S' (Sucursal)
    private fun calcularTotal() {
        viewLifecycleOwner.lifecycleScope.launch {
            CarroRepository.total.collect { subtotal ->
                total = subtotal

                var costoDelivery = 0.0
                // Como metodoEntrega es siempre "D", siempre se calcula el costo
                costoDelivery = when (tipoMovilidad) {
                    "M" -> 10.0
                    "A" -> 15.0
                    else -> 0.0
                }
                total += costoDelivery

                binding.tvTotal.text = "S/ ${String.format("%.2f", total)}"
                binding.tvSubtotal.text = "S/ ${String.format("%.2f", subtotal)}"

                if (costoDelivery > 0) {
                    binding.tvCostoDelivery.text = "S/ ${String.format("%.2f", costoDelivery)}"
                    binding.layoutCostoDelivery.visibility = View.VISIBLE
                } else {
                    binding.layoutCostoDelivery.visibility = View.GONE
                }
            }
        }
    }

    // CORRECCIÓN: onMapReady ahora siempre solicita la ubicación
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap?.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMapToolbarEnabled = false
            uiSettings.isMyLocationButtonEnabled = false

            setOnMapClickListener { latLng ->
                seleccionarUbicacion(latLng)
            }

            setOnMapLongClickListener { latLng ->
                seleccionarUbicacion(latLng)
            }

            setOnMarkerClickListener { marker ->
                true
            }

            setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragStart(marker: Marker) {
                    binding.tvDireccion.text = "Moviendo ubicación..."
                }

                override fun onMarkerDrag(marker: Marker) {
                }

                override fun onMarkerDragEnd(marker: Marker) {
                    ubicacionSeleccionada = marker.position
                    obtenerDireccionDesdeCoordenadas(
                        marker.position.latitude,
                        marker.position.longitude
                    )
                }
            })
        }

        // Ya no es condicional, siempre solicitamos la ubicación ya que es Delivery
        solicitarPermisoUbicacion()
    }

    private fun seleccionarUbicacion(latLng: LatLng, animate: Boolean = true) {
        ubicacionSeleccionada = latLng
        marcadorSeleccionado?.remove()

        marcadorSeleccionado = googleMap?.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("📍 Ubicación de entrega")
                .snippet("Arrastra para ajustar")
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .alpha(0.9f)
        )

        // Mostrar info window automáticamente
        marcadorSeleccionado?.showInfoWindow()

        // Animar cámara
        if (animate) {
            googleMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(latLng, 16f),
                350,
                null
            )
        } else {
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        }

        // Obtener dirección
        obtenerDireccionDesdeCoordenadas(latLng.latitude, latLng.longitude)
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacionActual() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                seleccionarUbicacion(latLng, true)
                Toast.makeText(requireContext(), "📍 Ubicación actual", Toast.LENGTH_SHORT).show()
            } else {
                // Ubicación por defecto (Lima, Perú)
                val limaDefault = LatLng(-12.0464, -77.0428)
                seleccionarUbicacion(limaDefault, true)
                Toast.makeText(
                    requireContext(),
                    "No se pudo obtener ubicación, mostrando ubicación por defecto",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.addOnFailureListener {
            Log.e("FinalizarCompra", "Error al obtener ubicación", it)
            Toast.makeText(
                requireContext(),
                "Error al obtener ubicación",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun obtenerDireccionDesdeCoordenadas(lat: Double, lng: Double) {
        binding.tvDireccion.text = "Buscando dirección..."
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val addresses = geocoder.getFromLocation(lat, lng, 1)

                    withContext(Dispatchers.Main) {
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            direccionObtenida = buildString {
                                if (address.thoroughfare != null) {
                                    append(address.thoroughfare)
                                    if (address.subThoroughfare != null) {
                                        append(" ${address.subThoroughfare}")
                                    }
                                    append(", ")
                                }
                                if (address.locality != null) {
                                    append("${address.locality}, ")
                                }
                                if (address.adminArea != null) {
                                    append(address.adminArea)
                                }
                            }.trim().removeSuffix(",")

                            binding.tvDireccion.text = direccionObtenida
                        } else {
                            direccionObtenida = "Dirección no encontrada ($lat, $lng)"
                            binding.tvDireccion.text = direccionObtenida
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("FinalizarCompra", "Error obteniendo dirección (IO)", e)
                        direccionObtenida = "$lat, $lng"
                        binding.tvDireccion.text = direccionObtenida
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FinalizarCompra", "Error al inicializar Geocoder", e)
            direccionObtenida = "$lat, $lng"
            binding.tvDireccion.text = direccionObtenida
        }
    }

    private fun setupMapButtons() {
        binding.btnFullscreen.setOnClickListener {
            mostrarMapaPantallaCompleta()
        }

        binding.btnUbicacionActual.setOnClickListener {
            solicitarPermisoUbicacion()
        }
    }

    private fun mostrarMapaPantallaCompleta() {
        if (googleMap == null) {
            Toast.makeText(requireContext(), "El mapa no está listo.", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Usa la ubicación actual o Lima por defecto.
        val initialLatLng = ubicacionSeleccionada ?: LatLng(-12.0464, -77.0428)

        // 2. Crea una instancia de tu DialogFragment, pasando la ubicación inicial
        val mapDialogFragment = MapaFullscreenDialogFragment(
            ubicacionInicial = initialLatLng,
            onUbicacionConfirmada = { nuevaUbicacion, nuevaDireccion ->

                // 3. Actualizar el Fragmento principal con los nuevos datos
                ubicacionSeleccionada = nuevaUbicacion
                direccionObtenida = nuevaDireccion
                seleccionarUbicacion(nuevaUbicacion, true)
                binding.tvDireccion.text = nuevaDireccion
            }
        )

        mapDialogFragment.show(childFragmentManager, "MapaFullscreenDialogFragment")
    }

    // Esta función es usada por el DialogFragment
    fun obtenerDireccionParaDialogo(latLng: LatLng, onResult: (String) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                withContext(Dispatchers.Main) {
                    val direccion = if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        buildString {
                            if (address.thoroughfare != null) {
                                append(address.thoroughfare)
                                if (address.subThoroughfare != null) append(" ${address.subThoroughfare}")
                                append(", ")
                            }
                            if (address.locality != null) append("${address.locality}, ")
                            if (address.adminArea != null) append(address.adminArea)
                        }.trim().removeSuffix(",")
                    } else {
                        "${latLng.latitude}, ${latLng.longitude}"
                    }
                    onResult(direccion)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult("${latLng.latitude}, ${latLng.longitude}")
                }
            }
        }
    }

    private fun solicitarPermisoUbicacion() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                obtenerUbicacionActual()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun mostrarDialogoPago() {
        if (!validarDatos()) return

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmar Compra")
            .setMessage("¿Deseas confirmar la compra por un total de S/ ${String.format("%.2f", total)}?")
            .setPositiveButton("Confirmar") { _, _ ->
                procesarCompra()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun validarDatos(): Boolean {
        if (items.isEmpty()) {
            Toast.makeText(requireContext(), "El carrito está vacío", Toast.LENGTH_SHORT).show()
            return false
        }

        if (ubicacionSeleccionada == null) {
            Toast.makeText(
                requireContext(),
                "Seleccione una ubicación en el mapa",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (tipoMovilidad == null) {
            Toast.makeText(
                requireContext(),
                "Seleccione el tipo de movilidad",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (binding.etNumeroTarjeta.text.isNullOrBlank() ||
            binding.etNumeroTarjeta.text.toString().length < 16) {
            binding.etNumeroTarjeta.error = "Número de tarjeta inválido"
            return false
        }

        if (binding.etCvv.text.isNullOrBlank() ||
            binding.etCvv.text.toString().length < 3) {
            binding.etCvv.error = "CVV inválido"
            return false
        }

        val mes = binding.spinnerMes.text.toString().toIntOrNull()
        val anio = binding.spinnerAnio.text.toString().toIntOrNull()
        val calendar = Calendar.getInstance()
        val mesActual = calendar.get(Calendar.MONTH) + 1
        val anioActual = calendar.get(Calendar.YEAR)

        if (mes == null || anio == null ||
            anio < anioActual ||
            (anio == anioActual && mes < mesActual)) {
            Toast.makeText(requireContext(), "Fecha de vencimiento inválida", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun procesarCompra() {
        if (items.isEmpty()) {
            Toast.makeText(requireContext(), "El carrito está vacío.", Toast.LENGTH_LONG).show()
            return
        }

        if (ubicacionSeleccionada == null) {
            Toast.makeText(requireContext(), "Debes seleccionar tu ubicación.", Toast.LENGTH_LONG).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnConfirmarCompra.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val idUsuario = userPreferences.obtenerIdUsuario()
                    ?: throw Exception("Usuario no autenticado")

                val totalCompra = total

                val ventaRequest = Venta(
                    usuario = UsuarioVentaDTO(idUsuario = idUsuario),
                    total = totalCompra,
                    estado = "P", // Pendiente
                    tipoVenta = "P", // Fijo en Pedido
                    metodoEntrega = "D", // Fijo en Delivery
                    especificaciones = binding.etEspecificaciones.text.toString().ifBlank { null },
                    detalles = items.map { item ->
                        DetalleVenta(
                            producto = ProductoVenta(item.producto.idProducto),
                            cantidad = item.cantidad
                        )
                    },
                    pedido = PedidoCompra(
                        direccionEntrega = direccionObtenida,
                        latitud = ubicacionSeleccionada?.latitude?.let { BigDecimal.valueOf(it) },
                        longitud = ubicacionSeleccionada?.longitude?.let { BigDecimal.valueOf(it) },
                        movilidad = tipoMovilidad
                    )
                )

                val response = compraApiService.guardarVentaDelivery(ventaRequest)

                if (response.valor) {
                    Toast.makeText(requireContext(), response.mensaje, Toast.LENGTH_LONG).show()
                    CarroRepository.limpiarCarrito()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.contenedorFragmento, InicioFragment())
                        .addToBackStack(null)
                        .commit()
                } else {
                    Toast.makeText(requireContext(), response.mensaje, Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("FinalizarCompra", "Error al procesar la compra", e)
                Toast.makeText(
                    requireContext(),
                    "Error al procesar la compra: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnConfirmarCompra.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}