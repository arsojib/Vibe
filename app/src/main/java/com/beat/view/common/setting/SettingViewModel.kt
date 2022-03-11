package com.beat.view.common.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beat.BuildConfig
import com.beat.core.data.model.Resource
import com.beat.core.data.model.RewardPointResponse
import com.beat.core.data.rest.Repository
import com.beat.core.data.storage.OfflineDownloadConnection
import com.beat.core.data.storage.PreferenceManager
import com.beat.util.Constants
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingViewModel @Inject constructor(
    private val repository: Repository,
    private val preferenceManager: PreferenceManager,
    private val offlineDownloadConnection: OfflineDownloadConnection
) : ViewModel() {

    private val rewardPointResponse = MutableLiveData<Resource<RewardPointResponse>>()

    fun getSettings(callback: (Boolean, Boolean, Boolean, String, String) -> Unit) {
        val playOnOffline = preferenceManager.getPlayOnOffline()
        val streamOnlyOnOffline = preferenceManager.getStreamOnlyOnOffline()
        val downloadOnlyOnOffline = preferenceManager.getDownloadOnlyOnOffline()
        val streamQuality = getQualityBySetting(preferenceManager.getAudioQuality())
        val version = BuildConfig.VERSION_NAME
        callback(playOnOffline, streamOnlyOnOffline, downloadOnlyOnOffline, streamQuality, version)
    }

    fun getAudioQuality(): String {
        val quality = preferenceManager.getAudioQuality()
        return if (quality.isEmpty()) Constants.NORMAL else quality
    }

    fun getRewardPoint() {
        viewModelScope.launch {
            rewardPointResponse.value = repository.getRewardPoint()
        }
    }

    fun setPlayOnOffline(active: Boolean) {
        preferenceManager.setPlayOnOffline(active)
    }

    fun setStreamOnlyOnOffline(active: Boolean) {
        preferenceManager.setStreamOnlyOnOffline(active)
    }

    fun setDownloadOnlyOnOffline(active: Boolean) {
        preferenceManager.setDownloadOnlyOnOffline(active)
    }

    fun setAudioQuality(quality: String) {
//        serviceConnection.setAudioFormat(quality)
        preferenceManager.setAudioQuality(quality)
    }

    fun deleteOfflineTracks() {
        offlineDownloadConnection.removeAll()
    }

    fun signOut() {
        preferenceManager.logOut()
    }

    fun getQualityBySetting(setting: String): String {
        return when (setting) {
            Constants.HIGH -> {
                Constants.HIGH_BIT
            }
            Constants.NORMAL -> {
                Constants.NORMAL_BIT
            }
            Constants.LOW -> {
                Constants.LOW_BIT
            }
            else -> {
                Constants.NORMAL_BIT
            }
        }
    }

    fun getRewardPointResponse(): LiveData<Resource<RewardPointResponse>> = rewardPointResponse

}