package com.cibertec.proyectodami

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.cibertec.proyectodami.databinding.ActivityRepartidorMainBinding
import com.cibertec.proyectodami.databinding.ButtonOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout

class RepartidorMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRepartidorMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRepartidorMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar toolbar
        setSupportActionBar(binding.toolbar)

        // Configurar tabs
        setupTabs()

        // Cargar fragment inicial
        if (savedInstanceState == null) {
            loadFragment(DisponiblesRepartidorFragment())
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(
            binding.tabLayout.newTab()
                .setText(R.string.main_nav_disponibles)
                .setIcon(R.drawable.ic_camion_g)
        )
        binding.tabLayout.addTab(
            binding.tabLayout.newTab()
                .setText(R.string.main_nav_activos)
                .setIcon(R.drawable.ic_documento_g)
        )
        binding.tabLayout.addTab(
            binding.tabLayout.newTab()
                .setText(R.string.main_nav_estadisticas)
                .setIcon(R.drawable.ic_estadistico_g)
        )

        // Manejar clicks
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadFragment(DisponiblesRepartidorFragment())
                    /*1 -> loadFragment(ProfileFragment())
                    2 -> loadFragment(SettingsFragment())*/
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        binding.opcion.setOnClickListener {
            val bottomSheetBinding = ButtonOptionsBinding.inflate(layoutInflater)

            val dialog = BottomSheetDialog(this)
            dialog.setContentView(bottomSheetBinding.root)
            dialog.show()

            // Ahora accedes directo con bottomSheetBinding
            bottomSheetBinding.ordenarDistancia.setOnClickListener {
                Toast.makeText(this, "Opción 1 seleccionada", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            bottomSheetBinding.ordenarValor.setOnClickListener {
                Toast.makeText(this, "Opción 2 seleccionada", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            bottomSheetBinding.actualizarLista.setOnClickListener {
                Toast.makeText(this, "Opción 3 seleccionada", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }
}