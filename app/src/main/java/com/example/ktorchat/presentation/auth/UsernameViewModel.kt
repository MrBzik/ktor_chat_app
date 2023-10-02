package com.example.ktorchat.presentation.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ktorchat.data.remote.AuthApi
import com.example.ktorchat.data.remote.requests.AuthRequest
import com.example.ktorchat.utils.Constants.AUTH_SIGN_IN
import com.example.ktorchat.utils.Constants.AUTH_SIGN_UP
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsernameViewModel @Inject constructor(
    private val authApi: AuthApi
) : ViewModel() {



    val authState = authApi.authStatus.receiveAsFlow()


    val authOption = mutableStateOf(AUTH_SIGN_UP)


    private val _usernameText = mutableStateOf("")
    val usernameText : State<String> = _usernameText

    fun updateUsername(text : String){
        _usernameText.value = text
    }


    private val _passwordText = mutableStateOf("")
    val passwordText : State<String> = _passwordText

    fun updatePassword(text : String){
        _passwordText.value = text
    }


//    private val _onJoinChat = MutableSharedFlow<String>()
//    val onJoinChat : SharedFlow<String> = _onJoinChat.asSharedFlow()
//
//    fun onJoinChatClick() = viewModelScope.launch {
//
//        if(_usernameText.value.isNotBlank())
//            _onJoinChat.emit(_usernameText.value)
//    }


    fun onSignClick(){
        if(authOption.value == AUTH_SIGN_UP)
            signUp()
        else signIn()
    }

    fun changeAuthOption(){

        if(authOption.value == AUTH_SIGN_UP)
            authOption.value = AUTH_SIGN_IN
        else authOption.value = AUTH_SIGN_UP
    }


    private fun provideAuthRequest() : AuthRequest? {

        return if(_usernameText.value.isNotBlank() && _passwordText.value.isNotBlank())
            AuthRequest(username = _usernameText.value, password = _passwordText.value)
        else null

    }


    private fun signUp() = viewModelScope.launch {
        provideAuthRequest()?.let {
            authApi.signUp(it)
        }
    }

    private fun signIn() = viewModelScope.launch {
        provideAuthRequest()?.let {
            authApi.signIn(it)
        }
    }

    init {

        viewModelScope.launch {
            authApi.authenticate()?.let {
                updateUsername(it.userName)
                updatePassword(it.userPassword)
            }
        }
    }

}