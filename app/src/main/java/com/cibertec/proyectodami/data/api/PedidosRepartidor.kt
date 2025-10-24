package com.cibertec.proyectodami.data.api

import com.cibertec.proyectodami.domain.model.dtos.PedidoRepartidorDTO
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PedidosRepartidor {
    @GET("repartidor/pendientes")
    suspend fun obtenerPedidosPendientes(): List<PedidoRepartidorDTO>

    @PUT("repartidor/asignar/{idPedido}")
    suspend fun asignarRepartidor(
        @Path("idPedido") idPedido: Int,
        @Query("idRepartidor") idRepartidor: Int
    ): PedidoRepartidorDTO

    @PUT("repartidor/encamino/{idPedido}")
    suspend fun caminoPedido(
        @Path("idPedido") idPedido: Int,
        @Query("idRepartidor") idRepartidor: Int
    ): PedidoRepartidorDTO
}