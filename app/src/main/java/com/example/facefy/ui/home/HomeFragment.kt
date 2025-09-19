package com.example.facefy.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.facefy.R
import com.example.facefy.data.models.ConnectionStatus
import com.example.facefy.databinding.FragmentHomeBinding
import com.example.facefy.ui.connection.ConnectionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val connectionViewModel: ConnectionViewModel by activityViewModels()

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
        
        setupObservers()
        setupButtons()
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            connectionViewModel.connectionData.collect { data ->
                updateConnectionStatus(data.status)
                updateLastConnected(data.lastConnected)
                data.errorMessage?.let { 
                    binding.textError.text = it
                    binding.textError.visibility = View.VISIBLE
                } ?: run {
                    binding.textError.visibility = View.GONE
                }
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
            else -> {}
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