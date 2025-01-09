package com.example.weather

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.weather.mqttmanager.SensorData
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
    val gradientColor = getGradient(BlueStart, BlueEnd)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
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

        // Mostrar o primeiro dado MQTT no header
        if (mqttCards.isNotEmpty()) {
            val latestData = mqttCards.last() // Pega o dado mais recente do MQTT
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

    // Estados observáveis para dados do Firebase e MQTT
    val firebaseCards by viewModel.cardsFirebaseData.collectAsState()
    val mqttCards by viewModel.cardsMqttData.collectAsState()

    // Obter localização
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

    // Layout principal
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Header com Localização
        if (!isLoading) {
            WeatherHeader(city = city, country = country, mqttCards = mqttCards)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seção inferior: Dados do Firebase
        Text(
            text = "Histórico de Sensores (Firebase)",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(8.dp)
        )
        if (firebaseCards.isEmpty()) {
            Text(
                text = "Nenhum dado recebido do Firebase.",
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp) // Adiciona um espaçamento externo ao Box
            .clip(RoundedCornerShape(16.dp)) // Torna as bordas arredondadas
            .background(gradientColor) // Define a cor de fundo (opcional)
            .padding(16.dp), // Espaçamento interno dentro do Box
        contentAlignment = Alignment.Center // Centraliza o conteúdo dentro do Box
    ){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .padding(vertical = 8.dp)
            .background(gradientColor)
            .padding(16.dp)
    ) {
        Text(
            text = "${data.temperature}°C",
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        WeatherImage(data.humidity, data.luminosity)
        Spacer(modifier = Modifier.height(16.dp))
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
            WeatherKeyVal("Pressão", "${data.pressure} atm")
            WeatherKeyVal("Altitude", "${data.altitude} m")
        }
        WeatherKeyVal("Sensação Térmica", "${data.termicSen} °C")
    }}
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

    Box(
        modifier = Modifier
            .fillMaxWidth(), // Faz o Box ocupar toda a largura disponível
        contentAlignment = Alignment.Center // Centraliza o conteúdo dentro do Box
    ) {
        Image(
            painter = painterResource(id = imageResource),
            contentDescription = "Weather Image",
            modifier = Modifier
                .size(160.dp)
                .scale(2.5f)
        )
    }
}

fun determineImageResource(humidity: Double, luminosity: Double): Int {
    return when {
        luminosity >= 3200.0 && humidity < 50.0 -> R.drawable.ensolarado // Sol forte (alta luminosidade, baixa humidade)
        luminosity >= 3200.0 && humidity >= 50.0 -> R.drawable.sol_com_nuvens // Sol com nuvens (alta luminosidade, alta humidade)
        luminosity in 2000.0..3199.0 && humidity >= 50.0 -> R.drawable.nublado // Dia nublado (luminosidade moderada, alta humidade)
        luminosity in 800.0..1999.0 && humidity > 70.0 -> R.drawable.chuvoso // Sombra ou chuva (luminosidade baixa, alta humidade)
        luminosity < 800.0 -> R.drawable.noite // Noite (luminosidade muito baixa)
        else -> R.drawable.nublado // Padrão (nublado)
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
            .height(200.dp)
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(25.dp))
                .background(getGradient(BlueStart, BlueEnd))
                .width(250.dp)
                .height(200.dp)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = card.date,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column {
                Text(
                    text = "Temperatura: ${card.temperature}°C",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Umidade: ${card.humidity}%",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Luminosidade: ${card.luminosity} lx",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Pressão: ${card.pressure} atm",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Altitude: ${card.altitude} m",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Sensação Térmica: ${card.termicSen}°C",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}