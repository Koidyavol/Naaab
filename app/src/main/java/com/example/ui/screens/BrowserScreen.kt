package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Message
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.DownloadListener
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.BrowserSettings
import com.example.data.model.BrowserTab
import com.example.protection.AdBlockManager
import com.example.protection.BlockType
import com.example.protection.ProtectionManager
import com.example.ui.theme.NaraDarkBackground
import com.example.ui.viewmodel.BrowserNavigationEvent
import com.example.ui.viewmodel.BrowserViewModel
import com.example.ui.viewmodel.ContextMenuData
import com.example.ui.viewmodel.ContextMenuType
import kotlinx.coroutines.flow.SharedFlow

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    currentTab: BrowserTab?,
    settings: BrowserSettings,
    protectionManager: ProtectionManager,
    navEvents: SharedFlow<BrowserNavigationEvent>,
    onPageStarted: (String) -> Unit,
    onPageFinished: (url: String, title: String?, canBack: Boolean, canForward: Boolean) -> Unit,
    onTitleChanged: (String?) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onBlockedResource: (BlockType) -> Unit,
    onBlockedRedirect: () -> Unit,
    onBlockedPopup: () -> Unit,
    onBlockedExternalApp: () -> Unit,
    onPromptDownload: (url: String, userAgent: String, contentDisposition: String, mimeType: String, contentLength: Long) -> Unit,
    onShowContextMenu: (ContextMenuData) -> Unit,
    onUpdateFindMatches: (Int, Int) -> Unit,
    onBackHandled: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentUrlTracked by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    // Intercept back button if webview can navigate backward
    BackHandler(enabled = currentTab?.canGoBack == true || (currentTab != null && !currentTab.isNewTab)) {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onBackHandled()
        }
    }

    // Handle navigation events triggered by the ViewModel or TopBar
    LaunchedEffect(webViewInstance) {
        navEvents.collect { event ->
            val wv = webViewInstance ?: return@collect
            when (event) {
                is BrowserNavigationEvent.LoadUrl -> {
                    hasError = false
                    wv.loadUrl(event.url)
                }
                is BrowserNavigationEvent.GoBack -> {
                    if (wv.canGoBack()) wv.goBack()
                }
                is BrowserNavigationEvent.GoForward -> {
                    if (wv.canGoForward()) wv.goForward()
                }
                is BrowserNavigationEvent.Reload -> {
                    hasError = false
                    wv.reload()
                }
                is BrowserNavigationEvent.StopLoading -> {
                    wv.stopLoading()
                }
                is BrowserNavigationEvent.FindInPage -> {
                    if (event.query.isNotBlank()) {
                        wv.findAllAsync(event.query)
                        wv.setFindListener { activeMatchOrdinal, numberOfMatches, _ ->
                            onUpdateFindMatches(activeMatchOrdinal + 1, numberOfMatches)
                        }
                        if (event.forward) {
                            wv.findNext(true)
                        } else {
                            wv.findNext(false)
                        }
                    }
                }
                is BrowserNavigationEvent.ClearFindMatches -> {
                    wv.clearMatches()
                }
                is BrowserNavigationEvent.ToggleDesktopMode -> {
                    wv.settings.userAgentString = if (event.enabled) DESKTOP_USER_AGENT else null
                    wv.settings.useWideViewPort = event.enabled
                    wv.settings.loadWithOverviewMode = event.enabled
                    wv.reload()
                }
            }
        }
    }

    // Synchronize URL navigation when currentTab changes
    LaunchedEffect(currentTab?.url, webViewInstance) {
        val target = currentTab?.url
        val wv = webViewInstance
        if (wv != null && !target.isNullOrBlank() && currentTab?.isNewTab == false) {
            if (wv.url != target) {
                hasError = false
                wv.loadUrl(target)
            }
        }
    }

    // Synchronize Desktop Mode or Settings changes
    LaunchedEffect(currentTab?.isDesktopMode) {
        webViewInstance?.let { wv ->
            val isDesktop = currentTab?.isDesktopMode == true
            wv.settings.userAgentString = if (isDesktop) DESKTOP_USER_AGENT else null
            wv.settings.useWideViewPort = isDesktop
            wv.settings.loadWithOverviewMode = isDesktop
        }
    }

    LaunchedEffect(settings.javascriptEnabled) {
        webViewInstance?.settings?.javaScriptEnabled = settings.javascriptEnabled
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NaraDarkBackground)
    ) {
        if (hasError) {
            ErrorScreen(
                onRetry = {
                    hasError = false
                    webViewInstance?.reload()
                }
            )
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        // WebSettings configuration
                        this.settings.apply {
                            javaScriptEnabled = settings.javascriptEnabled
                            domStorageEnabled = true
                            databaseEnabled = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            setSupportMultipleWindows(true)
                            javaScriptCanOpenWindowsAutomatically = false
                            builtInZoomControls = true
                            displayZoomControls = false
                            allowFileAccess = false
                            allowContentAccess = false
                        }

                        // Long press listener for Link & Image context menu
                        setOnLongClickListener {
                            val result = hitTestResult
                            when (result.type) {
                                WebView.HitTestResult.SRC_ANCHOR_TYPE,
                                WebView.HitTestResult.ANCHOR_TYPE -> {
                                    val extra = result.extra
                                    if (!extra.isNullOrBlank()) {
                                        onShowContextMenu(ContextMenuData(ContextMenuType.LINK, extra))
                                        true
                                    } else false
                                }
                                WebView.HitTestResult.IMAGE_TYPE -> {
                                    val extra = result.extra
                                    if (!extra.isNullOrBlank()) {
                                        onShowContextMenu(ContextMenuData(ContextMenuType.IMAGE, extra))
                                        true
                                    } else false
                                }
                                WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                                    val extra = result.extra
                                    if (!extra.isNullOrBlank()) {
                                        onShowContextMenu(ContextMenuData(ContextMenuType.IMAGE, extra))
                                        true
                                    } else false
                                }
                                else -> false
                            }
                        }

                        // Download listener
                        setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                            onPromptDownload(url, userAgent, contentDisposition, mimetype, contentLength)
                        }

                        // WebChromeClient
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                onProgressChanged(newProgress)
                            }

                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                onTitleChanged(title)
                            }

                            // Popup Protection: intercept automatic window creation
                            override fun onCreateWindow(
                                view: WebView?,
                                isDialog: Boolean,
                                isUserGesture: Boolean,
                                resultMsg: Message?
                            ): Boolean {
                                if (settings.popupBlocking && !isUserGesture) {
                                    onBlockedPopup()
                                    return false // Blocked automatic popup!
                                }
                                if (resultMsg != null) {
                                    // If user-gesture initiated, open in new tab
                                    val transport = resultMsg.obj as? WebView.WebViewTransport
                                    if (transport != null) {
                                        val tempWebView = WebView(ctx)
                                        tempWebView.webViewClient = object : WebViewClient() {
                                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                                request?.url?.toString()?.let { popupUrl ->
                                                    onPageStarted(popupUrl)
                                                    view?.loadUrl(popupUrl)
                                                }
                                                return true
                                            }
                                        }
                                        transport.webView = tempWebView
                                        resultMsg.sendToTarget()
                                        return true
                                    }
                                }
                                return false
                            }

                            // Permission Protection: Never auto-grant
                            override fun onPermissionRequest(request: PermissionRequest?) {
                                // In compliance with safety requirement, prompt or deny automatic requests
                                request?.deny()
                            }

                            override fun onGeolocationPermissionsShowPrompt(
                                origin: String?,
                                callback: GeolocationPermissions.Callback?
                            ) {
                                // Default deny automatic geolocation
                                callback?.invoke(origin, false, false)
                            }

                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                return true
                            }
                        }

                        // WebViewClient with Ad & Tracker Interception
                        webViewClient = object : WebViewClient() {
                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): WebResourceResponse? {
                                try {
                                    val requestUrl = request?.url?.toString() ?: return null
                                    val pageUrl = view?.url ?: ""

                                    if (settings.protectionEnabled) {
                                        val blockType = protectionManager.shouldBlockResource(
                                            requestUrl = requestUrl,
                                            pageUrl = pageUrl,
                                            adBlockingEnabled = settings.adBlocking,
                                            trackerBlockingEnabled = settings.trackerBlocking
                                        )

                                        if (blockType != BlockType.NONE) {
                                            onBlockedResource(blockType)
                                            return AdBlockManager.createEmptyResponse()
                                        }
                                    }
                                } catch (_: Exception) {}

                                return super.shouldInterceptRequest(view, request)
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val uri = request?.url ?: return false
                                val urlStr = uri.toString()
                                val scheme = uri.scheme

                                // 1. Check external application scheme (e.g. intent:, market:, fb:, whatsapp:)
                                if (protectionManager.isExternalAppScheme(scheme)) {
                                    if (settings.externalAppBlocking && !request.hasGesture()) {
                                        // Block automatic launch
                                        onBlockedExternalApp()
                                        return true
                                    }
                                    // If user tapped a link intentionally, open native app chooser
                                    return try {
                                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        ctx.startActivity(intent)
                                        true
                                    } catch (_: Exception) {
                                        true
                                    }
                                }

                                // 2. Automatic Redirect Protection
                                if (settings.redirectBlocking && !request.hasGesture() && request.isRedirect) {
                                    // Check if redirect is suspicious
                                    if (protectionManager.isMaliciousUrl(urlStr) || AdBlockManager.inspectUrl(urlStr) != BlockType.NONE) {
                                        onBlockedRedirect()
                                        return true // Block redirect! Stay on current page
                                    }
                                }

                                return false
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                url?.let {
                                    currentUrlTracked = it
                                    onPageStarted(it)
                                }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                url?.let {
                                    onPageFinished(it, view?.title, view?.canGoBack() == true, view?.canGoForward() == true)
                                }
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                if (request?.isForMainFrame == true) {
                                    hasError = true
                                }
                            }
                        }

                        // Load initial url if available and not new tab
                        if (currentTab != null && !currentTab.isNewTab) {
                            loadUrl(currentTab.url)
                        }

                        webViewInstance = this
                    }
                },
                update = { wv ->
                    webViewInstance = wv
                }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.destroy()
            webViewInstance = null
        }
    }
}
