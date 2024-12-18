package com.example.weather.data

data class WeatherData(
    val city: String,
    val country: String,
    val temp: Double,
    val condition: String,
    val humidity: Double,
    val luminosity: Double,
    val pressure: Double,
    val altitude: Double,
    val termicSen: Double
)