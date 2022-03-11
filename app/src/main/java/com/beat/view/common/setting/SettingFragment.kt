package com.beat.view.common.setting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.beat.R
import com.beat.base.BaseDaggerFragment
import com.beat.core.data.model.Resource
import com.beat.core.data.model.RewardPointResponse
import com.beat.databinding.SettingFragmentLayoutBinding
import com.beat.util.listener.AudioSettingMenuListener
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.authentication.AuthenticationActivity
import com.beat.view.dialog.AudioSettingMenuDialog
import javax.inject.Inject

class SettingFragment : BaseDaggerFragment(), AudioSettingMenuListener {

    private lateinit var binding: SettingFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory
    private lateinit var settingViewModel: SettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.setting_fragment_layout, container, false)
        settingViewModel =
            ViewModelProvider(this, providerFactory).get(SettingViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialComponent()
    }

    private fun initialComponent() {
        binding.header.title.text = getString(R.string.settings)

        settingViewModel.getSettings { playOnOffline: Boolean, streamOnlyOnWifi: Boolean,
                                       downloadOnlyOnWifi: Boolean, streamQuality: String, version: String ->
            binding.cbPlayOnOffline.isChecked = playOnOffline
            binding.cbStreamOnlyOnWifi.isChecked = streamOnlyOnWifi
            binding.cbDownloadOnlyOnWifi.isChecked = downloadOnlyOnWifi
            binding.quality.text = streamQuality
            binding.version.text = version
        }

        settingViewModel.getRewardPointResponse()
            .observe(viewLifecycleOwner, Observer<Resource<RewardPointResponse>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            resource.data?.balances?.get(0).let { point ->
                                point?.let {
                                    binding.rewardPoint.text = point.value.toString()
                                }
                            }
                        }
                        Resource.Status.ERROR -> {
                            TODO()
                        }
                        Resource.Status.LOADING -> {
                        }
                    }
                }
            })

        settingViewModel.getRewardPoint()

        binding.cbPlayOnOffline.setOnCheckedChangeListener { buttonView, isChecked ->
            settingViewModel.setPlayOnOffline(isChecked)
            binding.streamOnlyOnWifi.visibility = if (isChecked) View.GONE else View.VISIBLE
            binding.downloadOnlyOnWifi.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        binding.cbStreamOnlyOnWifi.setOnCheckedChangeListener { buttonView, isChecked ->
            settingViewModel.setStreamOnlyOnOffline(isChecked)
        }

        binding.cbDownloadOnlyOnWifi.setOnCheckedChangeListener { buttonView, isChecked ->
            settingViewModel.setDownloadOnlyOnOffline(isChecked)
        }

        binding.playOnOffline.setOnClickListener {
            binding.cbPlayOnOffline.isChecked = !binding.cbPlayOnOffline.isChecked
        }

        binding.streamOnlyOnWifi.setOnClickListener {
            binding.cbStreamOnlyOnWifi.isChecked = !binding.cbPlayOnOffline.isChecked
        }

        binding.downloadOnlyOnWifi.setOnClickListener {
            binding.cbDownloadOnlyOnWifi.isChecked = !binding.cbPlayOnOffline.isChecked
        }

        binding.streamingSetting.setOnClickListener {
            AudioSettingMenuDialog(mContext, settingViewModel.getAudioQuality(), this)
        }

        binding.deleteOfflineTracks.setOnClickListener {
//            settingViewModel.deleteOfflineTracks()
        }

        binding.signOutLayout.setOnClickListener {
            settingViewModel.signOut()
            val i = Intent(mContext, AuthenticationActivity::class.java)
            // set the new task and clear flags
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        }

        binding.header.back.setOnClickListener {
            onNavBackClick()
        }
    }

    override fun onQualityChange(quality: String) {
        settingViewModel.setAudioQuality(quality)
        binding.quality.text = settingViewModel.getQualityBySetting(quality)
    }

}