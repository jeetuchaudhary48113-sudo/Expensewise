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

import com.example.ExpenseWiseApplication

class AuthRepository(private val context: Context? = null) {

    companion object {
        private const val TAG = "AuthRepository"

        /**
         * Ensures that FirebaseApp is safely initialized and returns the initialized FirebaseApp instance.
         */
        @Synchronized
        fun getOrInitFirebaseApp(context: Context?): FirebaseApp {
            return try {
                FirebaseApp.getInstance()
            } catch (e: Exception) {
                Log.w(TAG, "Default FirebaseApp not initialized: ${e.message}. Initializing now...")
                val ctx = context?.applicationContext
                    ?: try { ExpenseWiseApplication.instance } catch (_: Throwable) { null }

                if (ctx != null) {
                    val app = try {
                        FirebaseApp.initializeApp(ctx)
                    } catch (t: Throwable) {
                        null
                    }
                    if (app != null) {
                        return app
                    }
                    FirebaseApp.initializeApp(ctx, ExpenseWiseApplication.defaultFirebaseOptions)
                } else {
                    throw IllegalStateException("Cannot initialize FirebaseApp without Android Context", e)
                }
            }
        }
    }

    private fun getAuth(): FirebaseAuth {
        val app = getOrInitFirebaseApp(context)
        return FirebaseAuth.getInstance(app)
    }

    val currentUser: FirebaseUser?
        get() = try {
            getAuth().currentUser
        } catch (e: Exception) {
            null
        }

    val isUserLoggedIn: Boolean
        get() = currentUser != null

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val auth = try {
            getAuth()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Could not get FirebaseAuth for authStateFlow: ${e.message}", e)
            null
        }
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
        return try {
            val auth = getAuth()
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Authentication succeeded but user is null"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign in failed: ${e.message}", e)
            Result.failure(Exception(getReadableErrorMessage(e)))
        }
    }

    suspend fun signUpWithEmail(name: String, email: String, password: String): Result<FirebaseUser> {
        return try {
            val auth = getAuth()
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
            Log.e("AuthRepository", "Sign up failed: ${e.message}", e)
            Result.failure(Exception(getReadableErrorMessage(e)))
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            val auth = getAuth()
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Password reset failed: ${e.message}", e)
            Result.failure(Exception(getReadableErrorMessage(e)))
        }
    }

    fun signOut() {
        try {
            getAuth().signOut()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error signing out: ${e.message}", e)
        }
    }

    private fun getReadableErrorMessage(e: Exception): String {
        val msg = e.localizedMessage ?: e.message ?: ""
        return when {
            e is FirebaseAuthInvalidUserException ->
                "No account found with this email. Please check your email or create a new account."
            e is FirebaseAuthInvalidCredentialsException -> {
                val errorCode = (e as? com.google.firebase.auth.FirebaseAuthException)?.errorCode
                when (errorCode) {
                    "ERROR_INVALID_EMAIL" -> "The email address is improperly formatted."
                    "ERROR_WRONG_PASSWORD" -> "Incorrect password. Please try again or tap 'Forgot Password?'"
                    else -> "Invalid email or password. Please verify and try again."
                }
            }
            e is FirebaseAuthUserCollisionException ->
                "An account with this email already exists. Please sign in instead."
            e is FirebaseAuthWeakPasswordException ->
                "Password is too weak: ${e.reason ?: "Please use at least 6 characters."}"
            e is com.google.firebase.FirebaseTooManyRequestsException ->
                "Too many attempts. Access is temporarily disabled for security. Please wait a moment."
            e is FirebaseNetworkException ->
                "Network connection error. Please verify your internet or Wi-Fi connection."
            msg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) || msg.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) ->
                "Email/Password sign-in provider is disabled in Firebase console (expensewise-de113)."
            msg.contains("API_KEY_SERVICE_BLOCKED", ignoreCase = true) ->
                "Firebase API key is blocked. Check Google Cloud Console API restrictions."
            msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ->
                "Incorrect email or password. Please check your credentials."
            e is com.google.firebase.auth.FirebaseAuthException ->
                "Authentication error (${e.errorCode}): ${e.localizedMessage ?: e.message ?: "Authentication failed."}"
            else -> msg.ifBlank { "Authentication failed (${e.javaClass.simpleName}). Please try again." }
        }
    }
}
