package com.beat.di.module.videoPlayer

import androidx.lifecycle.ViewModel
import com.beat.di.scope.ViewModelKey
import com.beat.view.videoPlayer.VideoPlayerViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class VideoPlayerViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(VideoPlayerViewModel::class)
    abstract fun bindVideoPlayerViewModel(videoPlayerViewModel: VideoPlayerViewModel): ViewModel

}