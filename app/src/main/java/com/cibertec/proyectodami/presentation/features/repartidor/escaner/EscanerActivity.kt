package com.cibertec.proyectodami.presentation.features.repartidor.escaner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size

import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider

import androidx.core.content.ContextCompat

import com.cibertec.proyectodami.databinding.ActivityEscanerBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class EscanerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEscanerBinding

    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null



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

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    this, cameraSelector, preview
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Error al iniciar cámara: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun animarLineaEscaneo() {
        binding.scanLine.animate()
            .translationY(-125f)
            .setDuration(0)
            .withEndAction {
                binding.scanLine.animate()
                    .translationY(125f)
                    .setDuration(2000)
                    .withEndAction { animarLineaEscaneo() }
                    .start()
            }
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
