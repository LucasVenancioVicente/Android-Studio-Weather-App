package com.example.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weather.mqttmanager.Decoder
import com.example.weather.mqttmanager.MQTTManager
import com.example.weather.mqttmanager.SensorData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WheatherViewModel(private val mqttManager: MQTTManager) : ViewModel() {

    private val _sensorData = MutableStateFlow(SensorData())
    val sensorData: StateFlow<SensorData> get() = _sensorData

    private val decoder = Decoder()

    fun connectAndSubscribe(topic: String) {
        mqttManager.connect(
            onSuccess = {
                mqttManager.subscribe(topic) { message ->
                    val data = decoder.decodeMessage(message)
                    viewModelScope.launch {
                        _sensorData.emit(data)
                    }
                }
            },
            onFailure = { error ->
                println("Erro ao conectar: ${error.message}")
            }
        )
    }

    override fun onCleared() {
        mqttManager.disconnect {
            println("Desconectado do MQTT")
        }
    }
}

class WheatherViewModelFactory(private val mqttManager: MQTTManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WheatherViewModel::class.java)) {
            return WheatherViewModel(mqttManager) as T
        }
        throw IllegalArgumentException("Classe ViewModel desconhecida")
    }
}

