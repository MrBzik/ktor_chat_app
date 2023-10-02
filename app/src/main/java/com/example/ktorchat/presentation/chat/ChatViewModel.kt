package com.example.ktorchat.presentation.chat

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.NoiseSuppressor
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.example.ktorchat.dagger.REGULAR
import com.example.ktorchat.data.local.MessagesDB
import com.example.ktorchat.data.local.entities.MessageDb
import com.example.ktorchat.data.local.mappers.toMessageDb
import com.example.ktorchat.data.local.mappers.toMessagePresent
import com.example.ktorchat.data.remote.ChatSocketService
import com.example.ktorchat.data.remote.MessageService
import com.example.ktorchat.data.remote.MessagesRemoteMediator
import com.example.ktorchat.data.remote.requests.OutgoingRequest
import com.example.ktorchat.domain.model.MessagePresent
import com.example.ktorchat.utils.Constants
import com.example.ktorchat.utils.Constants.CONNECTION_HOLD
import com.example.ktorchat.utils.Constants.CONNECTION_INITIATE
import com.example.ktorchat.utils.Constants.CONNECTION_LOST
import com.example.ktorchat.utils.Constants.MESSAGE_STATUS_READ
import com.example.ktorchat.utils.Constants.PAGE_SIZE
import com.example.ktorchat.utils.Constants.VOICE_CALL_ACCEPT
import com.example.ktorchat.utils.Constants.VOICE_CALL_DECLINE
import com.example.ktorchat.utils.Constants.VOICE_CALL_REQUEST
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named


const val sampleRate = 48000
const val channelConfig = AudioFormat.CHANNEL_IN_MONO
const val audioFormat = AudioFormat.ENCODING_PCM_16BIT
const val sessionId = 0

@SuppressLint("MissingPermission")
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageService : MessageService,
    private val chatSocketService : ChatSocketService,
    @Named(REGULAR) private val authPref : SharedPreferences,
    private val db : MessagesDB
) : ViewModel() {

    val userId by lazy {
        authPref.getLong(Constants.USER_ID, -1)
    }

    val userName by lazy {
        authPref.getString(Constants.USER_NAME, "") ?: ""
    }

    private val _isInVoiceCall = mutableStateOf(false)

    val isInVoiceCall = _isInVoiceCall

    lateinit var aec : AcousticEchoCanceler
    lateinit var nc : NoiseSuppressor


    private val audioTrack by lazy {


        AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .build()
            )
            .setAudioFormat(AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
            ).apply {
                if(Build.VERSION.SDK_INT >= 26){
                    setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                }
            }
            .build()
    }





    private val audioRecord by lazy {


        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        Log.d("CHECKTAGS", "calculated buffer size: $minBufferSize")
        AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, minBufferSize * 10)


    }


    private fun setAudioEffects(){

//        nc = NoiseSuppressor.create(audioRecord.audioSessionId)
//
//        nc.enabled = true

        aec = AcousticEchoCanceler.create(audioRecord.audioSessionId)

        aec.enabled = true

    }


    private val _messageText = mutableStateOf("")
    val messageText = _messageText

    val users = chatSocketService.users.asStateFlow()


    val isConnected = chatSocketService.isSessionActive.asStateFlow()

    var receiver : Long = -1
    var receiverName = ""

    private val _isInChatScreen = MutableStateFlow(false)
    val isInChatScreen = _isInChatScreen.asStateFlow()

    private val toolbarNavChannel = Channel<Int>()

    val toolbarNavFlow = toolbarNavChannel.receiveAsFlow()

    fun toolbarNavigation(command : Int){
        viewModelScope.launch {
            toolbarNavChannel.send(command)
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getChatRoomMessages() : Flow<PagingData<MessagePresent>> {

        Log.d("CHECKTAGS", "returning paging data flow")
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE
            ),
            remoteMediator = MessagesRemoteMediator(db, messageService, receiver)
        ) {
            db.dao.pagingSource(receiver)
        }.flow
            .map {pagingData ->
                pagingData.map {
                    it.toMessagePresent()
                }
            }
            .map {
                it.insertSeparators { first: MessagePresent?, second: MessagePresent? ->

                    if(first == null)
                        return@insertSeparators null

                    if(second == null)
                        return@insertSeparators MessagePresent.DateSeparator((first as MessagePresent.Message).date)

                    val firstMessage = (first as MessagePresent.Message)
                    val secondMessage = (second as MessagePresent.Message)


                    return@insertSeparators if(firstMessage.date != secondMessage.date)
                        MessagePresent.DateSeparator(first.date)
                    else null
                }
            }
            .cachedIn(viewModelScope)

    }


    fun onNavigateWithChatRooms(receiverId : Long, receiverName : String, isChatScreen : Boolean = true){

        receiver = receiverId
        this.receiverName = receiverName
        _isInChatScreen.value = isChatScreen

    }


    fun initiateNewConnectionState() = viewModelScope.launch{
        if(!chatSocketService.checkConnection())
            chatSocketService.setConnectionStateFlow(CONNECTION_INITIATE)
    }


    fun updateMessageText(text : String){
        _messageText.value = text
    }



    init {

        viewModelScope.launch {

            chatSocketService.incomingMessages.consumeEach { message ->

                Log.d("CHECKTAGS", "is sent? : ${message.status}")

                db.dao.insertOneNewMessage(message = message.toMessageDb())

            }
        }


        viewModelScope.launch {

            chatSocketService.incomingVoiceCall.consumeEach { request ->

                Log.d("CHECKTAGS", "getting response : ${request.status}")

                when(request.status){

                    VOICE_CALL_ACCEPT -> {

                        delay(1000)
                        startVoiceCall()

                    }

                    VOICE_CALL_REQUEST -> {

                        val response = OutgoingRequest.VoiceCallRequest(
                            receiver = receiver,
                            status = VOICE_CALL_ACCEPT,
                            type = "voice_call"
                        )
                        chatSocketService.requestVoiceCall(response)

                        delay(2000)

                        startVoiceCall()

                    }


                    VOICE_CALL_DECLINE -> {

                        stopVoiceCall()

                    }
                }
            }
        }


        viewModelScope.launch(Dispatchers.Default) {

            chatSocketService.incomingVoiceCallChunks.consumeEach { bytes ->

                Log.d("CHECKTAGS", "GETTING BYTES????")

                handleVoiceCallChunks(bytes)

            }

        }


        isConnected.mapLatest {

            Log.d("CHECKTAGS", "isConnected: $it")

            if(it == CONNECTION_INITIATE) {
                connectToServer()

            } else if(it == CONNECTION_LOST){

                while (isConnected.value == CONNECTION_LOST){

                    connectToServer()
                    delay(6500)
                    Log.d("CHECKTAGS", "looping")
                }
            } else {

            }

        }.launchIn(viewModelScope)

    }

    private fun connectToServer() = viewModelScope.launch {

        Log.d("CHECKTAGS", "calling connect to server")

        chatSocketService.initSession()

    }


    private fun testVoiceCall(){

        startVoiceCall()

    }


    fun requestVoiceCall() = viewModelScope.launch {

        testVoiceCall()

//        Log.d("CHECKTAGS", "requesting voice call")
//
//        val request = OutgoingRequest.VoiceCallRequest(
//            receiver = receiver,
//            status = VOICE_CALL_REQUEST,
//            type = "voice_call"
//        )
//
//        chatSocketService.requestVoiceCall(request)

    }


    fun requestStopVoiceCall() = viewModelScope.launch {

        stopVoiceCall()

//        val request = OutgoingRequest.VoiceCallRequest(
//            receiver = receiver,
//            status = VOICE_CALL_DECLINE,
//            type = "voice_call"
//        )
//
//        chatSocketService.requestVoiceCall(request)

    }


    private fun stopVoiceCall(){
        _isInVoiceCall.value = false
        audioRecord.stop()
        audioTrack.stop()
    }


    private fun startVoiceCall() = viewModelScope.launch(Dispatchers.Default) {
        _isInVoiceCall.value = true

        val buffer = ByteArray(8192)

        audioRecord.startRecording()
        audioTrack.play()

        setAudioEffects()

        while (_isInVoiceCall.value){

            audioRecord.read(buffer, 0, buffer.size)

            chatSocketService.sendVoiceBufferChunks(buffer)

        }
    }


    private fun handleVoiceCallChunks(buffer : ByteArray){

        if(_isInVoiceCall.value)
            audioTrack.write(buffer, 0, buffer.size)

    }



    fun sendNewMessage() = viewModelScope.launch {

        if(_messageText.value.isNotBlank()){

            val messageDb = MessageDb(
                timeStamp = System.currentTimeMillis(),
                username = authPref.getString(Constants.USER_NAME, "") ?: "",
                message = _messageText.value,
                userId = authPref.getLong(Constants.USER_ID, -1L),
                receiver = receiver,
                status = Constants.MESSAGE_STATUS_UNSENT
            )
            _messageText.value = ""
            chatSocketService.sendMessage(messageDb)
        }
    }

    fun updateMessageStatus(message : MessagePresent.Message) = viewModelScope.launch {

         val messageDb = message.copy(status = MESSAGE_STATUS_READ).toMessageDb()

        chatSocketService.sendMessage(messageDb)

    }




    fun disconnect() = viewModelScope.launch {
        Log.d("CHECKTAGS", "am i stopping it?")
//        chatSocketService.setConnectionStateFlow(CONNECTION_HOLD)
//        chatSocketService.closeSession()
    }


    override fun onCleared() {
        super.onCleared()
        disconnect()
        audioTrack.stop()
        audioTrack.release()
        audioRecord.stop()
        audioRecord.release()
    }

}