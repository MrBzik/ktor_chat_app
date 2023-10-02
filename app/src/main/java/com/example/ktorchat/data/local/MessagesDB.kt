package com.example.ktorchat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ktorchat.data.local.entities.MessageDb
import com.example.ktorchat.data.local.entities.PendingMessage

@Database(
    entities = [MessageDb::class,
               PendingMessage::class],
    version = 3
)
abstract class MessagesDB : RoomDatabase() {
    abstract val dao : MessagesDAO
}