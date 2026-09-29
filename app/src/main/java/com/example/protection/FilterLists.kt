package com.example.protection

object FilterLists {

    // Common advertising servers & ad exchanges
    val AD_HOSTS = hashSetOf(
        "doubleclick.net",
        "googleads.g.doubleclick.net",
        "pagead2.googlesyndication.com",
        "adservice.google.com",
        "pubmatic.com",
        "openx.net",
        "rubiconproject.com",
        "criteo.com",
        "criteo.net",
        "casalemedia.com",
        "adnxs.com",
        "advertising.com",
        "admob.com",
        "amazon-adsystem.com",
        "adsterra.com",
        "popads.net",
        "popcash.net",
        "exoclick.com",
        "propellerads.com",
        "outbrain.com",
        "taboola.com",
        "mgid.com",
        "adpushup.com",
        "infolinks.com",
        "media.net",
        "adcolony.com",
        "applovin.com",
        "unityads.unity3d.com",
        "vungle.com",
        "chartboost.com",
        "smartadserver.com",
        "adtechus.com",
        "bidswitch.net",
        "revcontent.com",
        "zergnet.com",
        "adroll.com",
        "adform.net",
        "carbonads.net",
        "srv.buysellads.com",
        "adblade.com",
        "adsupply.com",
        "bidvertiser.com",
        "trafficjunky.com",
        "adtrue.com"
    )

    // Common tracking & profiling domains
    val TRACKER_HOSTS = hashSetOf(
        "google-analytics.com",
        "analytics.google.com",
        "googletagmanager.com",
        "scorecardresearch.com",
        "quantserve.com",
        "hotjar.com",
        "statcounter.com",
        "mixpanel.com",
        "segment.io",
        "segment.com",
        "amplitude.com",
        "fullstory.com",
        "clarity.ms",
        "crazyegg.com",
        "mouseflow.com",
        "branch.io",
        "appsflyer.com",
        "adjust.com",
        "kochava.com",
        "singular.net",
        "pixel.facebook.com",
        "connect.facebook.net",
        "bat.bing.com",
        "yandex.ru/metrika",
        "mc.yandex.ru",
        "telemetry",
        "newrelic.com",
        "nr-data.net",
        "sentry.io",
        "bugsnag.com"
    )

    // Common malicious or scam / phishing indicator domains or hosts
    val MALICIOUS_HOSTS = hashSetOf(
        "malware-test.com",
        "testsafebrowsing.appspot.com",
        "wicar.org",
        "phishing-test.org",
        "suspicious-alert-update.com",
        "free-iphone-winner.xyz",
        "clean-my-android-phone-fast.net",
        "your-phone-is-infected-virus.top",
        "claim-prize-bonus2026.online"
    )

    // Keywords in URLs often associated with popunder / sneaky tracking scripts
    val SUSPICIOUS_URL_KEYWORDS = listOf(
        "/popunder.",
        "/adserver/",
        "/ads/banners/",
        "clicktrack.",
        "redirect_tracker=",
        "promo_ad=",
        "affiliate_tracker"
    )

    fun isAdHost(host: String): Boolean {
        val cleanHost = host.lowercase().trim()
        if (AD_HOSTS.contains(cleanHost)) return true
        for (adHost in AD_HOSTS) {
            if (cleanHost.endsWith(".$adHost")) return true
        }
        return false
    }

    fun isTrackerHost(host: String): Boolean {
        val cleanHost = host.lowercase().trim()
        if (TRACKER_HOSTS.contains(cleanHost)) return true
        for (trackerHost in TRACKER_HOSTS) {
            if (cleanHost.endsWith(".$trackerHost")) return true
        }
        return false
    }

    fun isMaliciousHost(host: String): Boolean {
        val cleanHost = host.lowercase().trim()
        if (MALICIOUS_HOSTS.contains(cleanHost)) return true
        for (maliciousHost in MALICIOUS_HOSTS) {
            if (cleanHost.endsWith(".$maliciousHost")) return true
        }
        return false
    }

    fun containsSuspiciousKeywords(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return SUSPICIOUS_URL_KEYWORDS.any { lowerUrl.contains(it) }
    }
}
