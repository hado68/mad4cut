package com.example.login.models

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("data") val data: T,
    @SerializedName("message") val message: String,
    @SerializedName("code") val code: String
)

data class StickerInfo(
    @SerializedName("id") val id: Long,
    @SerializedName("url") val url: String
)

data class StickerListInfo(
    @SerializedName("stickers") val stickers: List<Sticker>
)

data class Sticker(
    @SerializedName("id") val id: Long,
    @SerializedName("url") val url: String,
    @SerializedName("isShared") val isShared: Boolean
)

data class SharedStickerInfo(
    @SerializedName("sticker") val sticker: Sticker
)

data class ShareStickerRequest(
    @SerializedName("stickerId") val stickerId: Long
)