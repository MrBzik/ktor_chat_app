package com.example.ktorchat.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.ktorchat.data.local.entities.MessageDb
import com.example.ktorchat.data.local.entities.PendingMessage

@Dao
interface MessagesDAO {

    @Upsert
    suspend fun insertNewMessages(messages : List<MessageDb>)

    @Upsert
    suspend fun insertOneNewMessage(message : MessageDb)



    @Query("SELECT * FROM MessageDb where receiver =:receiver OR userId=:receiver ORDER BY timeStamp DESC")
    fun pagingSource(receiver : Long) : PagingSource<Int, MessageDb>

    @Query("SELECT EXISTS(SELECT * FROM MessageDb WHERE receiver =:receiver OR userId=:receiver)")
    suspend fun checkIfElementsExist(receiver: Long) : Boolean

    @Query("SELECT timeStamp FROM MessageDb where receiver =:receiver OR userId=:receiver ORDER BY timeStamp ASC LIMIT 1")
    suspend fun getOldestMessageTimestamp(receiver: Long) : Long?

    @Query("SELECT timeStamp FROM MessageDb where receiver =:receiver OR userId=:receiver ORDER BY timeStamp DESC LIMIT 1")
    suspend fun getLastMessageTimestamp(receiver: Long) : Long?


    @Insert
    suspend fun insertPendingMessage(message : PendingMessage)

    @Query("SELECT * FROM PendingMessage")
    suspend fun getPendingMessages() : List<PendingMessage>?

    @Delete
    suspend fun deletePendingMessage(message: PendingMessage)


}