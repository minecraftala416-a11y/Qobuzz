package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Track

@Entity(tableName = "favorite_tracks")
data class FavoriteTrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val albumTitle: String,
    val coverUrl: String,
    val durationSeconds: Int,
    val bitDepth: Int,
    val samplingRate: Double,
    val isHiRes: Boolean,
    val streamUrl: String,
    val lyrics: String = "",
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toTrack(): Track = Track(
        id = id,
        title = title,
        artist = artist,
        albumTitle = albumTitle,
        coverUrl = coverUrl,
        durationSeconds = durationSeconds,
        bitDepth = bitDepth,
        samplingRate = samplingRate,
        isHiRes = isHiRes,
        streamUrl = streamUrl,
        lyrics = lyrics
    )
}

@Entity(tableName = "history_tracks")
data class HistoryTrackEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val trackId: String,
    val title: String,
    val artist: String,
    val albumTitle: String,
    val coverUrl: String,
    val durationSeconds: Int,
    val bitDepth: Int,
    val samplingRate: Double,
    val isHiRes: Boolean,
    val streamUrl: String,
    val playedAt: Long = System.currentTimeMillis()
) {
    fun toTrack(): Track = Track(
        id = trackId,
        title = title,
        artist = artist,
        albumTitle = albumTitle,
        coverUrl = coverUrl,
        durationSeconds = durationSeconds,
        bitDepth = bitDepth,
        samplingRate = samplingRate,
        isHiRes = isHiRes,
        streamUrl = streamUrl
    )
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val coverUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_items")
data class PlaylistItemEntity(
    @PrimaryKey(autoGenerate = true) val itemId: Long = 0,
    val playlistId: Long,
    val trackId: String,
    val title: String,
    val artist: String,
    val albumTitle: String,
    val coverUrl: String,
    val durationSeconds: Int,
    val bitDepth: Int,
    val samplingRate: Double,
    val isHiRes: Boolean,
    val streamUrl: String,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toTrack(): Track = Track(
        id = trackId,
        title = title,
        artist = artist,
        albumTitle = albumTitle,
        coverUrl = coverUrl,
        durationSeconds = durationSeconds,
        bitDepth = bitDepth,
        samplingRate = samplingRate,
        isHiRes = isHiRes,
        streamUrl = streamUrl
    )
}
