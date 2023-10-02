package com.example.ktorchat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.ktorchat.domain.model.MessagePresent
import com.example.ktorchat.presentation.chat.ChatViewModel
import com.example.ktorchat.utils.Constants.MESSAGE_STATUS_NOT_READ
import com.example.ktorchat.utils.Constants.MESSAGE_STATUS_READ
import com.example.ktorchat.utils.Constants.MESSAGE_STATUS_UNSENT
import com.example.ktorchat.utils.Constants.TOOLBAR_NAV_FROM_CHAT_SCREEN
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.collectLatest
import java.text.DateFormat
import java.util.Date


@Composable
@Destination
fun ChatScreen(
    chatViewModel: ChatViewModel,
    navigator : DestinationsNavigator
    ){


    val messages = chatViewModel.getChatRoomMessages().collectAsLazyPagingItems()

    val messageText = chatViewModel.messageText
    val lifecycleOwner = LocalLifecycleOwner.current

    val voiceCallStatus = chatViewModel.isInVoiceCall


//
//    val lazyListState = rememberLazyListState(
//        initialFirstVisibleItemIndex = 0
//    )

    LaunchedEffect(Unit){

        snapshotFlow {
            messages.itemCount
        }.collectLatest {size ->

            if(size >= 0){

                for(i in 0 until size){
                    val message = messages[i]

                    if(message is MessagePresent.Message){

                        if(message.userId == chatViewModel.userId)
                            break

                        if(message.status == MESSAGE_STATUS_READ)
                            break

                        chatViewModel.updateMessageStatus(message)

                    }
                }
            }
        }
    }




    LaunchedEffect(key1 = Unit){

        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
            chatViewModel.toolbarNavFlow.collectLatest {
                if(it == TOOLBAR_NAV_FROM_CHAT_SCREEN)
                    navigator.popBackStack()
            }
        }
    }




    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        LazyColumn(modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
            reverseLayout = true,
//            state = lazyListState
        ){

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            items(count = messages.itemCount){index ->

                messages[index]?.let {message ->

                    if(message is MessagePresent.Message)
                        ChatMessage(message = message, userId = chatViewModel.userId)
                    else
                        DrawDate(date = (message as MessagePresent.DateSeparator).date)

                    Spacer(modifier = Modifier.height(32.dp))

                }
            }
        }


        TextField(messageText = messageText,
            isInVoiceCall = voiceCallStatus,
            onValueChanged = chatViewModel::updateMessageText,
            onSendMessage = chatViewModel::sendNewMessage,
            onVoiceCall = chatViewModel::requestVoiceCall,
            onStopVoiceCall = chatViewModel::requestStopVoiceCall
            )
    }
}


@Composable
fun DrawDate(date : String){


    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {


        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier
                .height(50.dp)
                .padding(vertical = 8.dp, horizontal = 16.dp),
            shape = CircleShape,
            shadowElevation = 10.dp,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .background(Color.White)) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    text = date)
            }
        }


        Spacer(modifier = Modifier.height(10.dp))


    }




//    Box(modifier = Modifier
//        .fillMaxWidth()
//        .height(50.dp)
//        .background(color = Color.White, shape = RoundedCornerShape(10.dp))
//        .shadow(10.dp)
//        ,
//        contentAlignment = Alignment.Center,
//
//    ) {
//        Text(text = date,
//            fontStyle = FontStyle.Italic
//        )
//    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextField(
    messageText : MutableState<String>,
    isInVoiceCall : MutableState<Boolean>,
    onValueChanged : (String) -> Unit,
    onSendMessage : () -> Unit,
    onVoiceCall : () -> Unit,
    onStopVoiceCall : () -> Unit
){


    Row(modifier = Modifier.fillMaxWidth()) {

        TextField(
            modifier = Modifier.weight(1f),
            value = messageText.value,
            onValueChange = {
                onValueChanged(it)
            },
            placeholder = {
                Text(text = "Type your message")
            }
        )

        IconButton(onClick = onSendMessage) {

            Icon(imageVector = Icons.Default.Send, contentDescription = "Send")

        }

        if(!isInVoiceCall.value){
            IconButton(onClick = onVoiceCall) {

                Icon(imageVector = Icons.Default.Call, contentDescription = "Call")

            }
        } else {

            IconButton(onClick = onStopVoiceCall) {

                Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = Color.Red)

            }

        }


    }
}

@Composable
@Preview
fun MessagePreview(){

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
    ) {

        val time = System.currentTimeMillis()

        val date = Date(time)
        val formatTime = DateFormat.getTimeInstance()
        val formatDate = DateFormat.getDateInstance()

        val dateString = formatDate.format(date)
        val timeString = formatTime.format(date)


        DrawDate(date = dateString)


        ChatMessage(message = MessagePresent.Message(
            username = "Batman",
            date = dateString,
            time = timeString,
            message = "Hi, my name is Bruce. I am batman. I wear leather pants and underwear",
            userId = 92,
            receiver = 45,
            status = 2,
            timeStamp = 0
        ), userId = 92)

    }
}


@Composable
fun ChatMessage(message : MessagePresent.Message, userId: Long){

    val isOwn = message.userId == userId

    BoxWithConstraints(contentAlignment = if(isOwn) Alignment.CenterEnd else Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),

        ) {

        Column(
            modifier = Modifier
//                .widthIn(min = if (isOwn) 100.dp else 0.dp, max = maxWidth * 0.8f)
                .width(IntrinsicSize.Max)
                .widthIn(max = maxWidth * 0.8f)

                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(10.dp)
                )
//                .drawBehind {
//                    val cornerRadius = 10.dp.toPx()
//                    val triangleHeight = 20.dp.toPx()
//                    val triangleWidth = 25.dp.toPx()
//                    val trianglePath = Path().apply {
//                        if (isOwn) {
//                            moveTo(size.width, size.height - cornerRadius)
//                            lineTo(size.width, size.height + triangleHeight)
//                            lineTo(
//                                size.width - triangleWidth, size.height - cornerRadius
//                            )
//                            close()
//                        } else {
//                            moveTo(0f, size.height - cornerRadius)
//                            lineTo(0f, size.height + triangleHeight)
//                            lineTo(
//                                triangleWidth, size.height - cornerRadius
//                            )
//                            close()
//                        }
//                    }
//
//                    drawPath(
//                        path = trianglePath,
//                        color = if (isOwn) Color.Black else Color.LightGray
//                    )
//                }
                .background(
                    color = if (isOwn) Color.White else Color.White,
                    shape = RoundedCornerShape(10.dp)
                )
//                .border(
//                    width = 1.dp,
//                    color = Color.LightGray,
//                    shape = RoundedCornerShape(10.dp)
//                )
                .padding(8.dp)

        ) {

            if(message.receiver == -1L)
                Text(text = message.username,
                fontWeight = FontWeight.Bold,
                color = Color.Black
                )


            Text(text = message.message,
                color = Color.Black
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                if(isOwn){
                    Box(){
                        when(message.status){
                            MESSAGE_STATUS_UNSENT ->
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "not sent")
                            MESSAGE_STATUS_NOT_READ ->
                                Icon(imageVector = Icons.Default.Done, contentDescription = "sent")
                            MESSAGE_STATUS_READ -> {
                                Row() {
                                    Icon(imageVector = Icons.Default.Done, contentDescription = "")
                                    Icon(imageVector = Icons.Default.Done,
                                        contentDescription = "read",
                                        modifier = Modifier.offset(x = -15.dp, y = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Text(text = message.time,
                    fontSize = 10.sp,
                    color = Color.Blue,
                    textAlign = TextAlign.End,

                    )
            }
        }
    }


}