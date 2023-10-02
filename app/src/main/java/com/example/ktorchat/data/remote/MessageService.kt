package com.example.ktorchat.data.remote

import com.example.ktorchat.data.remote.dto.DtoResponse


interface MessageService {


    suspend fun getNewChatMessages(lastMessage : Long, query : Long) : List<DtoResponse.MessageDto>

    suspend fun getOldChatMessages(lastMessage : Long, query: Long) : List<DtoResponse.MessageDto>

    suspend fun refreshChatMessages(query: Long) : List<DtoResponse.MessageDto>

    companion object {
        const val BASE_URL = "http://192.168.1.165:8080"
    }




    sealed class Endpoints(val url : String){
        object RefreshMessages : Endpoints("$BASE_URL/refresh_messages")
        object GetNewMessages : Endpoints("$BASE_URL/new_messages_page")
        object GetOldMessages : Endpoints("$BASE_URL/old_messages_page")
    }

}