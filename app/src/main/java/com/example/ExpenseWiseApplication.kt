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
                try {
                    val app = FirebaseApp.initializeApp(this)
                    Log.i("ExpenseWiseApp", "Default FirebaseApp initialized: ${app?.name}")
                } catch (e: Exception) {
                    Log.w("ExpenseWiseApp", "Default initializeApp failed, using explicit FirebaseOptions: ${e.message}")
                    val options = com.google.firebase.FirebaseOptions.Builder()
                        .setApplicationId("1:228650630825:android:13602ae0858abc1284b3b8")
                        .setApiKey("AIzaSyD5CZDqwRaz1n-bUFlksWqEqvhX7GFxRXM")
                        .setProjectId("expensewise-de113")
                        .setGcmSenderId("228650630825")
                        .setStorageBucket("expensewise-de113.firebasestorage.app")
                        .build()
                    val explicitApp = FirebaseApp.initializeApp(this, options)
                    Log.i("ExpenseWiseApp", "Explicit FirebaseApp initialized: ${explicitApp.name}")
                }
            }
        } catch (e: Exception) {
            Log.e("ExpenseWiseApp", "Fatal Firebase initialization exception: ${e.message}", e)
        }
    }
}
