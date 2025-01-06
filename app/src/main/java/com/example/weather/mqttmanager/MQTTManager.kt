package com.example.weather.mqttmanager

import android.content.Context
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MQTTManager(
    context: Context,
    private val serverUri: String,
    private val clientId: String,
    private val username: String? = null,
    private val password: String? = null
) {

    private val mqttClient: MqttAndroidClient = MqttAndroidClient(context, serverUri, clientId)

    fun connect(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        try {
            val options = MqttConnectOptions().apply {
                isCleanSession = true
                username?.let { userName = it }
                password?.let { this.password = it.toCharArray() }
            }

            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    onSuccess()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    onFailure(exception ?: Throwable("Erro desconhecido"))
                }
            })
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    fun subscribe(topic: String, qos: Int = 1, onMessageReceived: (String) -> Unit) {
        try {
            mqttClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    println("Inscrito com sucesso em $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    println("Falha ao se inscrever no tópico $topic: ${exception?.message}")
                }
            })

            mqttClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    println("Conexão perdida: ${cause?.message}")
                }

                override fun messageArrived(topic: String, message: MqttMessage) {
                    onMessageReceived(message.toString())
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {

                }
            })
        } catch (e: Exception) {
            println("Erro ao se inscrever: ${e.message}")
        }
    }

    fun disconnect(onComplete: () -> Unit) {
        mqttClient.disconnect(null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                onComplete()
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                println("Erro ao desconectar: ${exception?.message}")
            }
        })
    }
}

private fun CharArray.toCharArray(): CharArray? {
    return this?.copyOf()
}
