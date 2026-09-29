package com.example.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import com.example.data.local.entity.DownloadEntity
import com.example.data.repository.BrowserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class DownloadPromptData(
    val url: String,
    val userAgent: String,
    val contentDisposition: String,
    val mimeType: String,
    val contentLength: Long,
    val suggestedFileName: String
)

data class ActiveDownload(
    val downloadId: Long,
    val fileName: String,
    val progress: Int, // 0..100 or -1 if unknown
    val totalBytes: Long,
    val downloadedBytes: Long,
    val status: String // DOWNLOADING, SUCCESS, FAILED, PAUSED
)

class NaraDownloadManager(
    private val context: Context,
    private val repository: BrowserRepository,
    private val scope: CoroutineScope
) {

    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    private val _activeDownloads = MutableStateFlow<List<ActiveDownload>>(emptyList())
    val activeDownloads = _activeDownloads.asStateFlow()

    init {
        startPollingActiveDownloads()
    }

    fun parseDownloadInfo(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
        contentLength: Long
    ): DownloadPromptData {
        var guessedFileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        if (guessedFileName.isNullOrBlank() || guessedFileName == "downloadfile.bin") {
            try {
                val path = Uri.parse(url).lastPathSegment
                if (!path.isNullOrBlank()) {
                    guessedFileName = path
                }
            } catch (_: Exception) {}
        }
        return DownloadPromptData(
            url = url,
            userAgent = userAgent,
            contentDisposition = contentDisposition,
            mimeType = mimeType,
            contentLength = if (contentLength > 0) contentLength else -1L,
            suggestedFileName = guessedFileName
        )
    }

    fun startDownload(info: DownloadPromptData): Long {
        try {
            val uri = Uri.parse(info.url)
            val request = DownloadManager.Request(uri).apply {
                setTitle(info.suggestedFileName)
                setDescription("Mengunduh dengan Nara Browser...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, info.suggestedFileName)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
                if (info.mimeType.isNotBlank()) {
                    setMimeType(info.mimeType)
                }
            }

            val downloadId = downloadManager.enqueue(request)

            scope.launch(Dispatchers.IO) {
                val entity = DownloadEntity(
                    downloadId = downloadId,
                    fileName = info.suggestedFileName,
                    url = info.url,
                    filePath = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        info.suggestedFileName
                    ).absolutePath,
                    fileSize = if (info.contentLength > 0) info.contentLength else 0L,
                    mimeType = info.mimeType,
                    status = "DOWNLOADING",
                    timestamp = System.currentTimeMillis()
                )
                repository.addDownload(entity)
            }

            return downloadId
        } catch (e: Exception) {
            e.printStackTrace()
            return -1L
        }
    }

    fun cancelDownload(downloadId: Long) {
        try {
            downloadManager.remove(downloadId)
            _activeDownloads.value = _activeDownloads.value.filter { it.downloadId != downloadId }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startPollingActiveDownloads() {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val query = DownloadManager.Query()
                    val cursor = downloadManager.query(query)
                    val activeList = mutableListOf<ActiveDownload>()

                    if (cursor != null) {
                        val idCol = cursor.getColumnIndex(DownloadManager.COLUMN_ID)
                        val titleCol = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
                        val statusCol = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val downloadedCol = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val totalCol = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                        while (cursor.moveToNext()) {
                            val id = cursor.getLong(idCol)
                            val title = cursor.getString(titleCol) ?: "file"
                            val status = cursor.getInt(statusCol)
                            val downloaded = cursor.getLong(downloadedCol)
                            val total = cursor.getLong(totalCol)

                            val statusStr = when (status) {
                                DownloadManager.STATUS_RUNNING -> "DOWNLOADING"
                                DownloadManager.STATUS_SUCCESSFUL -> "SUCCESS"
                                DownloadManager.STATUS_FAILED -> "FAILED"
                                DownloadManager.STATUS_PAUSED -> "PAUSED"
                                else -> "PENDING"
                            }

                            val progress = if (total > 0) ((downloaded * 100) / total).toInt() else -1

                            // Only keep ongoing or recently finished ones in active list
                            if (status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PAUSED) {
                                activeList.add(
                                    ActiveDownload(
                                        downloadId = id,
                                        fileName = title,
                                        progress = progress,
                                        totalBytes = total,
                                        downloadedBytes = downloaded,
                                        status = statusStr
                                    )
                                )
                            }
                        }
                        cursor.close()
                    }
                    _activeDownloads.value = activeList
                } catch (_: Exception) {}
                delay(1000)
            }
        }
    }
}
