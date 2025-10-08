package com.cibertec.proyectodami

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.proyectodami.databinding.ActivitySeguimientoBinding

class SeguimientoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeguimientoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivitySeguimientoBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}