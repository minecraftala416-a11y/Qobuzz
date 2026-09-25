package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppNavigationTab
import com.example.ui.MusicViewModel
import com.example.ui.components.AppleMusicBottomBar
import com.example.ui.components.AppleMusicMiniPlayer
import com.example.ui.components.AppleMusicNowPlayingSheet
import com.example.ui.components.LyricsSheet
import com.example.ui.components.QueueSheet
import com.example.ui.screens.BrowseScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.ListenNowScreen
import com.example.ui.screens.RadioScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.AppleBackgroundDark
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                MainMusicApp()
            }
        }
    }
}

@Composable
fun MainMusicApp(
    viewModel: MusicViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val isPlayerSheetVisible by viewModel.isPlayerSheetVisible.collectAsState()
    val isLyricsSheetVisible by viewModel.isLyricsSheetVisible.collectAsState()
    val isQueueSheetVisible by viewModel.isQueueSheetVisible.collectAsState()
    val shuffleEnabled by viewModel.shuffleEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val queue by viewModel.queue.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    val isCurrentFavorite = currentTrack?.let { ct ->
        favorites.any { it.id == ct.id }
    } ?: false

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackgroundDark)
            .windowInsetsPadding(WindowInsets.statusBars),
        containerColor = AppleBackgroundDark,
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Apple Music Mini Player floats directly above the tab bar
                if (currentTrack != null) {
                    AppleMusicMiniPlayer(
                        track = currentTrack,
                        isPlaying = isPlaying,
                        playbackState = playbackState,
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        onPlayPauseClick = { viewModel.togglePlayPause() },
                        onNextClick = { viewModel.skipNext() },
                        onClick = { viewModel.openPlayerSheet() }
                    )
                }

                // Apple Music 5-Tab Navigation Bar
                AppleMusicBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppleBackgroundDark)
        ) {
            Crossfade(targetState = selectedTab, label = "tab_transition") { tab ->
                when (tab) {
                    AppNavigationTab.LISTEN_NOW -> ListenNowScreen(viewModel = viewModel)
                    AppNavigationTab.BROWSE -> BrowseScreen(viewModel = viewModel)
                    AppNavigationTab.RADIO -> RadioScreen(viewModel = viewModel)
                    AppNavigationTab.LIBRARY -> LibraryScreen(viewModel = viewModel)
                    AppNavigationTab.SEARCH -> SearchScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Apple Music Full-Screen Now Playing Sheet with Adaptive Blur
    if (isPlayerSheetVisible) {
        AppleMusicNowPlayingSheet(
            track = currentTrack,
            isPlaying = isPlaying,
            playbackState = playbackState,
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            isFavorite = isCurrentFavorite,
            shuffleEnabled = shuffleEnabled,
            repeatMode = repeatMode,
            onDismiss = { viewModel.closePlayerSheet() },
            onPlayPause = { viewModel.togglePlayPause() },
            onSeek = { viewModel.seekTo(it) },
            onNext = { viewModel.skipNext() },
            onPrevious = { viewModel.skipPrevious() },
            onToggleFavorite = { currentTrack?.let { viewModel.toggleFavorite(it) } },
            onToggleShuffle = { viewModel.toggleShuffle() },
            onCycleRepeat = { viewModel.cycleRepeatMode() },
            onToggleLyrics = { viewModel.toggleLyricsSheet() },
            onToggleQueue = { viewModel.toggleQueueSheet() }
        )
    }

    // Apple Music Time-Synced Lyrics Sheet with Progressive Word Sweep & Blur
    if (isLyricsSheetVisible) {
        val currentLyrics by viewModel.currentLyrics.collectAsState()
        val isLyricsLoading by viewModel.isLyricsLoading.collectAsState()
        LyricsSheet(
            track = currentTrack,
            lyrics = currentLyrics,
            isLoading = isLyricsLoading,
            currentPositionMs = currentPositionMs,
            onSeek = { viewModel.seekTo(it) },
            onDismiss = { viewModel.toggleLyricsSheet() }
        )
    }

    // Playing Next Queue Sheet
    if (isQueueSheetVisible) {
        QueueSheet(
            currentTrack = currentTrack,
            queue = queue,
            onSelectTrack = { viewModel.playTrack(it) },
            onRemoveFromQueue = { viewModel.removeFromQueue(it) },
            onClearQueue = { viewModel.clearQueue() },
            onDismiss = { viewModel.toggleQueueSheet() }
        )
    }
}
