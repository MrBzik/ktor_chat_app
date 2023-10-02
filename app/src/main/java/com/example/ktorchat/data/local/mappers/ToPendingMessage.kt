package com.example.ktorchat.data.local.mappers

import com.example.ktorchat.data.local.entities.PendingMessage
import com.example.ktorchat.data.remote.requests.OutgoingRequest

fun OutgoingRequest.OutgoingMessage.toPendingMessage() : PendingMessage{
    return PendingMessage(
        timeStamp = timeStamp,
        receiver = receiver,
        message = message,
        status = status
    )
}

fun PendingMessage.toOutgoingMessage() : OutgoingRequest.OutgoingMessage{
    return OutgoingRequest.OutgoingMessage(
        timeStamp = timeStamp,
        receiver = receiver,
        message = message,
        status = status,
        type = "message"
    )
}