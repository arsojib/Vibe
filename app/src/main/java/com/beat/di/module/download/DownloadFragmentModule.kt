package com.beat.di.module.download

import com.beat.view.content.download.DownloadedAlbumFragment
import com.beat.view.content.download.DownloadFragment
import com.beat.view.content.download.DownloadedPlaylistFragment
import com.beat.view.content.download.DownloadedTracksFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [DownloadViewModelModule::class])
abstract class DownloadFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeDownloadFragment(): DownloadFragment

    @ContributesAndroidInjector
    abstract fun contributeTracksFragment(): DownloadedTracksFragment

    @ContributesAndroidInjector
    abstract fun contributeAlbumFragment(): DownloadedAlbumFragment

    @ContributesAndroidInjector
    abstract fun contributePlaylistFragment(): DownloadedPlaylistFragment

}