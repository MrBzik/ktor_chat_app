package com.example.ktorchat.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.ktorchat.presentation.auth.AuthResult
import com.example.ktorchat.presentation.auth.UsernameViewModel
import com.example.ktorchat.ui.destinations.ChatRoomsDestination
import com.example.ktorchat.ui.destinations.UsernameScreenDestination

import com.example.ktorchat.utils.Constants.AUTH_SIGN_UP
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination(start = true)
fun UsernameScreen(
    usernameViewModel: UsernameViewModel = hiltViewModel(),
    navigator : DestinationsNavigator
//    onNavigate : () -> Unit
){

    Log.d("CHECKTAGS", "calling composable fun")

    val currentUsername by usernameViewModel.usernameText

    val currentPassword by usernameViewModel.passwordText

//    val authState by usernameViewModel.authState.collectAsState()

    val authOption by usernameViewModel.authOption

    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current


    val isLoading = remember {
        mutableStateOf(false)
    }

    var passwordVisibility: Boolean by remember { mutableStateOf(false) }



    LaunchedEffect(Unit){
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
            usernameViewModel.authState.collect { authState ->
                when (authState) {
                    is AuthResult.Authorized -> {
                        isLoading.value = false
                        Log.d("CHECKTAGS", "navigating to chatrooms")

                        navigator.navigate(ChatRoomsDestination){
                            popUpTo(UsernameScreenDestination.route){
                                inclusive = true
                            }
                        }
//                        onNavigate()
                    }

                    is AuthResult.Error -> {
                        isLoading.value = false
                        Toast.makeText(context, authState.message, Toast.LENGTH_LONG).show()
                    }

                    is AuthResult.Loading -> {
                        isLoading.value = true
                    }

                    AuthResult.Unauthorized -> {

                    }
                }
            }
        }
    }


//    LaunchedEffect(true){
//        usernameViewModel.onJoinChat.collectLatest {
//            onNavigate("$CHAT_SCREEN/$it")
//        }
//    }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
        
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
        ) {


            Text(modifier = Modifier.fillMaxWidth(),
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                text =  if(authOption == AUTH_SIGN_UP) "Create account"
            else "Login")

            Spacer(modifier = Modifier.height(32.dp))

            TextField(
                value = currentUsername,
                onValueChange = usernameViewModel::updateUsername,
                placeholder = {
                    Text(text = "Enter your name")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = currentPassword,
                onValueChange = usernameViewModel::updatePassword,
                visualTransformation = if(passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                              IconButton(onClick = {
                                  passwordVisibility = !passwordVisibility
                              }) {
                                  Icon(imageVector = Icons.Filled.Lock, contentDescription = "password visibility")
                              }
                },
                placeholder = {
                    Text(text = "Enter password")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            
            Button(onClick = usernameViewModel::onSignClick) {

                Text(text = if(authOption == AUTH_SIGN_UP) "Sign up"
                            else "Login")
            }


            Spacer(modifier = Modifier.height(30.dp))

            TextButton(onClick = usernameViewModel::changeAuthOption) {
                Text(text =  if(authOption == AUTH_SIGN_UP) "Login"
                else "Sign up")
            }


            if(isLoading.value){
                CircularProgressIndicator(modifier = Modifier.height(40.dp))
            }
        }
    }


}