package com.personal.spotarroxy.model

data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUrl: String,
    val streamUrl: String,
    val category: String = "Latest Hits",
    val durationMs: Long = 180_000L
)
