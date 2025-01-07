package com.example.weather

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.weather.location.LocationProvider
import com.example.weather.location.reverseGeocode
import com.example.weather.mqttmanager.SensorData

@Composable
fun WeatherPage(
    locationProvider: LocationProvider,
    viewModel: WheatherViewModel
) {
    var city by remember { mutableStateOf("Obtendo localização...") }
    var country by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showDetails by remember { mutableStateOf(false) }

    val sensorData by viewModel.sensorData.collectAsState(initial = SensorData())

    LaunchedEffect(Unit) {
        if (locationProvider.hasLocationPermission()) {
            val coordinates = locationProvider.getLastLocation()
            coordinates?.let { (latitude, longitude) ->
                val result = reverseGeocode(latitude, longitude)
                city = result.first
                country = result.second
            } ?: run {
                city = "Erro ao obter localização"
                country = ""
            }
        } else {
            locationProvider.requestLocationPermission()
        }
        isLoading = false
    }

    LaunchedEffect(Unit) {
        viewModel.connectAndSubscribe("sensor/topic")
    }

    if (isLoading) {
        Text(
            text = "Carregando localização...",
            modifier = Modifier.fillMaxSize(),
            textAlign = TextAlign.Center
        )
    } else if (showDetails) {
        WeatherDetails(
            city = city,
            country = country,
            data = sensorData
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { showDetails = true }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherDetails(city: String, country: String, data: SensorData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = city,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = country,
                    fontSize = 20.sp,
                    color = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "${data.temp}°C",
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        WeatherImage(data.humidity, data.luminosity)
        Text(
            text = data.condition,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    WeatherKeyVal("Umidade", "${data.humidity} %")
                    WeatherKeyVal("Luminosidade", "${data.luminosity} lx")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    WeatherKeyVal("Pressão", "${data.pressure} hPa")
                    WeatherKeyVal("Altitude", "${data.altitude} m")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    WeatherKeyVal("Sensação Térmica", "${data.termicSen} °C")
                }
            }
        }
    }
}

@Composable
fun WeatherKeyVal(key: String, value: String) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = key, fontWeight = FontWeight.SemiBold, color = Color.Gray)
    }
}

@Composable
fun WeatherImage(humidity: Double, luminosity: Double) {
    val imageResource = determineImageResource(humidity, luminosity)

    Image(
        painter = painterResource(id = imageResource),
        contentDescription = "Weather Image",
        modifier = Modifier
            .size(160.dp)
            .scale(2.5f)
    )
}

fun determineImageResource(humidity: Double, luminosity: Double): Int {
    return when {
        humidity >= 30.0 && luminosity >= 500.0 -> R.drawable.ensolarado // Ensolarado
        humidity > 80.0 && luminosity >= 300.0 -> R.drawable.sol_com_nuvens // Sol com nuvens
        humidity <= 80.0 && luminosity < 300.0 -> R.drawable.nublado // Nublado
        humidity > 80.0 && luminosity <= 300.0 -> R.drawable.chuvoso // Chuvoso
        else -> R.drawable.noite // Noite sem nuvens
    }
}
