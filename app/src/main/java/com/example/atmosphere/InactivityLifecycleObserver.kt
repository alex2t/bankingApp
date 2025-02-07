package com.example.atmosphere

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.atmosphere.viewmodels.auth.AuthViewModel

//Detects when the app is backgrounded or resumed

class InactivityLifecycleObserver(
    private val inactivityManager: InactivityManager,
    private val authViewModel: AuthViewModel
) : DefaultLifecycleObserver {

    // Called when the app moves to the foreground from the background.
    override fun onResume(owner: LifecycleOwner) {
        inactivityManager.resetTimer()
    }
//Called when the app leaves the foreground
    override fun onPause(owner: LifecycleOwner) {
        inactivityManager.cancelTimer()
    }

    override fun onStop(owner: LifecycleOwner) {
        if (authViewModel.loggedIn.value) {
            authViewModel.logout() // Log out when the app goes fully backgrounded
        }
    }

}
