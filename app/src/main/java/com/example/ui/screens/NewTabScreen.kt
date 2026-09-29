package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NaraMascotState
import com.example.ui.components.NaraMascot
import com.example.ui.theme.NaraCyanAccent
import com.example.ui.theme.NaraDarkBackground
import com.example.ui.theme.NaraSurface
import com.example.ui.theme.NaraSurfaceVariant
import com.example.ui.theme.NaraTextPrimary
import com.example.ui.theme.NaraTextSecondary
import java.util.Calendar

@Composable
fun NewTabScreen(
    isIncognito: Boolean,
    onSearchOrNavigate: (String) -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenProtection: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }

    val (timeOfDayLabel, timeIcon, greetingText) = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> Triple("Pagi", "☀️", "Selamat pagi, Tuan~\nMau mencari apa hari ini?")
            in 12..14 -> Triple("Siang", "☀️", "Selamat siang, Tuan~\nMau menjelajah ke mana?")
            in 15..18 -> Triple("Sore", "🌅", "Selamat sore, Tuan~\nAda yang ingin dicari?")
            else -> Triple("Malam", "🌙", "Selamat malam, Tuan~\nMasih ingin menjelajah?")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NaraDarkBackground)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
        ) {
            // Official Reference Sheet Section 3: Dialog / Sapaan Nara Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(NaraSurface)
                    .border(1.dp, Color(0xFF2C2C34), RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Speech bubble message from Nara
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NaraSurfaceVariant)
                            .border(1.dp, Color(0xFF33333F), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isIncognito) {
                                "Mode Incognito Aktif.\nSesi penjelajahan aman dan tidak disimpan."
                            } else {
                                greetingText
                            },
                            color = NaraTextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Nara Mascot from official reference sheet
                    NaraMascot(
                        state = if (isIncognito) NaraMascotState.PROTECTION else NaraMascotState.NORMAL,
                        size = 84.dp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Time pill matching reference sheet (#Pagi / #Siang / #Sore / #Malam)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF24242A))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$timeIcon $timeOfDayLabel",
                            color = NaraTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Search / URL input bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NaraSurface)
                    .border(1.dp, Color(0xFF33333E), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            if (query.isNotBlank()) {
                                onSearchOrNavigate(query)
                            }
                        },
                        modifier = Modifier.size(36.dp).testTag("new_tab_search_icon_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari",
                            tint = NaraCyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Ketik alamat situs atau kata kunci...",
                                color = NaraTextSecondary,
                                fontSize = 13.5.sp
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_tab_search_input"),
                            textStyle = TextStyle(color = NaraTextPrimary, fontSize = 14.sp),
                            cursorBrush = SolidColor(NaraCyanAccent),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (query.isNotBlank()) {
                                        onSearchOrNavigate(query)
                                    }
                                }
                            )
                        )
                    }

                    if (query.isNotBlank()) {
                        IconButton(
                            onClick = { onSearchOrNavigate(query) },
                            modifier = Modifier.size(36.dp).testTag("new_tab_submit_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Buka",
                                tint = NaraCyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Quick access shortcuts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickShortcutItem(
                    icon = Icons.Default.Bookmark,
                    label = "Bookmark",
                    onClick = onOpenBookmarks,
                    tag = "quick_bookmark_btn"
                )
                QuickShortcutItem(
                    icon = Icons.Default.History,
                    label = "History",
                    onClick = onOpenHistory,
                    tag = "quick_history_btn"
                )
                QuickShortcutItem(
                    icon = Icons.Default.Download,
                    label = "Unduhan",
                    onClick = onOpenDownloads,
                    tag = "quick_downloads_btn"
                )
                QuickShortcutItem(
                    icon = Icons.Default.Security,
                    label = "Proteksi",
                    onClick = onOpenProtection,
                    tag = "quick_protection_btn"
                )
            }
        }
    }
}

@Composable
private fun QuickShortcutItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NaraSurfaceVariant)
                .border(1.dp, Color(0xFF2C2C34), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = NaraTextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = NaraTextSecondary,
            fontSize = 11.sp
        )
    }
}
