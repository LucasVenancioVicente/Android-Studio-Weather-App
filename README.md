# Weather Monitoring App

This project is a weather monitoring application built with **Jetpack Compose** and **HiveMQ MQTT**. The app connects to an ESP32 microcontroller that collects temperature, humidity, and other environmental data using sensors. Data is transmitted via Wi-Fi and displayed in real-time within the app.

## Features

- Real-time weather updates using MQTT.
- Display of sensor data, such as temperature and humidity.
- Integration with an ESP32 microcontroller.
- Support for fallback default values when no sensor data is available.
- Built with modern Android development practices using Jetpack Compose.

## Technologies Used

- **Android Studio**: Development environment.
- **Jetpack Compose**: UI framework.
- **HiveMQ**: MQTT client for data communication.
- **ESP32**: Microcontroller for data collection.
- **Kotlin**: Programming language.

## How It Works

1. The ESP32 collects data from sensors (e.g., temperature and humidity).
2. Sensor data is sent to an MQTT broker (e.g., HiveMQ).
3. The app subscribes to the MQTT topic to receive and display the sensor data in real-time.
4. If no data is received, the app uses default sensor values.

## Prerequisites

- Android Studio installed.
- An ESP32 microcontroller configured to send data via MQTT.
- An MQTT broker (e.g., HiveMQ Cloud or a local broker).
- Kotlin 1.7 or higher.
