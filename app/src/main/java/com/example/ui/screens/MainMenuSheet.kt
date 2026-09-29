package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NaraCyanAccent
import com.example.ui.theme.NaraIncognitoPurple
import com.example.ui.theme.NaraSurface
import com.example.ui.theme.NaraSurfaceVariant
import com.example.ui.theme.NaraTextPrimary
import com.example.ui.theme.NaraTextSecondary

@Composable
fun MainMenuSheet(
    isIncognito: Boolean,
    isDesktopSite: Boolean,
    onOpenDownloads: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onToggleIncognito: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleDesktopSite: () -> Unit,
    onOpenFindInPage: () -> Unit,
    onBookmarkPage: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(NaraSurface)
            .padding(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Menu",
                color = NaraTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("close_main_menu")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Tutup",
                    tint = NaraTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick page actions: Bookmark, Find in page, Desktop site
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(NaraSurfaceVariant)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickMenuItem(
                icon = Icons.Default.BookmarkBorder,
                label = "Simpan",
                onClick = onBookmarkPage,
                tag = "menu_bookmark_page"
            )
            QuickMenuItem(
                icon = Icons.Default.FindInPage,
                label = "Cari",
                onClick = onOpenFindInPage,
                tag = "menu_find_page"
            )
            QuickMenuItem(
                icon = if (isDesktopSite) Icons.Default.PhoneAndroid else Icons.Default.Laptop,
                label = if (isDesktopSite) "Mobile" else "Desktop",
                onClick = onToggleDesktopSite,
                tag = "menu_toggle_desktop"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main 5 Core Menu Items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(NaraSurfaceVariant)
        ) {
            MainMenuItem(
                icon = Icons.Default.Download,
                label = "Downloads",
                onClick = onOpenDownloads,
                tag = "menu_downloads"
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
            MainMenuItem(
                icon = Icons.Default.Bookmark,
                label = "Bookmarks",
                onClick = onOpenBookmarks,
                tag = "menu_bookmarks"
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
            MainMenuItem(
                icon = Icons.Default.History,
                label = "History",
                onClick = onOpenHistory,
                tag = "menu_history"
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
            MainMenuItem(
                icon = Icons.Default.Security,
                label = if (isIncognito) "Keluar Incognito" else "Mode Incognito",
                iconTint = if (isIncognito) NaraIncognitoPurple else NaraCyanAccent,
                onClick = onToggleIncognito,
                tag = "menu_incognito"
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
            MainMenuItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                onClick = onOpenSettings,
                tag = "menu_settings"
            )
        }
    }
}

@Composable
private fun QuickMenuItem(
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
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag(tag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = NaraCyanAccent,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = NaraTextPrimary, fontSize = 11.sp)
    }
}

@Composable
private fun MainMenuItem(
    icon: ImageVector,
    label: String,
    iconTint: androidx.compose.ui.graphics.Color = NaraCyanAccent,
    onClick: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = label,
            color = NaraTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
