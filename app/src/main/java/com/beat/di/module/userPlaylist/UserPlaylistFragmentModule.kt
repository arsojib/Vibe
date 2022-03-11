package com.beat.di.module.userPlaylist

import com.beat.view.common.queue.PlayQueueFragment
import com.beat.view.common.userPlaylist.AddToPlaylistFragment
import com.beat.view.common.userPlaylist.EditPlaylistTrackFragment
import com.beat.view.common.userPlaylist.MyPlaylistFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [UserPlaylistViewModelModule::class])
abstract class UserPlaylistFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeMyPlaylistFragment(): MyPlaylistFragment

    @ContributesAndroidInjector
    abstract fun contributeAddToPlaylistFragment(): AddToPlaylistFragment

    @ContributesAndroidInjector
    abstract fun contributeEditPlaylistFragment(): EditPlaylistTrackFragment

    @ContributesAndroidInjector
    abstract fun contributePlayQueueFragment(): PlayQueueFragment

}