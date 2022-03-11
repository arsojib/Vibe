package com.beat.di.module.favorite

import androidx.lifecycle.ViewModel
import com.beat.di.scope.ViewModelKey
import com.beat.view.content.favourite.FavoriteViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class FavoriteViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(FavoriteViewModel::class)
    abstract fun bindFavoriteViewModel(favoriteViewModel: FavoriteViewModel): ViewModel

}