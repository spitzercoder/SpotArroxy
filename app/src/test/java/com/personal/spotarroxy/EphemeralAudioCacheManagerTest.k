package com.personal.spotarroxy

import com.personal.spotarroxy.cache.EphemeralAudioCacheManager
import com.personal.spotarroxy.model.MusicTrack
import com.personal.spotarroxy.network.ArroxyOpenSourceEngine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class EphemeralAudioCacheManagerTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var cacheManager: EphemeralAudioCacheManager
    private lateinit var engine: ArroxyOpenSourceEngine

    @Before
    fun setUp() {
        cacheManager = EphemeralAudioCacheManager(tempFolder.newFolder("app_cache"))
        engine = ArroxyOpenSourceEngine()
    }

    @Test
    fun changingSongImmediatelyDeletesPreviousSongFromCachedMemory() {
        val songA = MusicTrack("song_a_101", "First Song", "Artist A", "Album A", "", "")
        val songB = MusicTrack("song_b_202", "Second Song", "Artist B", "Album B", "", "")

        val sessionA = cacheManager.prepareTrackSync(songA) { _, targetFile ->
            targetFile.writeBytes(ByteArray(4096) { 0x1A })
        }
        assertTrue(sessionA.cachedFile.exists())
        assertEquals(1, cacheManager.listCachedFiles().size)

        val sessionB = cacheManager.prepareTrackSync(songB) { _, targetFile ->
            targetFile.writeBytes(ByteArray(8192) { 0x2B })
        }
        assertFalse(sessionA.cachedFile.exists())
        assertTrue(sessionB.cachedFile.exists())
        assertEquals(1, cacheManager.listCachedFiles().size)
        assertEquals("song_b_202", cacheManager.activeSession?.track?.id)
        assertTrue(cacheManager.evictionLog.any { it.trackId == "song_a_101" })
    }

    @Test
    fun evictCurrentTrackDeletesActiveCachedFile() {
        val song = MusicTrack("song_cleanup_303", "Cleanup Track", "Tester", "Test Album", "", "")
        val session = cacheManager.prepareTrackSync(song) { _, targetFile ->
            targetFile.writeBytes(ByteArray(2048) { 0x3C })
        }
        assertTrue(session.cachedFile.exists())

        cacheManager.evictCurrentTrack("User stopped playback")
        assertFalse(session.cachedFile.exists())
        assertEquals(0, cacheManager.listCachedFiles().size)
        assertNull(cacheManager.activeSession)
    }

    @Test
    fun arroxyUrlSanitizerStripsTrackingParameters() {
        val host = listOf("music", "example", "com").joinToString(".")
        val dirtyUrl = "https://$host/stream.m4a?id=99&si=abc123track&utm_source=share&pp=xyz&fbclid=111"
        val cleanUrl = engine.sanitizeStreamUrl(dirtyUrl)
        assertEquals("https://$host/stream.m4a?id=99", cleanUrl)
    }
}
