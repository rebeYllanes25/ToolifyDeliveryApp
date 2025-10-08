package com.cibertec.proyectodami

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.proyectodami.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnNuevoPedido.setOnClickListener{

            val intent = Intent(this, SeguimientoActivity::class.java)
            startActivity(intent)
        }
    }
}