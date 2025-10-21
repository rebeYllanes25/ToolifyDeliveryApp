package com.cibertec.proyectodami.presentation.features.repartidor.localizacion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.proyectodami.databinding.ActivityLocalizacionBinding
import com.cibertec.proyectodami.databinding.ActivityPerfilBinding

class LocalizacionActivity: AppCompatActivity() {
    private lateinit var binding : ActivityLocalizacionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocalizacionBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}