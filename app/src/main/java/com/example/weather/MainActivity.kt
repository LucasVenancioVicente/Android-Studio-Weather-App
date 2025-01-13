package com.example.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.example.weather.location.LocationProvider
import com.example.weather.mqttmanager.MQTTManager
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.hivemq.client.mqtt.datatypes.MqttQos

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locationProvider = LocationProvider(this) // obter a localizacao

        val mqttManager = MQTTManager( // credenciais do mqtt
            serverUri = "tcp://192.168.15.5:1883",
            clientId = "AndroidClient_${System.currentTimeMillis()}",
            username = null,
            password = null        )

        val weatherViewModel: WheatherViewModel by viewModels {
            WeatherViewModelFactory(mqttManager) // injeta o gerenciador mqtt na viewmodel
        }

        mqttManager.connect( // conectar ao broker
            onSuccess = {
                println("Conectado ao broker MQTT com sucesso")
                mqttManager.subscribe("sensores/dados", MqttQos.AT_LEAST_ONCE) { message ->
                    println("Mensagem recebida do MQTT: $message")
                    weatherViewModel.setupMqttListener()
                }
            },
            onFailure = { error ->
                println("Erro ao conectar ao broker MQTT: ${error.message}")
            }
        )

        setContent { // interface do app
            SetBarColor(color = MaterialTheme.colorScheme.background)
            WeatherPage(
                locationProvider = locationProvider,
                viewModel = weatherViewModel
            )
        }
    }
    @Composable // definir a cor da status bar
    private fun SetBarColor(color: androidx.compose.ui.graphics.Color) {
        val systemUiController = rememberSystemUiController()
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = color
            )
        }
    }
}
