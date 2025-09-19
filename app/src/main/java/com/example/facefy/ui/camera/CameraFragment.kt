package com.example.facefy.ui.camera

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.facefy.R
import com.example.facefy.ui.connection.ConnectionViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CameraFragment : Fragment() {

    private lateinit var connectionViewModel: ConnectionViewModel
    private lateinit var videoImageView: ImageView
    private lateinit var stopStreamingButton: Button
    private lateinit var noCameraText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        connectionViewModel = ViewModelProvider(requireActivity())[ConnectionViewModel::class.java]
        
        videoImageView = view.findViewById(R.id.video_image_view)
        stopStreamingButton = view.findViewById(R.id.stop_streaming_button)
        noCameraText = view.findViewById(R.id.no_camera_text)
        
        setupClickListeners()
        observeDetectionData()
        startCameraStatusCheck()
    }

    private fun setupClickListeners() {
        stopStreamingButton.setOnClickListener {
            connectionViewModel.stopStreaming()
            findNavController().navigateUp()
        }
    }

    private fun observeDetectionData() {
        viewLifecycleOwner.lifecycleScope.launch {
            connectionViewModel.faceDetectionData.collect { data ->
                if (connectionViewModel.isCameraActive()) {
                    data.videoFrame?.let { frameData ->
                        updateVideoFrame(frameData)
                        showVideoView()
                    }
                } else {
                    showNoCameraMessage()
                }
            }
        }
    }
    
    private fun showVideoView() {
        videoImageView.visibility = View.VISIBLE
        noCameraText.visibility = View.GONE
    }
    
    private fun showNoCameraMessage() {
        videoImageView.visibility = View.GONE
        noCameraText.visibility = View.VISIBLE
        noCameraText.text = "📷 Sin señal de cámara\n\nAsegúrese de que el cliente Python esté ejecutándose"
    }
    
    private fun startCameraStatusCheck() {
        viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                if (!connectionViewModel.isCameraActive()) {
                    showNoCameraMessage()
                }
                kotlinx.coroutines.delay(2000)
            }
        }
    }

    private fun updateVideoFrame(frameData: String) {
        try {
            val imageBytes = Base64.decode(frameData, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            bitmap?.let {
                videoImageView.setImageBitmap(it)
            }
        } catch (e: Exception) {
            
        }
    }
}