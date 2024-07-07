package com.example.login.models

data class ImageData(
    val id: Int,
    val url: String
)

data class ImagesResponse(
    val data: ImagesData,
    val message: String,
    val code: String
)

data class ImagesData(
    val images: List<ImageData>
)