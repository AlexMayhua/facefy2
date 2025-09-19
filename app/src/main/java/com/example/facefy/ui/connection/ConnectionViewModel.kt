package com.example.facefy.ui.connection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.facefy.data.repository.ConnectionRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class ConnectionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ConnectionRepository(application)
    
    val connectionData = repository.connectionData
    val connectionStatus = repository.connectionData.map { it.status }
    val lastConnected = repository.connectionData.map { it.lastConnected }
    val serverConfig = repository.serverConfig
    val faceDetectionData = repository.faceDetectionData
    val isStreamingActive = repository.isStreamingActive
    
    fun connect() {
        repository.connect()
    }
    
    fun disconnect() {
        repository.disconnect()
    }
    
    fun updateServerConfig(host: String, port: Int) {
        repository.updateServerConfig(host, port)
    }
    
    fun isConnected(): Boolean {
        return repository.isConnected()
    }
    
    fun startVideoStream() {
        repository.startVideoStream()
    }
    
    fun stopVideoStream() {
        repository.stopVideoStream()
    }
    
    fun startStreaming() {
        repository.startVideoStream()
    }
    
    fun stopStreaming() {
        repository.stopVideoStream()
    }
}