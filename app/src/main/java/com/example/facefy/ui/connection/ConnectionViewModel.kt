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
    val serverConfig = repository.serverConfig
    
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
}