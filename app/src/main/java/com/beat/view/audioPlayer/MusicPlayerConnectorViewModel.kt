package com.beat.view.audioPlayer

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class MusicPlayerConnectorViewModel @Inject constructor() : ViewModel() {

    val slideOffset = MutableLiveData<Float>()
    val dragView = MutableLiveData<View>()
    val expandViewClick = MutableLiveData<View>()

    fun getSlideOffset(): LiveData<Float> {
        return slideOffset
    }

    fun getDragView(): LiveData<View> {
        return dragView
    }

    fun getExpandViewClick(): LiveData<View> {
        return expandViewClick
    }

}