package com.example.weather.mqttmanager

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient

class MQTTManager( // construtor com os parametros para a conexao mqtt
    private val serverUri: String,
    private val clientId: String,
    private val username: String? = null,
    private val password: String? = null
) {

    private lateinit var mqttClient: Mqtt3AsyncClient

    fun connect(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) { // conexao com o mqtt
        try {
            mqttClient = MqttClient.builder()
                .useMqttVersion3() // versao do mqtt
                .serverHost("192.168.15.5") // endereco do broker
                .serverPort(1883) // porta do broker
                .buildAsync()

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

    fun subscribe(topic: String, qos: MqttQos = MqttQos.AT_LEAST_ONCE, onMessageReceived: (String) -> Unit) { // inscricao em um topico do mqtt
        if (!::mqttClient.isInitialized) {
            println("Erro: mqttClient não foi inicializado")
            return
        }

        try {
            mqttClient.subscribeWith()
                .topicFilter(topic)
                .qos(qos)
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

    fun disconnect(onComplete: () -> Unit) { // desconecta do mqtt
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

    fun setCallback(callback: (topic: String, message: String) -> Unit) { // configura um callback generico para receber mensagens em qualquer topico
        if (!::mqttClient.isInitialized) {
            println("Erro: mqttClient não foi inicializado")
            return
        }

        mqttClient.subscribeWith()
            .topicFilter("#") // subscricao em todos os topicos usando o #
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback { message ->
                val topic = message.topic.toString()
                val payload = message.payloadAsBytes.decodeToString()
                callback(topic, payload) // callback com o topico e a mensagem
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
