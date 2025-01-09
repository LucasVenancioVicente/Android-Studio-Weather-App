package com.example.weather.mqttmanager

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
class MQTTManager(
    private val serverUri: String,
    private val clientId: String,
    private val username: String? = null,
    private val password: String? = null
) {

    private lateinit var mqttClient: Mqtt3AsyncClient

    fun connect(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        try {
            mqttClient = MqttClient.builder()
                .useMqttVersion3() // Usar MQTT v3.1.1
                .serverHost("10.0.0.146") // Endereço do broker
                .serverPort(1883) // Porta do broker
                .buildAsync() // Cliente assíncrono

            mqttClient.connect()
                .whenComplete { ack, throwable ->
                    if (throwable == null) {
                        println("Conectado ao broker MQTT com sucesso!")
                        onSuccess()
                    } else {
                        println("Erro ao conectar ao broker MQTT: ${throwable.message}")
                        onFailure(throwable)
                    }
                }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    fun subscribe(topic: String, qos: MqttQos = MqttQos.AT_LEAST_ONCE, onMessageReceived: (String) -> Unit) {
        if (!::mqttClient.isInitialized) {
            println("Erro: mqttClient não foi inicializado")
            return
        }

        try {
            mqttClient.subscribeWith()
                .topicFilter(topic) // Nome do tópico
                .qos(qos) // Qualidade de Serviço
                .callback { message ->
                    val payload = message.payloadAsBytes.decodeToString()
                    println("Mensagem recebida no tópico '$topic': $payload")
                    onMessageReceived(payload)
                }
                .send()
        } catch (e: Exception) {
            println("Erro ao se inscrever: ${e.message}")
        }
    }

    fun disconnect(onComplete: () -> Unit) {
        if (!::mqttClient.isInitialized) {
            println("Erro: mqttClient não foi inicializado")
            return
        }

        mqttClient.disconnect()
            .whenComplete { _, throwable ->
                if (throwable == null) {
                    println("Desconectado com sucesso!")
                    onComplete()
                } else {
                    println("Erro ao desconectar: ${throwable.message}")
                }
            }
    }

    fun setCallback(callback: (topic: String, message: String) -> Unit) {
        if (!::mqttClient.isInitialized) {
            println("Erro: mqttClient não foi inicializado")
            return
        }

        mqttClient.subscribeWith()
            .topicFilter("#") // Subscrição em todos os tópicos usando o wildcard "#"
            .qos(MqttQos.AT_LEAST_ONCE) // Qualidade de Serviço
            .callback { message ->
                val topic = message.topic.toString()
                val payload = message.payloadAsBytes.decodeToString()
                callback(topic, payload) // Chama o callback com o tópico e a mensagem
            }
            .send()
            .whenComplete { _, throwable ->
                if (throwable == null) {
                    println("Callback configurado com sucesso!")
                } else {
                    println("Erro ao configurar o callback: ${throwable.message}")
                }
            }
    }
}
