package com.example.smartcafeadmin

import android.app.Activity
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth

// Sends the user back to the login screen if nobody is signed in
object AuthGuard {

    fun isSignedIn(activity: Activity): Boolean {
        if (FirebaseAuth.getInstance().currentUser == null) {
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            activity.finish()
            return false
        }
        return true
    }
}

