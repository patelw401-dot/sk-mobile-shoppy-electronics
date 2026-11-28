package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import coil.compose.AsyncImage
import com.example.R
import androidx.compose.foundation.BorderStroke
import com.example.model.RepairBooking
import com.example.model.ShopOrder
import com.example.ui.theme.*
import com.example.viewmodel.ShopUiState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSheet(
    uiState: ShopUiState,
    onGoogleSignIn: (idToken: String, onComplete: (Boolean, String?) -> Unit) -> Unit,
    onGoogleSignInProvider: (Activity, (Boolean, String?) -> Unit) -> Unit,
    onGoogleSignInError: (String) -> Unit,
    onEmailSignIn: (email: String, password: String, onComplete: (Boolean, String?) -> Unit) -> Unit = { _, _, cb -> cb(false, "Email login unavailable") },
    onEmailSignUp: (name: String, email: String, password: String, onComplete: (Boolean, String?) -> Unit) -> Unit = { _, _, _, cb -> cb(false, "Account creation unavailable") },
    onSendPasswordReset: (email: String, onComplete: (Boolean, String?) -> Unit) -> Unit = { _, cb -> cb(false, "Password reset unavailable") },
    onLogout: () -> Unit,
    onClearAuthError: () -> Unit,
    onUpdateProfile: (name: String, phone: String, address: String) -> Unit,
    onViewOrders: () -> Unit = {},
    onTrackOrder: (ShopOrder) -> Unit = {},
    onOpenAdmin: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var editMode by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(uiState.userName) }
    var phoneInput by remember { mutableStateOf(uiState.userPhone) }
    var addressInput by remember { mutableStateOf(uiState.userAddress) }

    // Customer Auth Sub-states
    var isGoogleLoading by remember { mutableStateOf(false) }
    var isEmailAuthLoading by remember { mutableStateOf(false) }
    var isSignUpMode by remember { mutableStateOf(false) }
    var authNameInput by remember { mutableStateOf("") }
    var authEmailInput by remember { mutableStateOf("") }
    var authPasswordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var resetSuccessMessage by remember { mutableStateOf<String?>(null) }

    fun handleGoogleSignIn() {
        val webClientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onGoogleSignInError("Google sign-in is not configured. Please contact support.")
            return
        }

        if (webClientId.isBlank() || webClientId.contains("not configured", ignoreCase = true)) {
            onGoogleSignInError("Google sign-in is not configured. Please contact support.")
            return
        }

        isGoogleLoading = true
        coroutineScope.launch {
            try {
                val hostActivity = context.findActivity()
                if (hostActivity == null) {
                    onGoogleSignInError("Unable to start Google sign-in. Please try again.")
                    isGoogleLoading = false
                    return@launch
                }
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
                if (credential is androidx.credentials.CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    onGoogleSignIn(googleIdToken) { _, _ -> isGoogleLoading = false }
                } else {
                    onGoogleSignInError("Google did not return a valid sign-in credential. Please try again.")
                    isGoogleLoading = false
                }
            } catch (e: GetCredentialException) {
                val type = e.type.orEmpty().ifBlank { e.javaClass.simpleName }
                val detail = "Credential Manager failure [$type]: ${e.message ?: "No additional details returned."}"
                Log.w("ProfileSheet", detail, e)
                if (e is GetCredentialCancellationException) {
                    isGoogleLoading = false
                    onGoogleSignInError("$detail. Sign-in was cancelled.")
                    return@launch
                }

                val hostActivity = context.findActivity()
                if (hostActivity == null) {
                    isGoogleLoading = false
                    onGoogleSignInError("$detail. Firebase OAuth fallback is unavailable because the Activity context is missing.")
                    return@launch
                }

                onGoogleSignInProvider(hostActivity) { success, fallbackError ->
                    isGoogleLoading = false
                    if (!success) {
                        onGoogleSignInError(
                            "$detail\nFirebase Google OAuth fallback failed: ${fallbackError ?: "No additional details returned."}"
                        )
                    }
                }
            } catch (e: Exception) {
                isGoogleLoading = false
                Log.e("ProfileSheet", "Google Sign-In failed", e)
                onGoogleSignInError(
                    "Google sign-in failed (${e.javaClass.simpleName}): ${e.localizedMessage ?: "No additional details returned."}"
                )
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BlackSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
                .testTag("profile_sheet_content")
        ) {
            // Header Row: Avatar, Name/Status, Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(BlackSurfaceSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!uiState.customerPhotoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = uiState.customerPhotoUrl,
                                contentDescription = "Customer Photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Avatar",
                                tint = GoldPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (editMode) "Edit Account Info" else uiState.userName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (uiState.isCustomerAuthenticated) Icons.Default.CheckCircle else Icons.Default.AccountCircle,
                                contentDescription = "Status",
                                tint = if (uiState.isCustomerAuthenticated) ShopSuccessGreen else TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.isCustomerAuthenticated) "Verified Customer • Udgir" else "Guest Mode • Public Browsing",
                                fontSize = 11.sp,
                                color = if (uiState.isCustomerAuthenticated) ShopSuccessGreen else TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMain)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error Banner
            if (uiState.customerAuthError != null) {
                Surface(
                    color = BlackSurfaceSecondary,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ColorError),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Error",
                            tint = ColorError,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.customerAuthError,
                            color = ColorError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onClearAuthError, modifier = Modifier.size(20.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = ColorError)
                        }
                    }
                }
            }

            // ==========================================
            // CASE 1: UNLOGGED CUSTOMER -> GOOGLE + EMAIL/PASSWORD SIGN-IN
            // ==========================================
            if (!uiState.isCustomerAuthenticated) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BlackSurfaceSecondary),
                    border = BorderStroke(1.dp, BlackBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Sign in to SK Mobile Shoppy and Electronics",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Browse items freely without login. Sign in with Google or Email & Password to place orders, book repairs, and track orders.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Existing Google Sign-In Button
                        Button(
                            onClick = {
                                resetSuccessMessage = null
                                onClearAuthError()
                                handleGoogleSignIn()
                            },
                            enabled = !isGoogleLoading && !isEmailAuthLoading && !uiState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("customer_google_signin_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0B0B0B)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isGoogleLoading) {
                                CircularProgressIndicator(
                                    color = Color(0xFF0B0B0B),
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Connecting with Google...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0B0B0B)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Google",
                                    tint = Color(0xFF0B0B0B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Continue with Google",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0B0B0B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = BlackBorder)
                            Text(
                                text = "  OR WITH EMAIL  ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = BlackBorder)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Sign In / Create Account Mode Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(BlackSurface)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("customer_auth_mode_signin")
                                    .clickable {
                                        isSignUpMode = false
                                        resetSuccessMessage = null
                                        onClearAuthError()
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = if (!isSignUpMode) GoldPrimary else Color.Transparent
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Sign In",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isSignUpMode) Color(0xFF0B0B0B) else TextSecondary
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("customer_auth_mode_signup")
                                    .clickable {
                                        isSignUpMode = true
                                        resetSuccessMessage = null
                                        onClearAuthError()
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSignUpMode) GoldPrimary else Color.Transparent
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Create Account",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSignUpMode) Color(0xFF0B0B0B) else TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (resetSuccessMessage != null) {
                            Surface(
                                color = Color(0xFF0E291D),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, ShopSuccessGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = ShopSuccessGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = resetSuccessMessage ?: "",
                                        color = Color(0xFFA7F3D0),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        if (isSignUpMode) {
                            OutlinedTextField(
                                value = authNameInput,
                                onValueChange = { authNameInput = it },
                                label = { Text("Full Name") },
                                placeholder = { Text("Enter your full name") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Full Name"
                                    )
                                },
                                colors = darkOutlinedTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("customer_name_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        OutlinedTextField(
                            value = authEmailInput,
                            onValueChange = { authEmailInput = it },
                            label = { Text("Email Address") },
                            placeholder = { Text("name@example.com") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email Address"
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = darkOutlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customer_email_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = authPasswordInput,
                            onValueChange = { authPasswordInput = it },
                            label = { Text("Password") },
                            placeholder = {
                                Text(if (isSignUpMode) "Minimum 6 characters" else "Enter your password")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Password"
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    modifier = Modifier.testTag("customer_password_visibility_btn")
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = darkOutlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customer_password_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        if (!isSignUpMode) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        resetSuccessMessage = null
                                        onClearAuthError()
                                        isEmailAuthLoading = true
                                        onSendPasswordReset(authEmailInput) { success, msg ->
                                            isEmailAuthLoading = false
                                            if (success) {
                                                resetSuccessMessage = msg
                                            }
                                        }
                                    },
                                    enabled = !isEmailAuthLoading && !isGoogleLoading && !uiState.isLoading,
                                    modifier = Modifier.testTag("customer_forgot_password_btn")
                                ) {
                                    Text(
                                        text = "Forgot Password?",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GoldHighlight
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        OutlinedButton(
                            onClick = {
                                resetSuccessMessage = null
                                onClearAuthError()
                                isEmailAuthLoading = true
                                if (isSignUpMode) {
                                    onEmailSignUp(authNameInput, authEmailInput, authPasswordInput) { _, _ ->
                                        isEmailAuthLoading = false
                                    }
                                } else {
                                    onEmailSignIn(authEmailInput, authPasswordInput) { _, _ ->
                                        isEmailAuthLoading = false
                                    }
                                }
                            },
                            enabled = !isEmailAuthLoading && !isGoogleLoading && !uiState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("customer_email_submit_btn"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.2.dp, GoldPrimary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = BlackSurface,
                                contentColor = GoldHighlight
                            )
                        ) {
                            if (isEmailAuthLoading || (uiState.isLoading && !isGoogleLoading)) {
                                CircularProgressIndicator(
                                    color = GoldPrimary,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isSignUpMode) "Creating Account..." else "Signing In...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldHighlight
                                )
                            } else {
                                Text(
                                    text = if (isSignUpMode) "Create Account" else "Sign In with Email",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldHighlight
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // ==========================================
            // CASE 2: AUTHENTICATED CUSTOMER -> PROFILE INFO & LINKING
            // ==========================================
            if (uiState.isCustomerAuthenticated) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BlackSurfaceSecondary),
                    border = BorderStroke(1.dp, BlackBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Account Details",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )

                            Button(
                                onClick = onLogout,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF331414), contentColor = ColorError),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("customer_logout_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sign Out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Customer Email
                        if (!uiState.customerEmail.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.customerEmail,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextMain
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        // Customer Phone
                        if (!uiState.customerPhone.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Phone",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.customerPhone,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextMain
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        // Delivery Address
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Address",
                                tint = GoldPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${uiState.userAddress}, ${uiState.userCity} ${uiState.userPin}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BlackBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Sign-In Method
                        Text(
                            text = "Authentication Method",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val authBadgeText = when {
                            uiState.isGoogleLinked && !uiState.customerEmail.isNullOrBlank() ->
                                "Verified Google Account (${uiState.customerEmail})"
                            uiState.isEmailLinked && !uiState.customerEmail.isNullOrBlank() ->
                                "Verified Email Account (${uiState.customerEmail})"
                            uiState.isPhoneLinked && !uiState.customerPhone.isNullOrBlank() ->
                                "Verified Mobile (${uiState.customerPhone})"
                            !uiState.customerEmail.isNullOrBlank() ->
                                "Verified Account (${uiState.customerEmail})"
                            else -> "Verified Customer Account"
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Verified Account",
                                    tint = ShopSuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = authBadgeText,
                                    fontSize = 12.sp,
                                    color = ShopSuccessGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Edit Profile Address Form
            if (editMode) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Full Name") },
                    colors = darkOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("edit_name_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Delivery Mobile Number") },
                    colors = darkOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("edit_phone_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    label = { Text("Delivery Address (Udgir)") },
                    colors = darkOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("edit_address_input"),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        onUpdateProfile(nameInput, phoneInput, addressInput)
                        editMode = false
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_profile_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0B0B0B))
                ) {
                    Text("Save Address Details", fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { editMode = true },
                        colors = ButtonDefaults.buttonColors(containerColor = BlackSurfaceSecondary, contentColor = GoldPrimary),
                        border = BorderStroke(1.dp, GoldPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("edit_profile_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit Delivery Address", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Repair Trackers (Requirement 4: Private records)
            Text(
                text = "Active Repair Trackers (${uiState.activeRepairBookings.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.activeRepairBookings.isEmpty()) {
                Text(
                    text = "No active repairs. Book online anytime for fast doorstep pickup in Udgir!",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            } else {
                uiState.activeRepairBookings.forEach { booking ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = BlackSurfaceSecondary),
                        border = BorderStroke(1.dp, BlackBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = "Repair",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${booking.bookingId} • ${booking.brand} ${booking.model}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMain
                                    )
                                    Text(
                                        text = booking.problem,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Surface(
                                color = BlackSurface,
                                border = BorderStroke(1.dp, GoldPrimary),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = booking.status,
                                    color = GoldPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Orders Quick Status (Requirement 4: Private customer records)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Orders (${uiState.ownedCustomerOrders.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )

                if (uiState.ownedCustomerOrders.isNotEmpty()) {
                    Text(
                        text = "View All & Track →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        modifier = Modifier
                            .clickable {
                                onViewOrders()
                                onDismiss()
                            }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.ownedCustomerOrders.isEmpty()) {
                Text(
                    text = "No previous orders yet. Browse our store for great discounts!",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            } else {
                uiState.ownedCustomerOrders.take(3).forEach { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onTrackOrder(order)
                                onDismiss()
                            },
                        colors = CardDefaults.cardColors(containerColor = BlackSurfaceSecondary),
                        border = BorderStroke(1.dp, BlackBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${order.orderId} • ₹${order.totalAmount}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMain
                                )
                                Text(
                                    text = "${order.items.size} item(s) • ${order.orderDate}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = BlackSurface,
                                border = BorderStroke(1.dp, GoldPrimary),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = order.status,
                                    color = GoldPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = BlackBorder)
            Spacer(modifier = Modifier.height(14.dp))

            // Admin Portal Link
            OutlinedButton(
                onClick = {
                    onDismiss()
                    onOpenAdmin()
                },
                modifier = Modifier.fillMaxWidth().height(42.dp).testTag("open_admin_portal_btn"),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GoldPrimary)
            ) {
                Icon(imageVector = Icons.Default.Build, contentDescription = "Admin", modifier = Modifier.size(16.dp), tint = GoldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Store Admin Portal Login", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
            }
        }
    }
}
