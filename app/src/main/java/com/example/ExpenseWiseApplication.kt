package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class ExpenseWiseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initFirebase()
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val app = FirebaseApp.initializeApp(this)
                Log.i("ExpenseWiseApp", "Firebase initialized successfully: ${app?.name}")
            }
        } catch (e: Exception) {
            Log.e("ExpenseWiseApp", "Firebase initialization caught exception: ${e.message}", e)
        }
    }
}
