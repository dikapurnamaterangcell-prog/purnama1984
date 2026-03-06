package com.youtubeproxy.player.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoItem(
    val videoId: String,
    val title: String,
    val uploader: String,
    val duration: Long,
    val thumbnailUrl: String,
    val views: Long,
    val uploadDate: String,
    val description: String = "",
    val videoStreams: List<VideoStream> = emptyList(),
    val audioStreams: List<AudioStream> = emptyList()
) : Parcelable

@Parcelize
data class VideoStream(
    val url: String,
    val quality: String,
    val mimeType: String,
    val width: Int,
    val height: Int,
    val fps: Int,
    val bitrate: Long,
    val videoOnly: Boolean
) : Parcelable

@Parcelize
data class AudioStream(
    val url: String,
    val quality: String,
    val mimeType: String,
    val bitrate: Long,
    val codec: String
) : Parcelable