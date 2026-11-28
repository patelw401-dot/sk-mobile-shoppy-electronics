package com.example.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.data.firebase.FirebaseConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApiNotAvailableException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/**
 * Customer Profile Data Model.
 * Stored in Firestore /customers/{uid} for private customer record persistence.
 */
data class CustomerProfile(
    val uid: String = "",
    val name: String = "",
    val email: String? = null,
    val phoneNumber: String? = null,
    val photoUrl: String? = null,
    val deliveryAddress: String = "",
    val city: String = "Udgir",
    val state: String = "Maharashtra",
    val pinCode: String = "413517",
    val isGoogleLinked: Boolean = false,
    val isPhoneLinked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)

sealed class PhoneAuthUiState {
    object Idle : PhoneAuthUiState()
    object SendingOtp : PhoneAuthUiState()
    data class OtpSent(val verificationId: String, val resendToken: PhoneAuthProvider.ForceResendingToken?) : PhoneAuthUiState()
    object VerifyingOtp : PhoneAuthUiState()
    object Verified : PhoneAuthUiState()
    data class Error(val message: String) : PhoneAuthUiState()
}

/**
 * Enterprise Service for Customer Authentication (Google Sign-In, Phone OTP, Account Linking).
 *
 * Strict Security Rules:
 * 1. Never grants admin privileges to customers.
 * 2. Only customer-owned Firestore documents (/customers/{uid}) are written.
 * 3. Never hardcodes, logs, or fakes OTPs.
 * 4. Provides granular error parsing for network, quota, expired OTP, invalid OTP, and credential-collision cases.
 */
object CustomerAuthService {
    private const val TAG = "CustomerAuthService"
    const val COLLECTION_CUSTOMERS = "customers"

    fun getFirebaseAuth(): FirebaseAuth? {
        return try {
            FirebaseConfig.getAuth()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FirebaseAuth instance: ${e.message}")
            null
        }
    }

    /**
     * Map Firebase Authentication and Phone Verification exceptions to clear, actionable customer messages.
     */
    fun parseAuthError(error: Throwable?): String {
        if (error == null) return "Authentication failed. Please try again."

        Log.e(TAG, "Customer Auth error: ${error.javaClass.simpleName}: ${error.message}", error)

        if (error is FirebaseAuthWeakPasswordException) {
            return "Password is too weak. Please enter at least 6 characters."
        }

        if (error is FirebaseAuthUserCollisionException) {
            return "An account with this email or phone number already exists. Please sign in instead."
        }

        if (error is FirebaseAuthInvalidUserException) {
            return when (error.errorCode) {
                "ERROR_USER_DISABLED" ->
                    "This account has been disabled. Please contact SK Mobile Shoppy and Electronics support."
                "ERROR_USER_NOT_FOUND" ->
                    "No account found with this email address. Please create an account or verify your email."
                else ->
                    "Account not found. Please check your email address or create a new account."
            }
        }

        if (error is FirebaseAuthInvalidCredentialsException) {
            val code = error.errorCode
            val msg = error.message.orEmpty()
            return when {
                code == "ERROR_INVALID_EMAIL" ->
                    "Please enter a valid email address."
                code == "ERROR_WRONG_PASSWORD" || code == "ERROR_INVALID_CREDENTIAL" ->
                    "Incorrect email or password. Please double check your credentials and try again."
                code == "ERROR_INVALID_VERIFICATION_CODE" || msg.contains("verification code", ignoreCase = true) ->
                    "Invalid 6-digit OTP entered. Please check the SMS and try again."
                code == "ERROR_SESSION_EXPIRED" || msg.contains("expired", ignoreCase = true) ->
                    "The OTP has expired. Please tap 'Resend OTP' to receive a new code."
                code == "ERROR_INVALID_PHONE_NUMBER" || msg.contains("phone number", ignoreCase = true) ->
                    "Invalid phone number format. Please enter a valid 10-digit Indian mobile number."
                else ->
                    "Invalid login credentials. Please double check and try again."
            }
        }

        if (error is FirebaseTooManyRequestsException) {
            return "Too many requests. Attempt limit exceeded. Please wait a few minutes before trying again."
        }

        if (error is FirebaseNetworkException) {
            return "Network connection error. Please check your internet connection and try again."
        }

        if (error is FirebaseApiNotAvailableException) {
            return "Authentication service is temporarily unavailable. Please try again shortly."
        }

        if (error is FirebaseAuthException) {
            return when (error.errorCode) {
                "ERROR_OPERATION_NOT_ALLOWED" ->
                    "This sign-in method is not enabled in Firebase Console. Please enable Email/Password & Google under Authentication > Sign-in method."
                "ERROR_EMAIL_ALREADY_IN_USE" ->
                    "An account with this email already exists. Please switch to Sign In."
                "ERROR_WEAK_PASSWORD" ->
                    "Password is too weak. Please enter at least 6 characters."
                "ERROR_INVALID_EMAIL" ->
                    "Please enter a valid email address."
                "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" ->
                    "Incorrect email or password. Please double check your credentials and try again."
                "ERROR_USER_NOT_FOUND" ->
                    "No account found with this email address. Please create an account first."
                "ERROR_TOO_MANY_REQUESTS" ->
                    "Too many requests. Please wait a few minutes before trying again."
                "ERROR_SESSION_EXPIRED" ->
                    "OTP expired. Please request a new verification code."
                "ERROR_INVALID_VERIFICATION_CODE" ->
                    "Incorrect OTP entered. Please verify the code sent to your phone."
                "ERROR_USER_DISABLED" ->
                    "This account has been disabled. Please contact SK Mobile Shoppy and Electronics support."
                "ERROR_CREDENTIAL_ALREADY_IN_USE" ->
                    "This credential is already linked to another account."
                else -> {
                    val msg = error.message.orEmpty()
                    if (msg.contains("network", ignoreCase = true) || msg.contains("timeout", ignoreCase = true)) {
                        "Network connection error. Please check your internet connection."
                    } else if (msg.contains("quota", ignoreCase = true) || msg.contains("too many", ignoreCase = true)) {
                        "Attempt limit reached. Please wait a moment before retrying."
                    } else {
                        "Authentication error: ${error.localizedMessage ?: "Please try again."}"
                    }
                }
            }
        }

        val rawMessage = error.message.orEmpty()
        return when {
            rawMessage.contains("network", ignoreCase = true) || rawMessage.contains("unable to resolve host", ignoreCase = true) ->
                "Network connection error. Please check your internet connection and try again."
            rawMessage.contains("quota", ignoreCase = true) ->
                "SMS quota exceeded. Please try again later or sign in with Google."
            rawMessage.contains("too many", ignoreCase = true) ->
                "Too many attempts. Access is temporarily rate-limited."
            rawMessage.contains("canceled", ignoreCase = true) || rawMessage.contains("cancelled", ignoreCase = true) ->
                "Sign-in was cancelled."
            else ->
                error.localizedMessage ?: "Authentication failed. Please verify your details."
        }
    }

    fun parseGoogleSignInError(error: Throwable?): String {
        val friendlyMessage = parseAuthError(error)
        val diagnostic = when (error) {
            is FirebaseAuthException ->
                "Firebase ${error.errorCode}: ${error.message ?: "No additional details returned."}"
            null -> "No exception details returned."
            else ->
                "${error.javaClass.simpleName}: ${error.localizedMessage ?: "No additional details returned."}"
        }
        return "$friendlyMessage\n$diagnostic"
    }

    /**
     * Validates Indian phone numbers (10 digits, starts with 6, 7, 8, or 9).
     * Returns the formatted E.164 phone string: +91XXXXXXXXXX, or null if invalid.
     */
    fun sanitizeAndValidateIndianPhone(input: String): String? {
        val digitsOnly = input.filter { it.isDigit() }
        val tenDigits = when {
            digitsOnly.length == 10 -> digitsOnly
            digitsOnly.length == 12 && digitsOnly.startsWith("91") -> digitsOnly.substring(2)
            digitsOnly.length == 11 && digitsOnly.startsWith("0") -> digitsOnly.substring(1)
            else -> return null
        }

        // Standard Indian mobile number prefixes (6, 7, 8, 9)
        if (!tenDigits.matches(Regex("^[6-9]\\d{9}$"))) {
            return null
        }

        return "+91$tenDigits"
    }

    /**
     * Initiate real Firebase Phone Authentication OTP verification flow.
     */
    fun sendVerificationCode(
        activity: Activity,
        phoneNumberE164: String,
        forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null,
        onCodeSent: (verificationId: String, token: PhoneAuthProvider.ForceResendingToken) -> Unit,
        onVerificationCompleted: (PhoneAuthCredential) -> Unit,
        onVerificationFailed: (Exception) -> Unit
    ) {
        val auth = getFirebaseAuth() ?: run {
            onVerificationFailed(Exception("Firebase Authentication is not available"))
            return
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "Instant phone verification completed")
                onVerificationCompleted(credential)
            }

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                Log.e(TAG, "Phone verification failed: ${e.message}", e)
                onVerificationFailed(e)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "Phone OTP sent successfully")
                onCodeSent(verificationId, token)
            }
        }

        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumberE164)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        if (forceResendingToken != null) {
            optionsBuilder.setForceResendingToken(forceResendingToken)
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    /**
     * Sign in or link using PhoneAuthCredential.
     */
    suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): Result<FirebaseUser> {
        val auth = getFirebaseAuth() ?: return Result.failure(Exception("Firebase Auth unavailable"))
        return try {
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: return Result.failure(Exception("User is null after sign-in"))
            syncCustomerProfileToFirestore(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign in with Google IdToken via CredentialManager.
     */
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        val auth = getFirebaseAuth() ?: return Result.failure(Exception("Firebase Auth unavailable"))
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: return Result.failure(Exception("User is null after sign-in"))
            syncCustomerProfileToFirestore(user)
            Result.success(user)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private val verificationSentAt = mutableMapOf<String, Long>()

    private suspend fun requireVerifiedEmail(auth: FirebaseAuth, user: FirebaseUser) {
        try {
            user.reload().await()
            if (!user.isEmailVerified) {
                val now = android.os.SystemClock.elapsedRealtime()
                val last = verificationSentAt[user.uid]
                if (last != null && now - last < 60_000L) {
                    throw IllegalStateException("Check your inbox or wait 60 seconds before requesting another verification email.")
                }
                user.sendEmailVerification().await()
                verificationSentAt[user.uid] = now
                throw IllegalStateException("Verification email sent. Open the link in your inbox or spam folder, then sign in again.")
            }
            user.getIdToken(true).await()
        } catch (e: Exception) {
            auth.signOut()
            throw e
        }
    }

    /**
     * Sign in customer with Email & Password.
     */
    suspend fun signInWithEmailPassword(email: String, password: String): Result<FirebaseUser> {
        val auth = getFirebaseAuth() ?: return Result.failure(Exception("Firebase Auth unavailable"))
        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: return Result.failure(Exception("User is null after sign-in"))
            requireVerifiedEmail(auth, user)
            syncCustomerProfileToFirestore(user)
            Result.success(user)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a new customer account with Email & Password and optional display name.
     */
    suspend fun signUpWithEmailPassword(name: String, email: String, password: String): Result<FirebaseUser> {
        val auth = getFirebaseAuth() ?: return Result.failure(Exception("Firebase Auth unavailable"))
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: return Result.failure(Exception("User is null after account creation"))
            val cleanName = name.trim()
            if (cleanName.isNotBlank()) {
                try {
                    val profileUpdates = userProfileChangeRequest {
                        displayName = cleanName
                    }
                    user.updateProfile(profileUpdates).await()
                    user.reload().await()
                } catch (profileErr: Exception) {
                    Log.w(TAG, "Profile displayName update warning: ${profileErr.message}")
                }
            }
            val finalUser = auth.currentUser ?: user
            requireVerifiedEmail(auth, finalUser)
            syncCustomerProfileToFirestore(finalUser)
            Result.success(finalUser)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send password reset link to customer's email address.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        val auth = getFirebaseAuth() ?: return Result.failure(Exception("Firebase Auth unavailable"))
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Firebase-hosted Google OAuth fallback for devices where Credential Manager cannot obtain a token.
     */
    suspend fun signInWithGoogleProvider(activity: Activity): Result<FirebaseUser> {
        val auth = getFirebaseAuth() ?: return Result.failure(Exception("Firebase Auth unavailable"))
        return try {
            val provider = OAuthProvider.newBuilder("google.com").apply {
                addCustomParameter("prompt", "select_account")
            }.build()
            val authTask = auth.pendingAuthResult
                ?: auth.startActivityForSignInWithProvider(activity, provider)
            val authResult = authTask.await()
            val user = authResult.user ?: return Result.failure(Exception("User is null after sign-in"))
            syncCustomerProfileToFirestore(user)
            Result.success(user)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Link an additional credential (e.g. Phone to existing Google account, or Google to existing Phone account).
     * Prevents account overwriting and handles collision safely.
     */
    suspend fun linkCredential(user: FirebaseUser, credential: AuthCredential): Result<FirebaseUser> {
        return try {
            val authResult = user.linkWithCredential(credential).await()
            val updatedUser = authResult.user ?: user
            syncCustomerProfileToFirestore(updatedUser)
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Synchronize customer profile into Firestore /customers/{uid} collection.
     * Complies strictly with zero-trust firestore.rules (request.auth.uid == customerId).
     */
    suspend fun syncCustomerProfileToFirestore(
        user: FirebaseUser,
        customDeliveryAddress: String? = null,
        context: Context? = null
    ) {
        try {
            val firestore = if (context != null) {
                FirebaseConfig.getFirestore(context)
            } else {
                try {
                    val app = com.google.firebase.FirebaseApp.getInstance()
                    com.google.firebase.firestore.FirebaseFirestore.getInstance(app, "ai-studio-android-skmobile-a123a057-9048-4d09-88d8-cfb38e18df87")
                } catch (e: Exception) {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                }
            }

            val uid = user.uid
            val customerDocRef = firestore.collection(COLLECTION_CUSTOMERS).document(uid)

            val isGoogleLinked = user.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }
            val isPhoneLinked = user.providerData.any { it.providerId == PhoneAuthProvider.PROVIDER_ID }
            val isEmailLinked = user.providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID }

            val profileData = mutableMapOf<String, Any>(
                "uid" to uid,
                "name" to (user.displayName ?: if (!user.phoneNumber.isNullOrBlank()) "Customer (${user.phoneNumber})" else "Customer"),
                "isGoogleLinked" to isGoogleLinked,
                "isPhoneLinked" to isPhoneLinked,
                "isEmailLinked" to isEmailLinked,
                "lastLoginAt" to System.currentTimeMillis()
            )

            user.email?.let { profileData["email"] = it }
            user.phoneNumber?.let { profileData["phoneNumber"] = it }
            user.photoUrl?.let { profileData["photoUrl"] = it.toString() }
            if (!customDeliveryAddress.isNullOrBlank()) {
                profileData["deliveryAddress"] = customDeliveryAddress
            }

            customerDocRef.set(profileData, SetOptions.merge()).await()
            Log.d(TAG, "Customer profile synchronized for UID: $uid")
        } catch (e: Exception) {
            Log.w(TAG, "Could not sync customer profile to Firestore: ${e.message}")
        }
    }

    /**
     * Sign out customer from Firebase Authentication.
     */
    fun signOut() {
        try {
            getFirebaseAuth()?.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Sign out error: ${e.message}")
        }
    }
}
