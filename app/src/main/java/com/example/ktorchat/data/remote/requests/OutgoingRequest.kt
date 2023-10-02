package com.example.ktorchat.data.remote.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class OutgoingRequest (

        ) {

    abstract val type: String

    @Serializable
    @SerialName("voice_call")
    data class VoiceCallRequest(
        override val type: String,
        val receiver : Long,
        val status : Int
    ) : OutgoingRequest()

    @Serializable
    @SerialName("message")
    data class OutgoingMessage(
        override val type: String,
        val receiver : Long,
        val message : String,
        val timeStamp : Long,
        val status : Int
    ) : OutgoingRequest()
}

