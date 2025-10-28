package com.cibertec.proyectodami.presentation.features.repartidor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.databinding.ActivityRepartidorMainBinding
import com.cibertec.proyectodami.listener.OptionsMenuListener
import com.cibertec.proyectodami.presentation.features.repartidor.activo.ActivoRepartidorFragment
import com.cibertec.proyectodami.presentation.features.repartidor.disponibles.DisponiblesRepartidorFragment
import com.cibertec.proyectodami.presentation.features.repartidor.estadistica.EstadisticaRepartidorFragment
import com.cibertec.proyectodami.presentation.features.repartidor.perfil.PerfilActivity
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class RepartidorMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRepartidorMainBinding
    val mainBinding get() = binding

    private var disponiblesFragment: DisponiblesRepartidorFragment? = null
    private var activoFragment: ActivoRepartidorFragment? = null
    private var estadisticasFragment: EstadisticaRepartidorFragment? = null
    private var activeFragment: Fragment? = null

    companion object {
        private const val KEY_CURRENT_TAB = "current_tab"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepartidorMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val userPreferences = UserPreferences(applicationContext)

        lifecycleScope.launch {
            userPreferences.nombreUsuario.collect { nombre ->
                binding.userName.text = nombre ?: getString(R.string.user_name)
            }
        }

        if (savedInstanceState == null) {
            setupFragments()
            binding.tabLayout.getTabAt(0)?.select()
        } else {
            restoreFragments(savedInstanceState)
        }

        binding.userProfileContainer.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }

        setupTabs()
        setupOptionsButton()
    }

    // ✨ NUEVO: Detectar cuando vuelves de otra actividad
    override fun onResume() {
        super.onResume()

        // Verificar si debe cambiar a Disponibles
        val cambiarADisponibles = intent.getBooleanExtra("CAMBIAR_A_DISPONIBLES", false)
        if (cambiarADisponibles) {
            binding.tabLayout.getTabAt(0)?.select()
            intent.removeExtra("CAMBIAR_A_DISPONIBLES") // Limpiar el flag
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_TAB, binding.tabLayout.selectedTabPosition)
    }

    private fun restoreFragments(savedInstanceState: Bundle) {
        disponiblesFragment = supportFragmentManager.findFragmentByTag("Disponibles") as? DisponiblesRepartidorFragment
        activoFragment = supportFragmentManager.findFragmentByTag("Activos") as? ActivoRepartidorFragment
        estadisticasFragment = supportFragmentManager.findFragmentByTag("Estadisticas") as? EstadisticaRepartidorFragment

        if (disponiblesFragment == null) {
            disponiblesFragment = DisponiblesRepartidorFragment()
        }
        if (activoFragment == null) {
            activoFragment = ActivoRepartidorFragment()
        }
        if (estadisticasFragment == null) {
            estadisticasFragment = EstadisticaRepartidorFragment()
        }

        val currentTab = savedInstanceState.getInt(KEY_CURRENT_TAB, 0)
        activeFragment = when (currentTab) {
            0 -> disponiblesFragment
            1 -> activoFragment
            2 -> estadisticasFragment
            else -> disponiblesFragment
        }

        binding.tabLayout.getTabAt(currentTab)?.select()

        if (currentTab == 0) {
            binding.opcion.show()
        } else {
            binding.opcion.hide()
        }
    }

    private fun setupFragments() {
        disponiblesFragment = DisponiblesRepartidorFragment()
        activoFragment = ActivoRepartidorFragment()
        estadisticasFragment = EstadisticaRepartidorFragment()

        supportFragmentManager.beginTransaction().apply {
            add(binding.fragmentContainer.id, activoFragment!!, "Activos")
            hide(activoFragment!!)

            add(binding.fragmentContainer.id, disponiblesFragment!!, "Disponibles")
            show(disponiblesFragment!!)

            add(binding.fragmentContainer.id, estadisticasFragment!!, "Estadisticas")
            hide(estadisticasFragment!!)
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
                    targetFragment?.let { switchFragment(it) }
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun switchFragment(targetFragment: Fragment) {
        if (targetFragment == activeFragment) return

        supportFragmentManager.beginTransaction().apply {
            activeFragment?.let { hide(it) }
            show(targetFragment)
        }.commit()

        activeFragment = targetFragment
    }

    private fun setupOptionsButton() {
        binding.opcion.setOnClickListener {
            val currentFragment = activeFragment
            if (currentFragment is OptionsMenuListener && currentFragment.isAdded) {
                currentFragment.onOptionsMenuClicked()
            }
        }
    }
}