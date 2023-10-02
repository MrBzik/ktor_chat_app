package com.example.ktorchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
sealed class DtoResponse {


    @Serializable
    @SerialName("message")
    data class MessageDto(
        val timestamp : Long,
        val username : String,
        val message : String,
        val userId : Long,
        val receiver : Long,
        val status : Int
    ) : DtoResponse()

    @Serializable
    @SerialName("users")
    data class UsersList(
        val users : List<UserInfo>
    ) : DtoResponse()

    @Serializable
    @SerialName("user")
    data class UserInfo(
        val isOnline : Boolean,
        val userName : String,
        val userId : Long,
        val profilePic : String?
    ) : DtoResponse()

    @Serializable
    @SerialName("voice_call")
    data class VoiceCallResponse(
        val receiver : Long,
        val status : Int
    ) : DtoResponse()


}