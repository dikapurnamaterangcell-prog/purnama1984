package com.youtubeproxy.player.data.preload

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.source.hls.HlsMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.youtubeproxy.player.domain.model.VideoItem
import com.youtubeproxy.player.utils.Constants
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import java.io.File

@UnstableApi
class VideoPreloadManager(private val context: Context) {
    
    private val preloadCache: Cache by lazy {
        SimpleCache(
            File(context.cacheDir, "preload_cache"),
            androidx.media3.datasource.cache.NoOpCacheEvictor(),
            androidx.media3.database.ExoDatabaseProvider(context)
        )
    }
    
    private val dataSourceFactory: DataSource.Factory by lazy {
        CacheDataSource.Factory()
            .setCache(preloadCache)
            .setUpstreamDataSourceFactory(
                OkHttpDataSource.Factory(
                    OkHttpClient.Builder()
                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .build()
                ).setUserAgent(Constants.USER_AGENT)
            )
            .setCacheWriteDataSinkFactory(null) // Read-only cache untuk preload
    }
    
    private val preloadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val preloadJobs = mutableMapOf<String, Job>()
    
    /**
     * Pre-load video chunks untuk playback tanpa buffering
     * Memuat 30 detik pertama + chunks berikutnya secara progresif
     */
    fun preloadVideo(videoItem: VideoItem, quality: String = "720p") {
        val videoId = videoItem.videoId
        
        // Batalkan preload sebelumnya jika ada
        preloadJobs[videoId]?.cancel()
        
        val job = preloadScope.launch {
            try {
                // Pilih stream yang sesuai
                val selectedStream = videoItem.videoStreams
                    .filter { !it.videoOnly } // Prioritaskan stream dengan audio
                    .find { it.quality.contains(quality) }
                    ?: videoItem.videoStreams.firstOrNull()
                
                val audioStream = videoItem.audioStreams
                    .maxByOrNull { it.bitrate } // Ambil kualitas audio terbaik
                
                if (selectedStream == null) {
                    return@launch
                }
                
                // Preload video chunks [citation:6]
                preloadVideoChunks(selectedStream.url, videoId)
                
                // Preload audio jika terpisah
                if (selectedStream.videoOnly && audioStream != null) {
                    preloadAudioChunks(audioStream.url, videoId)
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        preloadJobs[videoId] = job
    }
    
    private suspend fun preloadVideoChunks(videoUrl: String, videoId: String) {
        withContext(Dispatchers.IO) {
            try {
                val dataSource = dataSourceFactory.createDataSource()
                val uri = android.net.Uri.parse(videoUrl)
                
                // Buka koneksi dan baca chunk pertama
                val dataSpec = androidx.media3.datasource.DataSpec.Builder()
                    .setUri(uri)
                    .setPosition(0)
                    .setLength(Constants.PRELOAD_CHUNK_SIZE) // 5MB pertama
                    .build()
                
                dataSource.open(dataSpec)
                
                // Baca data untuk memicu cache
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytes = 0L
                
                while (dataSource.read(buffer, 0, buffer.size).also { bytesRead = it } != -1) {
                    totalBytes += bytesRead
                    if (totalBytes >= Constants.PRELOAD_CHUNK_SIZE) {
                        break
                    }
                }
                
                dataSource.close()
                
            } catch (e: Exception) {
                throw e
            }
        }
    }
    
    private suspend fun preloadAudioChunks(audioUrl: String, videoId: String) {
        // Implementasi serupa dengan preload video
        withContext(Dispatchers.IO) {
            try {
                val dataSource = dataSourceFactory.createDataSource()
                val uri = android.net.Uri.parse(audioUrl)
                
                val dataSpec = androidx.media3.datasource.DataSpec.Builder()
                    .setUri(uri)
                    .setPosition(0)
                    .setLength(Constants.PRELOAD_CHUNK_SIZE / 2) // Audio lebih kecil
                    .build()
                
                dataSource.open(dataSpec)
                
                val buffer = ByteArray(4096)
                var bytesRead: Int
                
                while (dataSource.read(buffer, 0, buffer.size).also { bytesRead = it } != -1) {
                    if (bytesRead <= 0) break
                }
                
                dataSource.close()
                
            } catch (e: Exception) {
                // Abaikan error audio, video tetap bisa diputar
            }
        }
    }
    
    fun cancelPreload(videoId: String) {
        preloadJobs[videoId]?.cancel()
        preloadJobs.remove(videoId)
    }
    
    fun clearCache() {
        preloadCache.release()
    }
    
    /**
     * Buat ExoPlayer dengan cache yang sama untuk playback dari cache [citation:2]
     */
    fun createPreloadedPlayer(): ExoPlayer {
        val trackSelector = DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters()
                    .setMaxVideoSize(1280, 720)
                    .setPreferredAudioLanguage("en")
            )
        }
        
        return ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .setMediaSourceFactory(
                ProgressiveMediaSource.Factory(dataSourceFactory)
            )
            .setSeekBackIncrementMs(5000)
            .setSeekForwardIncrementMs(10000)
            .build()
    }
}