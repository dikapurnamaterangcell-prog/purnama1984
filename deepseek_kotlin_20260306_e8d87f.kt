package com.youtubeproxy.player.utils

object Constants {
    // Piped API - menggunakan instance publik [citation:1]
    const val PIPED_API_BASE_URL = "https://pipedapi.kavin.rocks"
    
    // User Agent untuk request
    const val USER_AGENT = "YouTubeProxyPlayer/1.0 (Android)"
    
    // Preload configuration [citation:10]
    const val PRELOAD_CHUNK_SIZE = 5 * 1024 * 1024L // 5MB ~ 30 detik video 720p
    const val PRELOAD_NEXT_COUNT = 2 // Pre-load 2 video berikutnya
    
    // Cache configuration
    const val MAX_CACHE_SIZE = 200 * 1024 * 1024L // 200MB max cache
}