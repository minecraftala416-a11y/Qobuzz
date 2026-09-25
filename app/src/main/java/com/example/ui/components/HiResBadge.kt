package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Track
import com.example.ui.theme.AppleCardDark
import com.example.ui.theme.AppleMusicHiResGold
import com.example.ui.theme.AppleMusicRed
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary

@Composable
fun HiResBadge(
    track: Track,
    modifier: Modifier = Modifier,
    showDetailsOnClick: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }

    val isHiRes = track.isHiRes
    val badgeBg = if (isHiRes) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0x33FFD700),
                Color(0x22FFA500)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0x22FFFFFF),
                Color(0x11FFFFFF)
            )
        )
    }

    val borderColor = if (isHiRes) Color(0x66FFD700) else Color(0x33FFFFFF)
    val textColor = if (isHiRes) AppleMusicHiResGold else Color(0xFFDDDDDD)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(badgeBg)
            .border(0.8.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(enabled = showDetailsOnClick) { showDialog = true }
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .testTag("hires_badge")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isHiRes) Icons.Default.Stars else Icons.Default.MusicNote,
                contentDescription = "Audio Quality",
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isHiRes) "Hi-Res LOSSLESS" else "LOSSLESS",
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = " • ${track.bitDepth}-bit / ${track.samplingRate.toInt()} kHz",
                color = textColor.copy(alpha = 0.85f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    if (showDialog) {
        AudioQualityDetailsDialog(track = track, onDismiss = { showDialog = false })
    }
}

@Composable
fun AudioQualityDetailsDialog(
    track: Track,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = AppleMusicHiResGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Qobuz Lossless Audio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AppleTextPrimary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (track.isHiRes) {
                        "This studio master is streamed bit-perfect in Hi-Res 24-bit Lossless directly via Qobuz-DL engine."
                    } else {
                        "This track is streamed in 16-bit 44.1 kHz CD-quality Lossless without acoustic compression."
                    },
                    fontSize = 13.sp,
                    color = AppleTextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                SpecRow("Audio Codec", "FLAC (Free Lossless Audio Codec)")
                SpecRow("Bit Depth", "${track.bitDepth}-bit Studio Master")
                SpecRow("Sampling Rate", "${track.samplingRate} kHz")
                SpecRow("Bitrate", "${(track.bitDepth * track.samplingRate * 2 / 1000).toInt()} kbps (Approx.)")
                SpecRow("Engine", "Qobuz-DL REST API v0.2")

                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x22FA243C))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Tip: For maximum audio fidelity (up to 24-bit/192kHz), connect an external USB-C DAC (Digital-to-Analog Converter) or high-end audiophile headphones.",
                        fontSize = 11.sp,
                        color = Color(0xFFFFB3B8),
                        lineHeight = 15.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got It", color = AppleMusicRed, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = AppleCardDark,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = AppleTextSecondary)
        Text(text = value, fontSize = 12.sp, color = AppleTextPrimary, fontWeight = FontWeight.SemiBold)
    }
}
