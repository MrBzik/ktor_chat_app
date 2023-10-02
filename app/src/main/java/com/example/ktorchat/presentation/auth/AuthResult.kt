package com.example.ktorchat.presentation.auth

sealed class AuthResult(val message : String? = null) {
    object Authorized : AuthResult()
    object Unauthorized : AuthResult()
    object Loading : AuthResult()
    class Error(message : String) : AuthResult(message)
}