package com.personal.spotarroxy.network

import com.personal.spotarroxy.model.MusicTrack
import java.net.URI

class ArroxyOpenSourceEngine {
    private val blocked = setOf("si", "pp", "fbclid", "gclid", "feature")

    fun sanitizeStreamUrl(rawUrl: String): String = runCatching {
        val uri = URI(rawUrl)
        val q = uri.rawQuery ?: return rawUrl
        val kept = q.split("&").filter { it.isNotBlank() }.filter { p ->
            val k = p.substringBefore("=").lowercase()
            k !in blocked && !k.startsWith("utm_")
        }
        val base = "${uri.scheme}://${uri.rawAuthority}${uri.rawPath}"
        if (kept.isEmpty()) base else "$base?${kept.joinToString("&")}"
    }.getOrDefault(rawUrl)

    fun defaultCatalog(): List<MusicTrack> = listOf(
        MusicTrack("arroxy_101", "Midnight Obsidian", "Orionus Synth", "Nightfall", "", "", "Electronic", 215_000L),
        MusicTrack("arroxy_102", "Neon Drift", "Pulse Collective", "City Lights", "", "", "Pop", 198_000L),
        MusicTrack("arroxy_103", "Rain on Vinyl", "Kairo Lo-Fi", "Study Tape", "", "", "Lo-Fi", 174_000L)
    )
}
