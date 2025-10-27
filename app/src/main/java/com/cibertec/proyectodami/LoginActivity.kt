package com.cibertec.proyectodami

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.cibertec.proyectodami.data.api.UserAuth
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.databinding.ActivityLoginBinding
import com.cibertec.proyectodami.data.remote.RetrofitInstance
import com.cibertec.proyectodami.domain.util.FcmTokenHelper
import com.cibertec.proyectodami.presentation.features.cliente.ClienteMainActivity
import com.cibertec.proyectodami.presentation.features.repartidor.RepartidorMainActivity
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class LoginActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authApi: UserAuth
    private lateinit var userPreferences: UserPreferences

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(applicationContext)
        authApi = RetrofitInstance.create(userPreferences).create(UserAuth::class.java)

        val etCorreo = binding.etCorreo
        val etPassword = binding.etContrasenia
        val btnLogin = binding.btnLogin
        val linkRegistro = binding.linkRegistro

        fun checkFields() {
            val emailFilled = !etCorreo.text.isNullOrBlank()
            val passFilled = !etPassword.text.isNullOrBlank()

            if (emailFilled && passFilled) {
                btnLogin.isEnabled = true
                btnLogin.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.primary_repartidor)
                btnLogin.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                btnLogin.isEnabled = false
                btnLogin.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.gris)
                btnLogin.setTextColor(ContextCompat.getColor(this, R.color.gris_oscuro_leve))
            }
        }
        etCorreo.addTextChangedListener { checkFields() }
        etPassword.addTextChangedListener { checkFields() }

        btnLogin.setOnClickListener {
            val correo = etCorreo.text.toString()
            val clave = etPassword.text.toString()
            login(correo, clave)
        }

        linkRegistro.setOnClickListener {
            iraRegistro()
        }
    }

    private fun login(correo: String, clave: String) {
        launch {
            try {
                val response = withContext(Dispatchers.IO) { authApi.login(correo, clave) }
                val token = response.token

                if (token.isNullOrEmpty()) {
                    Toast.makeText(this@LoginActivity, "Token no recibido", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Guardar token JWT
                withContext(Dispatchers.IO) {
                    userPreferences.guardarToken(token)
                }

                // Recrear API con el nuevo token
                authApi = RetrofitInstance.create(userPreferences).create(UserAuth::class.java)

                // Obtener información del usuario
                val usuario = withContext(Dispatchers.IO) { authApi.getUsuarioInfo() }

                // Guardar datos del usuario
                withContext(Dispatchers.IO) {
                    userPreferences.guardarIdUsuario(usuario.idUsuario)
                    userPreferences.guardarNombreUsuario(usuario.nombres)
                    userPreferences.guardarRol(usuario.rol.idRol)
                }

                // Registrar token FCM en el backend
                FcmTokenHelper.obtenerYEnviarToken(
                    context = this@LoginActivity,
                    userPreferences = userPreferences
                )

                // Redirigir según rol
                when (usuario.rol.idRol) {
                    2 -> startActivity(Intent(this@LoginActivity, ClienteMainActivity::class.java))
                    4 -> startActivity(Intent(this@LoginActivity, RepartidorMainActivity::class.java))
                    else -> Toast.makeText(this@LoginActivity, "Rol no reconocido", Toast.LENGTH_SHORT).show()
                }

                finish()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@LoginActivity, "Error al iniciar sesión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun iraRegistro() {
        val intent = Intent(this, RegistroActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
