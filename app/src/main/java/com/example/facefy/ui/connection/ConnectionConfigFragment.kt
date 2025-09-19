package com.example.facefy.ui.connection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.facefy.R
import com.example.facefy.databinding.FragmentConnectionConfigBinding
import kotlinx.coroutines.launch

class ConnectionConfigFragment : Fragment() {
    private var _binding: FragmentConnectionConfigBinding? = null
    private val binding get() = _binding!!
    
    private val connectionViewModel: ConnectionViewModel by activityViewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectionConfigBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupButtons()
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            connectionViewModel.serverConfig.collect { config ->
                binding.editTextHost.setText(config.host)
                binding.editTextPort.setText(config.port.toString())
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            connectionViewModel.connectionStatus.collect { status ->
                when (status) {
                    com.example.facefy.data.models.ConnectionStatus.CONNECTED -> {
                        Toast.makeText(context, "Conexión exitosa", Toast.LENGTH_SHORT).show()
                    }
                    com.example.facefy.data.models.ConnectionStatus.ERROR -> {
                        Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun setupButtons() {
        binding.buttonSave.setOnClickListener {
            saveConfiguration()
        }
        
        binding.buttonCancel.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.buttonTestConnection.setOnClickListener {
            testConnection()
        }
    }
    
    private fun saveConfiguration() {
        val host = binding.editTextHost.text.toString().trim()
        val portText = binding.editTextPort.text.toString().trim()
        
        if (host.isEmpty()) {
            Toast.makeText(context, "Host no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }
        
        val port = portText.toIntOrNull()
        if (port == null || port < 1 || port > 65535) {
            Toast.makeText(context, "Puerto debe ser un número entre 1 y 65535", Toast.LENGTH_SHORT).show()
            return
        }
        
        connectionViewModel.updateServerConfig(host, port)
        Toast.makeText(context, "Configuración guardada", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }
    
    private fun testConnection() {
        val host = binding.editTextHost.text.toString().trim()
        val portText = binding.editTextPort.text.toString().trim()
        
        if (host.isEmpty() || portText.isEmpty()) {
            Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }
        
        val port = portText.toIntOrNull()
        if (port == null) {
            Toast.makeText(context, "Puerto inválido", Toast.LENGTH_SHORT).show()
            return
        }
        
        connectionViewModel.updateServerConfig(host, port)
        connectionViewModel.connect()
        
        Toast.makeText(context, "Probando conexión...", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}