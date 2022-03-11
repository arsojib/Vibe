package com.beat.view.videoPlayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beat.core.data.rest.Repository
import kotlinx.coroutines.launch
import javax.inject.Inject

class VideoPlayerViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    fun patchFavorite(id: String, type: String, favorite: Boolean) {
        viewModelScope.launch {
            repository.patchFavorite(id, type, favorite)
        }
    }

}