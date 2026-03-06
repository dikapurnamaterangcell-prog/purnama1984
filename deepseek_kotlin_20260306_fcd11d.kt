package com.youtubeproxy.player.data.repository

import com.youtubeproxy.player.data.api.ApiClient
import com.youtubeproxy.player.domain.model.AudioStream
import com.youtubeproxy.player.domain.model.VideoItem
import com.youtubeproxy.player.domain.model.VideoStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface VideoRepository {
    suspend fun getVideoStreams(videoId: String): Result<VideoItem>
}

class VideoRepositoryImpl : VideoRepository {
    
    override suspend fun getVideoStreams(videoId: String): Result<VideoItem> = runCatching {
        val response = ApiClient.pipedApiService.getVideoStreams(videoId)
        
        VideoItem(
            videoId = videoId,
            title = response.title,
            uploader = response.uploader,
            duration = response.duration,
            thumbnailUrl = response.thumbnailUrl,
            views = response.views,
            uploadDate = response.uploadDate,
            description = response.description,
            videoStreams = response.videoStreams.map { dto ->
                VideoStream(
                    url = dto.url,
                    quality = dto.quality,
                    mimeType = dto.mimeType,
                    width = dto.width,
                    height = dto.height,
                    fps = dto.fps,
                    bitrate = dto.bitrate,
                    videoOnly = dto.videoOnly
                )
            },
            audioStreams = response.audioStreams.map { dto ->
                AudioStream(
                    url = dto.url,
                    quality = dto.quality,
                    mimeType = dto.mimeType,
                    bitrate = dto.bitrate,
                    codec = dto.codec
                )
            }
        )
    }
}