package com.example.ktorchat.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PendingMessage(
    @PrimaryKey
    val timeStamp : Long,
    val receiver : Long,
    val message : String,
    val status : Int
)