package com.personal.spotarroxy.cache

import com.personal.spotarroxy.model.MusicTrack
import java.io.File
import java.net.URL

data class CacheSession(
    val track: MusicTrack,
    val cachedFile: File,
    val downloadedBytes: Long,
    val createdAtMs: Long = System.currentTimeMillis()
)

data class CacheEvictionEvent(
    val trackId: String,
    val fileName: String,
    val freedBytes: Long,
    val reason: String,
    val timestampMs: Long = System.currentTimeMillis()
)

class EphemeralAudioCacheManager(baseCacheDir: File) {

    private val cacheDir: File = File(baseCacheDir, "ephemeral_audio_cache").apply {
        if (!exists()) mkdirs()
    }

    @Volatile
    var activeSession: CacheSession? = null
        private set

    private val _evictionLog = mutableListOf<CacheEvictionEvent>()
    val evictionLog: List<CacheEvictionEvent>
        @Synchronized get() = _evictionLog.toList()

    @Synchronized
    fun prepareTrackSync(
        track: MusicTrack,
        downloader: (MusicTrack, File) -> Unit = ::defaultDownloadTrack
    ): CacheSession {
        val targetFile = File(cacheDir, "ephemeral_track_${track.id}.m4a")
        evictAllExcept(targetFile, "Track changed to ${track.title} (${track.id})")

        if (targetFile.exists()) {
            targetFile.delete()
        }

        downloader(track, targetFile)
        if (!targetFile.exists()) {
            targetFile.writeBytes(ByteArray(0))
        }

        val session = CacheSession(
            track = track,
            cachedFile = targetFile,
            downloadedBytes = targetFile.length()
        )
        activeSession = session
        return session
    }

    @Synchronized
    fun evictAllExcept(keepFile: File?, reason: String) {
        val previousTrackId = activeSession?.track?.id
        val files = listCachedFiles()
        for (file in files) {
            if (keepFile == null || file.absolutePath != keepFile.absolutePath) {
                val bytes = file.length()
                val inferredTrackId = previousTrackId
                    ?: file.name.removePrefix("ephemeral_track_").removeSuffix(".m4a")
                if (file.delete()) {
                    _evictionLog.add(
                        0,
                        CacheEvictionEvent(
                            trackId = inferredTrackId,
                            fileName = file.name,
                            freedBytes = bytes,
                            reason = reason
                        )
                    )
                }
            }
        }
    }

    @Synchronized
    fun evictCurrentTrack(reason: String = "Playback stopped") {
        evictAllExcept(null, reason)
        activeSession = null
    }

    @Synchronized
    fun listCachedFiles(): List<File> {
        return cacheDir.listFiles()
            ?.filter { it.isFile && it.name.startsWith("ephemeral_track_") }
            ?.sortedBy { it.name }
            ?: emptyList()
    }

    private fun defaultDownloadTrack(track: MusicTrack, targetFile: File) {
        runCatching {
            URL(track.streamUrl).openStream().use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }.onFailure {
            targetFile.writeBytes(ByteArray(16_384) { (it.hashCode() and 0xFF).toByte() })
        }
    }
}
