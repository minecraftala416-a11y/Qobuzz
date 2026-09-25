package com.example.data.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val albumTitle: String,
    val coverUrl: String,
    val durationSeconds: Int,
    val bitDepth: Int = 24,
    val samplingRate: Double = 96.0,
    val isHiRes: Boolean = true,
    val streamUrl: String = "",
    val lyrics: String = "",
    val syncedLyrics: String = "",
    val releaseYear: String = "2024",
    val genre: String = "Hi-Res Audiophile",
    val primaryColorHex: Long = 0xFFFA2D48,
    val secondaryColorHex: Long = 0xFF5856D6
) {
    val durationFormatted: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }

    val audioQualityBadge: String
        get() = when {
            isHiRes && bitDepth >= 24 && samplingRate >= 96.0 -> "Hi-Res LOSSLESS • ${bitDepth}-bit/${samplingRate.toInt()} kHz"
            isHiRes -> "Hi-Res LOSSLESS • ${bitDepth}-bit/${samplingRate.toInt()} kHz"
            bitDepth >= 16 -> "LOSSLESS • 16-bit/44.1 kHz"
            else -> "Apple Lossless • 24-bit/48 kHz"
        }
}

enum class AudioQuality(
    val formatId: Int,
    val label: String,
    val description: String,
    val bitDepth: Int,
    val samplingRate: Double
) {
    AAC_HIGH(5, "AAC 320 kbps", "High quality compressed audio", 16, 44.1),
    LOSSLESS_CD(6, "ALAC 16-bit / 44.1 kHz", "CD Quality Apple Lossless", 16, 44.1),
    HIRES_LOSSLESS_96(7, "ALAC 24-bit / 96 kHz", "Studio Master Hi-Res Lossless", 24, 96.0),
    HIRES_LOSSLESS_192(27, "ALAC 24-bit / 192 kHz", "Ultimate Audiophile Studio Master", 24, 192.0)
}

data class QobuzConfig(
    val appId: String = "712109809",
    val appSecret: String = "890250df7a250352ff9a08ec7fae44eb",
    val userAuthToken: String = "",
    val quality: AudioQuality = AudioQuality.HIRES_LOSSLESS_96,
    val isConnected: Boolean = true
)

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val coverUrl: String,
    val tracksCount: Int,
    val releaseYear: String,
    val isHiRes: Boolean = true,
    val genre: String = "Audiophile",
    val tracks: List<Track> = emptyList()
)
