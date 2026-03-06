package com.youtubeproxy.player.presentation

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import com.youtubeproxy.player.databinding.ActivityPlayerBinding
import com.youtubeproxy.player.domain.model.VideoItem
import com.youtubeproxy.player.presentation.viewmodel.PlayerViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPlayerBinding
    private val viewModel: PlayerViewModel by viewModel()
    
    private var player: androidx.media3.exoplayer.ExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val videoId = intent.getStringExtra("video_id") ?: return
        val videoTitle = intent.getStringExtra("video_title") ?: "Video Player"
        
        supportActionBar?.title = videoTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        setupObservers()
        
        // Load video
        viewModel.loadVideo(videoId)
    }
    
    private fun setupObservers() {
        viewModel.videoState.observe(this) { state ->
            when (state) {
                is PlayerViewModel.VideoState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.playerView.visibility = View.GONE
                }
                is PlayerViewModel.VideoState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.playerView.visibility = View.VISIBLE
                    initializePlayer(state.video)
                }
                is PlayerViewModel.VideoState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }
    
    private fun initializePlayer(video: VideoItem) {
        // Cari stream terbaik (prioritas 720p dengan audio)
        val videoStream = video.videoStreams
            .filter { !it.videoOnly }
            .find { it.quality.contains("720") || it.quality.contains("480") }
            ?: video.videoStreams.firstOrNull()
        
        if (videoStream == null) {
            Toast.makeText(this, "No suitable video stream found", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Buat player dari preload manager
        player = viewModel.preloadManager.createPreloadedPlayer().apply {
            playWhenReady = true
            
            // Buat media item
            val mediaItem = MediaItem.Builder()
                .setUri(videoStream.url)
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(video.title)
                        .setArtist(video.uploader)
                        .build()
                )
                .build()
            
            setMediaItem(mediaItem)
            prepare()
            
            // Listener untuk debug
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            // Sedang buffering, preload manager sudah bekerja
                        }
                        Player.STATE_READY -> {
                            // Siap diputar, tidak buffering
                        }
                    }
                }
                
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        // Saat mulai diputar, pre-load video berikutnya
                        lifecycleScope.launch {
                            viewModel.playNext() // Ini akan memicu preload
                        }
                    }
                }
            })
        }
        
        binding.playerView.player = player
    }
    
    override fun onResume() {
        super.onResume()
        if (player == null) {
            // Player akan diinisialisasi ulang oleh observer
        } else {
            player?.play()
        }
    }
    
    override fun onPause() {
        super.onPause()
        player?.pause()
    }
    
    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}