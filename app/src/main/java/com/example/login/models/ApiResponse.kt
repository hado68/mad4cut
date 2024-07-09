package com.example.login.models

data class Sticker(
    val id: Long,
    val url: String
)

data class StickerResponse(
    val data: Stickers,
    val message: String,
    val code: String
)

data class Stickers(
    val stickers: List<Sticker>
)