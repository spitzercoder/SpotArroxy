package com.personal.spotarroxy.network

import com.personal.spotarroxy.model.MusicTrack
import java.net.URI

class ArroxyOpenSourceEngine {

    private val blockedExactParams = setOf("si", "pp", "fbclid", "gclid", "feature")

    fun sanitizeStreamUrl(rawUrl: String): String {
        return runCatching {
            val uri = URI(rawUrl)
            val rawQuery = uri.rawQuery ?: return rawUrl
            val keptParams = rawQuery.split("&")
                .filter { it.isNotBlank() }
                .filter { pair ->
                    val key = pair.substringBefore("=").lowercase()
                    key !in blockedExactParams && !key.startsWith("utm_")
                }
            val base = buildString {
                if (uri.scheme != null) append(uri.scheme).append("://")
                if (uri.rawAuthority != null) append(uri.rawAuthority)
                if (uri.rawPath != null) append(uri.rawPath)
            }
            if (keptParams.isEmpty()) base else "$base?${keptParams.joinToString("&")}"
        }.getOrDefault(rawUrl)
    }

    fun defaultCatalog(): List<MusicTrack> = listOf(
        MusicTrack(
            id = "arroxy_101",
            title = "Midnight Obsidian",
            artist = "Orionus Synth",
            album = "Nightfall Sessions",
            artworkUrl = "",
            streamUrl = "",
            category = "Electronic",
            durationMs = 215_000L
        ),
        MusicTrack(
            id = "arroxy_102",
            title = "Neon Drift",
            artist = "Pulse Collective",
            album = "City Lights",
            artworkUrl = "",
            streamUrl = "",
            category = "Pop",
            durationMs = 198_000L
        )
    )
}
