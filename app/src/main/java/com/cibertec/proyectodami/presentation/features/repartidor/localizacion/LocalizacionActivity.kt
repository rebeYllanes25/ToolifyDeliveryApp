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
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.databinding.ActivityLocalizacionBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior

class LocalizacionActivity: AppCompatActivity(), OnMapReadyCallback {
   private lateinit var binding: ActivityLocalizacionBinding
    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    //Datos del pedido
    private var latitud: Double = 0.0
    private var longitud: Double = 0.0
    private var nombre: String = ""
    private var direccion: String = ""
    private var total: Double = 0.0

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





        setupUI()
        setupBottomSheet()
    }

    private fun obtenerDatosPedido(){

        direccion = intent.getStringExtra("DIRECCION")?: ""
        nombre = intent.getStringExtra("NOMBRE")?:""
        total = intent.getDoubleExtra("TOTAL", 0.0)
        latitud = intent.getDoubleExtra("LATITUD", -12.0464) // Lima por defecto
        longitud = intent.getDoubleExtra("LONGITUD", -77.0428)


    }
    private fun setupUI() {
        //header
        binding.tvNombreCliente.text = nombre

        //boton
        binding.tvClienteNombre.text = nombre
        binding.tvClienteDireccion.text = direccion
        binding.tvPrecio.text = getString(R.string.value_price, total)

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
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            if (location != null) {
                val miUbicacion = LatLng(location.latitude, location.longitude)

                // Centrar entre mi ubicación y la del cliente
                val builder = LatLngBounds.Builder()
                builder.include(miUbicacion)
                builder.include(LatLng(latitud, longitud))
                val bounds = builder.build()

                googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))

                calcularDistancia(location)
            } else {
                Toast.makeText(this, "No se pudo obtener tu ubicación actual", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarRutaEnMapa() {
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
                    val origenLatLng = LatLng(location.latitude, location.longitude)



                    val directionsHelper = DirectionHelper(this)
                    directionsHelper.obtenerRuta( origenLatLng,
                        destinoLatLng,
                        onSuccess = { puntos ->
                            // Dibujar la ruta real
                            val polylineOptions = PolylineOptions()
                                .addAll(puntos)
                                .width(10f)
                                .color(Color.parseColor("#FF7043"))
                                .geodesic(true)

                            googleMap?.addPolyline(polylineOptions)
                        },
                        onError = { error ->
                            Log.e("LocalizacionActivity", "Error obteniendo ruta: $error")
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

                }
                else {
                Toast.makeText(this, "No se puede obtener tu ubicación",
                    Toast.LENGTH_SHORT).show()
                 }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error al obtener unicación: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calcularDistancia(miUbicacion: Location) {
        val destino = Location("").apply {
            latitude = latitud
            longitude = longitud
        }

        val distanciaMetros = miUbicacion.distanceTo(destino)
        val distanciaKm = distanciaMetros / 1000

        // Actualizar UI
        binding.btnLlegada.text = String.format("Llegada (%.2f km)", distanciaKm)

        // Calcular tiempo estimado (asumiendo velocidad promedio de 30 km/h)
        val tiempoMinutos = (distanciaKm / 30.0) * 60

        // Aquí podrías actualizar los cards de distancia y tiempo
        // binding.tvDistancia.text = String.format("%.2f km", distanciaKm)
        // binding.tvTiempo.text = String.format("%.0f min", tiempoMinutos)
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

    private fun marcarLlegada() {
        // Aquí implementarías la lógica para marcar la llegada
        // Por ejemplo, actualizar el estado del pedido en el servidor
        android.widget.Toast.makeText(
            this,
            "Marcando llegada al destino...",
            android.widget.Toast.LENGTH_SHORT
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

