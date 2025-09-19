package com.example.facefy.data.models

data class ServerConfig(
    val host: String = "localhost",
    val port: Int = 8000
) {
    fun getWebSocketUrl(): String = "ws://$host:$port/ws/android"
}

data class ConnectionData(
    val status: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val serverConfig: ServerConfig = ServerConfig(),
    val lastConnected: Long? = null,
    val errorMessage: String? = null
)
