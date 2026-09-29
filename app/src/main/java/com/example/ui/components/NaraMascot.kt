package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.NaraMascotState
import com.example.ui.theme.NaraCyanAccent
import com.example.ui.theme.NaraErrorRed
import com.example.ui.theme.NaraSafeGreen
import com.example.ui.theme.NaraWarningAmber

/**
 * 2D Pixel-art Chibi Mascot Nara:
 * Maid outfit, cute horns, long neon hair with bangs.
 */
@Composable
fun NaraMascot(
    state: NaraMascotState = NaraMascotState.NORMAL,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    // Short 120ms animation upon state transition
    val scaleAnim by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 130),
        label = "nara_state_transition"
    )

    val (badgeIcon, badgeBg, badgeTint) = when (state) {
        NaraMascotState.NORMAL -> Triple<ImageVector?, Color, Color>(null, Color.Transparent, Color.Transparent)
        NaraMascotState.LOADING -> Triple(null, Color.Transparent, Color.Transparent)
        NaraMascotState.DOWNLOAD -> Triple(Icons.Default.Download, NaraCyanAccent, Color.Black)
        NaraMascotState.DOWNLOAD_SUCCESS -> Triple(Icons.Default.Check, NaraSafeGreen, Color.Black)
        NaraMascotState.DOWNLOAD_FAILED -> Triple(Icons.Default.Close, NaraErrorRed, Color.White)
        NaraMascotState.PROTECTION -> Triple(Icons.Default.Security, NaraCyanAccent, Color.Black)
        NaraMascotState.WARNING -> Triple(Icons.Default.PriorityHigh, NaraWarningAmber, Color.Black)
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(scaleAnim),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.nara_normal),
            contentDescription = "Maskot Nara (${state.name})",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(size)
        )

        // State indicator badge overlay
        if (badgeIcon != null) {
            val badgeSize = (size.value * 0.36f).coerceIn(12f, 24f).dp
            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .background(badgeBg, CircleShape)
                    .border(1.dp, Color(0xFF121212), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = badgeIcon,
                    contentDescription = state.name,
                    tint = badgeTint,
                    modifier = Modifier.size(badgeSize * 0.65f)
                )
            }
        }
    }
}

/**
 * Protection Icon at Address Bar.
 * Depicts the chibi maid mascot with small protection shield badge.
 */
@Composable
fun NaraShieldProtectionIcon(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        NaraMascot(
            state = if (isActive) NaraMascotState.PROTECTION else NaraMascotState.NORMAL,
            size = 32.dp
        )
    }
}
