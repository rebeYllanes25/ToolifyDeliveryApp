package com.cibertec.proyectodami.presentation.features.repartidor.escaner

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Size

import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider

import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope

import com.cibertec.proyectodami.databinding.ActivityEscanerBinding
import com.cibertec.proyectodami.domain.repository.PedidoRepartidorRepository
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class EscanerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEscanerBinding
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private var qrCodeScanned = false

    // Datos recibidos desde otra Activity
    private var idPedido: Int = 0
    private var idRepartidor: Int = 0

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                Toast.makeText(this, "Se requiere permiso de cámara", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEscanerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Recuperar datos desde la Activity anterior
        idPedido = intent.getIntExtra("idPedido", 0)
        idRepartidor = intent.getIntExtra("idRepartidor", 0)

        if (tienePermisoCamara()) {
            startCamera()
        } else {
            solicitarPermisoCamara()
        }

        animarLineaEscaneo()
    }

    private fun tienePermisoCamara(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun solicitarPermisoCamara() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .build()
                preview.setSurfaceProvider(binding.previewView.surfaceProvider)

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalyzer.setAnalyzer(cameraExecutor, QrCodeAnalyzer { qrCode ->
                    if (!qrCodeScanned) {
                        qrCodeScanned = true
                        onQrCodeDetected(qrCode)
                    }
                })

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Error al iniciar cámara: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun onQrCodeDetected(qrCode: String) {
        runOnUiThread {
            binding.scanLine.animate().cancel()

            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator.vibrate(200)
                }
            }

            Toast.makeText(this, "QR Detectado: $qrCode", Toast.LENGTH_SHORT).show()

            // Llamar a la API para confirmar entrega
            entregarPedido(qrCode)
        }
    }

    private fun entregarPedido(codigoQR: String) {
        // Obtener idRepartidor desde SharedPreferences (guardado al iniciar sesión)
        val prefs = getSharedPreferences("appPrefs", Context.MODE_PRIVATE)
        val idRepartidor = prefs.getInt("idRepartidor", 0) // 0 si no existe, deberías manejarlo

        if (idRepartidor == 0) {
            Toast.makeText(this, "Error: no se encontró ID de repartidor", Toast.LENGTH_LONG).show()
            qrCodeScanned = false
            animarLineaEscaneo()
            return
        }

        lifecycleScope.launch {
            try {
                PedidoRepartidorRepository.entregarPedido(
                    idPedido = idPedido,
                    codigoQR = codigoQR,
                    idRepartidor = idRepartidor
                )

                Toast.makeText(
                    this@EscanerActivity,
                    "Pedido entregado correctamente",
                    Toast.LENGTH_LONG
                ).show()

                val resultIntent = Intent().apply {
                    putExtra("QR_CODE", codigoQR)
                    putExtra("SCAN_SUCCESS", true)
                }
                setResult(RESULT_OK, resultIntent)

                Handler(Looper.getMainLooper()).postDelayed({
                    finish()
                }, 1500)

            } catch (e: Exception) {
                Toast.makeText(
                    this@EscanerActivity,
                    "Error al entregar pedido: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

                qrCodeScanned = false
                animarLineaEscaneo()
            }
        }
    }


    private fun animarLineaEscaneo() {
        binding.scanLine.animate()
            .translationY(-125f)
            .setDuration(0)
            .withEndAction {
                binding.scanLine.animate()
                    .translationY(125f)
                    .setDuration(2000)
                    .withEndAction {
                        if (!qrCodeScanned) {
                            animarLineaEscaneo()
                        }
                    }
                    .start()
            }
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private class QrCodeAnalyzer(private val onQrCodeDetected: (String) -> Unit) :
        ImageAnalysis.Analyzer {

        private val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )

        @androidx.camera.core.ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )

                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            barcode.rawValue?.let { qrCode ->
                                onQrCodeDetected(qrCode)
                            }
                        }
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }
}
