package com.example.data.model

data class ProtectionStats(
    val adsBlocked: Int = 0,
    val trackersBlocked: Int = 0,
    val popupsBlocked: Int = 0,
    val redirectsBlocked: Int = 0,
    val suspiciousBlocked: Int = 0,
    val autoDownloadsBlocked: Int = 0
) {
    val totalBlocked: Int
        get() = adsBlocked + trackersBlocked + popupsBlocked + redirectsBlocked + suspiciousBlocked + autoDownloadsBlocked
}

enum class NaraMascotState {
    NORMAL,            // Nara tersenyum
    LOADING,           // Nara terlihat fokus
    DOWNLOAD,          // Nara membawa file
    DOWNLOAD_SUCCESS,  // Nara senang
    DOWNLOAD_FAILED,   // Nara sedih
    PROTECTION,        // Nara memegang tameng
    WARNING            // Nara terlihat serius
}

data class BrowserSettings(
    val protectionEnabled: Boolean = true,
    val adBlocking: Boolean = true,
    val popupBlocking: Boolean = true,
    val redirectBlocking: Boolean = true,
    val trackerBlocking: Boolean = true,
    val autoDownloadBlocking: Boolean = true,
    val externalAppBlocking: Boolean = true,
    val maliciousSiteWarning: Boolean = true,
    val javascriptEnabled: Boolean = true,
    val cookiesEnabled: Boolean = true,
    val askBeforeDownload: Boolean = true,
    val desktopSiteDefault: Boolean = false,
    val searchEngineUrl: String = "https://www.google.com/search?q="
)
