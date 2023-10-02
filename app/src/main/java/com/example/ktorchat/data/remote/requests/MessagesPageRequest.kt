package com.example.ktorchat.data.remote.requests

import kotlinx.serialization.Serializable

@Serializable
data class MessagesPageRequest(
    val receiver : Long,
    val lastMessage : Long
)
