package com.youtubeproxy.player.domain.usercase

import com.youtubeproxy.player.data.repository.VideoRepository
import com.youtubeproxy.player.domain.model.VideoItem

class GetVideoStreamUseCase(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(videoId: String): Result<VideoItem> {
        return repository.getVideoStreams(videoId)
    }
}