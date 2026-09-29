package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NaraCyanAccent
import com.example.ui.theme.NaraSurface
import com.example.ui.theme.NaraSurfaceVariant
import com.example.ui.theme.NaraTextPrimary
import com.example.ui.theme.NaraTextSecondary

@Composable
fun FindInPageBar(
    query: String,
    matchCurrent: Int,
    matchTotal: Int,
    onQueryChange: (String) -> Unit,
    onFindNext: () -> Unit,
    onFindPrevious: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NaraSurface)
            .border(0.5.dp, MaterialTheme.colorScheme.outline)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NaraSurfaceVariant)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(
                    text = "Cari di halaman...",
                    color = NaraTextSecondary,
                    fontSize = 13.sp
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("find_in_page_input"),
                textStyle = TextStyle(color = NaraTextPrimary, fontSize = 13.sp),
                cursorBrush = SolidColor(NaraCyanAccent),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onFindNext() })
            )
        }

        if (query.isNotEmpty() && matchTotal > 0) {
            Text(
                text = "$matchCurrent/$matchTotal",
                color = NaraTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        IconButton(
            onClick = onFindPrevious,
            modifier = Modifier
                .size(32.dp)
                .testTag("find_prev_button")
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Sebelumnya",
                tint = NaraTextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(
            onClick = onFindNext,
            modifier = Modifier
                .size(32.dp)
                .testTag("find_next_button")
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Berikutnya",
                tint = NaraTextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(32.dp)
                .testTag("find_close_button")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Tutup pencarian",
                tint = NaraTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
