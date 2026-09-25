package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class LrcLyrics(
    val id: Long = 0,
    val trackName: String = "",
    val artistName: String = "",
    val albumName: String = "",
    val durationSeconds: Double = 0.0,
    val plainLyrics: String = "",
    val syncedLyrics: String = "",
    val isInstrumental: Boolean = false
)

object LrcLibClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private const val BASE_URL = "https://lrclib.net/api"

    /**
     * Cleans song title from annotations like "(Remastered)", "[24-Bit]", "feat. X"
     * to maximize match rate on LRCLIB.
     */
    private fun cleanTitle(title: String): String {
        return title
            .replace(Regex("\\(.*?\\)"), "")
            .replace(Regex("\\[.*?\\]"), "")
            .replace(Regex("(?i)feat\\..*"), "")
            .replace(Regex("(?i)ft\\..*"), "")
            .trim()
    }

    /**
     * Fetches real synced and plain lyrics from LRCLIB.net.
     * Tries exact match first (/api/get), then falls back to full-text search (/api/search).
     */
    suspend fun getLyrics(
        trackName: String,
        artistName: String,
        albumName: String = "",
        durationSeconds: Int = 0
    ): LrcLyrics? = withContext(Dispatchers.IO) {
        val cleanName = cleanTitle(trackName)
        val cleanArtist = artistName.split(",", "&", "feat.", "ft.")[0].trim()

        // 1. Try exact get endpoint
        val exactResult = tryGetExact(cleanName, cleanArtist, albumName, durationSeconds)
        if (exactResult != null && (exactResult.syncedLyrics.isNotBlank() || exactResult.plainLyrics.isNotBlank())) {
            return@withContext exactResult
        }

        // 2. Try full-text search fallback
        val searchResult = trySearch(cleanName, cleanArtist)
        if (searchResult != null) {
            return@withContext searchResult
        }

        // 3. Fallback search with original title
        if (cleanName != trackName) {
            return@withContext trySearch(trackName, cleanArtist)
        }

        null
    }

    private fun tryGetExact(
        trackName: String,
        artistName: String,
        albumName: String,
        durationSeconds: Int
    ): LrcLyrics? {
        try {
            val tEnc = URLEncoder.encode(trackName, "UTF-8")
            val aEnc = URLEncoder.encode(artistName, "UTF-8")
            var url = "$BASE_URL/get?track_name=$tEnc&artist_name=$aEnc"
            if (albumName.isNotBlank()) {
                url += "&album_name=${URLEncoder.encode(albumName, "UTF-8")}"
            }
            if (durationSeconds > 0) {
                url += "&duration=$durationSeconds"
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "AppleMusicQobuz/2.0 (Android; https://github.com/vitiko98/qobuz-dl)")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()
            if (response.isSuccessful && body.isNotBlank()) {
                return parseLyricsJson(JSONObject(body))
            }
        } catch (e: Exception) {
            Log.w("LrcLibClient", "Exact lookup failed for $trackName - $artistName: ${e.message}")
        }
        return null
    }

    private fun trySearch(trackName: String, artistName: String): LrcLyrics? {
        try {
            val query = "$artistName $trackName".trim()
            val qEnc = URLEncoder.encode(query, "UTF-8")
            val url = "$BASE_URL/search?q=$qEnc"

            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "AppleMusicQobuz/2.0 (Android; https://github.com/vitiko98/qobuz-dl)")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()
            if (response.isSuccessful && body.isNotBlank()) {
                val array = JSONArray(body)
                if (array.length() > 0) {
                    // Pick the best match with synced lyrics
                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        val synced = item.optString("syncedLyrics")
                        if (synced.isNotBlank()) {
                            return parseLyricsJson(item)
                        }
                    }
                    // Or first item
                    return parseLyricsJson(array.getJSONObject(0))
                }
            }
        } catch (e: Exception) {
            Log.w("LrcLibClient", "Search lookup failed for $trackName: ${e.message}")
        }
        return null
    }

    private fun parseLyricsJson(json: JSONObject): LrcLyrics {
        return LrcLyrics(
            id = json.optLong("id", 0),
            trackName = json.optString("trackName"),
            artistName = json.optString("artistName"),
            albumName = json.optString("albumName"),
            durationSeconds = json.optDouble("duration", 0.0),
            plainLyrics = json.optString("plainLyrics"),
            syncedLyrics = json.optString("syncedLyrics"),
            isInstrumental = json.optBoolean("instrumental", false)
        )
    }
}
