package com.beat.di.module.contentDetails

import androidx.lifecycle.ViewModel
import com.beat.di.module.main.MainScope
import com.beat.di.scope.ViewModelKey
import com.beat.view.audioPlayer.MusicPlayerConnectorViewModel
import com.beat.view.audioPlayer.MusicPlayerViewModel
import com.beat.view.audioPlayer.RadioPlayerViewModel
import com.beat.view.content.details.ContentDetailsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ContentDetailsViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(ContentDetailsViewModel::class)
    abstract fun bindContentDetailsViewModel(contentDetailsViewModel: ContentDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MusicPlayerViewModel::class)
    abstract fun bindMusicPlayerViewModel(musicPlayerViewModel: MusicPlayerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RadioPlayerViewModel::class)
    abstract fun bindRadioPlayerViewModel(radioPlayerViewModel: RadioPlayerViewModel): ViewModel

    @Binds
    @IntoMap
    @ContentScope
    @ViewModelKey(MusicPlayerConnectorViewModel::class)
    abstract fun bindMusicPlayerConnectorViewModel(musicPlayerConnectorViewModel: MusicPlayerConnectorViewModel): ViewModel

}