package com.example.ktorchat.data.remote

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.example.ktorchat.ui.PROFILE_PICTURE
import com.example.ktorchat.utils.Constants
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream

class ProfileServiceImpl(
    private val client: HttpClient,
    private val authPref : SharedPreferences
    ) : ProfileService {

    override suspend fun getProfilePicUri(): String? {

        return try {
            val token = authPref.getString(Constants.AUTH_TOKEN, "no token")

             client.get{
                url(ProfileService.Endpoints.GetProfilePicUri.url)
                header("Authorization", "Bearer $token")
            }

        } catch (e : Exception){
            Log.d("CHECKTAGS", e.stackTraceToString())
            null
        }
    }

    override suspend fun sendProfilePicBitmap(bitmap: Bitmap): Boolean {

        return try {

            val token = authPref.getString(Constants.AUTH_TOKEN, "no token")

            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            val byteArray = out.toByteArray()


            client.submitFormWithBinaryData<HttpResponse>(
                url = ProfileService.Endpoints.SendProfilePicBitmap.url,
                formData = formData {
                    append("bitmapName", PROFILE_PICTURE)
                    append("bitmap", byteArray, Headers.build {
                        append(HttpHeaders.ContentType, "image/png")
                        append(HttpHeaders.ContentDisposition, "filename=$PROFILE_PICTURE")
                    })
                }
            ){
                header("Authorization", "Bearer $token")
            }

            true

        } catch (e : Exception){
            Log.d("CHECKTAGS", e.stackTraceToString())
            false
        }



    }

    override suspend fun updateUserName(newName: String): Boolean {

        return try {

            val token = authPref.getString(Constants.AUTH_TOKEN, "no token")

            client.post<HttpResponse>{
                url(ProfileService.Endpoints.UpdateUserName.url)
                header("Authorization", "Bearer $token")
                parameter("user_name", newName)
            }
            true

        } catch (e : Exception){
            Log.d("CHECKTAGS", e.stackTraceToString())
            false
        }

    }
}