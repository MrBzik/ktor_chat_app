package com.example.ktorchat.data.remote

import com.example.ktorchat.data.local.entities.MessageDb
import com.example.ktorchat.data.remote.dto.DtoResponse
import com.example.ktorchat.data.remote.requests.OutgoingRequest
import io.ktor.http.cio.websocket.Frame
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface ChatSocketService {

    val isSessionActive : MutableStateFlow<Int>

    val incomingMessages : Channel<DtoResponse.MessageDto>

    val incomingVoiceCall : Channel<DtoResponse.VoiceCallResponse>

    val incomingVoiceCallChunks : Channel<ByteArray>

    val users : MutableStateFlow<List<DtoResponse.UserInfo>>

    suspend fun initSession()

    suspend fun sendMessage(messageDb: MessageDb)

    fun observeMessages() : Flow<Frame>

    suspend fun closeSession()

    suspend fun setConnectionStateFlow(state: Int)

    suspend fun sendVoiceBufferChunks(buffer : ByteArray)

    suspend fun checkConnection() : Boolean

    suspend fun requestVoiceCall(request : OutgoingRequest.VoiceCallRequest)

    companion object {
        const val BASE_URL = "ws://192.168.1.165:8080"
    }


    sealed class Endpoints(val url : String){
        object ChatSocket : Endpoints("$BASE_URL/chat-socket")
    }



}