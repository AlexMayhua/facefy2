package com.example.facefy.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.facefy.R
import com.example.facefy.data.models.ConnectionStatus
import com.example.facefy.databinding.FragmentHomeBinding
import com.example.facefy.ui.connection.ConnectionViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var connectionViewModel: ConnectionViewModel
    private var cameraAlertShown = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        connectionViewModel = ViewModelProvider(requireActivity())[ConnectionViewModel::class.java]
        
        setupButtons()
        observeConnectionData()
    }
    
    private fun observeConnectionData() {
        viewLifecycleOwner.lifecycleScope.launch {
            connectionViewModel.connectionStatus.collect { status ->
                updateConnectionStatus(status)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            connectionViewModel.lastConnected.collect { timestamp ->
                updateLastConnected(timestamp)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            connectionViewModel.faceDetectionData.collect { data ->
                data?.let {
                    updateFaceDetection(it.facesCount, connectionViewModel.isCameraActive())
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            connectionViewModel.isStreamingActive.collect { isStreaming ->
                updateVideoButton(isStreaming)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                if (connectionViewModel.isConnected()) {
                    val currentData = connectionViewModel.faceDetectionData.value
                    updateFaceDetection(currentData.facesCount, connectionViewModel.isCameraActive())
                }
                kotlinx.coroutines.delay(2000)
            }
        }
    }
    
    private fun setupButtons() {
        binding.buttonConnection.setOnClickListener {
            if (connectionViewModel.isConnected()) {
                showDisconnectDialog()
            } else {
                connectionViewModel.connect()
            }
        }
        
        binding.buttonConfigure.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_connectionConfigFragment)
        }
        
        binding.buttonLiveVideo.setOnClickListener {
            if (connectionViewModel.isStreamingActive.value) {
                connectionViewModel.stopStreaming()
            } else {
                connectionViewModel.startStreaming()
                findNavController().navigate(R.id.action_nav_home_to_cameraFragment)
            }
        }
    }

    private fun updateConnectionStatus(status: ConnectionStatus) {
        when (status) {
            ConnectionStatus.CONNECTED -> {
                binding.textConnectionStatus.text = "CONECTADO"
                binding.textConnectionStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
                )
                binding.buttonConnection.text = "DESCONECTAR"
                binding.viewStatusIndicator.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
                )
                binding.layoutFaceDetection.visibility = View.VISIBLE
                binding.buttonLiveVideo.visibility = View.VISIBLE
            }
            ConnectionStatus.CONNECTING -> {
                binding.textConnectionStatus.text = "CONECTANDO..."
                binding.textConnectionStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark)
                )
                binding.buttonConnection.text = "CONECTANDO..."
                binding.viewStatusIndicator.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark)
                )
            }
            ConnectionStatus.DISCONNECTED -> {
                binding.textConnectionStatus.text = "DESCONECTADO"
                binding.textConnectionStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                )
                binding.buttonConnection.text = "CONECTAR"
                binding.viewStatusIndicator.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                )
                binding.layoutFaceDetection.visibility = View.GONE
                binding.buttonLiveVideo.visibility = View.GONE
            }
            ConnectionStatus.ERROR -> {
                binding.textConnectionStatus.text = "ERROR"
                binding.textConnectionStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                )
                binding.buttonConnection.text = "REINTENTAR"
                binding.viewStatusIndicator.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                )
            }

            ConnectionStatus.RECONNECTING -> TODO()
        }
    }
    
    private fun updateLastConnected(timestamp: Long?) {
        if (timestamp != null) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            binding.textLastConnected.text = "Última conexión: ${dateFormat.format(Date(timestamp))}"
        } else {
            binding.textLastConnected.text = "Sin conexiones previas"
        }
    }
    
    private fun updateFaceDetection(facesCount: Int, isCameraActive: Boolean) {
        if (!isCameraActive) {
            binding.textFacesCount.text = "Sin cámara"
            if (!cameraAlertShown) {
                showCameraAlert()
            }
        } else {
            binding.textFacesCount.text = facesCount.toString()
            cameraAlertShown = false
        }
    }
    
    private fun showCameraAlert() {
        if (connectionViewModel.isConnected()) {
            cameraAlertShown = true
            AlertDialog.Builder(requireContext())
                .setTitle("⚠️ Cámara Inactiva")
                .setMessage("No se detecta actividad de cámara. Asegúrese de que el cliente Python esté ejecutándose y la cámara esté conectada.")
                .setPositiveButton("Entendido") { _, _ ->
                    cameraAlertShown = false
                }
                .setOnDismissListener {
                    cameraAlertShown = false
                }
                .show()
        }
    }
    
    private fun updateVideoButton(isStreaming: Boolean) {
        binding.buttonLiveVideo.text = if (isStreaming) {
            "🔴 STREAMING ACTIVO"
        } else {
            "📹 VER VIDEO EN VIVO"
        }
    }
    
    private fun showDisconnectDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Desconectar")
            .setMessage("¿Está seguro que desea desconectarse del servidor?")
            .setPositiveButton("Sí") { _, _ ->
                connectionViewModel.disconnect()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
