package com.cibertec.proyectodami.presentation.features.cliente.calificacion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.proyectodami.domain.model.dtos.CalificacionDTO
import com.cibertec.proyectodami.domain.model.dtos.requests.CalificarRequest
import com.cibertec.proyectodami.domain.repository.CalificacionRepository
import kotlinx.coroutines.launch

class CalificacionViewModel(
    private  val repository: CalificacionRepository
) : ViewModel(){
    private val _calificacionState = MutableLiveData<CalificacionState>()
    val calificacionState : LiveData<CalificacionState> = _calificacionState

    fun registrarCalificacion(idPedido:Int, puntuacion:Short,comentario:String?){
        viewModelScope.launch {
            _calificacionState.value = CalificacionState.Loading

            try {
                val request = CalificarRequest(
                    puntuacion = puntuacion,
                    comentario = comentario
                )
                val response = repository.registrarCalificacion(idPedido, request)

                if (response.isSuccessful){
                    response.body()?.let { calificacion ->
                        _calificacionState.value = CalificacionState.Success(calificacion)
                    } ?: run{
                        _calificacionState.value = CalificacionState.Error("Respuesta vacía")
                    }
                }else{
                    _calificacionState.value = CalificacionState.Error(
                        "Error: ${response.code()} - ${response.message()}")
                }
            }
            catch (e:Exception)
            {
                _calificacionState.value = CalificacionState.Error(
                    e.message ?: "Error desconocido")

             }
            }
        }
    }
    sealed class CalificacionState {
        object Loading : CalificacionState()
        data class Success(val calificacion: CalificacionDTO) : CalificacionState()
        data class Error(val message: String) : CalificacionState()
}