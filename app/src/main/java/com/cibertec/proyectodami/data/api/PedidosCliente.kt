package com.cibertec.proyectodami.data.api

import com.cibertec.proyectodami.domain.model.dtos.CalificacionDTO
import com.cibertec.proyectodami.domain.model.dtos.PedidoClienteDTO
import com.cibertec.proyectodami.domain.model.dtos.requests.CalificarRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PedidosCliente {

    @GET("{idCliente}/pedidos")
    suspend fun obtenerPedidos(
        @Path("idCliente") idCliente: Int,
        @Query("estado") estado: String
    ): List<PedidoClienteDTO>

    @GET("{idPedido}")
    suspend fun obtenerPedidoPorId(
        @Path("idPedido") idPedido: Int
    ): PedidoClienteDTO

    @POST("{idPedido}/calificar")
    suspend fun registrarCalificacion(
        @Path("idPedido") idPedido: Int,
        @Body request: CalificarRequest?
    ): Response<CalificacionDTO>
}