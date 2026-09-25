package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Track
import com.example.data.remote.LrcLyrics
import com.example.ui.theme.AppleMusicRed
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary

data class SyncedLine(
    val timestampMs: Long,
    val text: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsSheet(
    track: Track?,
    lyrics: LrcLyrics?,
    isLoading: Boolean,
    currentPositionMs: Long,
    onSeek: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    if (track == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()

    // Robust timestamp parser supporting [mm:ss.xx], [mm:ss.xxx], [mm:ss]
    val parsedLines: List<SyncedLine> = remember(lyrics?.syncedLyrics, track.syncedLyrics) {
        val raw = if (!lyrics?.syncedLyrics.isNullOrBlank()) {
            lyrics?.syncedLyrics.orEmpty()
        } else {
            track.syncedLyrics
        }

        if (raw.isBlank()) return@remember emptyList()

        val regex = Regex("\\[(\\d{1,2}):(\\d{2})(?:[\\.:](\\d{1,3}))?\\](.*)")
        val lines = mutableListOf<SyncedLine>()

        raw.lines().forEach { rawLine ->
            val trimmed = rawLine.trim()
            val match = regex.matchEntire(trimmed)
            if (match != null) {
                val min = match.groupValues[1].toLongOrNull() ?: 0L
                val sec = match.groupValues[2].toLongOrNull() ?: 0L
                val msPart = match.groupValues[3]
                val ms = when (msPart.length) {
                    1 -> (msPart.toLongOrNull() ?: 0L) * 100
                    2 -> (msPart.toLongOrNull() ?: 0L) * 10
                    3 -> msPart.toLongOrNull() ?: 0L
                    else -> 0L
                }
                val totalMs = min * 60000L + sec * 1000L + ms
                val text = match.groupValues[4].trim()
                if (text.isNotBlank()) {
                    lines.add(SyncedLine(totalMs, text))
                }
            }
        }
        lines.sortedBy { it.timestampMs }
    }

    // Accurate active index matching playback position
    val activeIndex by remember(parsedLines, currentPositionMs) {
        derivedStateOf {
            if (parsedLines.isEmpty()) -1
            else {
                var found = -1
                for (i in parsedLines.indices) {
                    if (currentPositionMs >= parsedLines[i].timestampMs) {
                        found = i
                    } else {
                        break
                    }
                }
                if (found == -1 && currentPositionMs < (parsedLines.firstOrNull()?.timestampMs ?: 0L)) {
                    found = 0
                }
                found
            }
        }
    }

    // Smooth auto-scroll that animates with active line
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && activeIndex < parsedLines.size && !listState.isScrollInProgress) {
            val target = (activeIndex - 1).coerceAtLeast(0)
            listState.animateScrollToItem(target)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        scrimColor = Color.Black.copy(alpha = 0.88f),
        dragHandle = null,
        modifier = Modifier.testTag("lyrics_sheet")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Apple Music Adaptive Blurred Gradient Background
            AppleAdaptiveBlurBackground(track = track)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                // Apple Music Header Bar with Track Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .shadow(8.dp, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            AsyncImage(
                                model = track.coverUrl,
                                contentDescription = track.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                color = AppleTextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = AppleMusicRed,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${track.artist} • Letra Sincronizada",
                                    color = AppleTextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("close_lyrics_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isLoading && parsedLines.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = AppleMusicRed,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Sincronizando letra con la música...",
                                color = AppleTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else if (parsedLines.isNotEmpty()) {
                    // Apple Music Full-Screen Time-Synced Lyrics with Bold Typography & Sweep Highlight
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("lyrics_list"),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp)
                    ) {
                        itemsIndexed(parsedLines) { index, line ->
                            val isActive = index == activeIndex
                            val isPast = index < activeIndex

                            val targetColor = when {
                                isActive -> Color.White
                                isPast -> Color.White.copy(alpha = 0.55f)
                                else -> Color.White.copy(alpha = 0.32f)
                            }

                            val animatedColor by animateColorAsState(
                                targetValue = targetColor,
                                animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f),
                                label = "lyricColor"
                            )

                            val scale by animateFloatAsState(
                                targetValue = if (isActive) 1.04f else 0.97f,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f),
                                label = "lyricScale"
                            )

                            // Calculate progressive sweep across active line
                            val nextTimestamp = parsedLines.getOrNull(index + 1)?.timestampMs ?: (line.timestampMs + 4000L)
                            val lineDuration = (nextTimestamp - line.timestampMs).coerceIn(1200L, 9000L)
                            val lineProgress = if (isActive) {
                                ((currentPositionMs - line.timestampMs).toFloat() / lineDuration).coerceIn(0f, 1f)
                            } else 0f

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .scale(scale)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isActive) Color.White.copy(alpha = 0.12f) else Color.Transparent)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        onSeek(line.timestampMs)
                                    }
                                    .padding(vertical = 12.dp, horizontal = 12.dp)
                            ) {
                                if (isActive) {
                                    // Dual-layer karaoke sweep effect for active line
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        // Base dimmed line
                                        Text(
                                            text = line.text,
                                            color = Color.White.copy(alpha = 0.40f),
                                            fontSize = 30.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = (-0.5).sp,
                                            lineHeight = 38.sp
                                        )
                                        // Sung luminous sweep
                                        Text(
                                            text = line.text,
                                            color = Color.White,
                                            fontSize = 30.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = (-0.5).sp,
                                            lineHeight = 38.sp,
                                            modifier = Modifier.drawWithContent {
                                                val clipWidth = size.width * lineProgress
                                                clipRect(right = clipWidth) {
                                                    this@drawWithContent.drawContent()
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    Text(
                                        text = line.text,
                                        color = animatedColor,
                                        fontSize = 23.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.5).sp,
                                        lineHeight = 31.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Plain text lyrics or fallback message
                    val plain = lyrics?.plainLyrics.orEmpty().ifBlank { track.lyrics }
                    if (plain.isNotBlank()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 80.dp)
                        ) {
                            item {
                                Text(
                                    text = plain,
                                    color = Color.White.copy(alpha = 0.88f),
                                    fontSize = 20.sp,
                                    lineHeight = 32.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(vertical = 20.dp)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Letra no disponible",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Disfruta de la música en alta fidelidad",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
