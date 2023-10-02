package com.example.ktorchat.data.remote.dto

import kotlinx.serialization.Serializable


@Serializable
data class TokenResponse(
    val token : String,
    val userId : Long
)
