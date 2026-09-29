package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val downloadId: Long = 0,
    val fileName: String,
    val url: String,
    val filePath: String = "",
    val fileSize: Long = 0L,
    val mimeType: String = "",
    val status: String = "COMPLETED", // DOWNLOADING, COMPLETED, FAILED, CANCELLED
    val timestamp: Long = System.currentTimeMillis()
)
