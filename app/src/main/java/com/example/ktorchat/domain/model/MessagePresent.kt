package com.example.ktorchat.domain.model

sealed class MessagePresent {

    data class Message(
        val timeStamp : Long,
        val username : String,
        val date : String,
        val time : String,
        val message : String,
        val userId : Long,
        val receiver : Long,
        val status : Int
    ) : MessagePresent()

    data class DateSeparator(
        val date : String
    ) : MessagePresent()


}
