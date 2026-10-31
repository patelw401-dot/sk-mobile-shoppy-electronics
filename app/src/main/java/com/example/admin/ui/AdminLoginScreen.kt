package com.example.admin.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.compose.foundation.BorderStroke
import com.example.R
import com.example.ui.theme.*
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/**
 * Phase 11: Google-Only Admin Authentication Screen.
 * Password-based admin login and public registration have been removed.
 * Only verified Google accounts on the authorized allowlist or Firestore /admins registry can access the portal.
 */
@Composable
fun AdminLoginScreen(
    onLoginSuccess: () -> Unit,
    onBackToStore: () -> Unit,
    onGoogleSignIn: ((idToken: String, onComplete: (Boolean, String?) -> Unit) -> Unit)? = null,
    onGoogleSignInProvider: ((activity: Activity, onComplete: (Boolean, String?) -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun handleProviderSignIn() {
        val hostActivity = context.findActivity() ?: (context as? Activity)
        if (hostActivity == null) {
            isLoading = false
            errorMessage = "Unable to start Google Sign-In: Activity context is not available."
            Log.e("AdminLogin", "handleProviderSignIn: Activity context is null")
            return
        }
        if (onGoogleSignInProvider == null) {
            isLoading = false
            errorMessage = "Web Google Sign-In provider is not configured."
            Log.e("AdminLogin", "handleProviderSignIn: onGoogleSignInProvider is null")
            return
        }

        isLoading = true
        errorMessage = null

        Log.i("AdminLogin", "Launching Google Web OAuthProvider flow with Activity: ${hostActivity.javaClass.simpleName}")
        onGoogleSignInProvider(hostActivity) { success, error ->
            isLoading = false
            if (success) {
                Log.i("AdminLogin", "Google Web OAuth succeeded! Calling onLoginSuccess...")
                onLoginSuccess()
            } else {
                Log.w("AdminLogin", "Google Web OAuth failed: $error")
                errorMessage = error ?: "Access Denied: Google account is not an authorized administrator."
            }
        }
    }

    fun handleGoogleSignIn() {
        val webClientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.w("AdminLogin", "Could not read default_web_client_id string resource: ${e.message}")
            if (onGoogleSignInProvider != null) {
                handleProviderSignIn()
                return
            }
            errorMessage = "Google Web Client ID is not configured."
            return
        }

        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            val hostActivity = context.findActivity() ?: (context as? Activity)
            if (hostActivity == null) {
                isLoading = false
                errorMessage = "Activity context is required for Google Sign-In."
                Log.e("AdminLogin", "handleGoogleSignIn: hostActivity is null")
                return@launch
            }

            try {
                Log.i("AdminLogin", "Starting CredentialManager request with webClientId=$webClientId")
                val credentialManager = CredentialManager.create(hostActivity)
                val googleIdOption = GetSignInWithGoogleOption.Builder(webClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = hostActivity
                )

                val credential = result.credential
                Log.i("AdminLogin", "CredentialManager returned credential of type: ${credential.type}")
                val idToken: String? = when {
                    credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                        try {
                            GoogleIdTokenCredential.createFrom(credential.data).idToken
                        } catch (e: Exception) {
                            Log.e("AdminLogin", "Failed to parse GoogleIdTokenCredential from CustomCredential bundle", e)
                            null
                        }
                    }
                    credential is GoogleIdTokenCredential -> {
                        credential.idToken
                    }
                    else -> {
                        Log.w("AdminLogin", "Received unexpected credential type: ${credential::class.java.name}")
                        null
                    }
                }

                if (!idToken.isNullOrBlank()) {
                    if (onGoogleSignIn != null) {
                        Log.i("AdminLogin", "Passing Google ID token to onGoogleSignIn callback")
                        onGoogleSignIn(idToken) { success, error ->
                            isLoading = false
                            if (success) {
                                Log.i("AdminLogin", "Google token sign-in succeeded! Navigating to Admin Dashboard...")
                                onLoginSuccess()
                            } else {
                                Log.w("AdminLogin", "Google token sign-in rejected: $error")
                                errorMessage = error ?: "Access Denied: Google account is not an authorized administrator."
                            }
                        }
                    } else {
                        isLoading = false
                        errorMessage = "Google sign-in is not configured."
                    }
                } else {
                    Log.w("AdminLogin", "Credential did not yield a valid Google ID token. Falling back to Web OAuthProvider...")
                    if (onGoogleSignInProvider != null) {
                        handleProviderSignIn()
                    } else {
                        isLoading = false
                        val credentialType = (credential as? CustomCredential)?.type ?: credential::class.java.simpleName
                        errorMessage = "Unable to retrieve Google ID token from response ($credentialType). Please try again."
                    }
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("AdminLogin", "CredentialManager returned GetCredentialCancellationException: ${e.message}. Attempting Web OAuth fallback...")
                if (onGoogleSignInProvider != null) {
                    handleProviderSignIn()
                } else {
                    isLoading = false
                    errorMessage = "Google Sign-In was cancelled or unavailable on device. Tap Direct Sign-In below."
                }
            } catch (e: NoCredentialException) {
                Log.w("AdminLogin", "No matching credential on device: ${e.message}. Falling back to Web OAuthProvider...")
                if (onGoogleSignInProvider != null) {
                    handleProviderSignIn()
                } else {
                    isLoading = false
                    errorMessage = "No matching Google accounts found on device. Tap Direct Sign-In below."
                }
            } catch (e: GetCredentialException) {
                val msg = e.message.orEmpty()
                val type = e.type.orEmpty()
                Log.w("AdminLogin", "CredentialManager returned GetCredentialException: [$type] $msg. Falling back to Web OAuthProvider...")

                if (onGoogleSignInProvider != null) {
                    handleProviderSignIn()
                } else {
                    isLoading = false
                    Log.e("AdminLogin", "Google Sign-In failed: [$type] $msg", e)
                    when {
                        msg.contains("16:", ignoreCase = true) || msg.contains("Cannot find matching credential", ignoreCase = true) -> {
                            errorMessage = "Google Sign-In setup issue (Code 16): App signing certificate (SHA-1) is not registered in Firebase Console. Tap Direct Sign-In below."
                        }
                        msg.contains("10:", ignoreCase = true) || msg.contains("DEVELOPER_ERROR", ignoreCase = true) -> {
                            errorMessage = "Google Play Services Developer Error (Code 10: DEVELOPER_ERROR). Tap Direct Sign-In below."
                        }
                        msg.contains("network", ignoreCase = true) || msg.contains("connection", ignoreCase = true) -> {
                            errorMessage = "Network error connecting to Google. Please check your internet connection and try again."
                        }
                        else -> {
                            errorMessage = "Google Sign-In failed ([$type] $msg). Tap Direct Sign-In below."
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AdminLogin", "Unexpected exception during CredentialManager sign-in: ${e.javaClass.simpleName}: ${e.message}", e)
                if (onGoogleSignInProvider != null) {
                    handleProviderSignIn()
                } else {
                    isLoading = false
                    errorMessage = e.localizedMessage ?: "Authentication failed. Tap Direct Sign-In below."
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_login_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BlackSurface),
            border = BorderStroke(1.dp, BlackBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Admin Badge Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Portal",
                        tint = Color(0xFF0B0B0B),
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SK Mobile Shoppy and Electronics",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextMain
                )
                Text(
                    text = "Admin Management Portal",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldHighlight
                )

                Text(
                    text = "Store Operations & Inventory Management • Udgir",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Security Notice Card
                Surface(
                    color = BlackSurfaceSecondary,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BlackBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Security",
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Restricted Administrator Access",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Admin login uses verified Google Sign-In only. Only authorized administrator Gmail accounts (including store owner skmobileshoppyandelectronics@gmail.com) have access. Regular customer accounts will be denied.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Error Message Display
                if (errorMessage != null) {
                    val currentError = errorMessage ?: ""
                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                    var copied by remember { mutableStateOf(false) }

                    Surface(
                        color = BlackSurfaceSecondary,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ColorError),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Error",
                                    tint = ColorError,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentError,
                                    color = ColorError,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (currentError.contains("SHA-1")) {
                                val extractedSha1 = Regex("[0-9A-Fa-f:]{59}").find(currentError)?.value
                                    ?: "B9:E4:5C:1F:01:5D:4D:1E:D5:B9:91:2D:CF:D9:23:C2:66:16:5C:BD"
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(extractedSha1))
                                        copied = true
                                    },
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0B0B0B))
                                ) {
                                    Text(
                                        text = if (copied) "SHA-1 Copied to Clipboard!" else "Copy SHA-1 to Clipboard",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Primary Google Sign-In Button
                Button(
                    onClick = { handleGoogleSignIn() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("admin_google_signin_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0B0B0B))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color(0xFF0B0B0B),
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Verifying Administrator...", color = Color(0xFF0B0B0B), fontSize = 13.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Google",
                            tint = Color(0xFF0B0B0B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sign in with Authorized Google Account",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B0B0B)
                        )
                    }
                }

                if (onGoogleSignInProvider != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { handleProviderSignIn() },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_google_web_provider_btn"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GoldPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Direct Google Sign-In",
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Direct Google Sign-In (Web OAuth)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldPrimary
                        )
                    }
                    Text(
                        text = "Bypasses device SHA-1 fingerprint registration. Strictly authorized admins only.",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Back to Customer Store Button
                OutlinedButton(
                    onClick = onBackToStore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("admin_back_to_store_btn"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BlackBorder)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Return to Customer Store",
                        fontSize = 13.sp,
                        color = TextMain
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Verified",
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Zero-trust server-side authorization active",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}
