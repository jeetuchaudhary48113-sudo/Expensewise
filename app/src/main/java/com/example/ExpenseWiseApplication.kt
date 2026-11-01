package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class ExpenseWiseApplication : Application() {

    companion object {
        lateinit var instance: ExpenseWiseApplication
            private set

        val defaultFirebaseOptions: FirebaseOptions by lazy {
            FirebaseOptions.Builder()
                .setApplicationId("1:228650630825:android:13602ae0858abc1284b3b8")
                .setApiKey("AIzaSyD5CZDqwRaz1n-bUFlksWqEqvhX7GFxRXM")
                .setProjectId("expensewise-de113")
                .setGcmSenderId("228650630825")
                .setStorageBucket("expensewise-de113.firebasestorage.app")
                .build()
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initFirebase()
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(this).isNotEmpty()) {
                Log.i("ExpenseWiseApp", "FirebaseApp already initialized: ${FirebaseApp.getInstance().name}")
                return
            }

            // Attempt default initialization from generated resources first
            val defaultApp = try {
                FirebaseApp.initializeApp(this)
            } catch (t: Throwable) {
                Log.w("ExpenseWiseApp", "Default FirebaseApp.initializeApp threw: ${t.message}")
                null
            }

            // If defaultApp is null (e.g. string resources could not be resolved automatically),
            // initialize with explicit options from google-services.json
            if (defaultApp == null) {
                Log.i("ExpenseWiseApp", "Initializing default FirebaseApp with explicit FirebaseOptions...")
                val explicitApp = FirebaseApp.initializeApp(this, defaultFirebaseOptions)
                Log.i("ExpenseWiseApp", "FirebaseApp successfully initialized with explicit options: ${explicitApp.name}")
            } else {
                Log.i("ExpenseWiseApp", "FirebaseApp successfully initialized from resources: ${defaultApp.name}")
            }
        } catch (e: Throwable) {
            Log.e("ExpenseWiseApp", "Fatal Firebase initialization exception: ${e.message}", e)
        }
    }
}
