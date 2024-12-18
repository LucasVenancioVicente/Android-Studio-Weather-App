package com.example.weather

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import org.json.JSONArray
import java.net.URL
import org.json.JSONObject

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
        )

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            var city by remember { mutableStateOf("Obtendo localização...") }
            var country by remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        println("Latitude: ${it.latitude}, Longitude: ${it.longitude}")
                        CoroutineScope(Dispatchers.IO).launch {
                            val result = reverseGeocode(it.latitude, it.longitude)
                            city = result.first
                            country = result.second
                        }
                    } ?: println("Localização não encontrada.")
                }
            }

            WeatherApp(city = city, country = country)
        }
    }
}

@Composable
fun WeatherApp(city: String, country: String) {
    WeatherDetails(
        data = WeatherData(
            city = city,
            country = country,
            temp = 32.0,
            condition = "Ensolarado",
            humidity = 1000.0,
            luminosity = 2000.0,
            pressure = 40.9,
            altitude = 50.8,
            termicSen = 35.0
        )
    )
}

fun reverseGeocode(latitude: Double, longitude: Double): Pair<String, String> {
    val apiKey = "e99618e77b5f17961360789172c5c173"
    val url = "https://api.openweathermap.org/geo/1.0/reverse?lat=$latitude&lon=$longitude&limit=1&appid=$apiKey"

    return try {
        val response = URL(url).readText()
        println("API Response: $response")

        val jsonArray = JSONArray(response)

        if (jsonArray.length() > 0) {
            val json = jsonArray.getJSONObject(0)
            val city = json.optString("name", "Cidade desconhecida")
            val country = json.optString("country", "País desconhecido")
            Pair(city, country)
        } else {
            Pair("Cidade desconhecida", "País desconhecido")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Pair("Erro ao obter localização", "")
    }
}