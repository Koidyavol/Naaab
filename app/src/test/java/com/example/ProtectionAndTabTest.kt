package com.example

import com.example.protection.AdBlockManager
import com.example.protection.BlockType
import com.example.tab.TabManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtectionAndTabTest {

    @Test
    fun testAdBlockManager_detectsAdHosts() {
        val result = AdBlockManager.inspectUrl("https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js")
        assertEquals(BlockType.AD, result)
    }

    @Test
    fun testAdBlockManager_detectsTrackerHosts() {
        val result = AdBlockManager.inspectUrl("https://analytics.google.com/analytics.js")
        assertEquals(BlockType.TRACKER, result)
    }

    @Test
    fun testAdBlockManager_allowsSafeUrls() {
        val result = AdBlockManager.inspectUrl("https://en.wikipedia.org/wiki/Kotlin")
        assertEquals(BlockType.NONE, result)
    }

    @Test
    fun testTabManager_openAndCloseAndRestore() {
        val tabManager = TabManager()
        assertEquals(1, tabManager.normalTabs.value.size)

        val tab2 = tabManager.openNewTab("https://github.com", isIncognito = false)
        assertEquals(2, tabManager.normalTabs.value.size)
        assertEquals(tab2.id, tabManager.currentTabId.value)

        // Close tab2
        tabManager.closeTab(tab2.id)
        assertEquals(1, tabManager.normalTabs.value.size)
        assertTrue(tabManager.canRestoreTab)

        // Restore tab2
        val restored = tabManager.restoreLastClosedTab()
        assertNotNull(restored)
        assertEquals("https://github.com", restored?.url)
        assertEquals(2, tabManager.normalTabs.value.size)
    }

    @Test
    fun testTabManager_incognitoSeparation() {
        val tabManager = TabManager()
        tabManager.openNewTab("https://secret.com", isIncognito = true)

        assertTrue(tabManager.isIncognitoMode.value)
        assertEquals(1, tabManager.incognitoTabs.value.size)
        assertEquals(1, tabManager.normalTabs.value.size)

        tabManager.clearIncognitoSession()
        assertFalse(tabManager.isIncognitoMode.value)
        assertEquals(0, tabManager.incognitoTabs.value.size)
    }
}
