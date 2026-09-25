package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SpatialAudio
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Track
import com.example.ui.MusicViewModel
import com.example.ui.theme.AppleCardDark
import com.example.ui.theme.AppleMusicHiResGold
import com.example.ui.theme.AppleMusicRed
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.AppleTextTertiary

data class RadioStation(
    val id: String,
    val name: String,
    val description: String,
    val host: String,
    val bannerUrl: String,
    val colorStart: Color,
    val colorEnd: Color,
    val initialTrackIndex: Int = 0
)

@Composable
fun RadioScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    var losslessEnabled by remember { mutableStateOf(true) }
    var spatialAudioEnabled by remember { mutableStateOf(true) }
    var soundCheckEnabled by remember { mutableStateOf(true) }

    val stations = listOf(
        RadioStation(
            id = "station_apple_music_1",
            name = "Apple Music 1",
            description = "The new music that matters today, broadcast live around the world.",
            host = "Zane Lowe & Guests",
            bannerUrl = "https://cdn-images.dzcdn.net/images/cover/5718f7c81c27e0b2417e2a4c45224f8a/1000x1000-000000-80-0-0.jpg",
            colorStart = Color(0xFFFA243C),
            colorEnd = Color(0xFF7A092B),
            initialTrackIndex = 0
        ),
        RadioStation(
            id = "station_apple_music_hits",
            name = "Apple Music Hits",
            description = "Songs you know and love from the '80s, '90s, and 2000s in studio master.",
            host = "Estelle & George Stroumboulopoulos",
            bannerUrl = "https://cdn-images.dzcdn.net/images/cover/fd00ebd6d36e2f1e63a3d5f99ee303d8/1000x1000-000000-80-0-0.jpg",
            colorStart = Color(0xFF8E2DE2),
            colorEnd = Color(0xFF4A00E0),
            initialTrackIndex = 1
        ),
        RadioStation(
            id = "station_classical_live",
            name = "Classical Symphony Radio",
            description = "24-Bit / 96kHz live orchestral masterworks and peaceful piano concertos.",
            host = "Apple Music Classical Curators",
            bannerUrl = "https://cdn-images.dzcdn.net/images/cover/5bbfab8a245582381e4b95f15caeb0eb/1000x1000-000000-80-0-0.jpg",
            colorStart = Color(0xFF2C3E50),
            colorEnd = Color(0xFF3498DB),
            initialTrackIndex = 2
        ),
        RadioStation(
            id = "station_acoustic_chill",
            name = "Acoustic & Chillout Radio",
            description = "Unplugged acoustic guitars, warm piano melodies, and soothing vocal sessions.",
            host = "Acoustic Lounge Master",
            bannerUrl = "https://cdn-images.dzcdn.net/images/cover/a0ad67d169d2d091e704043b23e7cf04/1000x1000-000000-80-0-0.jpg",
            colorStart = Color(0xFFD35400),
            colorEnd = Color(0xFFF39C12),
            initialTrackIndex = 5
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("radio_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Large Apple Music Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "BROADCAST",
                    color = AppleMusicRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Radio",
                        color = AppleTextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // Live badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x33FA243C))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(AppleMusicRed)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "LIVE STREAMS",
                                color = AppleMusicRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Live Station Hero Card (Apple Music 1)
        item {
            val liveStation = stations[0]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        val tracks = viewModel.curatedTracks
                        if (tracks.isNotEmpty()) {
                            viewModel.playTrack(tracks[0], tracks)
                        }
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AppleCardDark)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(liveStation.colorStart, liveStation.colorEnd)
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Radio,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "APPLE MUSIC LIVE",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "ON AIR",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column {
                            Text(
                                text = liveStation.name,
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${liveStation.host} • ${liveStation.description}",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Continuous 24-Bit Stream",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Listen to station",
                                    tint = liveStation.colorStart,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Station Cards Row
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(title = "Featured Stations", subtitle = "Continuous curated listening without interruption")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(stations) { station ->
                    Card(
                        modifier = Modifier
                            .width(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                val tracks = viewModel.curatedTracks
                                val start = station.initialTrackIndex.coerceIn(0, tracks.size - 1)
                                viewModel.playTrack(tracks[start], tracks)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AppleCardDark)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(station.colorStart, station.colorEnd)
                                        )
                                    )
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = station.name,
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.BottomStart)
                                )
                            }
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = station.description,
                                    color = AppleTextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "STATION",
                                        color = AppleMusicRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = AppleMusicRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Apple Music Audio Architecture & Sound Enhancements
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(
                title = "Audio Quality & Enhancements",
                subtitle = "Mastering configurations for bit-perfect continuous playback"
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppleCardDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Lossless Audio Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Lossless Audio (ALAC 24-Bit / 96kHz)",
                                color = AppleTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Bit-for-bit studio master reproduction up to 192kHz.",
                                color = AppleTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = losslessEnabled,
                            onCheckedChange = { losslessEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppleMusicRed
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Spatial Audio Simulation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Spatial Audio Simulation",
                                color = AppleTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Expands the acoustic stereo soundstage in headphones.",
                                color = AppleTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = spatialAudioEnabled,
                            onCheckedChange = { spatialAudioEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppleMusicRed
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sound Check / Volume Normalization
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sound Check (Continuous Levels)",
                                color = AppleTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Maintains consistent loudness across tracks without clipping.",
                                color = AppleTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = soundCheckEnabled,
                            onCheckedChange = { soundCheckEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppleMusicRed
                            )
                        )
                    }
                }
            }
        }
    }
}
