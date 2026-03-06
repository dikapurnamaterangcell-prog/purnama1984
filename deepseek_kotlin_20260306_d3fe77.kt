package com.youtubeproxy.player.data.api

import com.youtubeproxy.player.data.api.models.StreamsResponse
import com.youtubeproxy.player.utils.Constants
import retrofit2.http.GET
import retrofit2.http.Path

interface PipedApiService {
    @GET("/streams/{videoId}")
    suspend fun getVideoStreams(
        @Path("videoId") videoId: String
    ): StreamsResponse
}

// Api Client
object ApiClient {
    private val retrofit by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(Constants.PIPED_API_BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .client(okhttp3.OkHttpClient.Builder()
                .addInterceptor(okhttp3.logging.HttpLoggingInterceptor().apply {
                    level = okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
                })
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build())
            .build()
    }

    val pipedApiService: PipedApiService by lazy {
        retrofit.create(PipedApiService::class.java)
    }
}