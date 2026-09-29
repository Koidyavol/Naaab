package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "whitelist",
    indices = [Index(value = ["domain"], unique = true)]
)
data class WhitelistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val domain: String,
    val addedAt: Long = System.currentTimeMillis()
)
