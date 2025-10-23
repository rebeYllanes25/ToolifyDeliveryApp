package com.cibertec.proyectodami.presentation.features.cliente.calificacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cibertec.proyectodami.domain.repository.CalificacionRepository

class CalificacionViewModelFactory(
    private val repository: CalificacionRepository
) : ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalificacionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalificacionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}