package com.beat.di.module.favorite

import com.beat.view.content.favourite.FavoriteArtistFragment
import com.beat.view.content.favourite.FavoriteAudioFragment
import com.beat.view.content.favourite.FavoriteFragment
import com.beat.view.content.favourite.FavoriteVideoFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [FavoriteViewModelModule::class])
abstract class FavoriteFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeFavoriteFragment(): FavoriteFragment

    @ContributesAndroidInjector
    abstract fun contributeFavoriteAudioFragment(): FavoriteAudioFragment

    @ContributesAndroidInjector
    abstract fun contributeFavoriteVideoFragment(): FavoriteVideoFragment

    @ContributesAndroidInjector
    abstract fun contributeFavoriteArtistFragment(): FavoriteArtistFragment

}