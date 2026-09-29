package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrowserTab
import com.example.data.model.SecurityState
import com.example.ui.theme.NaraAddressBar
import com.example.ui.theme.NaraCyanAccent
import com.example.ui.theme.NaraErrorRed
import com.example.ui.theme.NaraSafeGreen
import com.example.ui.theme.NaraSurface
import com.example.ui.theme.NaraTextPrimary
import com.example.ui.theme.NaraTextSecondary
import com.example.ui.theme.NaraWarningAmber

@Composable
fun BrowserTopBar(
    currentTab: BrowserTab?,
    tabCount: Int,
    isIncognito: Boolean,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReloadOrStop: () -> Unit,
    onOpenProtectionPanel: () -> Unit,
    onOpenTabManager: () -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    var isFocused by remember { mutableStateOf(false) }
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(currentTab?.url ?: ""))
    }

    // Sync input with tab URL changes when user is NOT currently editing
    LaunchedEffect(currentTab?.url) {
        if (!isFocused) {
            val url = currentTab?.url.orEmpty()
            val display = if (currentTab?.isNewTab == true) "" else url
            textFieldValue = TextFieldValue(display)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(NaraSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // [Back]
            IconButton(
                onClick = onBack,
                enabled = currentTab?.canGoBack == true,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("nav_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = if (currentTab?.canGoBack == true) NaraTextPrimary else NaraTextSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // [Forward]
            IconButton(
                onClick = onForward,
                enabled = currentTab?.canGoForward == true,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("nav_forward_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Maju",
                    tint = if (currentTab?.canGoForward == true) NaraTextPrimary else NaraTextSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // [Refresh / Stop]
            IconButton(
                onClick = onReloadOrStop,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("nav_reload_button")
            ) {
                if (currentTab?.isLoading == true) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hentikan",
                        tint = NaraTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Muat Ulang",
                        tint = NaraTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // [Address Bar] - Rounded rectangle dengan radius sedang (10.dp), bukan pill penuh
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NaraAddressBar)
                    .border(
                        1.dp,
                        if (isFocused) NaraCyanAccent.copy(alpha = 0.8f) else Color(0xFF333333),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Security icon
                    if (!isFocused && currentTab != null && !currentTab.isNewTab) {
                        val (icon, tint) = when (currentTab.securityState) {
                            SecurityState.SECURE -> Pair(Icons.Default.Lock, NaraSafeGreen)
                            SecurityState.INSECURE -> Pair(Icons.Default.Lock, NaraTextSecondary)
                            SecurityState.WARNING, SecurityState.ERROR -> Pair(Icons.Default.Warning, NaraWarningAmber)
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Keamanan Situs",
                            tint = tint,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    // Text Field for URL / Search
                    Box(modifier = Modifier.weight(1f)) {
                        if (textFieldValue.text.isEmpty() && !isFocused) {
                            Text(
                                text = "Ketik URL atau cari dengan Google",
                                color = NaraTextSecondary,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = { newValue ->
                                textFieldValue = newValue
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    isFocused = focusState.isFocused
                                    if (focusState.isFocused && currentTab?.isNewTab == true) {
                                        textFieldValue = TextFieldValue("")
                                    }
                                }
                                .testTag("address_bar_input"),
                            textStyle = TextStyle(
                                color = NaraTextPrimary,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(NaraCyanAccent),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Go
                            ),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    focusManager.clearFocus()
                                    onNavigate(textFieldValue.text)
                                }
                            )
                        )
                    }

                    // [X] and [Go] Buttons when typing
                    AnimatedVisibility(
                        visible = isFocused && textFieldValue.text.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus input",
                                tint = NaraTextSecondary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable {
                                        textFieldValue = TextFieldValue("")
                                    }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Kunjungi URL atau Cari",
                                tint = NaraCyanAccent,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable {
                                        focusManager.clearFocus()
                                        onNavigate(textFieldValue.text)
                                    }
                            )
                        }
                    }
                }
            }

            // [Nara Protection Icon]
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onOpenProtectionPanel
                    )
                    .testTag("protection_panel_button"),
                contentAlignment = Alignment.Center
            ) {
                NaraShieldProtectionIcon(
                    isActive = currentTab?.totalBlocked ?: 0 > 0,
                    onClick = onOpenProtectionPanel
                )
            }

            // [Tab Count Button]
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(
                        1.5.dp,
                        if (isIncognito) Color(0xFFBB86FC) else NaraTextPrimary,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable(onClick = onOpenTabManager)
                    .testTag("tab_manager_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tabCount.toString(),
                    color = if (isIncognito) Color(0xFFBB86FC) else NaraTextPrimary,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // [Menu Button]
            IconButton(
                onClick = onOpenMenu,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("browser_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu Utama",
                    tint = NaraTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Thin loading progress bar directly beneath address bar
        if (currentTab?.isLoading == true) {
            val progressFraction = (currentTab.progress.coerceIn(0, 100)) / 100f
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = NaraCyanAccent,
                trackColor = Color.Transparent
            )
        } else {
            Spacer(modifier = Modifier.height(1.dp).background(Color(0xFF222222)))
        }
    }
}
