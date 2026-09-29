package com.example.protection

import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import java.net.URI

enum class BlockType {
    NONE,
    AD,
    TRACKER,
    SUSPICIOUS,
    MALICIOUS
}

object AdBlockManager {

    fun extractHost(url: String): String? {
        return try {
            val uri = URI(url)
            uri.host ?: run {
                Regex("^(?:https?://)?([^/:?#]+)").find(url)?.groupValues?.get(1)
            }
        } catch (_: Exception) {
            Regex("^(?:https?://)?([^/:?#]+)").find(url)?.groupValues?.get(1)
        }
    }

    fun inspectUrl(url: String): BlockType {
        try {
            val host = extractHost(url) ?: return BlockType.NONE

            if (FilterLists.isMaliciousHost(host)) {
                return BlockType.MALICIOUS
            }
            if (FilterLists.isAdHost(host)) {
                return BlockType.AD
            }
            if (FilterLists.isTrackerHost(host)) {
                return BlockType.TRACKER
            }
            if (FilterLists.containsSuspiciousKeywords(url)) {
                return BlockType.SUSPICIOUS
            }
        } catch (_: Exception) {
            // Ignore parse errors
        }
        return BlockType.NONE
    }

    /**
     * MUST return a brand new WebResourceResponse with a fresh unread ByteArrayInputStream
     * on every call, otherwise Chromium crashes with stream closed / SIGSEGV.
     */
    fun createEmptyResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            ByteArrayInputStream(ByteArray(0))
        )
    }
}
