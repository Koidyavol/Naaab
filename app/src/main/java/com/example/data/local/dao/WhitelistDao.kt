package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.WhitelistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WhitelistDao {
    @Query("SELECT * FROM whitelist ORDER BY addedAt DESC")
    fun getAllWhitelist(): Flow<List<WhitelistEntity>>

    @Query("SELECT COUNT(*) > 0 FROM whitelist WHERE domain = :domain")
    suspend fun isWhitelisted(domain: String): Boolean

    @Query("SELECT COUNT(*) > 0 FROM whitelist WHERE domain = :domain")
    fun observeIsWhitelisted(domain: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(whitelist: WhitelistEntity): Long

    @Query("DELETE FROM whitelist WHERE domain = :domain")
    suspend fun deleteByDomain(domain: String)

    @Query("DELETE FROM whitelist")
    suspend fun deleteAll()
}
