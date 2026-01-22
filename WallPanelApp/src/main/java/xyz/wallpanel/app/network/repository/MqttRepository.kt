package xyz.wallpanel.app.network.repository

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import xyz.wallpanel.app.network.model.MqttMessage
import xyz.wallpanel.app.network.model.MqttState

interface MqttRepository {
    val state: StateFlow<MqttState>
    val message: SharedFlow<MqttMessage>

    fun connect()
    fun disconnect()
    fun publish(topic: String, payload: String, retain: Boolean)
    fun restart()
}
