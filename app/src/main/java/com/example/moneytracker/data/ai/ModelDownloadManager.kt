package com.example.moneytracker.data.ai

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

sealed interface DownloadProgress {
    data object NotStarted : DownloadProgress
    data class Downloading(val percentage: Int) : DownloadProgress
    data object Completed : DownloadProgress
    data class Failed(val reason: String) : DownloadProgress
}

class ModelDownloadManager(
    private val context: Context
) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
    
    val modelFile: File = File(context.getExternalFilesDir(null) ?: context.filesDir, "models/qwen_0.5b.task")

    fun isModelDownloaded(): Boolean {
        return modelFile.exists() && modelFile.length() > 0
    }

    fun startDownload(url: String): Long {
        if (isModelDownloaded() || downloadManager == null) return -1L
        
        try {
            // Ensure parent directory exists
            modelFile.parentFile?.mkdirs()
            // If file exists, delete it to restart clean
            if (modelFile.exists()) {
                modelFile.delete()
            }

            val uri = Uri.parse(url)
            val request = DownloadManager.Request(uri)
                .setTitle("Qwen AI Model Download")
                .setDescription("Downloading local on-device AI budget assistant model (350MB)...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationUri(Uri.fromFile(modelFile))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            return downloadManager.enqueue(request)
        } catch (e: Exception) {
            e.printStackTrace()
            return -1L
        }
    }

    fun downloadStatusFlow(downloadId: Long): Flow<DownloadProgress> = flow {
        if (isModelDownloaded()) {
            emit(DownloadProgress.Completed)
            return@flow
        }
        
        if (downloadId == -1L || downloadManager == null) {
            emit(DownloadProgress.NotStarted)
            return@flow
        }

        var isDownloading = true
        while (isDownloading) {
            if (isModelDownloaded()) {
                emit(DownloadProgress.Completed)
                break
            }

            try {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                
                if (cursor != null && cursor.moveToFirst()) {
                    val bytesDownloadedIdx = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val bytesTotalIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)

                    val bytesDownloaded = if (bytesDownloadedIdx != -1) cursor.getInt(bytesDownloadedIdx) else 0
                    val bytesTotal = if (bytesTotalIdx != -1) cursor.getInt(bytesTotalIdx) else 0
                    val status = if (statusIdx != -1) cursor.getInt(statusIdx) else DownloadManager.STATUS_FAILED

                    cursor.close()

                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            emit(DownloadProgress.Completed)
                            isDownloading = false
                        }
                        DownloadManager.STATUS_FAILED -> {
                            emit(DownloadProgress.Failed("Download failed via system service."))
                            isDownloading = false
                        }
                        DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PENDING, DownloadManager.STATUS_PAUSED -> {
                            val percentage = if (bytesTotal > 0) {
                                ((bytesDownloaded.toLong() * 100) / bytesTotal).toInt()
                            } else {
                                0
                            }
                            emit(DownloadProgress.Downloading(percentage))
                        }
                    }
                } else {
                    cursor?.close()
                    // If cursor is null or empty but file is finished, consider completed
                    if (isModelDownloaded()) {
                        emit(DownloadProgress.Completed)
                    } else {
                        emit(DownloadProgress.NotStarted)
                    }
                    isDownloading = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (isModelDownloaded()) {
                    emit(DownloadProgress.Completed)
                } else {
                    emit(DownloadProgress.Failed(e.localizedMessage ?: "Query error"))
                }
                isDownloading = false
            }

            if (isDownloading) {
                delay(1000)
            }
        }
    }
}
