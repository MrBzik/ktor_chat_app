package com.example.ktorchat.presentation.profile

import android.app.Application
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ktorchat.dagger.PENDING
import com.example.ktorchat.data.remote.ChatSocketService
import com.example.ktorchat.data.remote.ProfileService
import com.example.ktorchat.ui.PROFILE_PICTURE
import com.example.ktorchat.utils.Constants.CONNECTION_GOOD
import com.example.ktorchat.utils.Constants.PENDING_PROFILE_PIC_UPDATE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ProfileViewModel @Inject constructor (
    private val app : Application,
    private val profileService: ProfileService,
    @Named(PENDING) private val pendingPref : SharedPreferences,
    private val chatSocketService: ChatSocketService
) : AndroidViewModel(app) {



    var profileBitmap : Bitmap? = null

    var userName : String = ""

    private val _bitmapUri : MutableStateFlow<Uri?> = MutableStateFlow(null)

    val bitmapUri = _bitmapUri.asStateFlow()


    var isToBackUpProfilePic = false


    fun updateBitmap(bitmap: Bitmap){
        profileBitmap = bitmap
    }


    private fun sendNewBitmapToServer(){
        profileBitmap?.let { bitmap ->
            viewModelScope.launch {
                trySendBitmap(bitmap)
            }
        }
    }


    private suspend fun trySendBitmap(bitmap: Bitmap){
        val check = profileService.sendProfilePicBitmap(bitmap)
        Log.d("CHECKTAGS", "was it a success?....")
        if(!check){
            Log.d("CHECKTAGS", "mark as pending")
            pendingPref.edit().putBoolean(PENDING_PROFILE_PIC_UPDATE, true).apply()
        }
    }


    fun initialGetBitmapUri(path : Uri?){

        path?.let {
            Log.d("CHECKTAGS", "local uri")
            _bitmapUri.value = it
        } ?: kotlin.run {
            Log.d("CHECKTAGS", "getting remote uri")
            getBitmapUrl()
        }

    }


    private fun getBitmapUrl() {
        viewModelScope.launch {
            profileService.getProfilePicUri()?.let {
                isToBackUpProfilePic = true
                _bitmapUri.value = Uri.parse(it)
            }
        }
    }


    fun updateUserName(){
        viewModelScope.launch {
            if(userName.isNotBlank()){
                profileService.updateUserName(userName)
            }
        }
    }


    fun savePicToFile(){
        profileBitmap?.let { bitmap ->

            val path = app.filesDir.absolutePath + PROFILE_PICTURE

            isToBackUpProfilePic = false
            val file = File(path)
            val out = FileOutputStream(file)
            out.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
    }


    fun onImageCropped(uri : Uri){

        _bitmapUri.value = uri

        getBitmapFromUri(uri).let { bitmap ->
            updateBitmap(bitmap)
            try {
                savePicToFile()
                sendNewBitmapToServer()
            } catch (e : Exception){
                Log.d("CHECKTAGS", e.stackTraceToString())
            }
        }
    }



    private suspend fun checkForPendingProfileUpdates(){

        if(pendingPref.getBoolean(PENDING_PROFILE_PIC_UPDATE, false)){

            val bitmap : Bitmap = if(profileBitmap != null ){
                profileBitmap!!
            } else {
                val filePath = app.filesDir.absolutePath + PROFILE_PICTURE
                getBitmapFromUri(Uri.parse(filePath))
            }

            trySendBitmap(bitmap)

        }
    }


    private fun getBitmapFromUri(uri : Uri) : Bitmap {
        return if(Build.VERSION.SDK_INT < 28){
            MediaStore.Images.Media.getBitmap(app.contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(app.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    }



    init {

        viewModelScope.launch {
            chatSocketService.isSessionActive.collectLatest {

                if(it == CONNECTION_GOOD){
                    checkForPendingProfileUpdates()
                }

            }
        }
    }


}