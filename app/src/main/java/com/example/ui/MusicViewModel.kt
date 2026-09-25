package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PlaylistEntity
import com.example.data.model.Album
import com.example.data.model.AudioQuality
import com.example.data.model.QobuzConfig
import com.example.data.model.Track
import com.example.data.remote.CuratedMusicData
import com.example.data.remote.LrcLyrics
import com.example.data.remote.QobuzClient
import com.example.data.repository.MusicRepository
import com.example.playback.AudioPlayerManager
import com.example.playback.PlaybackState
import com.example.playback.RepeatMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavigationTab {
    LISTEN_NOW,
    BROWSE,
    RADIO,
    LIBRARY,
    SEARCH
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val qobuzClient = QobuzClient()
    val repository = MusicRepository(database.musicDao(), qobuzClient)
    val playerManager = AudioPlayerManager(application)

    // Navigation & UI Sheets
    private val _selectedTab = MutableStateFlow(AppNavigationTab.LISTEN_NOW)
    val selectedTab: StateFlow<AppNavigationTab> = _selectedTab.asStateFlow()

    private val _isPlayerSheetVisible = MutableStateFlow(false)
    val isPlayerSheetVisible: StateFlow<Boolean> = _isPlayerSheetVisible.asStateFlow()

    private val _isLyricsSheetVisible = MutableStateFlow(false)
    val isLyricsSheetVisible: StateFlow<Boolean> = _isLyricsSheetVisible.asStateFlow()

    private val _isQueueSheetVisible = MutableStateFlow(false)
    val isQueueSheetVisible: StateFlow<Boolean> = _isQueueSheetVisible.asStateFlow()

    // Config defaults
    private val _config = MutableStateFlow(
        QobuzConfig(
            appId = "798273057",
            appSecret = "05a4851e74ee47fda346f50cfdfc4f09",
            userAuthToken = "",
            quality = AudioQuality.HIRES_LOSSLESS_96,
            isConnected = true
        )
    )
    val config: StateFlow<QobuzConfig> = _config.asStateFlow()

    // LRCLIB Synced Lyrics State
    private val _currentLyrics = MutableStateFlow<LrcLyrics?>(null)
    val currentLyrics: StateFlow<LrcLyrics?> = _currentLyrics.asStateFlow()

    private val _isLyricsLoading = MutableStateFlow(false)
    val isLyricsLoading: StateFlow<Boolean> = _isLyricsLoading.asStateFlow()

    private var lyricsJob: Job? = null

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Track>>(emptyList())
    val searchResults: StateFlow<List<Track>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    // Real Music Catalog (Full songs!)
    val curatedTracks = CuratedMusicData.realFeaturedTracks
    val featuredAlbums = CuratedMusicData.realFeaturedAlbums
    val genres = CuratedMusicData.genres

    val favorites: StateFlow<List<Track>> = repository.favorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val history: StateFlow<List<Track>> = repository.history.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val playlists: StateFlow<List<PlaylistEntity>> = repository.playlists.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Player State delegations
    val currentTrack: StateFlow<Track?> = playerManager.currentTrack
    val isPlaying: StateFlow<Boolean> = playerManager.isPlaying
    val playbackState: StateFlow<PlaybackState> = playerManager.playbackState
    val currentPositionMs: StateFlow<Long> = playerManager.currentPositionMs
    val durationMs: StateFlow<Long> = playerManager.durationMs
    val queue: StateFlow<List<Track>> = playerManager.queue
    val shuffleEnabled: StateFlow<Boolean> = playerManager.shuffleEnabled
    val repeatMode: StateFlow<RepeatMode> = playerManager.repeatMode
    val errorMessage: StateFlow<String?> = playerManager.errorMessage

    init {
        // Wire automatic stream URL resolution
        playerManager.onResolveStreamNeeded = { track ->
            repository.resolveStreamUrl(track, _config.value)
        }

        // Initialize queue with all real featured tracks
        val firstTrack = curatedTracks.firstOrNull()
        playerManager.setQueue(curatedTracks, startTrack = null)

        // Preload first track lyrics
        if (firstTrack != null) {
            loadLyricsForTrack(firstTrack)
        }
    }

    fun selectTab(tab: AppNavigationTab) {
        _selectedTab.value = tab
    }

    fun openPlayerSheet() {
        _isPlayerSheetVisible.value = true
    }

    fun closePlayerSheet() {
        _isPlayerSheetVisible.value = false
    }

    fun toggleLyricsSheet() {
        _isLyricsSheetVisible.value = !_isLyricsSheetVisible.value
        if (_isLyricsSheetVisible.value) {
            currentTrack.value?.let { loadLyricsForTrack(it) }
        }
    }

    fun toggleQueueSheet() {
        _isQueueSheetVisible.value = !_isQueueSheetVisible.value
    }

    fun playTrack(track: Track, newQueue: List<Track>? = null) {
        viewModelScope.launch {
            val resolvedTrack = if (track.streamUrl.isBlank()) {
                val stream = repository.resolveStreamUrl(track, _config.value)
                track.copy(streamUrl = stream)
            } else {
                track
            }
            playerManager.playTrack(resolvedTrack, newQueue)
            repository.recordHistory(resolvedTrack)
            loadLyricsForTrack(resolvedTrack)
        }
    }

    private fun loadLyricsForTrack(track: Track) {
        lyricsJob?.cancel()
        // If track already has embedded synced lyrics, show them immediately!
        if (track.syncedLyrics.isNotBlank()) {
            _currentLyrics.value = LrcLyrics(
                trackName = track.title,
                artistName = track.artist,
                albumName = track.albumTitle,
                syncedLyrics = track.syncedLyrics,
                plainLyrics = track.lyrics
            )
            _isLyricsLoading.value = false
            return
        }

        lyricsJob = viewModelScope.launch {
            _isLyricsLoading.value = true
            val lyrics = repository.fetchLyrics(track)
            if (lyrics != null) {
                _currentLyrics.value = lyrics
            }
            _isLyricsLoading.value = false
        }
    }

    fun playAlbum(album: Album) {
        val tracks = if (album.tracks.isNotEmpty()) album.tracks else curatedTracks
        val first = tracks.firstOrNull() ?: return
        playTrack(first, tracks)
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun skipNext() {
        playerManager.skipNext()
        playerManager.currentTrack.value?.let { loadLyricsForTrack(it) }
    }

    fun skipPrevious() {
        playerManager.skipPrevious()
        playerManager.currentTrack.value?.let { loadLyricsForTrack(it) }
    }

    fun toggleShuffle() {
        playerManager.toggleShuffle()
    }

    fun cycleRepeatMode() {
        playerManager.cycleRepeatMode()
    }

    fun removeFromQueue(index: Int) {
        playerManager.removeFromQueue(index)
    }

    fun clearQueue() {
        playerManager.clearQueue()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            _isSearching.value = true
            val results = repository.searchTracks(query, _config.value)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            val isFav = favorites.value.any { it.id == track.id }
            repository.toggleFavorite(track, isFav)
        }
    }

    fun createPlaylist(name: String, description: String = "") {
        viewModelScope.launch {
            val id = repository.createPlaylist(name, description)
            currentTrack.value?.let { track ->
                repository.addTrackToPlaylist(id, track)
            }
        }
    }

    fun addTrackToPlaylist(playlistId: Long, track: Track) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, track)
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
