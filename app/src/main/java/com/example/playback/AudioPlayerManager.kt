package com.example.playback

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.data.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class PlaybackState {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    ERROR
}

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

@OptIn(UnstableApi::class)
class AudioPlayerManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var exoPlayer: ExoPlayer? = null
    private var progressJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    var onResolveStreamNeeded: (suspend (Track) -> String?)? = null

    init {
        initializePlayer()
    }

    private fun initializePlayer() {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("AppleMusicPlayer/4.0 (Android; ExoPlayer)")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        exoPlayer = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build().apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_IDLE -> {
                                _playbackState.value = PlaybackState.IDLE
                            }
                            Player.STATE_BUFFERING -> {
                                _playbackState.value = PlaybackState.BUFFERING
                            }
                            Player.STATE_READY -> {
                                val dur = duration
                                if (dur > 0) {
                                    _durationMs.value = dur
                                }
                                if (playWhenReady) {
                                    _playbackState.value = PlaybackState.PLAYING
                                    _isPlaying.value = true
                                    startProgressUpdates()
                                } else {
                                    _playbackState.value = PlaybackState.PAUSED
                                    _isPlaying.value = false
                                }
                            }
                            Player.STATE_ENDED -> {
                                handleTrackCompletion()
                            }
                        }
                    }

                    override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                        _isPlaying.value = isPlayingNow
                        if (isPlayingNow) {
                            _playbackState.value = PlaybackState.PLAYING
                            startProgressUpdates()
                        } else if (_playbackState.value != PlaybackState.BUFFERING) {
                            _playbackState.value = PlaybackState.PAUSED
                            stopProgressUpdates()
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("AudioPlayerManager", "ExoPlayer error: ${error.message}", error)
                        _playbackState.value = PlaybackState.ERROR
                        _errorMessage.value = "Playback error: ${error.errorCodeName}"
                        _isPlaying.value = false
                        stopProgressUpdates()

                        // Continuous playback resilience: advance to next track on error
                        scope.launch {
                            delay(1200)
                            if (_playbackState.value == PlaybackState.ERROR) {
                                skipNext()
                            }
                        }
                    }
                })
            }
    }

    fun setQueue(tracks: List<Track>, startTrack: Track? = null) {
        _queue.value = tracks
        if (startTrack != null) {
            val index = tracks.indexOfFirst { it.id == startTrack.id }
            if (index >= 0) {
                _currentIndex.value = index
            }
            playTrack(startTrack)
        }
    }

    fun playTrack(track: Track, newQueue: List<Track>? = null) {
        if (newQueue != null && newQueue.isNotEmpty()) {
            _queue.value = newQueue
            val index = newQueue.indexOfFirst { it.id == track.id }
            _currentIndex.value = if (index >= 0) index else 0
        } else if (!_queue.value.any { it.id == track.id }) {
            _queue.value = _queue.value + track
            _currentIndex.value = _queue.value.size - 1
        } else {
            val index = _queue.value.indexOfFirst { it.id == track.id }
            if (index >= 0) _currentIndex.value = index
        }

        _currentTrack.value = track
        _errorMessage.value = null
        _currentPositionMs.value = 0L
        _durationMs.value = (track.durationSeconds * 1000).toLong()

        scope.launch {
            var url = track.streamUrl
            if (url.isBlank() || !url.startsWith("http")) {
                _playbackState.value = PlaybackState.BUFFERING
                url = onResolveStreamNeeded?.invoke(track).orEmpty()
            }
            startPlaybackWithUrl(track, url)
        }
    }

    private fun startPlaybackWithUrl(track: Track, streamUrl: String) {
        stopProgressUpdates()
        _playbackState.value = PlaybackState.BUFFERING
        _isPlaying.value = false

        if (streamUrl.isBlank()) {
            Log.e("AudioPlayerManager", "Cannot play: stream URL is empty")
            _playbackState.value = PlaybackState.ERROR
            _errorMessage.value = "Audio stream unavailable"
            // Auto advance
            scope.launch {
                delay(1000)
                skipNext()
            }
            return
        }

        try {
            if (exoPlayer == null) {
                initializePlayer()
            }

            val player = exoPlayer ?: return
            val mediaItem = MediaItem.Builder()
                .setUri(Uri.parse(streamUrl))
                .setMediaId(track.id)
                .build()

            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true
            startProgressUpdates()
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Failed to start ExoPlayer", e)
            _playbackState.value = PlaybackState.ERROR
            _errorMessage.value = "Playback initialization error: ${e.message}"
            _isPlaying.value = false
        }
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
            _playbackState.value = PlaybackState.PAUSED
            stopProgressUpdates()
        } else {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0)
            }
            player.playWhenReady = true
            _isPlaying.value = true
            _playbackState.value = PlaybackState.PLAYING
            startProgressUpdates()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _currentPositionMs.value = positionMs
    }

    fun skipNext() {
        val q = _queue.value
        if (q.isEmpty()) return

        if (_shuffleEnabled.value && q.size > 1) {
            var nextIndex = (q.indices).random()
            if (nextIndex == _currentIndex.value) {
                nextIndex = (nextIndex + 1) % q.size
            }
            _currentIndex.value = nextIndex
            playTrack(q[nextIndex])
            return
        }

        val nextIndex = _currentIndex.value + 1
        if (nextIndex < q.size) {
            _currentIndex.value = nextIndex
            playTrack(q[nextIndex])
        } else {
            // Continuous Playback: wrap around to first track
            _currentIndex.value = 0
            playTrack(q[0])
        }
    }

    fun skipPrevious() {
        // Standard Apple Music: if more than 3 seconds in, restart track
        if (_currentPositionMs.value > 3000) {
            seekTo(0)
            return
        }

        val q = _queue.value
        if (q.isEmpty()) return

        val prevIndex = _currentIndex.value - 1
        if (prevIndex >= 0) {
            _currentIndex.value = prevIndex
            playTrack(q[prevIndex])
        } else if (q.isNotEmpty()) {
            _currentIndex.value = q.size - 1
            playTrack(q.last())
        }
    }

    fun toggleShuffle() {
        _shuffleEnabled.value = !_shuffleEnabled.value
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
            RepeatMode.OFF -> RepeatMode.ALL
        }
    }

    fun addToQueue(track: Track) {
        _queue.value = _queue.value + track
    }

    fun removeFromQueue(index: Int) {
        val currentList = _queue.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _queue.value = currentList
            if (index < _currentIndex.value) {
                _currentIndex.value = _currentIndex.value - 1
            }
        }
    }

    fun clearQueue() {
        val cur = _currentTrack.value
        if (cur != null) {
            _queue.value = listOf(cur)
            _currentIndex.value = 0
        } else {
            _queue.value = emptyList()
            _currentIndex.value = 0
        }
    }

    private fun handleTrackCompletion() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                seekTo(0)
                exoPlayer?.playWhenReady = true
            }
            RepeatMode.ALL, RepeatMode.OFF -> {
                // Continuous playback guaranteed!
                skipNext()
            }
        }
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = scope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPositionMs.value = player.currentPosition
                        val dur = player.duration
                        if (dur > 0) {
                            _durationMs.value = dur
                        }
                    }
                }
                delay(80)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressUpdates()
        exoPlayer?.release()
        exoPlayer = null
    }
}
