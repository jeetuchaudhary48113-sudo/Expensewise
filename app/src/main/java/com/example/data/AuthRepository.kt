package com.example.data

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

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
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
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(getReadableErrorMessage(e)))
        }
    }

    fun signOut() {
        auth.signOut()
    }

    private fun getReadableErrorMessage(e: Exception): String {
        return when (e) {
            is FirebaseAuthInvalidUserException -> "No account found with this email. Please register."
            is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password."
            is FirebaseAuthUserCollisionException -> "An account with this email already exists."
            is FirebaseAuthWeakPasswordException -> "Password is too weak. Please use at least 6 characters."
            is FirebaseNetworkException -> "Network error. Please check your internet connection."
            else -> e.localizedMessage ?: "Authentication failed. Please try again."
        }
    }
}
