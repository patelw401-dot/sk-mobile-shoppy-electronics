package com.example.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.BuildConfig
import com.example.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val releaseNotes: String
)

object AppUpdateChecker {
    private const val TAG = "AppUpdateChecker"
    const val DEFAULT_VERCEL_URL = "https://sk-mobile-shoppy-live.vercel.app"
    private const val VERSION_MANIFEST_PATH = "/app-version.json"
    private const val DEFAULT_APK_PATH = "/sk-mobile-release.apk"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    fun getConfiguredBaseUrl(context: Context): String {
        val raw = try {
            context.getString(R.string.app_update_base_url)
        } catch (_: Exception) {
            DEFAULT_VERCEL_URL
        }.trim().trimEnd('/')

        return if (raw.startsWith("https://", ignoreCase = true)) {
            raw
        } else {
            DEFAULT_VERCEL_URL
        }
    }

    private fun trustedApkUrl(context: Context, url: String): Boolean {
        val candidate = Uri.parse(url)
        val trusted = Uri.parse(getConfiguredBaseUrl(context))
        return candidate.scheme.equals("https", true) &&
            candidate.host?.equals(trusted.host, true) == true &&
            candidate.port == trusted.port && candidate.userInfo == null &&
            candidate.path == DEFAULT_APK_PATH
    }

    suspend fun checkForUpdate(context: Context): AppUpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val baseUrl = getConfiguredBaseUrl(context)
            val manifestUrl = "$baseUrl$VERSION_MANIFEST_PATH?ts=${System.currentTimeMillis()}"
            val request = Request.Builder()
                .url(manifestUrl)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .header("Accept", "application/json")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.d(TAG, "Update check returned HTTP ${response.code}")
                    return@withContext null
                }

                val body = response.body?.string()?.trim()
                if (body.isNullOrBlank()) return@withContext null

                val json = JSONObject(body)
                val remoteVersionCode = json.optInt("versionCode", 0)
                val rawVersionName = json.optString("versionName", "").trim()
                val rawApkPath = json.optString("apkPath", DEFAULT_APK_PATH).trim().ifBlank { DEFAULT_APK_PATH }
                val releaseNotes = json.optString(
                    "releaseNotes",
                    "New features and improvements are available."
                ).trim().ifBlank { "New features and improvements are available." }

                if (remoteVersionCode <= BuildConfig.VERSION_CODE || rawVersionName.isBlank()) {
                    return@withContext null
                }

                val normalizedVersionName = rawVersionName.removePrefix("v").removePrefix("V")
                val resolvedApkUrl = when {
                    rawApkPath.startsWith("https://", ignoreCase = true) -> rawApkPath
                    rawApkPath.startsWith("/") -> "$baseUrl$rawApkPath"
                    else -> "$baseUrl/$rawApkPath"
                }

                if (!trustedApkUrl(context, resolvedApkUrl)) {
                    return@withContext null
                }

                AppUpdateInfo(
                    versionCode = remoteVersionCode,
                    versionName = normalizedVersionName,
                    apkUrl = resolvedApkUrl,
                    releaseNotes = releaseNotes
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.d(TAG, "Update check skipped: ${e.message}")
            null
        }
    }

    fun openApkDownload(context: Context, apkUrl: String): Boolean {
        if (!trustedApkUrl(context, apkUrl)) return false
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.w(TAG, "Could not launch APK download URL: ${e.message}")
            false
        }
    }
}
