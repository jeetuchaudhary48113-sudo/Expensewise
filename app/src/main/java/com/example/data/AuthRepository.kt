package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context? = null) {

    private fun getAuth(): FirebaseAuth? {
        return try {
            if (context != null && FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to retrieve FirebaseAuth: ${e.message}", e)
            null
        }
    }

    val currentUser: FirebaseUser?
        get() = getAuth()?.currentUser

    val isUserLoggedIn: Boolean
        get() = currentUser != null

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val auth = getAuth()
        if (auth == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        trySend(auth.currentUser)

        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose {
            try {
                auth.removeAuthStateListener(listener)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error removing auth state listener: ${e.message}")
            }
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        val auth = getAuth()
            ?: return Result.failure(Exception("Authentication service is unavailable. Please check internet connection."))

        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Authentication succeeded but user is null"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(getReadableErrorMessage(e)))
        }
    }

    suspend fun signUpWithEmail(name: String, email: String, password: String): Result<FirebaseUser> {
        val auth = getAuth()
            ?: return Result.failure(Exception("Authentication service is unavailable. Please check internet connection."))

        return try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user
            if (user != null) {
                if (name.isNotBlank()) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name.trim())
                        .build()
                    user.updateProfile(profileUpdates).await()
                }
                Result.success(user)
            } else {
                Result.failure(Exception("Registration succeeded but user profile is null"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(getReadableErrorMessage(e)))
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        val auth = getAuth()
            ?: return Result.failure(Exception("Authentication service is unavailable. Please check internet connection."))

        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(getReadableErrorMessage(e)))
        }
    }

    fun signOut() {
        try {
            getAuth()?.signOut()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error signing out: ${e.message}", e)
        }
    }

    private fun getReadableErrorMessage(e: Exception): String {
        val msg = e.localizedMessage ?: ""
        return when {
            e is FirebaseAuthInvalidUserException -> "No account found with this email. Please register."
            e is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password."
            e is FirebaseAuthUserCollisionException -> "An account with this email already exists."
            e is FirebaseAuthWeakPasswordException -> "Password is too weak. Please use at least 6 characters."
            e is FirebaseNetworkException -> "Network error. Please check your internet connection."
            msg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) || msg.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) ->
                "Email/Password sign-in is disabled in your Firebase console. Please enable Email/Password provider under Authentication."
            else -> msg.ifBlank { "Authentication failed. Please try again." }
        }
    }
}
