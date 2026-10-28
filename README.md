# SpotArroxy — Personal Android Music App

A personal-use Android music streaming application built with **Kotlin**, **Jetpack Compose**, and **Media3 ExoPlayer**, featuring a **Spotify-inspired dark UI** and an **ephemeral single-track cache** that automatically deletes each downloaded song from cached memory the instant the track changes.

## Key Features

1. **Spotify-Inspired Dark UI (Custom Jetpack Compose Implementation)**
   - Signature `#121212` obsidian canvas, `#181818` elevated surfaces, `#282828` cards, and `#1DB954` accent green.
   - **Home Tab**: Greeting bar, live cache badge, category filter pills, horizontal *Latest Hits* carousel, and full tracklist.
   - **Search Tab**: Spotify-style search bar with live debounced track queries.
   - **Temp Cache Monitor Tab**: Real-time inspection of the active temporary audio file, byte size, manual purge button, and automatic deletion audit log.
   - **Floating Mini-Player & Full-Screen Now Playing Sheet**: Expandable player with artwork, seekbar, and transport controls.

2. **Open-Source Discovery & Stream Intake**
   - Inspired by Arroxy (URL tracking parameter stripping for `si`, `pp`, `utm_*`, `fbclid`, `gclid` and stream intake profiles) and NewPipeExtractor.

3. **Strict Temporary Cache (Auto-Delete on Track Change)**
   - Managed by `EphemeralAudioCacheManager` in `app/src/main/java/com/personal/spotarroxy/cache/EphemeralAudioCacheManager.kt`.
   - Downloads the active song into `cacheDir/ephemeral_audio_cache/ephemeral_track_<id>.m4a`.
   - **Immediate Eviction**: As soon as a new song is selected, skipped, or playback stops, the previous song's cached file is immediately deleted from disk (`evictAllExcept`), ensuring at most **1** temporary audio file ever exists in cached memory.

## Building & Downloading the APK

GitHub Actions automatically runs JUnit tests (`EphemeralAudioCacheManagerTest`) and builds the debug APK (`SpotArroxy-debug.apk`) on every push. Download the compiled APK from the **Actions -> Build & Upload SpotArroxy APK -> Artifacts** section.
