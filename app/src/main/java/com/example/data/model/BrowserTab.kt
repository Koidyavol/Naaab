package com.example.data.model

import java.util.UUID

enum class SecurityState {
    SECURE,       // HTTPS
    INSECURE,     // HTTP
    WARNING,      // Malicious or suspicious
    ERROR         // Failed to load
}

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "",
    val title: String = "Tab Baru",
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isIncognito: Boolean = false,
    val isDesktopMode: Boolean = false,
    val securityState: SecurityState = SecurityState.SECURE,
    val lastAccessed: Long = System.currentTimeMillis(),
    val adsBlocked: Int = 0,
    val trackersBlocked: Int = 0,
    val popupsBlocked: Int = 0,
    val redirectsBlocked: Int = 0,
    val suspiciousBlocked: Int = 0,
    val autoDownloadsBlocked: Int = 0
) {
    val totalBlocked: Int
        get() = adsBlocked + trackersBlocked + popupsBlocked + redirectsBlocked + suspiciousBlocked + autoDownloadsBlocked

    val isNewTab: Boolean
        get() = url.isEmpty() || url == "about:blank" || url == "nara://newtab"

    val displayDomain: String
        get() {
            if (isNewTab) return "Tab Baru"
            return try {
                val uri = android.net.Uri.parse(url)
                uri.host ?: url
            } catch (e: Exception) {
                url
            }
        }
}
