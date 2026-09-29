package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.DownloadEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.local.entity.WhitelistEntity
import com.example.data.model.BrowserSettings
import com.example.data.model.BrowserTab
import com.example.data.model.NaraMascotState
import com.example.data.model.ProtectionStats
import com.example.data.model.SecurityState
import com.example.data.repository.BrowserRepository
import com.example.download.DownloadPromptData
import com.example.download.NaraDownloadManager
import com.example.protection.BlockType
import com.example.protection.ProtectionManager
import com.example.tab.TabManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SheetType {
    NONE,
    TAB_MANAGER,
    MENU,
    BOOKMARKS,
    HISTORY,
    DOWNLOADS,
    SETTINGS,
    WHITELIST,
    PROTECTION_PANEL
}

data class ContextMenuData(
    val type: ContextMenuType,
    val url: String,
    val extra: String? = null
)

enum class ContextMenuType {
    LINK,
    IMAGE
}

sealed class BrowserNavigationEvent {
    data class LoadUrl(val url: String) : BrowserNavigationEvent()
    object GoBack : BrowserNavigationEvent()
    object GoForward : BrowserNavigationEvent()
    object Reload : BrowserNavigationEvent()
    object StopLoading : BrowserNavigationEvent()
    data class FindInPage(val query: String, val forward: Boolean = true) : BrowserNavigationEvent()
    object ClearFindMatches : BrowserNavigationEvent()
    data class ToggleDesktopMode(val enabled: Boolean) : BrowserNavigationEvent()
}

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    val repository = BrowserRepository(application)
    val tabManager = TabManager()
    val protectionManager = ProtectionManager()
    val downloadManager = NaraDownloadManager(application, repository, viewModelScope)

    // Reactive streams from Repository
    val allHistory: StateFlow<List<HistoryEntity>> = repository.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBookmarks: StateFlow<List<BookmarkEntity>> = repository.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWhitelist: StateFlow<List<WhitelistEntity>> = repository.getAllWhitelist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDownloads: StateFlow<List<DownloadEntity>> = repository.getAllDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings
    private val _settings = MutableStateFlow(repository.loadSettings())
    val settings = _settings.asStateFlow()

    // Active BottomSheet / Fullscreen Dialog
    private val _activeSheet = MutableStateFlow(SheetType.NONE)
    val activeSheet = _activeSheet.asStateFlow()

    // Navigation events sent to WebView
    private val _navEvents = MutableSharedFlow<BrowserNavigationEvent>(extraBufferCapacity = 10)
    val navEvents: SharedFlow<BrowserNavigationEvent> = _navEvents.asSharedFlow()

    // Notification / Toast message from Nara
    private val _naraNotice = MutableSharedFlow<String>(extraBufferCapacity = 5)
    val naraNotice: SharedFlow<String> = _naraNotice.asSharedFlow()

    // Prompts
    private val _downloadPrompt = MutableStateFlow<DownloadPromptData?>(null)
    val downloadPrompt = _downloadPrompt.asStateFlow()

    private val _bookmarkPrompt = MutableStateFlow<String?>(null)
    val bookmarkPrompt = _bookmarkPrompt.asStateFlow()

    private val _maliciousUrlWarning = MutableStateFlow<String?>(null)
    val maliciousUrlWarning = _maliciousUrlWarning.asStateFlow()

    // Context Menu for Long Press
    private val _contextMenu = MutableStateFlow<ContextMenuData?>(null)
    val contextMenu = _contextMenu.asStateFlow()

    // Find in Page state
    private val _findInPageActive = MutableStateFlow(false)
    val findInPageActive = _findInPageActive.asStateFlow()
    private val _findQuery = MutableStateFlow("")
    val findQuery = _findQuery.asStateFlow()
    private val _findMatches = MutableStateFlow(Pair(0, 0)) // current, total
    val findMatches = _findMatches.asStateFlow()

    // Global mascot state for banners/modals
    private val _mascotState = MutableStateFlow(NaraMascotState.NORMAL)
    val mascotState = _mascotState.asStateFlow()

    // Search query for history
    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery = _historySearchQuery.asStateFlow()

    init {
        // Keep Whitelist in sync with ProtectionManager
        viewModelScope.launch {
            allWhitelist.collect { list ->
                protectionManager.setWhitelistedDomains(list.map { it.domain })
            }
        }
    }

    // Tab Operations
    fun openNewTab(url: String = "", isIncognito: Boolean = tabManager.isIncognitoMode.value) {
        tabManager.openNewTab(url, isIncognito)
        _activeSheet.value = SheetType.NONE
        if (url.isNotBlank()) {
            _navEvents.tryEmit(BrowserNavigationEvent.LoadUrl(url))
        }
    }

    fun switchTab(tabId: String, isIncognito: Boolean) {
        tabManager.switchTab(tabId, isIncognito)
        _activeSheet.value = SheetType.NONE
    }

    fun closeTab(tabId: String) {
        tabManager.closeTab(tabId)
    }

    fun restoreLastClosedTab() {
        val restored = tabManager.restoreLastClosedTab()
        if (restored != null && !restored.isNewTab) {
            _navEvents.tryEmit(BrowserNavigationEvent.LoadUrl(restored.url))
            _naraNotice.tryEmit("Tab berhasil dipulihkan, Tuan~")
        }
    }

    fun openSheet(sheet: SheetType) {
        _activeSheet.value = sheet
    }

    fun closeSheet() {
        _activeSheet.value = SheetType.NONE
    }

    // Address Bar / Navigation
    fun navigateTo(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return

        val targetUrl = formatUrlOrSearch(trimmed, _settings.value.searchEngineUrl)

        // Check if malicious domain warning should be triggered
        if (_settings.value.maliciousSiteWarning && protectionManager.isMaliciousUrl(targetUrl)) {
            _maliciousUrlWarning.value = targetUrl
            _mascotState.value = NaraMascotState.WARNING
            return
        }

        tabManager.updateCurrentTab {
            it.copy(
                url = targetUrl,
                title = trimmed,
                isLoading = true,
                securityState = if (targetUrl.startsWith("https://")) SecurityState.SECURE else SecurityState.INSECURE
            )
        }
        _navEvents.tryEmit(BrowserNavigationEvent.LoadUrl(targetUrl))
    }

    fun proceedToMaliciousSite(url: String) {
        _maliciousUrlWarning.value = null
        _mascotState.value = NaraMascotState.NORMAL
        tabManager.updateCurrentTab {
            it.copy(
                url = url,
                title = url,
                isLoading = true,
                securityState = SecurityState.WARNING
            )
        }
        _navEvents.tryEmit(BrowserNavigationEvent.LoadUrl(url))
    }

    fun dismissMaliciousWarning() {
        _maliciousUrlWarning.value = null
        _mascotState.value = NaraMascotState.NORMAL
    }

    private fun formatUrlOrSearch(input: String, searchEngine: String): String {
        // If already full url
        if (input.startsWith("http://", ignoreCase = true) || input.startsWith("https://", ignoreCase = true)) {
            return input
        }
        // If looks like a domain name (e.g., example.com, sub.domain.org/path, localhost:8080)
        val domainPattern = Regex("^[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}(/.*)?$")
        if (domainPattern.matches(input) || input.startsWith("localhost") || input.contains(".com") || input.contains(".org") || input.contains(".net") || input.contains(".id") || input.contains(".io")) {
            return "https://$input"
        }
        // Otherwise search via search engine
        return searchEngine + Uri.encode(input)
    }

    fun goBack() {
        _navEvents.tryEmit(BrowserNavigationEvent.GoBack)
    }

    fun goForward() {
        _navEvents.tryEmit(BrowserNavigationEvent.GoForward)
    }

    fun reloadOrStop() {
        val current = tabManager.currentTab ?: return
        if (current.isLoading) {
            _navEvents.tryEmit(BrowserNavigationEvent.StopLoading)
        } else {
            _navEvents.tryEmit(BrowserNavigationEvent.Reload)
        }
    }

    // Called by WebView callbacks
    fun onPageStarted(url: String) {
        val isHttps = url.startsWith("https://", ignoreCase = true)
        tabManager.updateCurrentTab {
            it.copy(
                url = url,
                isLoading = true,
                progress = 10,
                securityState = if (isHttps) SecurityState.SECURE else SecurityState.INSECURE
            )
        }
    }

    fun onPageFinished(url: String, title: String?, canBack: Boolean, canForward: Boolean) {
        val cleanTitle = if (title.isNullOrBlank()) url else title
        val isHttps = url.startsWith("https://", ignoreCase = true)

        tabManager.updateCurrentTab {
            it.copy(
                url = url,
                title = cleanTitle,
                isLoading = false,
                progress = 100,
                canGoBack = canBack,
                canGoForward = canForward,
                securityState = if (isHttps) SecurityState.SECURE else SecurityState.INSECURE
            )
        }

        // Save history if not incognito and not new tab
        val current = tabManager.currentTab
        if (current != null && !current.isIncognito && !current.isNewTab) {
            viewModelScope.launch {
                repository.addHistory(cleanTitle, url)
            }
        }
    }

    fun onTitleChanged(title: String?) {
        if (!title.isNullOrBlank()) {
            tabManager.updateCurrentTab { it.copy(title = title) }
        }
    }

    fun onProgressChanged(newProgress: Int) {
        tabManager.updateCurrentTab {
            it.copy(
                progress = newProgress,
                isLoading = newProgress < 100
            )
        }
    }

    fun onBlockedResource(type: BlockType) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            tabManager.updateCurrentTab { tab ->
                when (type) {
                    BlockType.AD -> tab.copy(adsBlocked = tab.adsBlocked + 1)
                    BlockType.TRACKER -> tab.copy(trackersBlocked = tab.trackersBlocked + 1)
                    BlockType.SUSPICIOUS -> tab.copy(suspiciousBlocked = tab.suspiciousBlocked + 1)
                    else -> tab
                }
            }
        }
    }

    fun onBlockedRedirect() {
        tabManager.updateCurrentTab { it.copy(redirectsBlocked = it.redirectsBlocked + 1) }
        _naraNotice.tryEmit("Nara memblokir pengalihan yang mencurigakan, Tuan~")
    }

    fun onBlockedPopup() {
        tabManager.updateCurrentTab { it.copy(popupsBlocked = it.popupsBlocked + 1) }
        _naraNotice.tryEmit("Nara memblokir popup mencurigakan, Tuan~")
    }

    fun onBlockedExternalApp() {
        _naraNotice.tryEmit("Nara memblokir pembukaan aplikasi eksternal otomatis, Tuan~")
    }

    // Bookmark Prompt & Actions
    fun requestBookmarkCurrentPage() {
        val current = tabManager.currentTab ?: return
        if (current.isNewTab) return
        _bookmarkPrompt.value = current.url
    }

    fun dismissBookmarkPrompt() {
        _bookmarkPrompt.value = null
    }

    fun saveCurrentBookmark() {
        val current = tabManager.currentTab ?: return
        viewModelScope.launch {
            repository.addBookmark(current.title, current.url)
            _bookmarkPrompt.value = null
            _naraNotice.tryEmit("Halaman berhasil disimpan ke Bookmark, Tuan~")
        }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch {
            repository.removeBookmark(id)
        }
    }

    // History Actions
    fun updateHistorySearch(query: String) {
        _historySearchQuery.value = query
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            _naraNotice.tryEmit("Semua history telah dibersihkan, Tuan~")
        }
    }

    // Whitelist Actions
    fun toggleWhitelistForCurrentSite() {
        val current = tabManager.currentTab ?: return
        val domain = current.displayDomain
        if (domain == "Tab Baru") return

        viewModelScope.launch {
            if (protectionManager.isDomainWhitelisted(current.url)) {
                repository.removeWhitelist(domain)
                _naraNotice.tryEmit("Protection diaktifkan kembali untuk $domain")
            } else {
                repository.addWhitelist(domain)
                _naraNotice.tryEmit("Protection dimatikan untuk $domain")
            }
        }
    }

    fun removeWhitelistDomain(domain: String) {
        viewModelScope.launch {
            repository.removeWhitelist(domain)
        }
    }

    fun clearAllWhitelist() {
        viewModelScope.launch {
            repository.clearAllWhitelist()
            _naraNotice.tryEmit("Semua whitelist dihapus, Tuan~")
        }
    }

    // Incognito
    fun toggleIncognito() {
        val currentIsIncognito = tabManager.isIncognitoMode.value
        if (!currentIsIncognito) {
            // Open new incognito tab
            tabManager.openNewTab(isIncognito = true)
            _activeSheet.value = SheetType.NONE
            _naraNotice.tryEmit("Mode Incognito aktif: Sesi penjelajahan aman dan tidak disimpan.")
        } else {
            // Switch back to normal
            tabManager.switchTab(
                tabManager.normalTabs.value.firstOrNull()?.id ?: tabManager.openNewTab().id,
                isIncognito = false
            )
            _activeSheet.value = SheetType.NONE
        }
    }

    fun closeAllIncognito() {
        // Clear cookies and session storage
        CookieManager.getInstance().removeAllCookies(null)
        WebStorage.getInstance().deleteAllData()
        tabManager.clearIncognitoSession()
        _naraNotice.tryEmit("Sesi Incognito ditutup dan jejak telah dihapus, Tuan~")
    }

    // Downloads
    fun promptDownload(promptData: DownloadPromptData) {
        if (!_settings.value.askBeforeDownload) {
            startDownload(promptData)
        } else {
            _downloadPrompt.value = promptData
            _mascotState.value = NaraMascotState.DOWNLOAD
        }
    }

    fun dismissDownloadPrompt() {
        _downloadPrompt.value = null
        _mascotState.value = NaraMascotState.NORMAL
    }

    fun confirmDownload() {
        val data = _downloadPrompt.value ?: return
        startDownload(data)
        _downloadPrompt.value = null
    }

    private fun startDownload(data: DownloadPromptData) {
        val id = downloadManager.startDownload(data)
        if (id != -1L) {
            _mascotState.value = NaraMascotState.DOWNLOAD
            _naraNotice.tryEmit("Mengunduh ${data.suggestedFileName}...")
        } else {
            _mascotState.value = NaraMascotState.DOWNLOAD_FAILED
            _naraNotice.tryEmit("Hiks... gagal memulai download, Tuan. 😔")
        }
    }

    fun cancelActiveDownload(downloadId: Long) {
        downloadManager.cancelDownload(downloadId)
    }

    fun deleteDownloadHistory(id: Long) {
        viewModelScope.launch {
            repository.deleteDownload(id)
        }
    }

    fun clearAllDownloadHistory() {
        viewModelScope.launch {
            repository.clearAllDownloads()
            _naraNotice.tryEmit("Riwayat download dibersihkan.")
        }
    }

    // Desktop Site Toggle
    fun toggleDesktopSite() {
        val current = tabManager.currentTab ?: return
        val newMode = !current.isDesktopMode
        tabManager.updateCurrentTab { it.copy(isDesktopMode = newMode) }
        _navEvents.tryEmit(BrowserNavigationEvent.ToggleDesktopMode(newMode))
        _naraNotice.tryEmit(if (newMode) "Beralih ke tampilan Desktop" else "Beralih ke tampilan Mobile")
    }

    // Find In Page
    fun openFindInPage() {
        _findInPageActive.value = true
        _findQuery.value = ""
        _activeSheet.value = SheetType.NONE
    }

    fun closeFindInPage() {
        _findInPageActive.value = false
        _findQuery.value = ""
        _navEvents.tryEmit(BrowserNavigationEvent.ClearFindMatches)
    }

    fun updateFindQuery(query: String) {
        _findQuery.value = query
        _navEvents.tryEmit(BrowserNavigationEvent.FindInPage(query, true))
    }

    fun findNext() {
        _navEvents.tryEmit(BrowserNavigationEvent.FindInPage(_findQuery.value, true))
    }

    fun findPrevious() {
        _navEvents.tryEmit(BrowserNavigationEvent.FindInPage(_findQuery.value, false))
    }

    fun updateFindMatches(active: Int, total: Int) {
        _findMatches.value = Pair(active, total)
    }

    // Context Menu
    fun showContextMenu(data: ContextMenuData) {
        _contextMenu.value = data
    }

    fun dismissContextMenu() {
        _contextMenu.value = null
    }

    // Settings Updates
    fun updateSettings(newSettings: BrowserSettings) {
        _settings.value = newSettings
        repository.saveSettings(newSettings)
    }

    // Clear Browsing Data
    fun clearBrowsingData(cookies: Boolean, cache: Boolean, history: Boolean) {
        viewModelScope.launch {
            if (cookies) {
                CookieManager.getInstance().removeAllCookies(null)
            }
            if (cache) {
                WebStorage.getInstance().deleteAllData()
            }
            if (history) {
                repository.clearAllHistory()
            }
            _naraNotice.tryEmit("Data penjelajahan berhasil dibersihkan, Tuan~")
        }
    }
}
