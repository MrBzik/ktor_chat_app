package com.example.ktorchat

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.example.ktorchat.presentation.chat.ChatViewModel
import com.example.ktorchat.ui.NavGraphs

import com.example.ktorchat.ui.theme.KtorchatTheme
import com.example.ktorchat.utils.Constants
import com.example.ktorchat.utils.Constants.TOOLBAR_NAV_FROM_CHAT_SCREEN
import com.example.ktorchat.utils.Constants.TOOLBAR_NAV_TO_PROFILE_SCREEN
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            KtorchatTheme {


                val chatViewModel : ChatViewModel = hiltViewModel()

//                val navController = rememberNavController()

                val lifecycleOwner = LocalLifecycleOwner.current

                val isConnected by chatViewModel.isConnected.collectAsStateWithLifecycle()

                val isInChatScreen by chatViewModel.isInChatScreen.collectAsStateWithLifecycle()

                DisposableEffect(key1 = lifecycleOwner){
                    val lifecycleObserver = LifecycleEventObserver { _, event ->
                        if(event == Lifecycle.Event.ON_START){
                            Log.d("CHECKTAGS", "calling viewmodel in ChatRooms")
                            chatViewModel.initiateNewConnectionState()
                        }

                        else if(event == Lifecycle.Event.ON_STOP)
                            chatViewModel.disconnect()
                    }
                    lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
                    }
                }



                Column(modifier = Modifier.fillMaxSize()) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(Color.LightGray)
                    ) {

                        Row(modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 16.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                            ) {


                            Text(text = "Profile", modifier = Modifier
                                .padding(16.dp)
                                .clickable { chatViewModel.toolbarNavigation(
                                    TOOLBAR_NAV_TO_PROFILE_SCREEN) }
                            )


                            if(isConnected == Constants.CONNECTION_GOOD)
                                Image(
                                    painter = painterResource(id = R.drawable.ic_connected), contentDescription = "Connected"
                                )
                            else
                                Image(painter = painterResource(id = R.drawable.ic_no_connection), contentDescription = "Not connected")
                        }


                        if(isInChatScreen){
                            
                            Row(modifier = Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
                                
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "back",
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .clickable(onClick = {
                                            chatViewModel.toolbarNavigation(TOOLBAR_NAV_FROM_CHAT_SCREEN)
                                        })
                                )
                                
                                Text(text = chatViewModel.receiverName)
                                
                            }
                        }
                    }

                    DestinationsNavHost(navGraph = NavGraphs.root, dependenciesContainerBuilder ={
                        dependency(chatViewModel)
                    } )

//                    NavHost(modifier = Modifier.weight(1f),
//                        navController = navController,
//                        startDestination = USERNAME_SCREEN){
//
//                        composable(USERNAME_SCREEN){
//                            UsernameScreen {
//                                navController.navigate(CHAT_ROOMS){
//                                    popUpTo(USERNAME_SCREEN){
//                                        inclusive = true
//                                    }
//                                }
//                            }
//                        }
//
//
//                        composable(CHAT_ROOMS){
//                            ChatRooms(chatViewModel = chatViewModel){
//
//                                navController.navigate(CHAT_SCREEN)
//                            }
//                        }
//
//
//                        composable(CHAT_SCREEN){
//                            Log.d("CHECKTAGS", "navigating to chat screen!!!")
//                            ChatScreen(chatViewModel = chatViewModel)
//                        }
//                    }


                }
            }
        }
    }
}

