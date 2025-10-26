package com.cibertec.proyectodami.data.dataStore

import com.cibertec.proyectodami.data.api.UserAuth
import com.cibertec.proyectodami.domain.model.dtos.requests.UsuarioRequest
import com.cibertec.proyectodami.domain.model.dtos.responses.RegisterResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(private val apiService: UserAuth) {

    suspend fun registrarUsuario(usuario: UsuarioRequest): Result<RegisterResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.registro(usuario)
                Result.success(response)
            } catch (e: HttpException) {
                Result.failure(Exception(obtenerMensajeError(e)))
            } catch (e: IOException) {
                Result.failure(Exception("Sin conexión a internet"))
            } catch (e: Exception) {
                Result.failure(Exception("Error inesperado: ${e.message}"))
            }
        }
    }

    private fun obtenerMensajeError(exception: HttpException): String {
        return when (exception.code()) {
            400 -> "Datos inválidos o incompletos"
            401 -> "Credenciales incorrectas"
            403 -> "Acceso denegado"
            404 -> "Recurso no encontrado"
            409 -> "El usuario ya existe"
            422 -> "Los datos enviados no son válidos"
            500 -> "Error interno del servidor"
            502 -> "Servidor no disponible"
            503 -> "Servicio no disponible"
            else -> "Error del servidor (${exception.code()})"
        }
    }
}