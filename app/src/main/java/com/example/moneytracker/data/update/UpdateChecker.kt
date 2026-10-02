package com.example.moneytracker.data.update

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object UpdateCheckerConfig {
    /**
     * Centralized URL location for the version.json file.
     * Edit this single constant to change the update server URL.
     */
    const val VERSION_CHECK_URL = "https://github.com/rvhrakotonaina-tech/Money-tracker-app/blob/main/version.json"
}

class UpdateChecker(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build(),
    private val moshi: Moshi = Moshi.Builder().build()
) {
    suspend fun checkForUpdate(context: Context): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            var targetUrl = UpdateCheckerConfig.VERSION_CHECK_URL.trim()
            if (targetUrl.contains("github.com") && targetUrl.contains("/blob/")) {
                targetUrl = targetUrl
                    .replace("github.com", "raw.githubusercontent.com")
                    .replace("/blob/", "/")
            }

            val request = Request.Builder()
                .url(targetUrl)
                .header("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return@withContext null
            }

            val responseBody = response.body?.string() ?: run {
                response.close()
                return@withContext null
            }
            response.close()

            val adapter = moshi.adapter(UpdateInfo::class.java)
            val remoteInfo = adapter.fromJson(responseBody) ?: return@withContext null

            val installedVersionCode = getInstalledVersionCode(context)

            // Strictly compare remote versionCode > installed versionCode
            if (remoteInfo.versionCode > installedVersionCode && remoteInfo.downloadUrl.isNotBlank()) {
                remoteInfo
            } else {
                null
            }
        } catch (e: Exception) {
            // Safe fallback on any error, offline state, or malformed JSON
            e.printStackTrace()
            null
        }
    }

    fun getInstalledVersionCode(context: Context): Long {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            PackageInfoCompat.getLongVersionCode(packageInfo)
        } catch (e: Exception) {
            1L
        }
    }
}
