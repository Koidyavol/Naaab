package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrowserSettings
import com.example.data.model.BrowserTab
import com.example.data.model.NaraMascotState
import com.example.ui.components.NaraMascot
import com.example.ui.theme.NaraCyanAccent
import com.example.ui.theme.NaraDarkBackground
import com.example.ui.theme.NaraSafeGreen
import com.example.ui.theme.NaraSurface
import com.example.ui.theme.NaraSurfaceVariant
import com.example.ui.theme.NaraTextPrimary
import com.example.ui.theme.NaraTextSecondary

@Composable
fun ProtectionSheet(
    currentTab: BrowserTab?,
    isWhitelisted: Boolean,
    settings: BrowserSettings,
    onToggleProtection: (Boolean) -> Unit,
    onToggleWhitelistForSite: () -> Unit,
    onOpenSettings: () -> Unit,
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NaraMascot(
                    state = if (isWhitelisted) NaraMascotState.WARNING else NaraMascotState.PROTECTION,
                    size = 40.dp
                )
                Column {
                    Text(
                        text = "Nara Protection",
                        color = NaraTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isWhitelisted) "Proteksi dimatikan untuk situs ini" else if (settings.protectionEnabled) "Status: AKTIF" else "Status: NONAKTIF",
                        color = if (isWhitelisted) NaraTextSecondary else if (settings.protectionEnabled) NaraSafeGreen else NaraTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("close_protection_sheet")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Tutup",
                    tint = NaraTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Total blocked stat card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NaraSurfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Elemen Diblokir",
                        color = NaraTextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${currentTab?.totalBlocked ?: 0}",
                        color = NaraCyanAccent,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Switch(
                    checked = settings.protectionEnabled && !isWhitelisted,
                    onCheckedChange = { onToggleProtection(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NaraDarkBackground,
                        checkedTrackColor = NaraCyanAccent,
                        uncheckedThumbColor = NaraTextSecondary,
                        uncheckedTrackColor = NaraDarkBackground
                    ),
                    modifier = Modifier.testTag("protection_global_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Category Breakdown
        Text(
            text = "Kategori yang Diblokir pada Tab Ini:",
            color = NaraTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(NaraSurfaceVariant)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BlockedCategoryRow("Iklan (Ads)", currentTab?.adsBlocked ?: 0)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
            BlockedCategoryRow("Pelacak (Trackers)", currentTab?.trackersBlocked ?: 0)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
            BlockedCategoryRow("Popup Otomatis", currentTab?.popupsBlocked ?: 0)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
            BlockedCategoryRow("Pengalihan (Redirects)", currentTab?.redirectsBlocked ?: 0)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
            BlockedCategoryRow("Request Mencurigakan", currentTab?.suspiciousBlocked ?: 0)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
            BlockedCategoryRow("Download Otomatis", currentTab?.autoDownloadsBlocked ?: 0)
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Whitelist toggle for current site
        if (currentTab != null && !currentTab.isNewTab) {
            Button(
                onClick = onToggleWhitelistForSite,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isWhitelisted) NaraCyanAccent else NaraSurfaceVariant,
                    contentColor = if (isWhitelisted) NaraDarkBackground else NaraTextPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("toggle_whitelist_site_button")
            ) {
                Text(
                    text = if (isWhitelisted) "Aktifkan Protection untuk situs ini" else "Matikan Protection untuk situs ini",
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun BlockedCategoryRow(name: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, color = NaraTextPrimary, fontSize = 13.sp)
        Text(
            text = "$count",
            color = if (count > 0) NaraCyanAccent else NaraTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
