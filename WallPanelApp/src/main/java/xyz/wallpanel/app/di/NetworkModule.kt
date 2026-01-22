package xyz.wallpanel.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import xyz.wallpanel.app.network.repository.MqttRepository
import xyz.wallpanel.app.network.repository.MqttRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindMqttRepository(
        mqttRepositoryImpl: MqttRepositoryImpl
    ): MqttRepository
}
