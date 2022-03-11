package com.beat.di.module.contentDetails

import com.beat.view.audioPlayer.MusicPlayerFragment
import com.beat.view.audioPlayer.RadioPlayerFragment
import com.beat.view.content.details.AlbumDetailsFragment
import com.beat.view.content.details.ArtistDetailsFragment
import com.beat.view.content.details.PlaylistDetailsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [ContentDetailsViewModelModule::class])
abstract class ContentDetailsFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeAlbumDetailsFragment(): AlbumDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributePlaylistDetailsFragment(): PlaylistDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeArtistDetailsFragment(): ArtistDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeMusicPlayerFragment(): MusicPlayerFragment

    @ContributesAndroidInjector
    abstract fun contributeRadioPlayerFragment(): RadioPlayerFragment

}