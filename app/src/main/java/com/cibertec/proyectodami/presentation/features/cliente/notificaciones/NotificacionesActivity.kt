package com.cibertec.proyectodami.presentation.features.cliente.notificaciones

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.proyectodami.R
import com.cibertec.proyectodami.data.api.Notificaciones
import com.cibertec.proyectodami.data.dataStore.UserPreferences
import com.cibertec.proyectodami.data.remote.RetrofitInstance
import com.cibertec.proyectodami.databinding.ActivityNotificacionesBinding
import com.cibertec.proyectodami.presentation.common.adapters.NotificacionesAdapter
import com.cibertec.proyectodami.presentation.features.cliente.ClienteMainActivity
import kotlinx.coroutines.launch

class NotificacionesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificacionesBinding
    private lateinit var adapter: NotificacionesAdapter
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificacionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)

        setupRecycler()
        cargarNotificaciones()
    }

    private fun setupRecycler() {
        adapter = NotificacionesAdapter(
            notificaciones = mutableListOf(),
            onNotificacionClick = {
                irAInicio()
            },
            onMarcarLeida = { notificacion ->
                marcarComoLeida(notificacion.id)
            },
            onDescartar = { notificacion ->
                descartarNotificacion(notificacion.id)
            }
        )

        binding.recyclerNotificaciones.adapter = adapter
        binding.recyclerNotificaciones.layoutManager = LinearLayoutManager(this)
    }

    private fun cargarNotificaciones() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val usuarioId = userPreferences.obtenerIdUsuario()
                val rol = userPreferences.obtenerRol()

                if (usuarioId == -1) {
                    Toast.makeText(this@NotificacionesActivity, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    return@launch
                }

                val api = RetrofitInstance.create(userPreferences).create(Notificaciones::class.java)

                val response = api.obtenerNotificaciones(usuarioId)

                if (response.isSuccessful && response.body() != null) {
                    val notificaciones = response.body()!!

                    if (notificaciones.isEmpty()) {
                        binding.recyclerNotificaciones.visibility = View.GONE
                        binding.tvNoNotificaciones.visibility = View.VISIBLE
                    } else {
                        binding.recyclerNotificaciones.visibility = View.VISIBLE
                        binding.tvNoNotificaciones.visibility = View.GONE
                        adapter.actualizarNotificaciones(notificaciones)
                    }
                } else {
                    Toast.makeText(this@NotificacionesActivity, "Error al obtener notificaciones", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@NotificacionesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Notificaciones", "Error cargando notificaciones", e)
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun marcarComoLeida(notificacionId: Int) {
        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.create(userPreferences).create(Notificaciones::class.java)
                api.marcarNotificacionLeida(notificacionId)
            } catch (e: Exception) {
                Log.e("Notificaciones", "Error al marcar como leída: ${e.message}")
            }
        }
    }

    private fun descartarNotificacion(notificacionId: Int) {
        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.create(userPreferences).create(Notificaciones::class.java)
                api.eliminarTokenFcm(notificacionId)
                Toast.makeText(this@NotificacionesActivity, "Notificación descartada", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@NotificacionesActivity, "Error al descartar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun irAInicio() {
        val intent = Intent(this, ClienteMainActivity::class.java)
        intent.putExtra("abrirFragment", "inicio")
        startActivity(intent)
    }
}