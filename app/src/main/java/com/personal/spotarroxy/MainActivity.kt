package com.personal.spotarroxy

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.personal.spotarroxy.cache.*
import com.personal.spotarroxy.model.MusicTrack
import com.personal.spotarroxy.network.ArroxyOpenSourceEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PlayerUiState(
    val latestHits: List<MusicTrack> = emptyList(),
    val currentTrack: MusicTrack? = null,
    val isPlaying: Boolean = false,
    val activeCacheSession: CacheSession? = null,
    val cachedFileCount: Int = 0,
    val evictionHistory: List<CacheEvictionEvent> = emptyList(),
    val statusBanner: String = "Ephemeral single-track cache active: previous song auto-deletes on track change."
)

class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val engine = ArroxyOpenSourceEngine()
    private val cacheManager = EphemeralAudioCacheManager(application.cacheDir)
    private val allTracks = engine.defaultCatalog()
    private val exoPlayer: ExoPlayer by lazy { ExoPlayer.Builder(application).build() }
    private val _uiState = MutableStateFlow(PlayerUiState(latestHits = allTracks))
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun playTrack(track: MusicTrack) {
        viewModelScope.launch {
            val clean = track.copy(streamUrl = engine.sanitizeStreamUrl(track.streamUrl))
            val session = withContext(Dispatchers.IO) { cacheManager.prepareTrackSync(clean) }
            runCatching {
                exoPlayer.setMediaItem(MediaItem.fromUri(session.cachedFile.toURI().toString()))
                exoPlayer.prepare()
                exoPlayer.play()
            }
            _uiState.update {
                it.copy(
                    currentTrack = clean,
                    isPlaying = true,
                    activeCacheSession = session,
                    cachedFileCount = cacheManager.listCachedFiles().size,
                    evictionHistory = cacheManager.evictionLog,
                    statusBanner = "Playing temp file: ${session.cachedFile.name}"
                )
            }
        }
    }

    fun stopAndPurgeCache() {
        runCatching { exoPlayer.stop() }
        cacheManager.evictCurrentTrack("Manual purge")
        _uiState.update { it.copy(isPlaying = false, activeCacheSession = null, cachedFileCount = 0, evictionHistory = cacheManager.evictionLog) }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[MusicPlayerViewModel::class.java]
        setContent {
            val state by viewModel.uiState.collectAsState()
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF1DB954), background = Color(0xFF121212), surface = Color(0xFF181818))) {
                Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("SpotArroxy", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(state.statusBanner, color = Color(0xFF1DB954), fontSize = 13.sp)
                    Button(onClick = viewModel::stopAndPurgeCache) { Text("Purge Temp Cache (${state.cachedFileCount} file)") }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.latestHits) { track ->
                            Surface(color = Color(0xFF282828), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().clickable { viewModel.playTrack(track) }) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(track.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text("${track.artist} • ${track.album}", color = Color(0xFFB3B3B3), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
