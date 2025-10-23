package com.cibertec.proyectodami.domain.repository
import com.cibertec.proyectodami.data.api.PedidosCliente
import com.cibertec.proyectodami.domain.model.dtos.CalificacionDTO
import com.cibertec.proyectodami.domain.model.dtos.requests.CalificarRequest
import retrofit2.Response

class CalificacionRepository(private val apiService:PedidosCliente) {

    suspend fun registrarCalificacion(
        idPedido: Int,
        request: CalificarRequest
    ): Response<CalificacionDTO>{
        return  apiService.registrarCalificacion(idPedido,request)
    }
}