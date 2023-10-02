package com.example.ktorchat.data.remote

import android.content.SharedPreferences
import com.example.ktorchat.data.remote.dto.DtoResponse
import com.example.ktorchat.data.remote.requests.MessagesPageRequest

import com.example.ktorchat.utils.Constants
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType

class MessageServiceImpl(
    private val client : HttpClient,
    private val authPref : SharedPreferences
    ) : MessageService {


    override suspend fun refreshChatMessages(query: Long): List<DtoResponse.MessageDto> {
        return try {

            val token = authPref.getString(Constants.AUTH_TOKEN, "no token")

            client.get{
                url(MessageService.Endpoints.RefreshMessages.url)
                header("Authorization", "Bearer $token")
                parameter("receiver", query.toString())
            }

        } catch (e : Exception){
            emptyList()
        }
    }

    override suspend fun getNewChatMessages(lastMessage: Long, query: Long): List<DtoResponse.MessageDto> {

        return try {

            val token = authPref.getString(Constants.AUTH_TOKEN, "no token")
            val request = MessagesPageRequest(
                receiver = query,
                lastMessage = lastMessage
            )

            client.get{
                url(MessageService.Endpoints.GetNewMessages.url)
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                body = request


            }

        } catch (e : Exception){
            emptyList()
        }
    }

    override suspend fun getOldChatMessages(lastMessage: Long, query: Long): List<DtoResponse.MessageDto> {
        return try {

            val token = authPref.getString(Constants.AUTH_TOKEN, "no token")

            val request = MessagesPageRequest(
                receiver = query,
                lastMessage = lastMessage
            )

            client.get{
                url(MessageService.Endpoints.GetOldMessages.url)
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                body = request
            }

        } catch (e : Exception){
            emptyList()
        }
    }
}