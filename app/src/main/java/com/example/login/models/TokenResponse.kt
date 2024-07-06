package com.example.login.models

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("data") val data: ResponseData,
    @SerializedName("message") val message: String,
    @SerializedName("code") val code: String
)
