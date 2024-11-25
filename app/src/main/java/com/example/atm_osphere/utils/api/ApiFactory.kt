package com.example.atm_osphere.utils.api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class ApiFactory {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()



    suspend fun postRequest(url: String, jsonBody: String): String {
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            val call = client.newCall(request)
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    response.body?.string() ?: "No response body"
                } else {
                    throw IOException("HTTP Error: ${response.code}")
                }
            } catch (e: SocketTimeoutException) {
                throw IOException("Request timed out", e)
            } finally {
                call.cancel()
            }
        }
    }

}