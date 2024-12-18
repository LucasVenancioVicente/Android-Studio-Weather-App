package com.example.weather.location

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationProvider(private val activity: Activity) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)

    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION_CODE
        )
    }

    @SuppressLint("MissingPermission")
    suspend fun getLastLocation(): Pair<Double, Double>? {
        if (!hasLocationPermission()) {
            return null
        }

        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        continuation.resume(Pair(it.latitude, it.longitude))
                    } ?: continuation.resume(null)
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }
    }

    companion object {
        const val REQUEST_LOCATION_PERMISSION_CODE = 1
    }
}

suspend fun reverseGeocode(latitude: Double, longitude: Double): Pair<String, String> {
    val apiKey = "e99618e77b5f17961360789172c5c173"
    val url = "https://api.openweathermap.org/geo/1.0/reverse?lat=$latitude&lon=$longitude&limit=1&appid=$apiKey"

    return withContext(Dispatchers.IO) {
        try {
            val response = URL(url).readText()
            val jsonArray = JSONArray(response)

            if (jsonArray.length() > 0) {
                val jsonObject = jsonArray.getJSONObject(0)
                val city = jsonObject.optString("name", "Cidade desconhecida")
                val country = jsonObject.optString("country", "País desconhecido")
                Pair(city, country)
            } else {
                Pair("Cidade desconhecida", "País desconhecido")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair("Erro ao obter cidade", "Erro ao obter país")
        }
    }
}
