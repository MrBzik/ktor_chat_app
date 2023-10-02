package com.example.ktorchat.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.AsyncImage
import com.example.ktorchat.R
import com.example.ktorchat.data.remote.dto.DtoResponse
import com.example.ktorchat.presentation.chat.ChatViewModel
import com.example.ktorchat.ui.destinations.ChatScreenDestination
import com.example.ktorchat.ui.destinations.ProfileScreenDestination
import com.example.ktorchat.utils.Constants
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.collectLatest


@Composable
@Destination
fun ChatRooms(
    chatViewModel: ChatViewModel,
    navigator : DestinationsNavigator,
//    onNavigate : () -> Unit
){

    val users by chatViewModel.users.collectAsStateWithLifecycle()

    chatViewModel.onNavigateWithChatRooms(-1L, "", false)


//    LaunchedEffect(key1 = true){
//        chatViewModel.users.collect{}
//    }


    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(key1 = Unit){

        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
            chatViewModel.toolbarNavFlow.collectLatest {
                if(it == Constants.TOOLBAR_NAV_TO_PROFILE_SCREEN)
                    navigator.navigate(ProfileScreenDestination)
            }
        }
    }



    LazyColumn(modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
    ){

        items(users){user ->

            if(user.userId != chatViewModel.userId){
                DisplayChatRoom(user) {
                    chatViewModel.onNavigateWithChatRooms(user.userId, user.userName)
                    navigator.navigate(ChatScreenDestination)
                }
            }
        }

        item {

            DisplayChatRoom(userInfo = DtoResponse.UserInfo(false, userName = "Chat with everyone", -1L, null), false) {
                chatViewModel.onNavigateWithChatRooms(-1, "Chat with everyone")
                navigator.navigate(ChatScreenDestination)
//                onNavigate()
            }
        }
    }
}


@Composable
fun DisplayChatRoom(userInfo : DtoResponse.UserInfo, isRegularChatRoom : Boolean = true, onClick : () -> Unit){

    Row(modifier = Modifier
        .fillMaxWidth()
        .height(80.dp)
        .clickable {
            onClick()
        }
        .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically

    ) {

        AsyncImage(
            model = userInfo.profilePic,
            contentDescription = null,
            placeholder = painterResource(R.drawable.ic_profile_pic_placeholder),
            fallback = painterResource(R.drawable.ic_profile_pic_placeholder),
            error = painterResource(R.drawable.ic_profile_pic_placeholder),
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))


        Text(text = userInfo.userName,
            fontSize = 20.sp
        )

        Text(text = if (userInfo.isOnline) "online" else "offline",
            fontSize = 13.sp,
            color =  if(userInfo.isOnline) Color.Green else Color.Gray,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
            )

    }

    if(isRegularChatRoom)
        Divider(color = Color.LightGray, modifier = Modifier.padding(horizontal = 8.dp))

}

