package com.example.tab

import com.example.data.model.BrowserTab
import com.example.data.model.SecurityState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Stack

class TabManager {

    private val _normalTabs = MutableStateFlow<List<BrowserTab>>(listOf(BrowserTab(title = "Tab Baru", url = "")))
    val normalTabs = _normalTabs.asStateFlow()

    private val _incognitoTabs = MutableStateFlow<List<BrowserTab>>(emptyList())
    val incognitoTabs = _incognitoTabs.asStateFlow()

    private val _currentTabId = MutableStateFlow<String>(_normalTabs.value.first().id)
    val currentTabId = _currentTabId.asStateFlow()

    private val _isIncognitoMode = MutableStateFlow<Boolean>(false)
    val isIncognitoMode = _isIncognitoMode.asStateFlow()

    // Closed tabs stack for "Restore tab terakhir"
    private val closedTabsStack = Stack<BrowserTab>()

    val currentTab: BrowserTab?
        get() {
            val list = if (_isIncognitoMode.value) _incognitoTabs.value else _normalTabs.value
            return list.find { it.id == _currentTabId.value } ?: list.firstOrNull()
        }

    fun openNewTab(url: String = "", isIncognito: Boolean = _isIncognitoMode.value): BrowserTab {
        val newTab = BrowserTab(
            url = url,
            title = if (url.isBlank()) "Tab Baru" else url,
            isIncognito = isIncognito
        )
        if (isIncognito) {
            _incognitoTabs.value = _incognitoTabs.value + newTab
            _isIncognitoMode.value = true
        } else {
            _normalTabs.value = _normalTabs.value + newTab
            _isIncognitoMode.value = false
        }
        _currentTabId.value = newTab.id
        return newTab
    }

    fun switchTab(tabId: String, isIncognito: Boolean) {
        _isIncognitoMode.value = isIncognito
        _currentTabId.value = tabId
    }

    fun closeTab(tabId: String): BrowserTab? {
        val isIncognito = _isIncognitoMode.value
        val currentList = if (isIncognito) _incognitoTabs.value else _normalTabs.value
        val tabToClose = currentList.find { it.id == tabId } ?: return null

        // Save to restore stack only if normal tab (privacy rule: incognito tabs should not linger)
        if (!isIncognito && !tabToClose.isNewTab) {
            closedTabsStack.push(tabToClose)
        }

        val updatedList = currentList.filter { it.id != tabId }

        if (isIncognito) {
            _incognitoTabs.value = updatedList
            if (updatedList.isEmpty()) {
                // If all incognito tabs closed, switch back to normal tabs
                _isIncognitoMode.value = false
                val normal = _normalTabs.value
                _currentTabId.value = if (normal.isNotEmpty()) normal.first().id else openNewTab().id
            } else if (_currentTabId.value == tabId) {
                _currentTabId.value = updatedList.last().id
            }
        } else {
            if (updatedList.isEmpty()) {
                // Keep at least one tab open
                val brandNew = BrowserTab(title = "Tab Baru", url = "")
                _normalTabs.value = listOf(brandNew)
                _currentTabId.value = brandNew.id
            } else {
                _normalTabs.value = updatedList
                if (_currentTabId.value == tabId) {
                    _currentTabId.value = updatedList.last().id
                }
            }
        }
        return currentTab
    }

    fun restoreLastClosedTab(): BrowserTab? {
        if (closedTabsStack.isEmpty()) return null
        val restored = closedTabsStack.pop()
        val copy = restored.copy(id = java.util.UUID.randomUUID().toString())
        _normalTabs.value = _normalTabs.value + copy
        _isIncognitoMode.value = false
        _currentTabId.value = copy.id
        return copy
    }

    val canRestoreTab: Boolean
        get() = closedTabsStack.isNotEmpty()

    fun updateCurrentTab(transform: (BrowserTab) -> BrowserTab) {
        val isIncognito = _isIncognitoMode.value
        val list = if (isIncognito) _incognitoTabs.value else _normalTabs.value
        val tabId = _currentTabId.value

        val updated = list.map {
            if (it.id == tabId) transform(it) else it
        }

        if (isIncognito) {
            _incognitoTabs.value = updated
        } else {
            _normalTabs.value = updated
        }
    }

    fun updateTabById(tabId: String, transform: (BrowserTab) -> BrowserTab) {
        _normalTabs.value = _normalTabs.value.map { if (it.id == tabId) transform(it) else it }
        _incognitoTabs.value = _incognitoTabs.value.map { if (it.id == tabId) transform(it) else it }
    }

    fun clearIncognitoSession() {
        _incognitoTabs.value = emptyList()
        _isIncognitoMode.value = false
        val normal = _normalTabs.value
        _currentTabId.value = if (normal.isNotEmpty()) normal.first().id else openNewTab().id
    }
}
