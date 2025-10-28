package com.cibertec.proyectodami.data.api

import com.cibertec.proyectodami.domain.model.dtos.responses.LoginResponse
import com.cibertec.proyectodami.domain.model.dtos.UsuarioDTO
import com.cibertec.proyectodami.domain.model.dtos.requests.UsuarioRequest
import com.cibertec.proyectodami.domain.model.dtos.responses.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface UserAuth {
    @POST("auth/login")
    @FormUrlEncoded
    suspend fun login(
        @Field("correo") correo: String,
        @Field("clave") clave: String
    ): LoginResponse

    @GET("auth/me")
    suspend fun getUsuarioInfo(): UsuarioDTO

    @POST("auth/register")
    suspend fun registro(
        @Body usuario: UsuarioRequest
    ): RegisterResponse
}
