package com.youtubeproxy.player.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.youtubeproxy.player.databinding.ActivityMainBinding
import com.youtubeproxy.player.presentation.adapter.VideoAdapter
import com.youtubeproxy.player.presentation.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: PlayerViewModel by viewModel()
    private lateinit var videoAdapter: VideoAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupObservers()
        
        // Contoh video ID untuk demo
        loadSampleVideos()
    }
    
    private fun setupRecyclerView() {
        videoAdapter = VideoAdapter { videoItem ->
            // Navigasi ke PlayerActivity
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("video_id", videoItem.videoId)
                putExtra("video_title", videoItem.title)
            }
            startActivity(intent)
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = videoAdapter
        }
    }
    
    private fun setupObservers() {
        viewModel.videoState.observe(this) { state ->
            when (state) {
                is PlayerViewModel.VideoState.Loading -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                }
                is PlayerViewModel.VideoState.Success -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    // Update UI jika perlu
                }
                is PlayerViewModel.VideoState.Error -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }
    
    private fun loadSampleVideos() {
        lifecycleScope.launch {
            // Simulasi loading data
            binding.progressBar.visibility = android.view.View.VISIBLE
            delay(1000)
            
            // Contoh data (dalam implementasi nyata, ambil dari API trending)
            val sampleVideos = listOf(
                "dQw4w9WgXcQ" to "Rick Astley - Never Gonna Give You Up",
                "kXYiU_JCYtU" to "Luar Biasa - Ed Sheeran"
            ).mapIndexed { index, (id, title) ->
                com.youtubeproxy.player.domain.model.VideoItem(
                    videoId = id,
                    title = title,
                    uploader = "Sample Channel",
                    duration = 212,
                    thumbnailUrl = "https://img.youtube.com/vi/$id/hqdefault.jpg",
                    views = 1000000,
                    uploadDate = "2023-01-01",
                    description = "Sample description"
                )
            }
            
            videoAdapter.submitList(sampleVideos)
            binding.progressBar.visibility = android.view.View.GONE
        }
    }
}