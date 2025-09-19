package com.example.facefy.data.network

import com.example.facefy.data.models.ConnectionStatus
import com.example.facefy.data.models.FaceDetectionData
import com.example.facefy.data.models.ServerConfig
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class WebSocketManager {
    private var webSocketClient: WebSocketClient? = null
    private val gson = Gson()
    
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _faceDetectionData = MutableStateFlow(FaceDetectionData())
    val faceDetectionData: StateFlow<FaceDetectionData> = _faceDetectionData

    private val _isStreamingActive = MutableStateFlow(false)
    val isStreamingActive: StateFlow<Boolean> = _isStreamingActive

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
                    message?.let { processMessage(it) }
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
        _isStreamingActive.value = false
    }
    
    fun isConnected(): Boolean {
        return _connectionStatus.value == ConnectionStatus.CONNECTED
    }

    fun requestVideoStream() {
        if (isConnected()) {
            _isStreamingActive.value = true
            webSocketClient?.send(gson.toJson(mapOf("action" to "start_stream")))
        }
    }

    fun stopVideoStream() {
        if (isConnected()) {
            _isStreamingActive.value = false
            webSocketClient?.send(gson.toJson(mapOf("action" to "stop_stream")))
        }
    }

    private fun processMessage(message: String) {
        try {
            val jsonMap = gson.fromJson(message, Map::class.java)
            
            when (jsonMap["num_faces"] != null) {
                true -> {
                    val facesCount = (jsonMap["num_faces"] as Double).toInt()
                    _faceDetectionData.value = FaceDetectionData(
                        facesCount = facesCount,
                        timestamp = System.currentTimeMillis(),
                        videoFrame = jsonMap["video_frame"] as? String,
                        isStreaming = _isStreamingActive.value
                    )
                }
                false -> {
                    
                }
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error procesando mensaje: ${e.message}"
        }
    }
}