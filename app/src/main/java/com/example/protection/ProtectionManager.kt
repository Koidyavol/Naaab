package com.example.protection

import android.net.Uri

class ProtectionManager {

    private val whitelistedDomains = mutableSetOf<String>()

    fun setWhitelistedDomains(domains: Collection<String>) {
        synchronized(whitelistedDomains) {
            whitelistedDomains.clear()
            whitelistedDomains.addAll(domains.map { it.lowercase().trim() })
        }
    }

    fun isDomainWhitelisted(url: String): Boolean {
        return try {
            val host = AdBlockManager.extractHost(url)?.lowercase() ?: return false
            synchronized(whitelistedDomains) {
                whitelistedDomains.contains(host) || whitelistedDomains.any { host.endsWith(".$it") }
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Inspects resource request for ad or tracker blocking
     */
    fun shouldBlockResource(
        requestUrl: String,
        pageUrl: String,
        adBlockingEnabled: Boolean,
        trackerBlockingEnabled: Boolean
    ): BlockType {
        if (isDomainWhitelisted(pageUrl)) {
            return BlockType.NONE
        }

        val type = AdBlockManager.inspectUrl(requestUrl)
        return when (type) {
            BlockType.AD -> if (adBlockingEnabled) BlockType.AD else BlockType.NONE
            BlockType.TRACKER -> if (trackerBlockingEnabled) BlockType.TRACKER else BlockType.NONE
            BlockType.SUSPICIOUS -> if (adBlockingEnabled || trackerBlockingEnabled) BlockType.SUSPICIOUS else BlockType.NONE
            BlockType.MALICIOUS -> BlockType.MALICIOUS
            BlockType.NONE -> BlockType.NONE
        }
    }

    /**
     * Checks if a scheme is an external app launcher (like intent:, market:, fb:, whatsapp:, etc.)
     */
    fun isExternalAppScheme(scheme: String?): Boolean {
        if (scheme == null) return false
        val s = scheme.lowercase()
        return s !in listOf("http", "https", "about", "javascript", "data", "blob", "file", "nara")
    }

    /**
     * Checks if a URL points to a known malicious destination
     */
    fun isMaliciousUrl(url: String): Boolean {
        return try {
            val host = AdBlockManager.extractHost(url) ?: return false
            FilterLists.isMaliciousHost(host)
        } catch (_: Exception) {
            false
        }
    }
}
