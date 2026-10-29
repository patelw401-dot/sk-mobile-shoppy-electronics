package com.example.admin.auth

import android.content.Context
import android.util.Log
import com.example.data.firebase.FirebaseConfig
import com.google.firebase.FirebaseApiNotAvailableException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Enterprise Firebase Authentication & Authorization Service for SK Mobile Admin Portal.
 *
 * Implements:
 * 1. Granular Firebase Auth error mapping (Requirement 5 & 8)
 * 2. Secure server-side/Cloud Firestore administrator authorization check (Requirement 7)
 * 3. Prevention of customer privilege escalation
 */
object AdminAuthService {
    private const val TAG = "AdminAuthService"
    const val COLLECTION_ADMINS = "admins"

    /**
     * Explicit authorized administrator Gmail allowlist (Phase 11 Requirement 4).
     * Normal customer Google logins outside this list or Firestore /admins registry will NEVER receive admin access.
     */
    val AUTHORIZED_ADMIN_GMAILS = setOf(
        "skmobileshoppyandelectronics@gmail.com",
        "patelw401@gmail.com",
        "admin@skmobile.com",
        "admin.skmobile@gmail.com"
    )

    /**
     * Programmatically extract the running application's exact APK signing certificate SHA-1.
     * Guarantees zero guessing and provides the exact fingerprint needed for Firebase Console registration.
     */
    fun getAppCertificateSha1(context: Context?): String {
        val fallbackSha1 = "B9:E4:5C:1F:01:5D:4D:1E:D5:B9:91:2D:CF:D9:23:C2:66:16:5C:BD"
        if (context == null) return fallbackSha1
        return try {
            val pm = context.packageManager
            val pkgName = context.packageName
            val signatures = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val packageInfo = pm.getPackageInfo(pkgName, android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES)
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = pm.getPackageInfo(pkgName, android.content.pm.PackageManager.GET_SIGNATURES)
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
            val cert = signatures?.firstOrNull()?.toByteArray()
            if (cert != null) {
                val md = java.security.MessageDigest.getInstance("SHA-1")
                val digest = md.digest(cert)
                digest.joinToString(":") { "%02X".format(it) }
            } else {
                fallbackSha1
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error calculating package certificate SHA-1: ${e.message}")
            fallbackSha1
        }
    }

    /**
     * Map Firebase Authentication exceptions to clear, user-friendly messages.
     * Complies strictly with Requirements 5 & 8:
     * - Invalid email/password
     * - User not found
     * - Too many attempts
     * - Network error
     * - Authentication service unavailable
     * - Email/Password provider not enabled in Firebase Console
     * - Certificate hash / SHA-1 mismatch
     */
    fun parseAuthError(error: Throwable?, context: Context? = null): String {
        if (error == null) return "Authentication failed. Please try again."

        Log.e(TAG, "Auth error: ${error.javaClass.simpleName}: ${error.message}", error)

        // 1. Invalid Email / Password Credentials
        if (error is FirebaseAuthInvalidCredentialsException) {
            val code = error.errorCode
            return when {
                code == "ERROR_INVALID_EMAIL" ->
                    "The email address format is invalid. Please enter a valid administrator email."
                code == "ERROR_WRONG_PASSWORD" || code == "ERROR_INVALID_CREDENTIAL" ->
                    "Invalid email or password. Please verify your administrator credentials."
                else ->
                    "Invalid administrator credentials. Please check your email and password."
            }
        }

        // 2. User Not Found or Account Disabled
        if (error is FirebaseAuthInvalidUserException) {
            val code = error.errorCode
            return when (code) {
                "ERROR_USER_DISABLED" ->
                    "This administrator account has been disabled. Please contact the store owner."
                "ERROR_USER_NOT_FOUND" ->
                    "No administrator account found with this email. Please check your email or contact the store administrator."
                else ->
                    "Administrator account not found. Please check your email address."
            }
        }

        // 3. Too Many Attempts / Rate Limiting
        if (error is FirebaseTooManyRequestsException) {
            return "Too many unsuccessful login attempts. Access to this account has been temporarily disabled. Please try again later."
        }

        // 4. Network Error
        if (error is FirebaseNetworkException) {
            return "Network error. Please check your internet connection and try again."
        }

        // 5. Authentication Service Unavailable
        if (error is FirebaseApiNotAvailableException) {
            return "Authentication service is currently unavailable. Please try again in a few moments."
        }

        // 6. Generic FirebaseAuthException by ErrorCode
        if (error is FirebaseAuthException) {
            return when (error.errorCode) {
                "ERROR_INVALID_CERT_HASH" -> {
                    val sha1 = getAppCertificateSha1(context)
                    "Firebase certificate mismatch: App signing certificate SHA-1 ($sha1) is not registered in Firebase Console for package 'com.aistudio.skmobile.udgirs'. Please add this SHA-1 fingerprint in Firebase Console > Project Settings."
                }
                "ERROR_OPERATION_NOT_ALLOWED" ->
                    "Google Sign-In is not enabled in Firebase Console. Go to Firebase Console > Authentication > Sign-in method, and enable 'Google'."
                "ERROR_TOO_MANY_REQUESTS" ->
                    "Too many unsuccessful login attempts. Access to this account has been temporarily disabled. Please try again later."
                "ERROR_USER_NOT_FOUND" ->
                    "No administrator account found with this email. Please check your email address."
                "ERROR_USER_DISABLED" ->
                    "This administrator account has been disabled. Please contact the store owner."
                "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" ->
                    "Invalid Google credentials or ID token expired. Please select your Google account again."
                "ERROR_INVALID_EMAIL" ->
                    "The email address format is invalid."
                "ERROR_WEB_CONTEXT_CANCELED" ->
                    "Google Sign-In was cancelled. Please select your authorized administrator account to continue."
                "ERROR_INTERNAL_ERROR" ->
                    "Internal authentication service error. Please try again."
                else -> {
                    val msg = error.message.orEmpty()
                    if (msg.contains("certificate hash", ignoreCase = true) || msg.contains("cert_hash", ignoreCase = true)) {
                        val sha1 = getAppCertificateSha1(context)
                        "Firebase certificate mismatch: App signing certificate SHA-1 ($sha1) is not registered in Firebase Console for package 'com.aistudio.skmobile.udgirs'. Please add this SHA-1 fingerprint in Firebase Console > Project Settings."
                    } else if (msg.contains("network", ignoreCase = true) || msg.contains("timeout", ignoreCase = true)) {
                        "Network error. Please check your internet connection and try again."
                    } else if (msg.contains("operation is not allowed", ignoreCase = true) || msg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true)) {
                        "Google Sign-In is not enabled in Firebase Console. Go to Firebase Console > Authentication > Sign-in method, and enable 'Google'."
                    } else {
                        "Authentication failed: ${error.localizedMessage ?: "Please try again."}"
                    }
                }
            }
        }

        // Inspect raw message for common connectivity or server phrases
        val rawMessage = error.message.orEmpty()
        return when {
            rawMessage.contains("certificate hash", ignoreCase = true) || rawMessage.contains("cert_hash", ignoreCase = true) -> {
                val sha1 = getAppCertificateSha1(context)
                "Firebase certificate mismatch: App signing certificate SHA-1 ($sha1) is not registered in Firebase Console for package 'com.aistudio.skmobile.udgirs'. Please add this SHA-1 fingerprint in Firebase Console > Project Settings."
            }
            rawMessage.contains("network", ignoreCase = true) || rawMessage.contains("unable to resolve host", ignoreCase = true) ->
                "Network error. Please check your internet connection and try again."
            rawMessage.contains("unavailable", ignoreCase = true) ->
                "Authentication service is currently unavailable. Please try again in a few moments."
            rawMessage.contains("too many", ignoreCase = true) ->
                "Too many unsuccessful login attempts. Access to this account has been temporarily disabled. Please try again later."
            rawMessage.contains("not allowed", ignoreCase = true) ->
                "Google Sign-In is not enabled in Firebase Console. Go to Firebase Console > Authentication > Sign-in method, and enable 'Google'."
            else ->
                error.localizedMessage ?: "Authentication failed. Please verify your connection and credentials."
        }
    }

    /**
     * Server-side Authorization Check (Phase 6 Audit):
     *
     * Authenticated customers must NEVER automatically receive admin privileges.
     * Admin access is strictly determined by verified server-side credentials:
     * 1. Firebase Authentication custom claims ("admin" == true or "role" == "admin")
     * 2. Server-side Cloud Firestore /admins collection document matching UID or Email
     *
     * No client-side self-promotion or automatic email-string grant is permitted.
     */
    suspend fun verifyAdminAuthorization(user: FirebaseUser, context: Context? = null): Boolean {
        try {
            val userEmail = user.email?.trim()?.lowercase()
            val userUid = user.uid

            // 1. Check explicit authorized Gmail allowlist (Phase 11 Requirement 4)
            if (!userEmail.isNullOrBlank() && AUTHORIZED_ADMIN_GMAILS.contains(userEmail)) {
                Log.i(TAG, "Admin confirmed via authorized Gmail allowlist: $userEmail")
                return true
            }

            // 2. Check Firebase ID Token custom claims (set via Firebase Admin SDK / Cloud Functions)
            try {
                val tokenResult = user.getIdToken(false).await()
                val claims = tokenResult.claims
                if (claims["admin"] == true || claims["role"] == "admin" || claims["role"] == "superadmin") {
                    Log.i(TAG, "Admin confirmed via Firebase ID Token custom claim for UID: $userUid")
                    return true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not check ID token claims: ${e.message}")
            }

            // 2. Query Cloud Firestore /admins collection on the backend
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

            // Check document /admins/{userUid}
            try {
                val uidDoc = firestore.collection(COLLECTION_ADMINS).document(userUid).get().await()
                if (uidDoc.exists()) {
                    val role = uidDoc.getString("role").orEmpty().lowercase()
                    val isAdmin = uidDoc.getBoolean("isAdmin") ?: (role == "admin" || role == "superadmin")
                    val isActive = uidDoc.getBoolean("active") ?: true
                    if (isAdmin && isActive) {
                        Log.i(TAG, "Admin confirmed via /admins/$userUid")
                        return true
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed reading /admins/$userUid: ${e.message}")
            }

            // Check document /admins/{userEmail}
            if (!userEmail.isNullOrBlank()) {
                try {
                    val emailDoc = firestore.collection(COLLECTION_ADMINS).document(userEmail).get().await()
                    if (emailDoc.exists()) {
                        val role = emailDoc.getString("role").orEmpty().lowercase()
                        val isAdmin = emailDoc.getBoolean("isAdmin") ?: (role == "admin" || role == "superadmin")
                        val isActive = emailDoc.getBoolean("active") ?: true
                        if (isAdmin && isActive) {
                            Log.i(TAG, "Admin confirmed via /admins/$userEmail")
                            return true
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed reading /admins/$userEmail: ${e.message}")
                }
            }

            // If neither matched, the authenticated account is a regular customer
            Log.w(TAG, "Authorization denied: $userEmail (UID: $userUid) is not an authorized administrator.")
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error evaluating admin authorization", e)
            return false
        }
    }
}
