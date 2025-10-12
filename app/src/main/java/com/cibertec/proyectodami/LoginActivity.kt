package com.cibertec.proyectodami

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cibertec.proyectodami.databinding.ActivityLoginBinding
import androidx.core.widget.addTextChangedListener

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val etCorreo = binding.etCorreo
        val etPassword = binding.etContrasenia
        val btnLogin = binding.btnLogin

        fun checkFields() {
            val emailFilled = !etCorreo.text.isNullOrBlank()
            val passFilled = !etPassword.text.isNullOrBlank()

            if (emailFilled && passFilled) {
                btnLogin.isEnabled = true
                btnLogin.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_repartidor)
                btnLogin.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                btnLogin.isEnabled = false
                btnLogin.backgroundTintList = ContextCompat.getColorStateList(this, R.color.gris)
                btnLogin.setTextColor(ContextCompat.getColor(this, R.color.gris_oscuro_leve))
            }
        }

        etCorreo.addTextChangedListener { checkFields() }
        etPassword.addTextChangedListener { checkFields() }

    }
}