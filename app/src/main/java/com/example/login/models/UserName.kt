package com.example.login.models

import com.google.gson.annotations.SerializedName

data class UserName(
    @SerializedName("name") val name: String,
)
