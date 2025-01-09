package com.example.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weather.mqttmanager.MQTTManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class WheatherViewModel(private val mqttManager: MQTTManager) : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val sensorRef = database.getReference("sensorReadings")

    // Dados do Firebase
    private val _cardsFirebaseData = MutableStateFlow<List<SensorCard>>(emptyList())
    val cardsFirebaseData: StateFlow<List<SensorCard>> get() = _cardsFirebaseData

    // Dados do MQTT
    private val _cardsMqttData = MutableStateFlow<List<SensorCard>>(emptyList())
    val cardsMqttData: StateFlow<List<SensorCard>> get() = _cardsMqttData

    private val _mqttData = MutableStateFlow<SensorCard?>(null)
    val mqttData: StateFlow<SensorCard?> get() = _mqttData

    init {
        fetchFirebaseSensorData()
        setupMqttListener()
    }

    fun setupMqttListener() {
        try {
            mqttManager.setCallback { topic, message ->
                try {
                    val data = JSONObject(message)

                    val sensorCard = SensorCard(
                        date = getCurrentDateTime(),
                        temperature = data.optDouble("Temperatura", 0.0),
                        humidity = data.optDouble("Umidade", 0.0),
                        luminosity = data.optDouble("Luminosidade", 0.0),
                        pressure = data.optDouble("Pressão", 0.0),
                        altitude = data.optDouble("altitude", 0.0),
                        termicSen = data.optDouble("SensacaoTermica", 0.0)
                    )

                    // Atualizar o estado local
                    _cardsMqttData.value = _cardsMqttData.value + sensorCard

                    // Gravar no Firebase

                } catch (e: JSONException) {
                    println("Erro ao processar mensagem MQTT: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("Erro ao configurar o listener MQTT: ${e.message}")
        }
    }

    private fun getCurrentDateTime(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }

    private fun fetchFirebaseSensorData() {
        sensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cardsList = mutableListOf<SensorCard>()
                for (data in snapshot.children) {
                    val date = data.child("data_hora").getValue(String::class.java) ?: ""
                    val temperature = data.child("temperatura").getValue(Double::class.java) ?: 0.0
                    val humidity = data.child("umidade").getValue(Double::class.java) ?: 0.0
                    val luminosity = data.child("luminosidade").getValue(Double::class.java) ?: 0.0
                    val pressure = data.child("pressao").getValue(Double::class.java) ?: 0.0
                    val altitude = data.child("altitude").getValue(Double::class.java) ?: 0.0
                    val termicSen = data.child("sensacao_termica").getValue(Double::class.java) ?: 0.0

                    cardsList.add(
                        SensorCard(
                            date = date,
                            temperature = temperature,
                            humidity = humidity,
                            luminosity = luminosity,
                            pressure = pressure,
                            altitude = altitude,
                            termicSen = termicSen
                        )
                    )
                }
                _cardsFirebaseData.value = cardsList
            }

            override fun onCancelled(error: DatabaseError) {
                println("Erro ao ler dados do Firebase: ${error.message}")
            }
        })
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

