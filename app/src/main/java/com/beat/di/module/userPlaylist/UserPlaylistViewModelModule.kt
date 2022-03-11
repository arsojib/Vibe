package com.beat.di.module.userPlaylist

import androidx.lifecycle.ViewModel
import com.beat.di.scope.ViewModelKey
import com.beat.view.common.queue.PlayQueueViewModel
import com.beat.view.common.userPlaylist.UserPlaylistViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class UserPlaylistViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(UserPlaylistViewModel::class)
    abstract fun bindUserPlaylistViewModel(userPlaylistViewModel: UserPlaylistViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PlayQueueViewModel::class)
    abstract fun bindPlayQueueViewModel(playQueueViewModel: PlayQueueViewModel): ViewModel

}