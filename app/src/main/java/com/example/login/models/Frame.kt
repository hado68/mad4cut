package com.example.login.models

data class FrameData(
    val id: Long,
    val url: String
)

data class FrameResponse(
    val data: Framesdata,
    val message: String,
    val code: String
)

data class Framesdata(
    val frames: List<FrameData>
)