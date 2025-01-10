package com.example.weather

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.weather.location.LocationProvider
import com.example.weather.location.reverseGeocode
import com.example.weather.ui.theme.BlueEnd
import com.example.weather.ui.theme.BlueStart


data class SensorCard(
    val date: String ,
    val temperature: Double ,
    val humidity: Double ,
    val luminosity: Double ,
    val pressure: Double ,
    val altitude: Double ,
    val termicSen: Double
)

@Composable
fun WeatherHeader(city: String, country: String, mqttCards: List<SensorCard>) {
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

        if (mqttCards.isNotEmpty()) {
            val latestData = mqttCards.last() // dado mais recente
            WeatherDetails(latestData)
        } else {
            Text(
                text = "Aguardando dados do MQTT...",
                fontSize = 18.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WeatherPage(
    locationProvider: LocationProvider,
    viewModel: WheatherViewModel
) {
    var city by remember { mutableStateOf("Obtendo localização...") }
    var country by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val firebaseCards by viewModel.cardsFirebaseData.collectAsState()
    val mqttCards by viewModel.cardsMqttData.collectAsState()

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
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(scrollState)
            .clip(RoundedCornerShape(16.dp))
    ) {

        if (!isLoading) {
            WeatherHeader(city = city, country = country, mqttCards = mqttCards)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Histórico dos últimos dias",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(8.dp),
        )
        if (firebaseCards.isEmpty()) {
            Text(
                text = "Nenhum dado recebido.",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            LazyRow {
                items(firebaseCards) { card ->
                    CardItem(card = card)
                }
            }
        }
    }
}

@Composable
fun WeatherDetails(data: SensorCard) {
    val gradientColor = getGradient(BlueStart, BlueEnd)

    Column {
        Text(
            text = "${data.temperature}°C",
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        WeatherImage(data.humidity, data.luminosity)
        Card(
            modifier = Modifier.padding(16.dp),
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
        luminosity >= 3200.0 && humidity < 50.0 -> R.drawable.ensolarado // sol
        luminosity >= 3200.0 && humidity >= 50.0 -> R.drawable.sol_com_nuvens // sol com nuvens
        luminosity in 2000.0..3199.0 && humidity >= 50.0 -> R.drawable.nublado // nublado
        luminosity in 800.0..1999.0 && humidity > 70.0 -> R.drawable.chuvoso // chuvoso
        luminosity < 800.0 -> R.drawable.noite // noite
        else -> R.drawable.nublado // nublado
    }
}

fun getGradient(
    startColor: Color,
    endColor: Color,
): Brush {
    return Brush.horizontalGradient(
        colors = listOf(startColor, endColor)
    )
}

@Composable
fun CardsSection(cards: List<SensorCard>) {
    LazyRow {
        items(cards.size) { index ->
            CardItem(card = cards[index])
        }
    }
}

@Composable
fun CardItem(card: SensorCard) {
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(getGradient(BlueStart, BlueEnd))
            .fillMaxWidth()
            .height(250.dp)
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(25.dp))
                .background(getGradient(BlueStart, BlueEnd))
                .width(250.dp)
                .height(225.dp)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = card.date,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(15.dp))
            Column {
                Text(
                    text = "Temperatura: ${card.temperature}°C",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    text = "Umidade: ${card.humidity}%",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    text = "Luminosidade: ${card.luminosity} lx",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    text = "Pressão: ${card.pressure} atm",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    text = "Altitude: ${card.altitude} m",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    text = "Sensação Térmica: ${card.termicSen}°C",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}