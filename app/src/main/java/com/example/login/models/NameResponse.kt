package com.example.login.models

import com.google.gson.annotations.SerializedName

data class NameResponse(
    @SerializedName("data") val data: UserName,
    @SerializedName("message") val message: String,
    @SerializedName("code") val code: String
)
