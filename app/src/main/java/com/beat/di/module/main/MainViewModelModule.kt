package com.beat.di.module.main

import androidx.lifecycle.ViewModel
import com.beat.di.scope.ViewModelKey
import com.beat.view.audioPlayer.MusicPlayerConnectorViewModel
import com.beat.view.audioPlayer.MusicPlayerViewModel
import com.beat.view.audioPlayer.RadioPlayerViewModel
import com.beat.view.main.MainViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel

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
    @MainScope
    @ViewModelKey(MusicPlayerConnectorViewModel::class)
    abstract fun bindMusicPlayerConnectorViewModel(musicPlayerConnectorViewModel: MusicPlayerConnectorViewModel): ViewModel

}