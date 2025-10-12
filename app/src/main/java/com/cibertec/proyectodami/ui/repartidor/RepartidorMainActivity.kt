package com.cibertec.proyectodami.ui.repartidor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.databinding.ActivityRepartidorMainBinding
import com.cibertec.proyectodami.listener.OptionsMenuListener
import com.cibertec.proyectodami.ui.repartidor.activo.ActivoRepartidorFragment
import com.cibertec.proyectodami.ui.repartidor.disponibles.DisponiblesRepartidorFragment
import com.cibertec.proyectodami.ui.repartidor.estadistica.EstadisticaRepartidorFragment
import com.google.android.material.tabs.TabLayout

class RepartidorMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRepartidorMainBinding
    val mainBinding get() = binding
    private val disponiblesFragment = DisponiblesRepartidorFragment()
    private val activoFragment = ActivoRepartidorFragment()
    private val estadisticasFragment = EstadisticaRepartidorFragment()
    private var activeFragment: Fragment = disponiblesFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepartidorMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        if (savedInstanceState == null) {
            setupFragments()
        }

        setupTabs()
        setupOptionsButton()
    }

    private fun setupFragments() {
        supportFragmentManager.beginTransaction().apply {
            add(binding.fragmentContainer.id, activoFragment, "Activos")
            hide(activoFragment)

            add(binding.fragmentContainer.id, disponiblesFragment, "Disponibles")
            show(disponiblesFragment)

            add(binding.fragmentContainer.id, estadisticasFragment, "Estadisticas")
            hide(estadisticasFragment)
        }.commit()
        activeFragment = disponiblesFragment
    }

    private fun setupTabs() {
        binding.tabLayout.apply {
            addTab(newTab()
                .setText(R.string.main_nav_disponibles)
                .setIcon(R.drawable.ic_camion_g))

            addTab(newTab()
                .setText(R.string.main_nav_activos)
                .setIcon(R.drawable.ic_documento_g))

            addTab(newTab()
                .setText(R.string.main_nav_estadisticas)
                .setIcon(R.drawable.ic_estadistico_g))

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val targetFragment = when (tab?.position) {
                        0 -> {
                            binding.opcion.show()
                            disponiblesFragment
                        }
                        1 -> {
                            binding.opcion.hide()
                            activoFragment
                        }
                        2 -> {
                            binding.opcion.hide()
                            estadisticasFragment
                        }
                        else -> {
                            binding.opcion.hide()
                            disponiblesFragment
                        }
                    }
                    switchFragment(targetFragment)
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun switchFragment(targetFragment: Fragment) {
        if (targetFragment == activeFragment) return
        supportFragmentManager.beginTransaction().apply {
            hide(activeFragment)
            show(targetFragment)
        }.commit()

        activeFragment = targetFragment
    }

    private fun setupOptionsButton() {
        binding.opcion.setOnClickListener {
            if (activeFragment is OptionsMenuListener) {
                (activeFragment as OptionsMenuListener).onOptionsMenuClicked()
            }
        }
    }
}