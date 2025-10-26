package com.cibertec.proyectodami

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.cibertec.proyectodami.data.api.UserAuth
import com.cibertec.proyectodami.data.dataStore.AuthRepository
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.data.remote.RetrofitInstance
import com.cibertec.proyectodami.databinding.ActivityRegistroBinding
import com.cibertec.proyectodami.domain.model.dtos.requests.Distrito
import com.cibertec.proyectodami.domain.model.dtos.requests.UsuarioRequest
import com.cibertec.proyectodami.domain.model.dtos.responses.RegisterResponse
import com.cibertec.proyectodami.domain.repository.PedidoClienteRepository
import kotlinx.coroutines.launch

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding
    private lateinit var authRepository: AuthRepository

    private val distritos = mapOf(
        "Ancón" to 1,
        "Ate" to 2,
        "Barranco" to 3,
        "Breña" to 4,
        "Carabayllo" to 5,
        "Chorrillos" to 6,
        "Comas" to 7,
        "La Molina" to 8,
        "Miraflores" to 9,
        "San Isidro" to 10
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inyecciond el repositorio
        val userPreferences = UserPreferences(applicationContext)
        val retrofit = RetrofitInstance.create(userPreferences)
        val apiService = retrofit.create(UserAuth::class.java)
        authRepository = AuthRepository(apiService)

        setupDistritoSpinner()
        setupListeners()
    }

    private fun setupDistritoSpinner() {
        val distritosNombres = distritos.keys.toList()
        val adapter = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, distritosNombres)
        binding.spinnerDistrito.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.btnRegistrarse.setOnClickListener {
            if (validarCampos()) {
                registrarUsuario()
            }
        }

        binding.tvIniciarSesion.setOnClickListener {
            irALogin()
        }
    }

    private fun validarCampos(): Boolean {

        val nombres = binding.etNombres.text.toString().trim()
        if (nombres.isEmpty()) {
            binding.etNombres.error = "El nombre es requerido"
            binding.etNombres.requestFocus()
            return false
        }

        val apePaterno = binding.etApePaterno.text.toString().trim()
        if (apePaterno.isEmpty()) {
            binding.etApePaterno.error = "El apellido paterno es requerido"
            binding.etApePaterno.requestFocus()
            return false
        }

        val apeMaterno = binding.etApeMaterno.text.toString().trim()
        if (apeMaterno.isEmpty()) {
            binding.etApeMaterno.error = "El apellido materno es requerido"
            binding.etApeMaterno.requestFocus()
            return false
        }

        val correo = binding.etCorreo.text.toString().trim()
        if (correo.isEmpty()) {
            binding.etCorreo.error = "El correo es requerido"
            binding.etCorreo.requestFocus()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.etCorreo.error = "Ingrese un correo válido"
            binding.etCorreo.requestFocus()
            return false
        }

        val clave = binding.etClave.text.toString().trim()
        if (clave.isEmpty()) {
            binding.etClave.error = "La contraseña es requerida"
            binding.etClave.requestFocus()
            return false
        }
        if (clave.length < 6) {
            binding.etClave.error = "La contraseña debe tener al menos 6 caracteres"
            binding.etClave.requestFocus()
            return false
        }

        val nroDocumento = binding.etNroDocumento.text.toString().trim()
        if (nroDocumento.isEmpty()) {
            binding.etNroDocumento.error = "El número de documento es requerido"
            binding.etNroDocumento.requestFocus()
            return false
        }
        if (nroDocumento.length != 8) {
            binding.etNroDocumento.error = "El documento debe tener 8 dígitos"
            binding.etNroDocumento.requestFocus()
            return false
        }

        val telefono = binding.etTelefono.text.toString().trim()
        if (telefono.isEmpty()) {
            binding.etTelefono.error = "El teléfono es requerido"
            binding.etTelefono.requestFocus()
            return false
        }
        if (telefono.length != 9) {
            binding.etTelefono.error = "El teléfono debe tener 9 dígitos"
            binding.etTelefono.requestFocus()
            return false
        }

        val direccion = binding.etDireccion.text.toString().trim()
        if (direccion.isEmpty()) {
            binding.etDireccion.error = "La dirección es requerida"
            binding.etDireccion.requestFocus()
            return false
        }

        val distritoSeleccionado = binding.spinnerDistrito.text.toString().trim()
        if (distritoSeleccionado.isEmpty()) {
            Toast.makeText(this, "Por favor seleccione un distrito", Toast.LENGTH_SHORT).show()
            binding.spinnerDistrito.requestFocus()
            return false
        }
        if (!distritos.containsKey(distritoSeleccionado)) {
            Toast.makeText(this, "Por favor seleccione un distrito válido", Toast.LENGTH_SHORT).show()
            binding.spinnerDistrito.requestFocus()
            return false
        }

        return true
    }

    private fun registrarUsuario() {

        val nombres = binding.etNombres.text.toString().trim()
        val apePaterno = binding.etApePaterno.text.toString().trim()
        val apeMaterno = binding.etApeMaterno.text.toString().trim()
        val correo = binding.etCorreo.text.toString().trim()
        val clave = binding.etClave.text.toString().trim()
        val nroDocumento = binding.etNroDocumento.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()
        val direccion = binding.etDireccion.text.toString().trim()
        val distritoNombre = binding.spinnerDistrito.text.toString().trim()
        val idDistrito = distritos[distritoNombre] ?: 0

        val usuarioRequest = UsuarioRequest(
            nombres = nombres,
            apePaterno = apePaterno,
            apeMaterno = apeMaterno,
            correo = correo,
            clave = clave,
            nroDocumento = nroDocumento,
            direccion = direccion,
            distrito = Distrito(idDistrito),
            telefono = telefono
        )

        guardarNuevoUsuario(usuarioRequest)
    }

    private fun guardarNuevoUsuario(usuarioRequest: UsuarioRequest) {
        lifecycleScope.launch {

            val resultado = authRepository.registrarUsuario(usuarioRequest)
            resultado.fold(
                onSuccess = { registerResponse ->
                    mostrarDialogRespuesta(registerResponse)
                },
                onFailure = { excepcion ->
                    Toast.makeText(
                        this@RegistroActivity,
                        excepcion.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }

    private fun mostrarDialogRespuesta(response: RegisterResponse) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(if (response.valor) "Registro Exitoso" else "Error en Registro")
        builder.setMessage(response.mensaje)
        builder.setCancelable(false)

        if (response.valor) {
            builder.setPositiveButton("Iniciar Sesión") { dialog, _ ->
                dialog.dismiss()
                irALogin()
            }
        } else {
            builder.setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
            }
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun irALogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

}