package com.cibertec.proyectodami.data.api

import com.cibertec.proyectodami.domain.model.dtos.PedidoClienteDTO
import com.cibertec.proyectodami.domain.model.dtos.responses.ProductosResponse
import com.cibertec.proyectodami.domain.model.dtos.responses.ResultadoResponse
import com.cibertec.proyectodami.domain.model.entities.Categoria
import com.cibertec.proyectodami.domain.model.entities.Venta
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CompraCliente {
    @POST("venta/delivery")
    suspend fun guardarVentaDelivery(
        @Body venta: Venta
    ): ResultadoResponse

    @GET("cliente/producto")
    suspend fun listProductosYCategorias(
        @Query("idCategorias") idCategorias: List<Int>? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 12,
        @Query("order") order: String? = null
    ): ProductosResponse

    @GET("categoria/listaAll")
    suspend fun listCategorias(): List<Categoria>
}