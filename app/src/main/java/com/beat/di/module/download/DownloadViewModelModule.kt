package com.beat.di.module.download

import androidx.lifecycle.ViewModel
import com.beat.di.scope.ViewModelKey
import com.beat.view.content.download.DownloadViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class DownloadViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(DownloadViewModel::class)
    abstract fun bindDownloadViewModel(downloadViewModel: DownloadViewModel): ViewModel

}