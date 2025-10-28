package com.cibertec.proyectodami.data.dataStore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
        val USER_ID_KEY = intPreferencesKey("user_id")
        private val USER_NOMBRE_KEY = stringPreferencesKey("user_nombre")
        private val USER_CORREO_KEY = stringPreferencesKey("user_correo")
        private val USER_TELEFONO_KEY = stringPreferencesKey("user_telefono")
        private val ROL_KEY = intPreferencesKey("rol")
        private val PEDIDO_ACTIVO_ID = intPreferencesKey("pedido_activo_id")
        private val PEDIDO_ACTIVO_NUM = stringPreferencesKey("pedido_activo_num")
        private val PEDIDO_ACTIVO_ESTADO = stringPreferencesKey("pedido_activo_estado")
        private val PEDIDO_ACTIVO_JSON = stringPreferencesKey("pedido_activo_json")
    }

    private val dataStore = context.dataStore

    suspend fun guardarToken(token: String) {
        context.dataStore.edit { it[TOKEN_KEY] = token }
    }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }

    suspend fun obtenerToken(): String? {
        return token.first()
    }

    suspend fun guardarIdUsuario(id: Int) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = id
        }
    }

    val idUsuario: Flow<Int> = context.dataStore.data
        .map { prefs -> prefs[USER_ID_KEY] ?: -1 }

    suspend fun obtenerIdUsuario(): Int {
        return idUsuario.first()
    }

    suspend fun guardarNombreUsuario(nombre: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_NOMBRE_KEY] = nombre
        }
    }

    val nombreUsuario: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[USER_NOMBRE_KEY] }

    suspend fun obtenerNombreUsuario(): String? {
        return nombreUsuario.first()
    }

    suspend fun guardarTelefono(telefono: String) {
        dataStore.edit { it[USER_TELEFONO_KEY] = telefono }
    }

    val telefono: Flow<String?> = dataStore.data.map { it[USER_TELEFONO_KEY] }

    suspend fun obtenerTelefono(): String? = telefono.first()

    suspend fun guardarCorreo(correo: String) {
        dataStore.edit { it[USER_CORREO_KEY] = correo }
    }

    val correo: Flow<String?> = dataStore.data.map { it[USER_CORREO_KEY] }

    suspend fun obtenerCorreo(): String? = correo.first()

    suspend fun guardarRol(rol: Int) {
        context.dataStore.edit { prefs ->
            prefs[ROL_KEY] = rol
        }
    }

    val rol: Flow<Int?> = context.dataStore.data
        .map { prefs -> prefs[ROL_KEY] }

    suspend fun obtenerRol(): Int? {
        return rol.first()
    }


    val tienePedidoActivo: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PEDIDO_ACTIVO_ID] != null
        }

    val pedidoActivoJson: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PEDIDO_ACTIVO_JSON]
        }

    suspend fun guardarPedidoActivo(
        idPedido: Int,
        numPedido: String,
        estado: String,
        pedidoJson: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[PEDIDO_ACTIVO_ID] = idPedido
            preferences[PEDIDO_ACTIVO_NUM] = numPedido
            preferences[PEDIDO_ACTIVO_ESTADO] = estado
            preferences[PEDIDO_ACTIVO_JSON] = pedidoJson
        }
    }

    suspend fun limpiarPedidoActivo() {
        context.dataStore.edit { preferences ->
            preferences.remove(PEDIDO_ACTIVO_ID)
            preferences.remove(PEDIDO_ACTIVO_NUM)
            preferences.remove(PEDIDO_ACTIVO_ESTADO)
            preferences.remove(PEDIDO_ACTIVO_JSON)
        }
    }

    suspend fun obtenerIdPedidoActivo(): Int? {
        return context.dataStore.data.map { preferences ->
            preferences[PEDIDO_ACTIVO_ID]
        }.first()
    }

    suspend fun limpiarDatos() {
        context.dataStore.edit { it.clear() }
    }

}