package com.example.data.repository

import com.example.data.local.FavoriteTrackEntity
import com.example.data.local.HistoryTrackEntity
import com.example.data.local.MusicDao
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistItemEntity
import com.example.data.model.Album
import com.example.data.model.QobuzConfig
import com.example.data.model.Track
import com.example.data.remote.CuratedMusicData
import com.example.data.remote.QobuzClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepository(
    private val musicDao: MusicDao,
    private val qobuzClient: QobuzClient
) {
    val favorites: Flow<List<Track>> = musicDao.getAllFavorites().map { list ->
        list.map { it.toTrack() }
    }

    val history: Flow<List<Track>> = musicDao.getRecentHistory().map { list ->
        list.map { it.toTrack() }
    }

    val playlists: Flow<List<PlaylistEntity>> = musicDao.getAllPlaylists()

    fun getPlaylistTracks(playlistId: Long): Flow<List<Track>> {
        return musicDao.getPlaylistTracks(playlistId).map { list ->
            list.map { it.toTrack() }
        }
    }

    fun isFavorite(trackId: String): Flow<Boolean> = musicDao.isFavorite(trackId)

    suspend fun toggleFavorite(track: Track, isCurrentlyFavorite: Boolean) {
        if (isCurrentlyFavorite) {
            musicDao.deleteFavorite(track.id)
        } else {
            musicDao.insertFavorite(
                FavoriteTrackEntity(
                    id = track.id,
                    title = track.title,
                    artist = track.artist,
                    albumTitle = track.albumTitle,
                    coverUrl = track.coverUrl,
                    durationSeconds = track.durationSeconds,
                    bitDepth = track.bitDepth,
                    samplingRate = track.samplingRate,
                    isHiRes = track.isHiRes,
                    streamUrl = track.streamUrl,
                    lyrics = track.lyrics
                )
            )
        }
    }

    suspend fun recordHistory(track: Track) {
        musicDao.insertHistory(
            HistoryTrackEntity(
                trackId = track.id,
                title = track.title,
                artist = track.artist,
                albumTitle = track.albumTitle,
                coverUrl = track.coverUrl,
                durationSeconds = track.durationSeconds,
                bitDepth = track.bitDepth,
                samplingRate = track.samplingRate,
                isHiRes = track.isHiRes,
                streamUrl = track.streamUrl
            )
        )
    }

    suspend fun createPlaylist(name: String, description: String = ""): Long {
        return musicDao.insertPlaylist(
            PlaylistEntity(
                name = name,
                description = description,
                coverUrl = "https://static.qobuz.com/images/covers/50/06/0724384960650_600.jpg"
            )
        )
    }

    suspend fun addTrackToPlaylist(playlistId: Long, track: Track) {
        musicDao.insertPlaylistItem(
            PlaylistItemEntity(
                playlistId = playlistId,
                trackId = track.id,
                title = track.title,
                artist = track.artist,
                albumTitle = track.albumTitle,
                coverUrl = track.coverUrl,
                durationSeconds = track.durationSeconds,
                bitDepth = track.bitDepth,
                samplingRate = track.samplingRate,
                isHiRes = track.isHiRes,
                streamUrl = track.streamUrl
            )
        )
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        musicDao.removePlaylistItem(playlistId, trackId)
    }

    suspend fun deletePlaylist(playlistId: Long) {
        musicDao.deletePlaylist(playlistId)
    }

    suspend fun searchTracks(query: String, config: QobuzConfig): List<Track> {
        return qobuzClient.search(query, config)
    }

    fun getRealFeaturedTracks(): List<Track> = CuratedMusicData.realFeaturedTracks

    fun getRealFeaturedAlbums(): List<Album> = CuratedMusicData.realFeaturedAlbums

    suspend fun resolveStreamUrl(track: Track, config: QobuzConfig): String {
        return qobuzClient.getStreamUrl(track, config)
    }

    suspend fun loginToQobuz(email: String, pwd: String, appId: String): Pair<Boolean, String> {
        return qobuzClient.login(email, pwd, appId)
    }

    suspend fun fetchLyrics(track: Track): com.example.data.remote.LrcLyrics? {
        return qobuzClient.fetchLyricsFromLrcLib(track)
    }

    suspend fun testConnection(config: QobuzConfig): Pair<Boolean, String> {
        return qobuzClient.testConnection(config)
    }
}
