package com.example.ktorchat.data.remote

import android.graphics.Bitmap
import android.net.Uri

interface ProfileService {


    suspend fun getProfilePicUri() : String?

    suspend fun sendProfilePicBitmap(bitmap: Bitmap) : Boolean

    suspend fun updateUserName(newName : String) : Boolean

    companion object {
        const val BASE_URL = "http://192.168.1.165:8080"
    }




    sealed class Endpoints(val url : String){
        object GetProfilePicUri : Endpoints("$BASE_URL/get_profile_pic_uri")
        object SendProfilePicBitmap : Endpoints("$BASE_URL/send_profile_pic_bitmap")
        object UpdateUserName : Endpoints("$BASE_URL/update_user_name")
    }



}