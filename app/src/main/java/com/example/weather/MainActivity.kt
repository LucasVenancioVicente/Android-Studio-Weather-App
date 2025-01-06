package com.example.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.weather.location.LocationProvider
import com.example.weather.mqttmanager.MQTTManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locationProvider = LocationProvider(this)

        val mqttManager = MQTTManager(
            context = this,
            serverUri = "ssl://mqtt-dashboard.com:8884",
            clientId = "clientId-gGYYyghaN5",
            username = "lucas",
            password = "lucas"
        )

        val wheatherViewModel: WheatherViewModel by viewModels {
            WheatherViewModelFactory(mqttManager)
        }

        setContent {
            WeatherPage(
                locationProvider = locationProvider,
                viewModel = wheatherViewModel
            )
        }
    }
}
