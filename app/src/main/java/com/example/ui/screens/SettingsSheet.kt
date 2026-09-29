package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrowserSettings
import com.example.data.model.NaraMascotState
import com.example.ui.components.NaraMascot
import com.example.ui.theme.NaraCyanAccent
import com.example.ui.theme.NaraDarkBackground
import com.example.ui.theme.NaraErrorRed
import com.example.ui.theme.NaraSurface
import com.example.ui.theme.NaraSurfaceVariant
import com.example.ui.theme.NaraTextPrimary
import com.example.ui.theme.NaraTextSecondary

@Composable
fun SettingsSheet(
    settings: BrowserSettings,
    onUpdateSettings: (BrowserSettings) -> Unit,
    onOpenWhitelist: () -> Unit,
    onClearBrowsingData: (cookies: Boolean, cache: Boolean, history: Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NaraDarkBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NaraSurface)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = NaraCyanAccent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = "Pengaturan",
                    color = NaraTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("close_settings_sheet")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Tutup",
                    tint = NaraTextSecondary
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category: Protection
            item {
                SettingsCategorySection(title = "PROTECTION") {
                    SettingsSwitchRow(
                        title = "Proteksi Utama",
                        subtitle = "Aktifkan engine keamanan Nara Browser",
                        checked = settings.protectionEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(protectionEnabled = it)) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                    SettingsSwitchRow(
                        title = "Blokir Iklan (Ad Blocking)",
                        subtitle = "Memblokir banner, video ads, dan server iklan",
                        checked = settings.adBlocking,
                        enabled = settings.protectionEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(adBlocking = it)) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                    SettingsSwitchRow(
                        title = "Blokir Popup (Popup Blocking)",
                        subtitle = "Mencegah popup dan tab baru liar tanpa izin",
                        checked = settings.popupBlocking,
                        enabled = settings.protectionEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(popupBlocking = it)) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                    SettingsSwitchRow(
                        title = "Blokir Pengalihan (Redirect Blocking)",
                        subtitle = "Mencegah pengalihan otomatis berantai",
                        checked = settings.redirectBlocking,
                        enabled = settings.protectionEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(redirectBlocking = it)) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                    SettingsSwitchRow(
                        title = "Blokir Pelacak (Tracker Blocking)",
                        subtitle = "Mencegah skrip analitik & fingerprinting",
                        checked = settings.trackerBlocking,
                        enabled = settings.protectionEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(trackerBlocking = it)) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                    SettingsSwitchRow(
                        title = "Blokir Download Otomatis",
                        subtitle = "Mencegah download berulang tanpa izin",
                        checked = settings.autoDownloadBlocking,
                        enabled = settings.protectionEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(autoDownloadBlocking = it)) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                    SettingsSwitchRow(
                        title = "Blokir Buka Aplikasi Eksternal",
                        subtitle = "Mencegah peluncuran otomatis intent/app",
                        checked = settings.externalAppBlocking,
                        enabled = settings.protectionEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(externalAppBlocking = it)) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                    SettingsSwitchRow(
                        title = "Peringatan Situs Berbahaya",
                        subtitle = "Menampilkan peringatan sebelum membuka situs mencurigakan",
                        checked = settings.maliciousSiteWarning,
                        enabled = settings.protectionEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(maliciousSiteWarning = it)) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                    SettingsClickableRow(
                        title = "Whitelist Proteksi",
                        subtitle = "Kelola daftar situs yang dikecualikan dari proteksi",
                        onClick = onOpenWhitelist
                    )
                }
            }

            // Category: Privacy
            item {
                SettingsCategorySection(title = "PRIVACY") {
                    SettingsClickableRow(
                        title = "Hapus Cookies & Cache",
                        subtitle = "Bersihkan data situs tersimpan",
                        onClick = { onClearBrowsingData(true, true, false) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                    SettingsClickableRow(
                        title = "Hapus Seluruh Data Browsing",
                        subtitle = "Bersihkan cookies, cache, dan history",
                        onClick = { onClearBrowsingData(true, true, true) }
                    )
                }
            }

            // Category: Downloads
            item {
                SettingsCategorySection(title = "DOWNLOADS") {
                    SettingsSwitchRow(
                        title = "Tanya Sebelum Mengunduh",
                        subtitle = "Tampilkan dialog konfirmasi Nara dengan detail file",
                        checked = settings.askBeforeDownload,
                        onCheckedChange = { onUpdateSettings(settings.copy(askBeforeDownload = it)) }
                    )
                }
            }

            // Category: Browser & Web Engine
            item {
                SettingsCategorySection(title = "BROWSER") {
                    SettingsSwitchRow(
                        title = "JavaScript",
                        subtitle = "Aktifkan eksekusi JavaScript pada halaman web",
                        checked = settings.javascriptEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(javascriptEnabled = it)) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                    SettingsSwitchRow(
                        title = "Tampilan Desktop Default",
                        subtitle = "Selalu meminta versi desktop untuk tab baru",
                        checked = settings.desktopSiteDefault,
                        onCheckedChange = { onUpdateSettings(settings.copy(desktopSiteDefault = it)) }
                    )
                }
            }

            // Category: Appearance
            item {
                SettingsCategorySection(title = "APPEARANCE") {
                    SettingsStaticRow(
                        title = "Tema Aplikasi",
                        subtitle = "Dark Mode Default (#121212)"
                    )
                }
            }

            // Category: About
            item {
                SettingsCategorySection(title = "ABOUT") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NaraMascot(state = NaraMascotState.NORMAL, size = 48.dp)
                        Spacer(modifier = Modifier.size(14.dp))
                        Column {
                            Text(
                                text = "Nara Browser",
                                color = NaraTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Versi 1.0.0 (Lightweight & Shielded)",
                                color = NaraTextSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Nara Browser dibuat untuk browsing yang ringan, cepat, dan terlindungi.",
                                color = NaraTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCategorySection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            color = NaraCyanAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = NaraSurface),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (enabled) NaraTextPrimary else NaraTextSecondary.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = NaraTextSecondary,
                fontSize = 11.sp
            )
        }
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NaraDarkBackground,
                checkedTrackColor = NaraCyanAccent,
                uncheckedThumbColor = NaraTextSecondary,
                uncheckedTrackColor = NaraDarkBackground
            )
        )
    }
}

@Composable
private fun SettingsClickableRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = NaraTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = NaraTextSecondary,
                fontSize = 11.sp
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = NaraTextSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun SettingsStaticRow(
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                color = NaraTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = NaraTextSecondary,
                fontSize = 11.sp
            )
        }
    }
}
