package xyz.wallpanel.app.network.model

sealed class MqttState {
    object Disconnected : MqttState()
    object Connecting : MqttState()
    object Connected : MqttState()
    data class Error(val message: String) : MqttState()
}
