package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NaraMascotState
import com.example.download.DownloadPromptData
import com.example.ui.theme.NaraCyanAccent
import com.example.ui.theme.NaraDarkBackground
import com.example.ui.theme.NaraErrorRed
import com.example.ui.theme.NaraIncognitoPurple
import com.example.ui.theme.NaraSurface
import com.example.ui.theme.NaraSurfaceVariant
import com.example.ui.theme.NaraTextPrimary
import com.example.ui.theme.NaraTextSecondary
import com.example.ui.theme.NaraWarningAmber
import com.example.ui.viewmodel.ContextMenuData
import com.example.ui.viewmodel.ContextMenuType
import java.util.Locale

// 1. Bookmark Confirmation Dialog
@Composable
fun BookmarkConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NaraSurface,
        shape = RoundedCornerShape(14.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NaraMascot(state = NaraMascotState.NORMAL, size = 36.dp)
                Text(
                    text = "Nara",
                    color = NaraTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Text(
                text = "Apakah Tuan ingin menyimpan halaman ini?",
                color = NaraTextPrimary,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NaraCyanAccent,
                    contentColor = NaraDarkBackground
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_bookmark_btn")
            ) {
                Text("Simpan", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_bookmark_btn")
            ) {
                Text("Batal", color = NaraTextSecondary)
            }
        }
    )
}

// 2. Download Confirmation Dialog
@Composable
fun DownloadConfirmationDialog(
    promptData: DownloadPromptData,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NaraSurface,
        shape = RoundedCornerShape(14.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NaraMascot(state = NaraMascotState.DOWNLOAD, size = 36.dp)
                Text(
                    text = "Nara",
                    color = NaraTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Apakah Tuan ingin mengunduh file ini?",
                    color = NaraTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NaraSurfaceVariant)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Nama: ${promptData.suggestedFileName}",
                            color = NaraTextPrimary,
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (promptData.contentLength > 0) {
                            val mb = promptData.contentLength / (1024.0 * 1024.0)
                            val sizeStr = if (mb >= 1.0) String.format(Locale.US, "%.1f MB", mb) else "${promptData.contentLength / 1024} KB"
                            Text(
                                text = "Ukuran: $sizeStr",
                                color = NaraTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        if (promptData.mimeType.isNotBlank()) {
                            Text(
                                text = "Tipe: ${promptData.mimeType}",
                                color = NaraTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NaraCyanAccent,
                    contentColor = NaraDarkBackground
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_download_btn")
            ) {
                Text("Unduh", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_download_btn")
            ) {
                Text("Batal", color = NaraTextSecondary)
            }
        }
    )
}

// 3. Malicious Site Warning Dialog
@Composable
fun MaliciousSiteWarningDialog(
    url: String,
    onBack: () -> Unit,
    onProceed: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onBack,
        containerColor = NaraSurface,
        shape = RoundedCornerShape(14.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NaraMascot(state = NaraMascotState.WARNING, size = 36.dp)
                Text(
                    text = "Peringatan Keamanan",
                    color = NaraWarningAmber,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Nara menemukan sesuatu yang mencurigakan. Situs ini mungkin berbahaya.",
                    color = NaraTextPrimary,
                    fontSize = 14.sp
                )
                Text(
                    text = url,
                    color = NaraTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NaraCyanAccent,
                    contentColor = NaraDarkBackground
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("malicious_warning_back_btn")
            ) {
                Text("Kembali", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onProceed,
                modifier = Modifier.testTag("malicious_warning_proceed_btn")
            ) {
                Text("Tetap Buka", color = NaraErrorRed)
            }
        }
    )
}

// 4. Long Press Context Menu Dialog
@Composable
fun ContextMenuDialog(
    data: ContextMenuData,
    onDismiss: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onOpenInNewTab: (String) -> Unit,
    onDownloadUrl: (String) -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NaraSurface,
        shape = RoundedCornerShape(12.dp),
        title = {
            Text(
                text = if (data.type == ContextMenuType.LINK) "Tautan" else "Gambar",
                color = NaraTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = data.url,
                    color = NaraTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                if (data.type == ContextMenuType.LINK) {
                    ContextMenuItem("Buka") {
                        onDismiss()
                        onOpenUrl(data.url)
                    }
                    ContextMenuItem("Buka di Tab Baru") {
                        onDismiss()
                        onOpenInNewTab(data.url)
                    }
                    ContextMenuItem("Salin Link") {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("URL", data.url))
                        onDismiss()
                    }
                    ContextMenuItem("Download Link") {
                        onDismiss()
                        onDownloadUrl(data.url)
                    }
                    ContextMenuItem("Bagikan") {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, data.url)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Bagikan Link"))
                        onDismiss()
                    }
                } else {
                    ContextMenuItem("Buka Gambar") {
                        onDismiss()
                        onOpenUrl(data.url)
                    }
                    ContextMenuItem("Download Gambar") {
                        onDismiss()
                        onDownloadUrl(data.url)
                    }
                    ContextMenuItem("Salin Alamat Gambar") {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Image URL", data.url))
                        onDismiss()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = NaraTextSecondary)
            }
        }
    )
}

@Composable
private fun ContextMenuItem(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp)
    ) {
        Text(text = label, color = NaraTextPrimary, fontSize = 14.sp)
    }
}
