package com.youtubeproxy.player.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtubeproxy.player.data.preload.VideoPreloadManager
import com.youtubeproxy.player.domain.model.VideoItem
import com.youtubeproxy.player.domain.usercase.GetVideoStreamUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val getVideoStreamUseCase: GetVideoStreamUseCase,
    private val preloadManager: VideoPreloadManager
) : ViewModel() {
    
    private val _videoState = MutableStateFlow<VideoState>(VideoState.Initial)
    val videoState: StateFlow<VideoState> = _videoState.asStateFlow()
    
    private val _currentVideo = MutableStateFlow<VideoItem?>(null)
    val currentVideo: StateFlow<VideoItem?> = _currentVideo.asStateFlow()
    
    // Daftar video untuk playlist (contoh)
    private val playlist = mutableListOf<VideoItem>()
    private var currentIndex = -1
    
    fun loadVideo(videoId: String) {
        viewModelScope.launch {
            _videoState.value = VideoState.Loading
            
            val result = getVideoStreamUseCase(videoId)
            
            result.fold(
                onSuccess = { videoItem ->
                    _videoState.value = VideoState.Success(videoItem)
                    _currentVideo.value = videoItem
                    
                    // Pre-load video saat ini
                    preloadManager.preloadVideo(videoItem)
                    
                    // Pre-load video berikutnya dalam playlist
                    preloadNextVideos(videoItem)
                },
                onFailure = { error ->
                    _videoState.value = VideoState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }
    
    private fun preloadNextVideos(currentVideo: VideoItem) {
        // Cari index video saat ini di playlist
        val currentIdx = playlist.indexOfFirst { it.videoId == currentVideo.videoId }
        if (currentIdx == -1) return
        
        // Pre-load 2 video berikutnya [citation:10]
        for (i in 1..2) {
            val nextIdx = currentIdx + i
            if (nextIdx < playlist.size) {
                preloadManager.preloadVideo(playlist[nextIdx])
            }
        }
        
        // Pre-load 1 video sebelumnya (untuk navigasi back)
        val prevIdx = currentIdx - 1
        if (prevIdx >= 0) {
            preloadManager.preloadVideo(playlist[prevIdx])
        }
    }
    
    fun setPlaylist(videos: List<VideoItem>, startIndex: Int = 0) {
        playlist.clear()
        playlist.addAll(videos)
        currentIndex = startIndex
        
        if (playlist.isNotEmpty()) {
            loadVideo(playlist[startIndex].videoId)
        }
    }
    
    fun playNext() {
        if (currentIndex + 1 < playlist.size) {
            currentIndex++
            loadVideo(playlist[currentIndex].videoId)
        }
    }
    
    fun playPrevious() {
        if (currentIndex - 1 >= 0) {
            currentIndex--
            loadVideo(playlist[currentIndex].videoId)
        }
    }
    
    override fun onCleared() {
        preloadManager.clearCache()
        super.onCleared()
    }
    
    sealed class VideoState {
        object Initial : VideoState()
        object Loading : VideoState()
        data class Success(val video: VideoItem) : VideoState()
        data class Error(val message: String) : VideoState()
    }
}