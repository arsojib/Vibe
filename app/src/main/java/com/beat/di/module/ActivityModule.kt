package com.beat.di.module

import com.beat.di.module.authentication.AuthenticationFragmentBuildersModule
import com.beat.di.module.authentication.AuthenticationScope
import com.beat.di.module.authentication.AuthenticationViewModelModule
import com.beat.di.module.common.CommonFragmentModule
import com.beat.di.module.common.CommonViewModelModule
import com.beat.di.module.contentDetails.ContentDetailsFragmentModule
import com.beat.di.module.contentDetails.ContentScope
import com.beat.di.module.download.DownloadFragmentModule
import com.beat.di.module.favorite.FavoriteFragmentModule
import com.beat.di.module.main.MainFragmentModule
import com.beat.di.module.main.MainScope
import com.beat.di.module.main.MainViewModelModule
import com.beat.di.module.splash.SplashScope
import com.beat.di.module.splash.SplashViewModelModule
import com.beat.di.module.userPlaylist.UserPlaylistFragmentModule
import com.beat.di.module.videoPlayer.VideoPlayerViewModelModule
import com.beat.view.authentication.AuthenticationActivity
import com.beat.view.common.CommonActivity
import com.beat.view.content.ContentActivity
import com.beat.view.main.MainActivity
import com.beat.view.splash.SplashActivity
import com.beat.view.videoPlayer.FullScreenVideoPlayerActivity
import com.beat.view.videoPlayer.VideoPlayerActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {

    @SplashScope
    @ContributesAndroidInjector(modules = [SplashViewModelModule::class])
    abstract fun contributeSplashActivity(): SplashActivity

    @AuthenticationScope
    @ContributesAndroidInjector(modules = [AuthenticationViewModelModule::class, AuthenticationFragmentBuildersModule::class])
    abstract fun contributeAuthenticationActivity(): AuthenticationActivity

    @MainScope
    @ContributesAndroidInjector(modules = [MainViewModelModule::class, MainFragmentModule::class])
    abstract fun contributeMainActivity(): MainActivity

    @ContentScope
    @ContributesAndroidInjector(modules = [ContentDetailsFragmentModule::class, FavoriteFragmentModule::class, DownloadFragmentModule::class])
    abstract fun contributeContentActivity(): ContentActivity

    @ContributesAndroidInjector(modules = [VideoPlayerViewModelModule::class])
    abstract fun contributeVideoPlayerActivity(): VideoPlayerActivity

    @ContributesAndroidInjector
    abstract fun contributeFullScreenVideoPlayerActivity(): FullScreenVideoPlayerActivity

    @ContributesAndroidInjector(modules = [CommonFragmentModule::class, CommonViewModelModule::class, UserPlaylistFragmentModule::class])
    abstract fun contributeCommonActivity(): CommonActivity

}