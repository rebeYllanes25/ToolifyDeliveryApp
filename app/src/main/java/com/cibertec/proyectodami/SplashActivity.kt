package com.cibertec.proyectodami

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cibertec.proyectodami.data.api.UserAuth
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.data.remote.RetrofitInstance
import com.cibertec.proyectodami.databinding.ActivitySplashBinding
import com.cibertec.proyectodami.presentation.features.cliente.ClienteMainActivity
import com.cibertec.proyectodami.presentation.features.repartidor.RepartidorMainActivity
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class SplashActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var authApi: UserAuth

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userPreferences = UserPreferences(applicationContext)
        authApi = RetrofitInstance.create(userPreferences).create(UserAuth::class.java)

        startAnimations()

        Handler(Looper.getMainLooper()).postDelayed({
            checkSession()
        }, 2000)


        FirebaseApp.initializeApp(this)
        Log.d("FirebaseTest", "Firebase inicializado correctamente")
    }

    private fun startAnimations() {
        val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        binding.logo.startAnimation(scaleAnimation)

        val fadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.appName.startAnimation(fadeAnimation)

        binding.progressBar.alpha = 0f
        binding.progressBar.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(400)
            .start()
    }

    private fun checkSession() {
        launch {
            try {
                val token = userPreferences.obtenerToken()

                if (token.isNullOrEmpty()) {
                    navigateToLogin()
                    return@launch
                }

                // Verificar que el token sea válido con el backend
                val usuario = withContext(Dispatchers.IO) {
                    authApi.getUsuarioInfo()
                }

                // Token válido, navegar según el rol
                when (usuario.rol.idRol) {
                    2 -> navigateToCliente()
                    4 -> navigateToRepartidor()
                    else -> navigateToLogin()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // Token inválido o expirado, limpiar y ir al login
                userPreferences.limpiarDatos()
                navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun navigateToCliente() {
        startActivity(Intent(this, ClienteMainActivity::class.java))
        finish()
    }

    private fun navigateToRepartidor() {
        startActivity(Intent(this, RepartidorMainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}