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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrowserTab
import com.example.ui.theme.NaraCyanAccent
import com.example.ui.theme.NaraDarkBackground
import com.example.ui.theme.NaraIncognitoPurple
import com.example.ui.theme.NaraSurface
import com.example.ui.theme.NaraSurfaceVariant
import com.example.ui.theme.NaraTextPrimary
import com.example.ui.theme.NaraTextSecondary

@Composable
fun TabManagerSheet(
    normalTabs: List<BrowserTab>,
    incognitoTabs: List<BrowserTab>,
    currentTabId: String,
    isIncognitoCurrent: Boolean,
    canRestoreTab: Boolean,
    onSelectTab: (String, Boolean) -> Unit,
    onCloseTab: (String) -> Unit,
    onOpenNewTab: (Boolean) -> Unit,
    onRestoreTab: () -> Unit,
    onCloseSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryIndex by remember {
        mutableIntStateOf(if (isIncognitoCurrent) 1 else 0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NaraDarkBackground)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pengelola Tab",
                color = NaraTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (canRestoreTab && selectedCategoryIndex == 0) {
                    IconButton(
                        onClick = onRestoreTab,
                        modifier = Modifier.testTag("restore_last_tab_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = "Pulihkan Tab",
                            tint = NaraCyanAccent
                        )
                    }
                }

                IconButton(
                    onClick = onCloseSheet,
                    modifier = Modifier.testTag("close_tab_manager")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = NaraTextSecondary
                    )
                }
            }
        }

        // Tabs category selector: Normal vs Incognito
        TabRow(
            selectedTabIndex = selectedCategoryIndex,
            containerColor = NaraSurface,
            contentColor = NaraTextPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedCategoryIndex]),
                    color = if (selectedCategoryIndex == 1) NaraIncognitoPurple else NaraCyanAccent
                )
            }
        ) {
            Tab(
                selected = selectedCategoryIndex == 0,
                onClick = { selectedCategoryIndex = 0 },
                text = {
                    Text(
                        text = "Normal (${normalTabs.size})",
                        color = if (selectedCategoryIndex == 0) NaraTextPrimary else NaraTextSecondary,
                        fontSize = 14.sp
                    )
                }
            )
            Tab(
                selected = selectedCategoryIndex == 1,
                onClick = { selectedCategoryIndex = 1 },
                text = {
                    Text(
                        text = "Incognito (${incognitoTabs.size})",
                        color = if (selectedCategoryIndex == 1) NaraIncognitoPurple else NaraTextSecondary,
                        fontSize = 14.sp
                    )
                }
            )
        }

        val activeList = if (selectedCategoryIndex == 1) incognitoTabs else normalTabs
        val isIncognitoCategory = selectedCategoryIndex == 1

        // Tab List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (activeList.isEmpty() && isIncognitoCategory) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = NaraIncognitoPurple,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tidak ada tab Incognito yang aktif",
                                color = NaraTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            items(activeList, key = { it.id }) { tab ->
                val isSelected = tab.id == currentTabId && (isIncognitoCurrent == isIncognitoCategory)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectTab(tab.id, isIncognitoCategory) }
                        .testTag("tab_item_${tab.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) NaraSurfaceVariant else NaraSurface
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isSelected) {
                                if (isIncognitoCategory) NaraIncognitoPurple else NaraCyanAccent
                            } else Color(0xFF2C2C2C)
                        ),
                        width = if (isSelected) 1.5.dp else 1.dp
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (tab.isNewTab) "Tab Baru" else tab.title,
                                color = NaraTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tab.displayDomain,
                                color = NaraTextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { onCloseTab(tab.id) },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("close_tab_button_${tab.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup Tab",
                                tint = NaraTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Add Tab Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NaraSurface)
                .padding(16.dp)
        ) {
            Button(
                onClick = { onOpenNewTab(isIncognitoCategory) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isIncognitoCategory) NaraIncognitoPurple else NaraCyanAccent,
                    contentColor = NaraDarkBackground
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("add_new_tab_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = if (isIncognitoCategory) "Buka Tab Incognito Baru" else "Buka Tab Baru",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
