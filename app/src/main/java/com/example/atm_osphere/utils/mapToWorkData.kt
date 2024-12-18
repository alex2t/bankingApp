package com.example.atm_osphere.utils
import androidx.work.Data

fun mapToWorkData(input: Map<String, Any>): Data {
    val builder = Data.Builder()
    input.forEach { (key, value) ->
        when (value) {
            is String -> builder.putString(key, value)
            is Int -> builder.putInt(key, value)
            is Boolean -> builder.putInt(key, if (value) 1 else 0)
            is Float -> builder.putFloat(key, value)
            is Long -> builder.putLong(key, value)
        }
    }
    return builder.build()
}