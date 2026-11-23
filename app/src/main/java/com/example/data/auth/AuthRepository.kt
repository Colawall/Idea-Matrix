package com.example.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.ShareCodeUtils
import com.example.data.model.UserProfile
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    companion object {
        private const val TAG = "AuthRepository"

        fun create(context: Context): AuthRepository {
            val dbId = context.applicationContext.getString(R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(dbId)
            val auth = FirebaseAuth.getInstance()
            return AuthRepository(auth, db)
        }
    }

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = user
            if (user != null) {
                repositoryScope.launch {
                    loadOrCreateProfile(user)
                }
            } else {
                _userProfile.value = null
            }
        }

        auth.currentUser?.let { user ->
            repositoryScope.launch {
                loadOrCreateProfile(user)
            }
        }
    }

    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        _isLoading.value = true
        _authError.value = null

        try {
            val webClientId = context.getString(R.string.default_web_client_id)
            val credentialManager = CredentialManager.create(context)

            // Strict exclusivity: exactly one credential option per request
            val googleOption = GetSignInWithGoogleOption.Builder(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build()

            val result = try {
                credentialManager.getCredential(context, request)
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In was cancelled by user: ${e.message}")
                _isLoading.value = false
                val msg = "Sign-in was cancelled. Tap 'Sign in with Google' when you're ready."
                _authError.value = msg
                return@withContext Result.failure(Exception(msg))
            }

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: throw IllegalStateException("Firebase user is null after sign in")

                loadOrCreateProfile(user)
                _isLoading.value = false
                _authError.value = null
                Result.success(user)
            } else {
                _isLoading.value = false
                val error = Exception("Unexpected credential format received. Please try again.")
                _authError.value = error.message
                Result.failure(error)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sign in failed", e)
            _isLoading.value = false
            val friendlyMsg = when {
                e is java.net.UnknownHostException || e.message?.contains("network", ignoreCase = true) == true || e.message?.contains("offline", ignoreCase = true) == true || e.message?.contains("connection", ignoreCase = true) == true ->
                    "No internet connection. Please check your network and try again."
                e is GetCredentialCancellationException || e.message?.contains("cancel", ignoreCase = true) == true ->
                    "Sign-in was cancelled. Tap 'Sign in with Google' when you're ready."
                else ->
                    "Unable to sign in right now. Please try again."
            }
            _authError.value = friendlyMsg
            Result.failure(e)
        }
    }

    private suspend fun loadOrCreateProfile(user: FirebaseUser): UserProfile {
        val userDocRef = db.collection("users").document(user.uid)
        val snapshot = try {
            userDocRef.get().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user doc: ${e.message}")
            null
        }

        val existingProfile = snapshot?.data?.let { UserProfile.fromMap(it) }

        if (existingProfile != null && existingProfile.shareCode.isNotBlank()) {
            _userProfile.value = existingProfile
            return existingProfile
        }

        // Generate unique shareable code IM-XXXXXX with unambiguous characters
        val generatedCode = ShareCodeUtils.generateCode()
        val newProfile = UserProfile(
            uid = user.uid,
            displayName = user.displayName ?: user.email?.substringBefore("@") ?: "Collaborator",
            email = user.email ?: "",
            photoUrl = user.photoUrl?.toString() ?: "",
            shareCode = generatedCode,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        try {
            userDocRef.set(newProfile.toMap()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save profile: ${e.message}")
        }

        _userProfile.value = newProfile
        return newProfile
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _userProfile.value = null
    }

    fun clearError() {
        _authError.value = null
    }
}
