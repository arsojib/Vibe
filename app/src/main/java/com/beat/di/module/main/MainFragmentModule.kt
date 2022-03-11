package com.beat.di.module.main

import com.beat.view.audioPlayer.MusicPlayerFragment
import com.beat.view.audioPlayer.RadioPlayerFragment
import com.beat.view.main.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeHomeFragment(): HomeFragment

    @ContributesAndroidInjector
    abstract fun contributeVideoFragment(): VideoFragment

    @ContributesAndroidInjector
    abstract fun contributeRadioFragment(): RadioFragment

    @ContributesAndroidInjector
    abstract fun contributeSearchFragment(): SearchFragment

    @ContributesAndroidInjector
    abstract fun contributeProfileFragment(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeMusicPlayerFragment(): MusicPlayerFragment

    @ContributesAndroidInjector
    abstract fun contributeRadioPlayerFragment(): RadioPlayerFragment

}