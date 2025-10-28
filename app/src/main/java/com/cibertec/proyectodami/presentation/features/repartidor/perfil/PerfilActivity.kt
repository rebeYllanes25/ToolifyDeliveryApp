package com.cibertec.proyectodami.presentation.features.repartidor.perfil

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.cibertec.proyectodami.LoginActivity
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.data.api.UserAuth
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.data.remote.RetrofitInstance
import com.cibertec.proyectodami.databinding.ActivityPerfilBinding
import com.cibertec.proyectodami.domain.repository.PedidoRepartidorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var authApi: UserAuth

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                saveImageLocally(it)
                loadSavedProfileImage()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(applicationContext)
        authApi = RetrofitInstance.create(userPreferences).create(UserAuth::class.java)

        setupToolbar()
        cargarDatosUsuario()
        loadSavedProfileImage()

        binding.imgPerfil.setOnClickListener {
            openImagePicker()
        }

        // En tu PerfilActivity
        binding.btnCerrarSesion.setOnClickListener {
            lifecycleScope.launch {
                try {
                    // Verificar si hay pedido activo
                    val idPedidoActivo = userPreferences.obtenerIdPedidoActivo()

                    if (idPedidoActivo != null) {
                        PedidoRepartidorRepository.completarPedido()
                        userPreferences.limpiarPedidoActivo()
                    }

                    userPreferences.limpiarDatos()

                    val intent = Intent(this@PerfilActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()

                } catch (e: Exception) {
                    Toast.makeText(
                        this@PerfilActivity,
                        "Error al cerrar sesión",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setupToolbar() {
        ContextCompat.getDrawable(this, R.drawable.ic_arrow_back)?.let { icon ->
            icon.setTint(ContextCompat.getColor(this, R.color.white))
            binding.toolbar.navigationIcon = icon
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun cargarDatosUsuario() {
        lifecycleScope.launch {
            try {
                val usuario = withContext(Dispatchers.IO) {
                    authApi.getUsuarioInfo()
                }

                binding.tvNombreUsuario.text =
                    "${usuario.nombres} ${usuario.apePaterno} ${usuario.apeMaterno}"
                binding.tvEmail.text = usuario.correo
                binding.tvTipoUsuario.text = usuario.descripcionRol
                binding.tvDireccion.text = usuario.direccion
                binding.tvTelefono.text = usuario.telefono

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@PerfilActivity,
                    "Error al cargar datos del usuario",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun saveImageLocally(imageUri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Obtener la orientación del EXIF desde el Uri
            val correctedBitmap = correctImageOrientation(imageUri, bitmap)

            // Guardar el bitmap corregido
            val file = File(filesDir, "perfil.jpg")
            val outputStream = FileOutputStream(file)
            correctedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun correctImageOrientation(imageUri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = contentResolver.openInputStream(imageUri)
            val exif = inputStream?.let { ExifInterface(it) }
            inputStream?.close()

            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL

            rotateBitmap(bitmap, orientation)
        } catch (e: Exception) {
            e.printStackTrace()
            bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }

        return try {
            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0,
                bitmap.width, bitmap.height,
                matrix, true
            )
            bitmap.recycle()
            rotatedBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            bitmap
        }
    }

    private fun loadSavedProfileImage() {
        val file = File(filesDir, "perfil.jpg")
        if (file.exists()) {
            try {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                binding.imgPerfil.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }
}