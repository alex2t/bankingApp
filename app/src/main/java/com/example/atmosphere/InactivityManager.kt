package com.example.atmosphere

import android.content.Context
import android.os.Handler
import android.os.Looper


class InactivityManager(
    private val context: Context,
    private val logoutAction: () -> Unit,
    private val navigateToHome: () -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private val inactivityTimeout: Long = 5 * 60 * 1000 // 5 minutes
    private val inactivityRunnable = Runnable {
        logoutAction()
        navigateToHome()
    }

    fun resetTimer() {
        handler.removeCallbacks(inactivityRunnable)
        handler.postDelayed(inactivityRunnable, inactivityTimeout)
    }

    fun cancelTimer() {
        handler.removeCallbacks(inactivityRunnable)
    }

    fun cleanup() {
        handler.removeCallbacks(inactivityRunnable)
    }
}
