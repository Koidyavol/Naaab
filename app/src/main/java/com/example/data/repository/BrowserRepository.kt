package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.DownloadEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.local.entity.WhitelistEntity
import com.example.data.model.BrowserSettings
import kotlinx.coroutines.flow.Flow

class BrowserRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val historyDao = db.historyDao()
    private val bookmarkDao = db.bookmarkDao()
    private val whitelistDao = db.whitelistDao()
    private val downloadDao = db.downloadDao()

    private val prefs: SharedPreferences =
        context.getSharedPreferences("nara_browser_prefs", Context.MODE_PRIVATE)

    // History
    fun getAllHistory(): Flow<List<HistoryEntity>> = historyDao.getAllHistory()
    fun searchHistory(query: String): Flow<List<HistoryEntity>> = historyDao.searchHistory(query)
    suspend fun addHistory(title: String, url: String) {
        if (url.isNotBlank() && !url.startsWith("about:") && !url.startsWith("nara:")) {
            historyDao.insert(HistoryEntity(title = title.ifBlank { url }, url = url))
        }
    }
    suspend fun clearAllHistory() = historyDao.deleteAll()

    // Bookmarks
    fun getAllBookmarks(): Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
    fun isBookmarked(url: String): Flow<Boolean> = bookmarkDao.isBookmarked(url)
    suspend fun addBookmark(title: String, url: String) =
        bookmarkDao.insert(BookmarkEntity(title = title.ifBlank { url }, url = url))
    suspend fun removeBookmark(id: Long) = bookmarkDao.deleteById(id)
    suspend fun removeBookmarkByUrl(url: String) = bookmarkDao.deleteByUrl(url)

    // Whitelist
    fun getAllWhitelist(): Flow<List<WhitelistEntity>> = whitelistDao.getAllWhitelist()
    fun observeIsWhitelisted(domain: String): Flow<Boolean> = whitelistDao.observeIsWhitelisted(domain)
    suspend fun isDomainWhitelisted(domain: String): Boolean = whitelistDao.isWhitelisted(domain)
    suspend fun addWhitelist(domain: String) =
        whitelistDao.insert(WhitelistEntity(domain = domain.lowercase().trim()))
    suspend fun removeWhitelist(domain: String) =
        whitelistDao.deleteByDomain(domain.lowercase().trim())
    suspend fun clearAllWhitelist() = whitelistDao.deleteAll()

    // Downloads
    fun getAllDownloads(): Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()
    suspend fun addDownload(download: DownloadEntity): Long = downloadDao.insert(download)
    suspend fun updateDownload(download: DownloadEntity) = downloadDao.update(download)
    suspend fun deleteDownload(id: Long) = downloadDao.deleteById(id)
    suspend fun clearAllDownloads() = downloadDao.deleteAll()

    // Settings
    fun loadSettings(): BrowserSettings {
        return BrowserSettings(
            protectionEnabled = prefs.getBoolean("protection_enabled", true),
            adBlocking = prefs.getBoolean("ad_blocking", true),
            popupBlocking = prefs.getBoolean("popup_blocking", true),
            redirectBlocking = prefs.getBoolean("redirect_blocking", true),
            trackerBlocking = prefs.getBoolean("tracker_blocking", true),
            autoDownloadBlocking = prefs.getBoolean("auto_download_blocking", true),
            externalAppBlocking = prefs.getBoolean("external_app_blocking", true),
            maliciousSiteWarning = prefs.getBoolean("malicious_warning", true),
            javascriptEnabled = prefs.getBoolean("javascript_enabled", true),
            cookiesEnabled = prefs.getBoolean("cookies_enabled", true),
            askBeforeDownload = prefs.getBoolean("ask_before_download", true),
            desktopSiteDefault = prefs.getBoolean("desktop_site_default", false),
            searchEngineUrl = prefs.getString("search_engine", "https://www.google.com/search?q=")
                ?: "https://www.google.com/search?q="
        )
    }

    fun saveSettings(settings: BrowserSettings) {
        prefs.edit()
            .putBoolean("protection_enabled", settings.protectionEnabled)
            .putBoolean("ad_blocking", settings.adBlocking)
            .putBoolean("popup_blocking", settings.popupBlocking)
            .putBoolean("redirect_blocking", settings.redirectBlocking)
            .putBoolean("tracker_blocking", settings.trackerBlocking)
            .putBoolean("auto_download_blocking", settings.autoDownloadBlocking)
            .putBoolean("external_app_blocking", settings.externalAppBlocking)
            .putBoolean("malicious_warning", settings.maliciousSiteWarning)
            .putBoolean("javascript_enabled", settings.javascriptEnabled)
            .putBoolean("cookies_enabled", settings.cookiesEnabled)
            .putBoolean("askBeforeDownload", settings.askBeforeDownload)
            .putBoolean("desktop_site_default", settings.desktopSiteDefault)
            .putString("search_engine", settings.searchEngineUrl)
            .apply()
    }
}
