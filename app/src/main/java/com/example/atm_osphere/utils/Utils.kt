package com.example.atm_osphere.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Utils {
    fun getUserAgent(): String {
        return System.getProperty("http.agent") ?: "Unknown User Agent"
    }

    fun getRemoteIp(): String? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api64.ipify.org?format=json")
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val json = response.body?.string()
                return json?.let { JSONObject(it).getString("ip") }
            }
        }
        return null
    }
}

object OutputManager {
    private val _userAgent = MutableStateFlow<String>("")
    val userAgent: StateFlow<String> = _userAgent

    private val _remoteIp = MutableStateFlow<String>("")
    val remoteIp: StateFlow<String> = _remoteIp

    suspend fun collectIpAndUserAgent() {
        if (_userAgent.value.isNotEmpty() && _remoteIp.value.isNotEmpty()) return // Already collected

        val userAgentValue = withContext(Dispatchers.IO) { Utils.getUserAgent() }
        _userAgent.value = userAgentValue

        val remoteIpValue = withContext(Dispatchers.IO) { Utils.getRemoteIp() }
        _remoteIp.value = remoteIpValue ?: "IP not found"
    }

    suspend fun getUserAgentAndRemoteIp(): Pair<String, String> {
        collectIpAndUserAgent()
        return Pair(userAgent.value, remoteIp.value)
    }
    fun resetOutput() {
        _userAgent.value = ""
        _remoteIp.value = ""
    }
}
