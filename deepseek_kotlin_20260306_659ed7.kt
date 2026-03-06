package com.youtubeproxy.player.di

import com.youtubeproxy.player.data.preload.VideoPreloadManager
import com.youtubeproxy.player.data.repository.VideoRepository
import com.youtubeproxy.player.data.repository.VideoRepositoryImpl
import com.youtubeproxy.player.domain.usercase.GetVideoStreamUseCase
import com.youtubeproxy.player.presentation.viewmodel.PlayerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Repository
    single<VideoRepository> { VideoRepositoryImpl() }
    
    // Preload Manager
    single { VideoPreloadManager(androidContext()) }
    
    // Use Cases [citation:3]
    factory { GetVideoStreamUseCase(get()) }
    
    // ViewModels
    viewModel { PlayerViewModel(get(), get()) }
}