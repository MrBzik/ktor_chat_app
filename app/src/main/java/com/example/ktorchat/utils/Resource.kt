package com.example.ktorchat.utils

sealed class Resource<T>(val data : T? = null, val message : String? = null) {

    class Success<T>(data: T?) : Resource<T>(data = data)
    class Error<T>(data: T? = null, message: String) : Resource<T>(data = data, message = message)

}