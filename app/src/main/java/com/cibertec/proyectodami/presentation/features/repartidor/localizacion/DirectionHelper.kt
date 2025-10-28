package com.cibertec.proyectodami.presentation.features.repartidor.localizacion

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class DirectionHelper(private val context: Context) {



    companion object {

        private const val API_KEY = "AIzaSyDAgXOgDEHZwlwq9XeABKmgbHy-5bNYuOg"
    }

    fun obtenerRuta(
        origen: LatLng,
        destino: LatLng,
        onSuccess: (List<LatLng>) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origen.latitude},${origen.longitude}" +
                "&destination=${destino.latitude},${destino.longitude}" +
                "&mode=driving" +
                "&key=$API_KEY"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                (context as? Activity)?.runOnUiThread {
                    onError("Error de conexión: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    try {
                        val puntos = parsearRuta(responseBody)
                        (context as? Activity)?.runOnUiThread {
                            if (puntos.isNotEmpty()) {
                                onSuccess(puntos)
                            } else {
                                onError("No se encontró una ruta")
                            }
                        }
                    } catch (e: Exception) {
                        (context as? Activity)?.runOnUiThread {
                            onError("Error al procesar la ruta: ${e.message}")
                        }
                    }
                }
            }
        })
    }

    private fun parsearRuta(jsonResponse: String): List<LatLng> {
        val puntos = mutableListOf<LatLng>()

        try {
            val json = JSONObject(jsonResponse)
            val routes = json.getJSONArray("routes")

            if (routes.length() > 0) {
                val route = routes.getJSONObject(0)
                val legs = route.getJSONArray("legs")

                for (i in 0 until legs.length()) {
                    val leg = legs.getJSONObject(i)
                    val steps = leg.getJSONArray("steps")

                    for (j in 0 until steps.length()) {
                        val step = steps.getJSONObject(j)
                        val polyline = step.getJSONObject("polyline")
                        val encodedString = polyline.getString("points")

                        // Decodificar polyline
                        val decodedPoints = decodificarPolyline(encodedString)
                        puntos.addAll(decodedPoints)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DirectionsHelper", "Error parseando JSON: ${e.message}")
        }

        return puntos
    }

    // Función para decodificar polyline de Google
    private fun decodificarPolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0

            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0

            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(latLng)
        }

        return poly
    }
}


