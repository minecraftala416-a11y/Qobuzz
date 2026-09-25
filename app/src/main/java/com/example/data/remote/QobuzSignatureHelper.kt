package com.example.data.remote

import java.security.MessageDigest

object QobuzSignatureHelper {

    fun generateSignature(
        endpoint: String = "trackgetFileUrl",
        formatId: Int,
        trackId: String,
        requestTs: Long,
        appSecret: String
    ): String {
        // qobuz-dl signature algorithm:
        // Concatenate method and sorted parameters (excluding app_id and user_auth_token)
        // string: "trackgetFileUrlformat_id<format_id>intentstreamtrack_id<track_id><request_ts><app_secret>"
        val raw = "${endpoint}format_id${formatId}intentstreamtrack_id${trackId}${requestTs}${appSecret}"
        return md5(raw)
    }

    fun md5(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
