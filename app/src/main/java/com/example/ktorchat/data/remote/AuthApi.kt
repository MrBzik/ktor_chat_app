package com.example.ktorchat.data.remote

import com.example.ktorchat.data.remote.requests.AuthRequest
import com.example.ktorchat.domain.model.EncryptedUserInfo
import com.example.ktorchat.presentation.auth.AuthResult
import kotlinx.coroutines.channels.Channel

interface AuthApi {

    val authStatus : Channel<AuthResult>

    suspend fun signUp(request: AuthRequest)

    suspend fun signIn(request: AuthRequest)

    suspend fun authenticate() : EncryptedUserInfo?

    companion object {
        const val BASE_URL = "http://192.168.1.165:8080"
    }


    sealed class Endpoints(val url : String){
        object SignUp : Endpoints("$BASE_URL/signup")
        object SignIn : Endpoints("$BASE_URL/signin")
        object Authenticate : Endpoints("$BASE_URL/authenticate")

    }

}