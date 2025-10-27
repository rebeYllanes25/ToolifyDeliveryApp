package com.cibertec.proyectodami.data.api

import com.cibertec.proyectodami.domain.model.dtos.NotificacionDTO
import com.cibertec.proyectodami.domain.model.dtos.requests.FcmTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface Notificaciones {
    @POST("notificacion/fcm-token")
    suspend fun registrarTokenFcm(
        @Query("usuarioId") usuarioId: Int,
        @Body tokenRequest: FcmTokenRequest
    ): Response<Map<String, String>>

    @DELETE("notificacion/fcm-token/{usuarioId}")
    suspend fun eliminarTokenFcm(
        @Path("usuarioId") usuarioId: Int
    ): Response<Map<String, String>>

    @GET("notificacion/usuario/{usuarioId}")
    suspend fun obtenerNotificaciones(
        @Path("usuarioId") usuarioId: Int
    ): Response<List<NotificacionDTO>>

    @PUT("notificacion/{id}/leer")
    suspend fun marcarNotificacionLeida(
        @Path("id") notificacionId: Int
    ): Response<Map<String, String>>
}