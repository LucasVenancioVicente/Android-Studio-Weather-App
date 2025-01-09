package com.example.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.weather.location.LocationProvider
import com.example.weather.mqttmanager.MQTTManager
import com.hivemq.client.mqtt.datatypes.MqttQos

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locationProvider = LocationProvider(this)

        // Configuração do MQTTManager com as credenciais corretas
        val mqttManager = MQTTManager(
            serverUri = "tcp://10.0.0.146:1883",
            clientId = "AndroidClient_${System.currentTimeMillis()}",
            username = null, // Defina credenciais, se necessário
            password = null        )

        // Inicialização do ViewModel
        val weatherViewModel: WheatherViewModel by viewModels {
            WheatherViewModelFactory(mqttManager)
        }

        // Conectar ao MQTT broker
        mqttManager.connect(
            onSuccess = {
                println("Conectado ao broker MQTT com sucesso")
                // Inscrição no tópico
                mqttManager.subscribe("sensores/dados", MqttQos.AT_LEAST_ONCE) { message ->
                    println("Mensagem recebida do MQTT: $message")
                    weatherViewModel.setupMqttListener()
                }
            },
            onFailure = { error ->
                println("Erro ao conectar ao broker MQTT: ${error.message}")
            }
        )

        // Exibir a interface
        setContent {
            WeatherPage(
                locationProvider = locationProvider,
                viewModel = weatherViewModel
            )
        }
    }
}
