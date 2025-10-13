package com.cibertec.proyectodami.presentation.features.cliente.rastreo

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.proyectodami.R

class RastreoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rastreo)

        val pedidoId = intent.getStringExtra("PEDIDO_ID")

        val btnBack: ImageView = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // Aquí implementarías la lógica de rastreo
        // Integración con Google Maps, tracking en tiempo real, etc.
    }
}