package xyz.wallpanel.app.network.repository

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3MessageException
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5MessageException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import xyz.wallpanel.app.network.IMqttManagerListener
import xyz.wallpanel.app.network.MQTT3Service
import xyz.wallpanel.app.network.MQTT5Service
import xyz.wallpanel.app.network.MQTTServiceInterface
import xyz.wallpanel.app.network.MQTTOptions
import xyz.wallpanel.app.network.model.MqttMessage
import xyz.wallpanel.app.network.model.MqttState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mqttOptions: MQTTOptions
) : MqttRepository, IMqttManagerListener, DefaultLifecycleObserver {

    private val _state = MutableStateFlow<MqttState>(MqttState.Disconnected)
    override val state: StateFlow<MqttState> = _state.asStateFlow()

    private val _message = MutableSharedFlow<MqttMessage>()
    override val message: SharedFlow<MqttMessage> = _message.asSharedFlow()

    private var mqttService: MQTTServiceInterface? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Implementation of IMqttManagerListener

    override fun handleMqttConnected() {
        _state.value = MqttState.Connected
    }

    override fun handleMqttDisconnected() {
        _state.value = MqttState.Disconnected
    }

    override fun handleMqttException(errorMessage: String) {
        _state.value = MqttState.Error(errorMessage)
    }

    override fun subscriptionMessage(id: String, topic: String, payload: String) {
        scope.launch {
            _message.emit(MqttMessage(id, topic, payload))
        }
    }

    // MqttRepository methods

    override fun connect() {
        if (_state.value is MqttState.Connected || _state.value is MqttState.Connecting) return
        
        _state.value = MqttState.Connecting
        startMqtt()
    }

    override fun disconnect() {
        stopMqtt()
    }

    override fun publish(topic: String, payload: String, retain: Boolean) {
        mqttService?.publish(topic, payload, retain)
    }

    override fun restart() {
        stopMqtt()
        startMqtt()
    }

    // Internal helpers

    private fun startMqtt() {
        if (!mqttOptions.isValid) {
            handleMqttException("Invalid MQTT Options")
             return
        }

        try {
            if (mqttOptions.getVersion() == "3.1.1") {
                if (mqttService == null || mqttService !is MQTT3Service) {
                    mqttService = MQTT3Service(context, mqttOptions, this)
                } else {
                    mqttService?.reconfigure(context, mqttOptions, this)
                }
            } else {
                 if (mqttService == null || mqttService !is MQTT5Service) {
                    mqttService = MQTT5Service(context, mqttOptions, this)
                } else {
                    mqttService?.reconfigure(context, mqttOptions, this)
                }
            }
        } catch (t: Throwable) {
            Timber.e(t, "Could not create MQTT Service")
            handleMqttException(t.message ?: "Unknown error creating service")
        }
    }

    private fun stopMqtt() {
        try {
            mqttService?.close()
        } catch (e: Exception) {
            Timber.e(e)
        }
        mqttService = null
        _state.value = MqttState.Disconnected
    }
}
