package com.example.ktorchat.data.remote

import android.content.SharedPreferences
import android.util.Log
import com.example.ktorchat.data.local.MessagesDB
import com.example.ktorchat.data.local.entities.MessageDb
import com.example.ktorchat.data.local.mappers.toOutgoingMessage
import com.example.ktorchat.data.local.mappers.toPendingMessage
import com.example.ktorchat.data.remote.dto.DtoResponse
import com.example.ktorchat.data.remote.requests.OutgoingRequest
import com.example.ktorchat.utils.Constants
import com.example.ktorchat.utils.Constants.CONNECTION_GOOD
import com.example.ktorchat.utils.Constants.CONNECTION_HOLD
import com.example.ktorchat.utils.Constants.CONNECTION_LOST
import com.example.ktorchat.utils.Constants.PENDING_MESSAGES
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.Exception

class ChatSocketServiceImpl(
    private val client: HttpClient,
    private val authPref : SharedPreferences,
    private val pendingPref : SharedPreferences,
    private val db : MessagesDB
    ) : ChatSocketService {


    private val json = Json {
        classDiscriminator = "type"
    }


    private var socket : WebSocketSession? = null

    override val isSessionActive = MutableStateFlow(CONNECTION_HOLD)

    override val users: MutableStateFlow<List<DtoResponse.UserInfo>> = MutableStateFlow(emptyList())

    override val incomingMessages: Channel<DtoResponse.MessageDto> = Channel()

    override val incomingVoiceCall: Channel<DtoResponse.VoiceCallResponse> = Channel()

    override val incomingVoiceCallChunks: Channel<ByteArray> = Channel()

    override suspend fun setConnectionStateFlow(state: Int) {
        isSessionActive.value = state
    }

    override suspend fun initSession() {



        Log.d("CHECKTAGS", "START_TIME:${System.currentTimeMillis()}")

        val token = authPref.getString(Constants.AUTH_TOKEN, "no token")

         try {
            socket = client.webSocketSession {
                url(ChatSocketService.Endpoints.ChatSocket.url)
                header("Authorization", "Bearer $token")
            }

            if(socket?.isActive == true) {

//                incomingFramesFlow = socket!!.incoming.consumeAsFlow()

                isSessionActive.value = CONNECTION_GOOD

                checkForPendingMessages()

                socket?.incoming?.consumeEach { frame ->



                    try {
                        if(frame is Frame.Text) {



                            val text = frame.readText()

                            Log.d("CHECKTAGS", "got message: $text")

                            val json = Json.decodeFromString<DtoResponse>(text)


                            when (json) {
                                is DtoResponse.MessageDto -> {

                                    incomingMessages.send(json)

                                }

                                is DtoResponse.VoiceCallResponse -> {

                                    incomingVoiceCall.send(json)

                                }

                                is DtoResponse.UsersList -> {

                                    this.users.value = json.users

                                }

                                is DtoResponse.UserInfo -> {
                                    Log.d("CHECKTAGS", "got single user")
                                }
                            }

                        } else if(frame is Frame.Binary){

                            Log.d("CHECKTAGS", "got chunk")

                            incomingVoiceCallChunks.send(frame.data)

                        }

                    } catch (e : Exception){
                        Log.d("CHECKTAGS", e.stackTraceToString())
                    }
                }

                socket?.coroutineContext?.job?.invokeOnCompletion {
                    if(isSessionActive.value == CONNECTION_GOOD)
                        isSessionActive.value = CONNECTION_LOST
                }
            }

        } catch (e : Exception){
             Log.d("CHECKTAGS", "ENDIN_TIME:${System.currentTimeMillis()}")
             isSessionActive.value = CONNECTION_LOST
        }
    }

    override suspend fun checkConnection(): Boolean {
        return socket?.isActive == true
    }





    override suspend fun requestVoiceCall(request: OutgoingRequest.VoiceCallRequest) {
        try {

            val jsonRequest = json.encodeToString(request)

//            val jsonRequest = Json.encodeToString<OutgoingRequest>(request)
            Log.d("CHECKTAGS", "request: $jsonRequest")
            socket?.send(Frame.Text(jsonRequest))
        } catch (e : java.lang.Exception){
            Log.d("CHECKTAGS", e.stackTraceToString())
        }

    }

    override suspend fun sendVoiceBufferChunks(buffer: ByteArray) {
        try {
            Log.d("CHECKTAGS", "sending chunk : ${buffer.size}")
            socket?.send(Frame.Binary(true, buffer))
        } catch (e : Exception){
            Log.d("CHECKTAGS", e.stackTraceToString())
        }

    }

    private suspend fun checkForPendingMessages(){

        Log.d("CHECKTAGS", "!!!!!!!!!!CALLING CHECK!!!!!!!!!!!!!")

        if(pendingPref.getBoolean(PENDING_MESSAGES, false)){

            db.dao.getPendingMessages()?.let { pendingMessages ->

                pendingMessages.forEach { message ->
                    socket?.let {
                        val jsonMessage = Json.encodeToString(
                            message.toOutgoingMessage()
                        )
                        Log.d("CHECKTAGS", "time on resend: ${message.timeStamp}")
                        it.send(Frame.Text(jsonMessage))
                        db.dao.deletePendingMessage(message)
                    }
                }
            }

            if(socket?.isActive == true)
                pendingPref.edit().putBoolean(PENDING_MESSAGES, false).apply()
        }
    }



    override suspend fun sendMessage(messageDb: MessageDb)  {

        db.dao.insertOneNewMessage(messageDb)

        val outgoingMessage = OutgoingRequest.OutgoingMessage(
            message = messageDb.message,
            receiver = messageDb.receiver,
            timeStamp = messageDb.timeStamp,
            status = messageDb.status,
            type = "message"
        )

        val jsonMessage = json.encodeToString(outgoingMessage)

        Log.d("CHECKTAGS", "sending message: $jsonMessage")

        if(socket?.isActive == true){
            socket?.send(Frame.Text(jsonMessage))
        } else {
            db.dao.insertPendingMessage(outgoingMessage.toPendingMessage())
            pendingPref.edit().putBoolean(PENDING_MESSAGES, true).apply()
        }


    }

    override fun observeMessages(): Flow<Frame> {


        return socket!!.incoming.receiveAsFlow()
//            .filter {
//            it is Frame.Text
//        }
//            .map {
//            Json.decodeFromString<Message>((it as Frame.Text)
//                .readText()).toMessagePresentation()
//        }
    }

    override suspend fun closeSession() {
        socket?.close()
    }
}