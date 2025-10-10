package com.cibertec.proyectodami

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.cibertec.proyectodami.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.ContainerFragments) as androidx.navigation.fragment.NavHostFragment

        val navController = navHostFragment.navController

        binding.btnInicio.setOnClickListener {
            navController.navigate(R.id.mainFragment)
        }

        binding.btnNotificaciones.setOnClickListener {
            navController.navigate(R.id.twoFragment)
        }

        binding.btnHistorial.setOnClickListener {
            navController.navigate(R.id.oneFragment5)
        }
    }
}