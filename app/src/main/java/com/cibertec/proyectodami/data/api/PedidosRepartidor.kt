package com.cibertec.proyectodami.data.api

import com.cibertec.proyectodami.domain.model.dtos.PedidoRepartidorDTO
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PedidosRepartidor {
    @GET("repartidor/aceptados")
    suspend fun obtenerPedidosAceptados(): List<PedidoRepartidorDTO>

    @PUT("repartidor/encamino/{idPedido}")
    suspend fun caminoPedido(
        @Path("idPedido") idPedido: Int,
        @Query("idRepartidor") idRepartidor: Int
    ): PedidoRepartidorDTO
}