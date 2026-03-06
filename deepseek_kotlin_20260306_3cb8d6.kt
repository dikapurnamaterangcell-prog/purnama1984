package com.youtubeproxy.player.data.api.models

import com.google.gson.annotations.SerializedName

data class StreamsResponse(
    @SerializedName("title") val title: String,
    @SerializedName("uploader") val uploader: String,
    @SerializedName("duration") val duration: Long,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String,
    @SerializedName("views") val views: Long,
    @SerializedName("uploadDate") val uploadDate: String,
    @SerializedName("description") val description: String,
    @SerializedName("videoStreams") val videoStreams: List<VideoStreamDto>,
    @SerializedName("audioStreams") val audioStreams: List<AudioStreamDto>,
    @SerializedName("proxyUrl") val proxyUrl: String
)

data class VideoStreamDto(
    @SerializedName("url") val url: String,
    @SerializedName("quality") val quality: String,
    @SerializedName("mimeType") val mimeType: String,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int,
    @SerializedName("fps") val fps: Int,
    @SerializedName("bitrate") val bitrate: Long,
    @SerializedName("videoOnly") val videoOnly: Boolean
)

data class AudioStreamDto(
    @SerializedName("url") val url: String,
    @SerializedName("quality") val quality: String,
    @SerializedName("mimeType") val mimeType: String,
    @SerializedName("bitrate") val bitrate: Long,
    @SerializedName("codec") val codec: String
)