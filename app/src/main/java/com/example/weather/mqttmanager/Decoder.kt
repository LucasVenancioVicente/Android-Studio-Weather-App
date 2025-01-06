package com.example.weather.mqttmanager

import org.json.JSONObject

class Decoder {

    fun decodeMessage(message: String): SensorData {
        return try {
            val json = JSONObject(message)
            SensorData(
                temp = json.optDouble("temp", 0.0),
                condition = json.optString("condition", ""),
                humidity = json.optDouble("humidity", 0.0),
                luminosity = json.optDouble("luminosity", 0.0),
                pressure = json.optDouble("pressure", 0.0),
                altitude = json.optDouble("altitude", 0.0),
                termicSen = json.optDouble("termicSen", 0.0)
            )
        } catch (e: Exception) {
            println("Erro ao decodificar a mensagem: ${e.message}")
            SensorData()
        }
    }
}

data class SensorData(
    val temp: Double = 0.0,
    val condition: String = "erro",
    val humidity: Double = 0.0,
    val luminosity: Double = 0.0,
    val pressure: Double = 0.0,
    val altitude: Double = 0.0,
    val termicSen: Double = 0.0
)