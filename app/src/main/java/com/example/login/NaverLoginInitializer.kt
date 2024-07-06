package com.example.login

import android.content.Context
import com.navercorp.nid.NaverIdLoginSDK

object NaverLoginInitializer {
    fun initialize(context: Context) {
        NaverIdLoginSDK.initialize(context, "5ffKa76jEfDNau0i6tC1", "nIUD8ozS8L", "몰입네컷")
    }
}