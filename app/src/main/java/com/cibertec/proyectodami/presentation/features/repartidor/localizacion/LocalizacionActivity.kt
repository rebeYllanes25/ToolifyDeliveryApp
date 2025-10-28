package com.cibertec.proyectodami.presentation.features.repartidor.localizacion


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.databinding.ActivityLocalizacionBinding
import com.cibertec.proyectodami.domain.model.dtos.PedidoRepartidorDTO
import com.cibertec.proyectodami.domain.repository.PedidoRepartidorRepository
import com.cibertec.proyectodami.presentation.features.repartidor.escaner.EscanerActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class LocalizacionActivity: AppCompatActivity(), OnMapReadyCallback {
   private lateinit var binding: ActivityLocalizacionBinding
    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    //Datos del pedido
    private var pedidoId: Int = 0
    private var latitud: Double = 0.0
    private var longitud: Double = 0.0
    private var nombre: String = ""
    private var direccion: String = ""
    private var total: Double = 0.0
    private var especificaciones: String = ""
    private var estadoPedido: String = ""
    private var repartidorId: Int = 0

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    }


    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                // Permiso concedido
                enableMyLocation()
                mostrarRutaEnMapa()
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                // Solo permiso aproximado
                enableMyLocation()
                mostrarRutaEnMapa()
            }
            else -> {
                // Permiso denegado
                mostrarDialogoPermisosDenegados()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityLocalizacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Inicializar ubicacion
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        obtenerDatosPedido()

        //Inicializar mapa
        var mapViewBundle: Bundle? = null
        if(savedInstanceState!= null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }

        mapView = binding.mapView
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)


        binding.btnCerca.setOnClickListener {
            marcarPedidoCerca()
        }


        setupUI()
        setupBottomSheet()
    }

    private fun setupUI() {
        //header
        binding.tvNombreCliente.text = nombre

        //boton
        binding.tvClienteNombre.text = nombre
        binding.tvClienteDireccion.text = direccion
        binding.tvPrecio.text = getString(R.string.value_price, total)

        binding.tvInstrucciones.text = especificaciones

        //boton regresar
        binding.fabBack.setOnClickListener {
            finish()
        }

        // Botón GPS (centrar en ubicación actual)
        binding.fabGPS.setOnClickListener {
            centrarEnUbicacionActual()
        }

        // Botón En Ruta (abrir Google Maps)
        binding.btnEnRuta.setOnClickListener {
            abrirGoogleMapsNavegacion()
        }

        // Botón Llegada
        binding.btnLlegada.setOnClickListener {
            marcarLlegada()
        }

    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.peekHeight = 300
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Configurar mapa
        googleMap?.apply {
            uiSettings.isZoomControlsEnabled = false
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isCompassEnabled = true
        }

        // Verificar permisos y mostrar ubicación
        if (checkLocationPermission()) {
            enableMyLocation()
            mostrarRutaEnMapa()
        } else {
            requestLocationPermission()
        }
    }

    private fun checkGPSEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as android.location.LocationManager
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
    }


    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            /*
            if (!checkGPSEnabled()) {
                Toast.makeText(this, "Por favor activa el GPS", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                return
            }
            */


            googleMap?.isMyLocationEnabled = true
            obtenerUbicacionActual()
        } else {
            requestLocationPermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacionActual() {
        Log.d("LocalizacionActivity", "🔍 Intentando obtener ubicación actual...")

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            if (location != null) {
                Log.d("LocalizacionActivity", "✅ Ubicación obtenida: Lat=${location.latitude}, Lng=${location.longitude}")

                val miUbicacion = LatLng(location.latitude, location.longitude)

                // Centrar entre mi ubicación y la del cliente
                val builder = LatLngBounds.Builder()
                builder.include(miUbicacion)
                builder.include(LatLng(latitud, longitud))
                val bounds = builder.build()

                googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))

                // IMPORTANTE: Llamar a calcular distancia con la ubicación obtenida
                calcularDistancia(location)
            } else {
                Log.e("LocalizacionActivity", "❌ Location es null")
                Toast.makeText(this, "No se pudo obtener tu ubicación actual", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Log.e("LocalizacionActivity", "❌ Error al obtener ubicación: ${e.message}")
            Toast.makeText(this, "Error obteniendo ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarRutaEnMapa() {
        Log.d("LocalizacionActivity", "🗺️ Mostrando ruta en mapa...")
        Log.d("LocalizacionActivity", "📍 Destino: Lat=$latitud, Lng=$longitud")

        // Marker del cliente
        val destinoLatLng = LatLng(latitud, longitud)
        googleMap?.addMarker(
            MarkerOptions()
                .position(destinoLatLng)
                .title(nombre)
                .snippet(direccion)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        // Obtener ubicación actual y dibujar ruta
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, null
            ).addOnSuccessListener { location ->
                if(location != null){
                    Log.d("LocalizacionActivity", "✅ Ubicación para ruta obtenida: Lat=${location.latitude}, Lng=${location.longitude}")

                    val origenLatLng = LatLng(location.latitude, location.longitude)

                    val directionsHelper = DirectionHelper(this)
                    directionsHelper.obtenerRuta(
                        origenLatLng,
                        destinoLatLng,
                        onSuccess = { puntos ->
                            Log.d("LocalizacionActivity", "✅ Ruta obtenida con ${puntos.size} puntos")

                            // Dibujar la ruta real
                            val polylineOptions = PolylineOptions()
                                .addAll(puntos)
                                .width(10f)
                                .color(Color.parseColor("#FF7043"))
                                .geodesic(true)

                            googleMap?.addPolyline(polylineOptions)
                        },
                        onError = { error ->
                            Log.e("LocalizacionActivity", "❌ Error obteniendo ruta: $error")
                            Toast.makeText(this, "Error al obtener la ruta", Toast.LENGTH_SHORT).show()

                            // Fallback: dibujar línea recta
                            val polylineOptions = PolylineOptions()
                                .add(origenLatLng)
                                .add(destinoLatLng)
                                .width(10f)
                                .color(Color.parseColor("#FF7043"))
                                .geodesic(true)

                            googleMap?.addPolyline(polylineOptions)
                        }
                    )

                    // IMPORTANTE: Calcular distancia también aquí
                    calcularDistancia(location)
                }
                else {
                    Log.e("LocalizacionActivity", "❌ Location es null en mostrarRutaEnMapa")
                    Toast.makeText(this, "No se puede obtener tu ubicación", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Log.e("LocalizacionActivity", "❌ Error al obtener ubicación: ${e.message}")
                Toast.makeText(this, "Error al obtener ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w("LocalizacionActivity", "⚠️ Sin permisos de ubicación")
        }
    }

    private fun calcularDistancia(miUbicacion: Location) {
        Log.d("LocalizacionActivity", "📏 Calculando distancia...")
        Log.d("LocalizacionActivity", "📍 Mi ubicación: Lat=${miUbicacion.latitude}, Lng=${miUbicacion.longitude}")
        Log.d("LocalizacionActivity", "📍 Destino: Lat=$latitud, Lng=$longitud")

        val destino = Location("").apply {
            latitude = latitud
            longitude = longitud
        }

        val distanciaMetros = miUbicacion.distanceTo(destino)
        val distanciaKm = distanciaMetros / 1000

        Log.d("LocalizacionActivity", "📏 Distancia calculada: ${distanciaMetros}m = ${distanciaKm}km")

        binding.distancia.text = String.format("Llegada (%.2f km)", distanciaKm)

        // CORRECCIÓN: velocidad promedio de 20 km/h (no 60)
        val tiempoEstimadoHoras = distanciaKm / 20  // Cambiado de 60 a 20
        val tiempoEstimadoMinutos = (tiempoEstimadoHoras * 60).roundToInt()

        Log.d("LocalizacionActivity", "⏱️ Tiempo estimado: ${tiempoEstimadoMinutos} minutos")

        binding.tvTiempo.text = String.format("%d min", tiempoEstimadoMinutos)
    }

    private fun obtenerDatosPedido(){
        pedidoId = intent.getIntExtra("PEDIDO_ID", 0)
        repartidorId = intent.getIntExtra("REAPRTIDOR",0)
        direccion = intent.getStringExtra("DIRECCION")?: ""
        nombre = intent.getStringExtra("NOMBRE")?:""
        total = intent.getDoubleExtra("TOTAL", 0.0)
        latitud = intent.getDoubleExtra("LATITUD", -12.0464)
        longitud = intent.getDoubleExtra("LONGITUD", -77.0428)
        especificaciones = intent.getStringExtra("ESPECIFICACIONES")?:""
        estadoPedido = intent.getStringExtra("ESTADO")?:""

        Log.d("LocalizacionActivity", "📦 Datos del pedido recibidos:")
        Log.d("LocalizacionActivity", "   - Pedido ID: $pedidoId")
        Log.d("LocalizacionActivity", "   - Repartidor ID: $repartidorId")
        Log.d("LocalizacionActivity", "   - Cliente: $nombre")
        Log.d("LocalizacionActivity", "   - Dirección: $direccion")
        Log.d("LocalizacionActivity", "   - Destino: Lat=$latitud, Lng=$longitud")
        Log.d("LocalizacionActivity", "   - Total: S/$total")
        Log.d("LocalizacionActivity", "   - Estado: $estadoPedido")
    }

    private fun centrarEnUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                if(location !=null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    googleMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                    )
                }else{
                    Toast.makeText(this, "No se puede obtener tu ubicacion actual", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun abrirGoogleMapsNavegacion() {
        val uri = "google.navigation:q=$latitud,$longitud"
        val intent = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse(uri)
        )
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }


    private fun marcarPedidoCerca() {
        binding.btnCerca.isEnabled = false

        lifecycleScope.launch {
            try {

                PedidoRepartidorRepository.marcarPedidoCerca(pedidoId)

                Toast.makeText(
                    this@LocalizacionActivity,
                    "Pedido marcado como cerca. Cliente notificado.",
                    Toast.LENGTH_LONG
                ).show()



            } catch (e: Exception) {
                Toast.makeText(
                    this@LocalizacionActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

                binding.btnCerca.isEnabled = true
            }
        }
    }


    private fun marcarLlegada() {
        val intent = Intent(this, EscanerActivity::class.java)
        intent.putExtra("idPedido", pedidoId)
        intent.putExtra("idRepartidor", repartidorId)

        startActivity(intent)

        finish()

        Toast.makeText(
            this,
            "Confirmar llegada",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun mostrarDialogoPermisosDenegados() {
        AlertDialog.Builder(this)
            .setTitle("Permiso denegado")
            .setMessage("Sin acceso a la ubicación, no podemos mostrar tu posición en el mapa. ¿Deseas ir a configuración para habilitarlo?")
            .setPositiveButton("Ir a configuración") { _, _ ->
                abrirConfiguracionApp()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun abrirConfiguracionApp() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
                mostrarRutaEnMapa()
            }
        }
    }

    // Lifecycle del MapView
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }
        mapView.onSaveInstanceState(mapViewBundle)
    }

}

