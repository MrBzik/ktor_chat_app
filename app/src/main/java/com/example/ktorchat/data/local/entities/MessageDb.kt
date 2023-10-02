package com.example.ktorchat.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MessageDb(
    @PrimaryKey
    val timeStamp : Long,
    val username : String,
    val message : String,
    val userId : Long,
    val receiver : Long,
    val status : Int
)
