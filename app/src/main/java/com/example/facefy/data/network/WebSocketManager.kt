package com.example.facefy.data.network

import com.example.facefy.data.models.ConnectionStatus
import com.example.facefy.data.models.ServerConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class WebSocketManager {
    private var webSocketClient: WebSocketClient? = null
    
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun connect(serverConfig: ServerConfig) {
        if (_connectionStatus.value == ConnectionStatus.CONNECTED) {
            return
        }
        
        _connectionStatus.value = ConnectionStatus.CONNECTING
        _errorMessage.value = null
        
        try {
            val uri = URI(serverConfig.getWebSocketUrl())
            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshake: ServerHandshake?) {
                    _connectionStatus.value = ConnectionStatus.CONNECTED
                }
                
                override fun onMessage(message: String?) {
                    
                }
                
                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    _connectionStatus.value = ConnectionStatus.DISCONNECTED
                }
                
                override fun onError(ex: Exception?) {
                    _connectionStatus.value = ConnectionStatus.ERROR
                    _errorMessage.value = ex?.message ?: "Error de conexión"
                }
            }
            
            webSocketClient?.connect()
            
        } catch (e: Exception) {
            _connectionStatus.value = ConnectionStatus.ERROR
            _errorMessage.value = e.message ?: "Error al crear conexión"
        }
    }
    
    fun disconnect() {
        webSocketClient?.close()
        webSocketClient = null
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        _errorMessage.value = null
    }
    
    fun isConnected(): Boolean {
        return _connectionStatus.value == ConnectionStatus.CONNECTED
    }
}