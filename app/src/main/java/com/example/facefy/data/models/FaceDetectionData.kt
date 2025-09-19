package com.example.facefy.data.models

data class FaceDetectionData(
    val facesCount: Int = 0,
    val timestamp: Long = 0L,
    val videoFrame: String? = null,
    val isStreaming: Boolean = false
)
