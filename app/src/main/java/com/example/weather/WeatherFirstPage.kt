package com.example.weather

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.weather.location.LocationProvider
import com.example.weather.location.reverseGeocode
import com.example.weather.mqttmanager.SensorData
import com.example.weather.ui.theme.BlueEnd
import com.example.weather.ui.theme.BlueStart

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
    val gradientColor = getGradient(BlueStart, BlueEnd)

    Column {
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
            Card(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .background(gradientColor)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column {
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
        Spacer(modifier = Modifier.height(16.dp))
        CardsSection()
    }
}

@Composable
fun WeatherKeyVal(key: String, value: String) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = key, fontWeight = FontWeight.SemiBold, color = Color.White)
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

data class Card(
    val cardDate: String,
    val cardTemp: Double,
    val cardHumidity: Double,
    val cardLuminosity: Double,
    val cardPressure: Double,
    val cardAltitude: Double,
    val cardTermicSen: Double,
    val color: Brush
)

val cards = listOf(
    Card(
        cardDate = "05/01",
        cardTemp = 34.0,
        cardHumidity = 70.0,
        cardLuminosity = 400.0,
        cardPressure = 1013.0,
        cardAltitude = 250.0,
        cardTermicSen = 35.0,
        color = getGradient(BlueStart, BlueEnd),
    ),
    Card(
        cardDate = "06/01",
        cardTemp = 30.0,
        cardHumidity = 65.0,
        cardLuminosity = 600.0,
        cardPressure = 1010.0,
        cardAltitude = 200.0,
        cardTermicSen = 33.0,
        color = getGradient(BlueStart, BlueEnd),
    ),
    Card(
        cardDate = "07/01",
        cardTemp = 29.0,
        cardHumidity = 75.0,
        cardLuminosity = 500.0,
        cardPressure = 1015.0,
        cardAltitude = 270.0,
        cardTermicSen = 32.0,
        color = getGradient(BlueStart, BlueEnd),
    ),
)

fun getGradient(
    startColor: Color,
    endColor: Color,
): Brush {
    return Brush.horizontalGradient(
        colors = listOf(startColor, endColor)
    )
}

@Composable
fun CardsSection() {
    LazyRow {
        items(cards.size) { index ->
            CardItem(index)
        }
    }
}

@Composable
fun CardItem(index: Int) {
    val card = cards[index]
    var lastItemPaddingEnd = 0.dp
    if (index == cards.size - 1) {
        lastItemPaddingEnd = 16.dp
    }

    Box(
        modifier = Modifier
            .padding(start = 16.dp, end = lastItemPaddingEnd)
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(25.dp))
                .background(card.color)
                .width(250.dp)
                .height(200.dp)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = card.cardDate,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column{
                Text(
                    text = "Temperatura: ${card.cardTemp}°C",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Umidade: ${card.cardHumidity}%",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Luminosidade: ${card.cardLuminosity} lx",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Pressão: ${card.cardPressure} hPa",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Altitude: ${card.cardAltitude} m",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Sensação Térmica: ${card.cardTermicSen}°C",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}