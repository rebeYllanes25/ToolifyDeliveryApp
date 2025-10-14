package com.cibertec.proyectodami.data.dataStore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
        val USER_ID_KEY = intPreferencesKey("user_id")
        private val USER_NOMBRE_KEY = stringPreferencesKey("user_nombre")
    }

    suspend fun guardarToken(token: String) {
        context.dataStore.edit { it[TOKEN_KEY] = token }
    }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }

    suspend fun guardarIdUsuario(id: Int) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = id
        }
    }

    val idUsuario: Flow<Int> = context.dataStore.data
        .map { prefs -> prefs[USER_ID_KEY] ?: -1 }

    suspend fun guardarNombreUsuario(nombre: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_NOMBRE_KEY] = nombre
        }
    }

    val nombreUsuario: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[USER_NOMBRE_KEY] }

    suspend fun limpiarDatos() {
        context.dataStore.edit { it.clear() }
    }
}
