package com.example.ktorchat.data.local.mappers

import com.example.ktorchat.data.local.entities.MessageDb
import com.example.ktorchat.data.remote.dto.DtoResponse
import com.example.ktorchat.domain.model.MessagePresent
import java.text.DateFormat
import java.util.Date


fun DtoResponse.MessageDto.toMessageDb() : MessageDb {
    return MessageDb(
        username = username,
        timeStamp = timestamp,
        message = message,
        userId = userId,
        receiver = receiver,
        status = status
    )
}

fun MessageDb.toMessagePresent() : MessagePresent {

    val date = Date(timeStamp)
    val formatDate = DateFormat.getDateInstance()
    val formatTime = DateFormat.getTimeInstance()

    val dateString = formatDate.format(date)
    val timeString = formatTime.format(date)

    return MessagePresent.Message(
        username = username,
        date = dateString,
        time = timeString,
        message = message,
        userId = userId,
        receiver = receiver,
        status = status,
        timeStamp = timeStamp
    )
}


fun MessagePresent.Message.toMessageDb() : MessageDb{
    return MessageDb(
        username = username,
        timeStamp = timeStamp,
        message = message,
        userId = userId,
        receiver = receiver,
        status = status
    )
}