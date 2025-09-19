package com.example.facefy.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.facefy.data.models.ConnectionData
import com.example.facefy.data.models.ConnectionStatus
import com.example.facefy.data.models.ServerConfig
import com.example.facefy.data.network.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

class ConnectionRepository(context: Context) {
    private val webSocketManager = WebSocketManager()
    private val sharedPrefs: SharedPreferences = 
        context.getSharedPreferences("facefy_prefs", Context.MODE_PRIVATE)
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _serverConfig = MutableStateFlow(loadServerConfig())
    val serverConfig: StateFlow<ServerConfig> = _serverConfig
    
    val connectionData: StateFlow<ConnectionData> = combine(
        webSocketManager.connectionStatus,
        webSocketManager.errorMessage,
        _serverConfig
    ) { status: ConnectionStatus, error: String?, config: ServerConfig ->
        ConnectionData(
            status = status,
            serverConfig = config,
            lastConnected = if (status == ConnectionStatus.CONNECTED) System.currentTimeMillis() else null,
            errorMessage = error
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = ConnectionData()
    )
    
    val faceDetectionData = webSocketManager.faceDetectionData
    val isStreamingActive = webSocketManager.isStreamingActive
    
    fun connect() {
        webSocketManager.connect(_serverConfig.value)
    }
    
    fun disconnect() {
        webSocketManager.disconnect()
    }
    
    fun updateServerConfig(host: String, port: Int) {
        val newConfig = ServerConfig(host, port)
        _serverConfig.value = newConfig
        saveServerConfig(newConfig)
    }
    
    fun isConnected(): Boolean {
        return webSocketManager.isConnected()
    }
    
    fun startVideoStream() {
        webSocketManager.requestVideoStream()
    }
    
    fun stopVideoStream() {
        webSocketManager.stopVideoStream()
    }
    
    fun startStreaming() {
        webSocketManager.requestVideoStream()
    }
    
    fun stopStreaming() {
        webSocketManager.stopVideoStream()
    }
    
    private fun loadServerConfig(): ServerConfig {
        val host = sharedPrefs.getString("server_host", "localhost") ?: "localhost"
        val port = sharedPrefs.getInt("server_port", 8000)
        return ServerConfig(host, port)
    }
    
    private fun saveServerConfig(config: ServerConfig) {
        sharedPrefs.edit()
            .putString("server_host", config.host)
            .putInt("server_port", config.port)
            .apply()
    }
}