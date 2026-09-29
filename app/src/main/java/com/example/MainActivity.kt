package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.download.DownloadPromptData
import com.example.ui.components.BookmarkConfirmationDialog
import com.example.ui.components.BrowserTopBar
import com.example.ui.components.ContextMenuDialog
import com.example.ui.components.DownloadConfirmationDialog
import com.example.ui.components.FindInPageBar
import com.example.ui.components.MaliciousSiteWarningDialog
import com.example.ui.screens.BookmarksSheet
import com.example.ui.screens.BrowserScreen
import com.example.ui.screens.DownloadsSheet
import com.example.ui.screens.HistorySheet
import com.example.ui.screens.MainMenuSheet
import com.example.ui.screens.NewTabScreen
import com.example.ui.screens.ProtectionSheet
import com.example.ui.screens.SettingsSheet
import com.example.ui.screens.TabManagerSheet
import com.example.ui.screens.WhitelistSheet
import com.example.ui.theme.NaraBrowserTheme
import com.example.ui.theme.NaraDarkBackground
import com.example.ui.theme.NaraSurface
import com.example.ui.viewmodel.BrowserViewModel
import com.example.ui.viewmodel.SheetType
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle URL opened from external intent
        handleIntent(intent)

        setContent {
            NaraBrowserTheme(darkTheme = true) {
                NaraBrowserApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val data = intent.dataString
            if (!data.isNullOrBlank()) {
                viewModel.navigateTo(data)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NaraBrowserApp(viewModel: BrowserViewModel) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Tab state
    val normalTabs by viewModel.tabManager.normalTabs.collectAsStateWithLifecycle()
    val incognitoTabs by viewModel.tabManager.incognitoTabs.collectAsStateWithLifecycle()
    val currentTabId by viewModel.tabManager.currentTabId.collectAsStateWithLifecycle()
    val isIncognito by viewModel.tabManager.isIncognitoMode.collectAsStateWithLifecycle()
    val currentTab = viewModel.tabManager.currentTab

    val totalTabsCount = if (isIncognito) incognitoTabs.size else normalTabs.size

    // Settings & State
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val activeSheet by viewModel.activeSheet.collectAsStateWithLifecycle()
    val findInPageActive by viewModel.findInPageActive.collectAsStateWithLifecycle()
    val findQuery by viewModel.findQuery.collectAsStateWithLifecycle()
    val findMatches by viewModel.findMatches.collectAsStateWithLifecycle()

    // Dialog prompts
    val downloadPrompt by viewModel.downloadPrompt.collectAsStateWithLifecycle()
    val bookmarkPrompt by viewModel.bookmarkPrompt.collectAsStateWithLifecycle()
    val maliciousWarning by viewModel.maliciousUrlWarning.collectAsStateWithLifecycle()
    val contextMenuData by viewModel.contextMenu.collectAsStateWithLifecycle()

    // Data lists
    val historyList by viewModel.allHistory.collectAsStateWithLifecycle()
    val bookmarksList by viewModel.allBookmarks.collectAsStateWithLifecycle()
    val whitelistList by viewModel.allWhitelist.collectAsStateWithLifecycle()
    val activeDownloads by viewModel.downloadManager.activeDownloads.collectAsStateWithLifecycle()
    val downloadHistory by viewModel.allDownloads.collectAsStateWithLifecycle()
    val historySearchQuery by viewModel.historySearchQuery.collectAsStateWithLifecycle()

    val isCurrentSiteWhitelisted = currentTab != null && viewModel.protectionManager.isDomainWhitelisted(currentTab.url)

    // Collect Nara notices to show in snackbar
    LaunchedEffect(Unit) {
        viewModel.naraNotice.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(NaraDarkBackground),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.statusBars)
                .background(NaraDarkBackground)
        ) {
            // Top Navigation / Address Bar
            BrowserTopBar(
                currentTab = currentTab,
                tabCount = totalTabsCount,
                isIncognito = isIncognito,
                onNavigate = { viewModel.navigateTo(it) },
                onBack = { viewModel.goBack() },
                onForward = { viewModel.goForward() },
                onReloadOrStop = { viewModel.reloadOrStop() },
                onOpenProtectionPanel = { viewModel.openSheet(SheetType.PROTECTION_PANEL) },
                onOpenTabManager = { viewModel.openSheet(SheetType.TAB_MANAGER) },
                onOpenMenu = { viewModel.openSheet(SheetType.MENU) }
            )

            // Find in Page Bar
            AnimatedVisibility(
                visible = findInPageActive,
                enter = slideInVertically(),
                exit = slideOutVertically()
            ) {
                FindInPageBar(
                    query = findQuery,
                    matchCurrent = findMatches.first,
                    matchTotal = findMatches.second,
                    onQueryChange = { viewModel.updateFindQuery(it) },
                    onFindNext = { viewModel.findNext() },
                    onFindPrevious = { viewModel.findPrevious() },
                    onClose = { viewModel.closeFindInPage() }
                )
            }

            // Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (currentTab == null || currentTab.isNewTab) {
                    NewTabScreen(
                        isIncognito = isIncognito,
                        onSearchOrNavigate = { viewModel.navigateTo(it) },
                        onOpenBookmarks = { viewModel.openSheet(SheetType.BOOKMARKS) },
                        onOpenHistory = { viewModel.openSheet(SheetType.HISTORY) },
                        onOpenDownloads = { viewModel.openSheet(SheetType.DOWNLOADS) },
                        onOpenProtection = { viewModel.openSheet(SheetType.PROTECTION_PANEL) }
                    )
                } else {
                    BrowserScreen(
                        currentTab = currentTab,
                        settings = settings,
                        protectionManager = viewModel.protectionManager,
                        navEvents = viewModel.navEvents,
                        onPageStarted = { viewModel.onPageStarted(it) },
                        onPageFinished = { url, title, canBack, canForward ->
                            viewModel.onPageFinished(url, title, canBack, canForward)
                        },
                        onTitleChanged = { viewModel.onTitleChanged(it) },
                        onProgressChanged = { viewModel.onProgressChanged(it) },
                        onBlockedResource = { viewModel.onBlockedResource(it) },
                        onBlockedRedirect = { viewModel.onBlockedRedirect() },
                        onBlockedPopup = { viewModel.onBlockedPopup() },
                        onBlockedExternalApp = { viewModel.onBlockedExternalApp() },
                        onPromptDownload = { url, userAgent, contentDisposition, mimeType, contentLength ->
                            val parsed = viewModel.downloadManager.parseDownloadInfo(
                                url = url,
                                userAgent = userAgent,
                                contentDisposition = contentDisposition,
                                mimeType = mimeType,
                                contentLength = contentLength
                            )
                            viewModel.promptDownload(parsed)
                        },
                        onShowContextMenu = { viewModel.showContextMenu(it) },
                        onUpdateFindMatches = { active, total -> viewModel.updateFindMatches(active, total) },
                        onBackHandled = {
                            if (!currentTab.isNewTab) {
                                viewModel.navigateTo("")
                            }
                        }
                    )
                }
            }
        }

        // Bottom Sheets for Sub-screens & Managers
        if (activeSheet != SheetType.NONE) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeSheet() },
                sheetState = sheetState,
                containerColor = NaraSurface,
                dragHandle = null
            ) {
                when (activeSheet) {
                    SheetType.TAB_MANAGER -> {
                        TabManagerSheet(
                            normalTabs = normalTabs,
                            incognitoTabs = incognitoTabs,
                            currentTabId = currentTabId,
                            isIncognitoCurrent = isIncognito,
                            canRestoreTab = viewModel.tabManager.canRestoreTab,
                            onSelectTab = { id, isIncog -> viewModel.switchTab(id, isIncog) },
                            onCloseTab = { viewModel.closeTab(it) },
                            onOpenNewTab = { viewModel.openNewTab(isIncognito = it) },
                            onRestoreTab = { viewModel.restoreLastClosedTab() },
                            onCloseSheet = { viewModel.closeSheet() }
                        )
                    }
                    SheetType.PROTECTION_PANEL -> {
                        ProtectionSheet(
                            currentTab = currentTab,
                            isWhitelisted = isCurrentSiteWhitelisted,
                            settings = settings,
                            onToggleProtection = { viewModel.updateSettings(settings.copy(protectionEnabled = it)) },
                            onToggleWhitelistForSite = { viewModel.toggleWhitelistForCurrentSite() },
                            onOpenSettings = { viewModel.openSheet(SheetType.SETTINGS) },
                            onClose = { viewModel.closeSheet() }
                        )
                    }
                    SheetType.MENU -> {
                        MainMenuSheet(
                            isIncognito = isIncognito,
                            isDesktopSite = currentTab?.isDesktopMode == true,
                            onOpenDownloads = { viewModel.openSheet(SheetType.DOWNLOADS) },
                            onOpenBookmarks = { viewModel.openSheet(SheetType.BOOKMARKS) },
                            onOpenHistory = { viewModel.openSheet(SheetType.HISTORY) },
                            onToggleIncognito = { viewModel.toggleIncognito() },
                            onOpenSettings = { viewModel.openSheet(SheetType.SETTINGS) },
                            onToggleDesktopSite = {
                                viewModel.toggleDesktopSite()
                                viewModel.closeSheet()
                            },
                            onOpenFindInPage = { viewModel.openFindInPage() },
                            onBookmarkPage = {
                                viewModel.closeSheet()
                                viewModel.requestBookmarkCurrentPage()
                            },
                            onClose = { viewModel.closeSheet() }
                        )
                    }
                    SheetType.BOOKMARKS -> {
                        BookmarksSheet(
                            bookmarks = bookmarksList,
                            onSelectBookmark = {
                                viewModel.navigateTo(it)
                                viewModel.closeSheet()
                            },
                            onDeleteBookmark = { viewModel.deleteBookmark(it) },
                            onClose = { viewModel.closeSheet() }
                        )
                    }
                    SheetType.HISTORY -> {
                        HistorySheet(
                            historyList = historyList,
                            searchQuery = historySearchQuery,
                            onSearchChange = { viewModel.updateHistorySearch(it) },
                            onSelectHistory = {
                                viewModel.navigateTo(it)
                                viewModel.closeSheet()
                            },
                            onClearAllHistory = { viewModel.clearAllHistory() },
                            onClose = { viewModel.closeSheet() }
                        )
                    }
                    SheetType.DOWNLOADS -> {
                        DownloadsSheet(
                            activeDownloads = activeDownloads,
                            downloadHistory = downloadHistory,
                            onCancelActiveDownload = { viewModel.cancelActiveDownload(it) },
                            onDeleteDownloadHistory = { viewModel.deleteDownloadHistory(it) },
                            onClearAllDownloads = { viewModel.clearAllDownloadHistory() },
                            onClose = { viewModel.closeSheet() }
                        )
                    }
                    SheetType.SETTINGS -> {
                        SettingsSheet(
                            settings = settings,
                            onUpdateSettings = { viewModel.updateSettings(it) },
                            onOpenWhitelist = { viewModel.openSheet(SheetType.WHITELIST) },
                            onClearBrowsingData = { cookies, cache, history ->
                                viewModel.clearBrowsingData(cookies, cache, history)
                            },
                            onClose = { viewModel.closeSheet() }
                        )
                    }
                    SheetType.WHITELIST -> {
                        WhitelistSheet(
                            whitelist = whitelistList,
                            onRemoveDomain = { viewModel.removeWhitelistDomain(it) },
                            onClearAll = { viewModel.clearAllWhitelist() },
                            onClose = { viewModel.openSheet(SheetType.SETTINGS) }
                        )
                    }
                    SheetType.NONE -> {}
                }
            }
        }

        // Bookmark Prompt Dialog
        if (bookmarkPrompt != null) {
            BookmarkConfirmationDialog(
                onDismiss = { viewModel.dismissBookmarkPrompt() },
                onConfirm = { viewModel.saveCurrentBookmark() }
            )
        }

        // Download Prompt Dialog
        downloadPrompt?.let { promptData ->
            DownloadConfirmationDialog(
                promptData = promptData,
                onDismiss = { viewModel.dismissDownloadPrompt() },
                onConfirm = { viewModel.confirmDownload() }
            )
        }

        // Malicious Warning Dialog
        maliciousWarning?.let { warningUrl ->
            MaliciousSiteWarningDialog(
                url = warningUrl,
                onBack = { viewModel.dismissMaliciousWarning() },
                onProceed = { viewModel.proceedToMaliciousSite(warningUrl) }
            )
        }

        // Context Menu Dialog (Long press Link or Image)
        contextMenuData?.let { menuData ->
            ContextMenuDialog(
                data = menuData,
                onDismiss = { viewModel.dismissContextMenu() },
                onOpenUrl = { viewModel.navigateTo(it) },
                onOpenInNewTab = { viewModel.openNewTab(it) },
                onDownloadUrl = {
                    val parsed = viewModel.downloadManager.parseDownloadInfo(
                        url = it,
                        userAgent = "",
                        contentDisposition = "",
                        mimeType = "",
                        contentLength = -1L
                    )
                    viewModel.promptDownload(parsed)
                }
            )
        }
    }
}
