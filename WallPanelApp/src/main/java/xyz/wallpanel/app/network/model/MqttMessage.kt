package xyz.wallpanel.app.network.model

data class MqttMessage(
    val id: String,
    val topic: String,
    val payload: String
)
