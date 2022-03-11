package com.beat.view.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.beat.R
import com.beat.base.BaseDaggerFragment
import com.beat.core.data.model.Resource
import com.beat.core.data.model.UserResponse
import com.beat.data.bindingAdapter.getDate
import com.beat.data.model.SubscriptionPlan
import com.beat.databinding.ProfileFragmentLayoutBinding
import com.beat.util.Constants
import com.beat.util.event.SubscriptionChange
import com.beat.util.moveToSubscription
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.activity.SupportActivity
import com.beat.view.common.CommonActivity
import com.beat.view.content.ContentActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

class ProfileFragment : BaseDaggerFragment() {

    private lateinit var binding: ProfileFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private lateinit var mainViewModel: MainViewModel

    companion object {
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.profile_fragment_layout, container, false)
        mainViewModel = ViewModelProvider(this, providerFactory).get(MainViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialComponent()
    }

    private fun initialComponent() {
//        binding.recyclerView.isNestedScrollingEnabled = false
//        binding.recyclerView.adapter = TrackAdapter(mContext, ArrayList())

        mainViewModel.getUserResponse()
            .observe(viewLifecycleOwner, Observer<Resource<UserResponse>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            val user = mainViewModel.getUserFromResponse(resource.data!!)
                            if (user != null) {
                                binding.user = user
                            }
                        }
                        Resource.Status.ERROR -> {

                        }
                        Resource.Status.LOADING -> {

                        }
                    }
                }
            })

        mainViewModel.getCurrentSubscriptionResponse()
            .observe(viewLifecycleOwner, Observer<Resource<SubscriptionPlan>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            binding.plan.text = resource.data!!.name
                            binding.validTill.text = getString(
                                R.string.valid_till,
                                getDate(resource.data!!.nextBillingDate)
                            )
                            binding.subscribeLayout.setBackgroundResource(R.drawable.rectangle_corner_round_fiveteen)
                            binding.userType.text = getString(R.string.Subscribed_user)
                        }
                        Resource.Status.ERROR -> {
                            binding.plan.text = getString(R.string.subscribe_a_plan)
                            binding.validTill.text =
                                getString(R.string.enjoy_millions_of_songs_amp_videos)
                            binding.subscribeLayout.setBackgroundResource(R.drawable.rectangle_corner_round_four)
                            binding.userType.text = getString(R.string.free_user)
                        }
                        Resource.Status.LOADING -> {

                        }
                    }
                }
            })

        mainViewModel.getUserRequest()
        mainViewModel.currentSubscription()

        binding.downloadsLayout.setOnClickListener {
            val intent = Intent(mContext, ContentActivity::class.java)
            intent.putExtra("key", Constants.DOWNLOADS)
            startActivity(intent)
        }

        binding.myMusicLayout.setOnClickListener {
            val intent = Intent(mContext, ContentActivity::class.java)
            intent.putExtra("key", Constants.FAVORITES)
            startActivity(intent)
        }

        binding.settingsLayout.setOnClickListener {
            val intent = Intent(mContext, CommonActivity::class.java)
            intent.putExtra("key", Constants.SETTING)
            startActivity(intent)
        }

        binding.referFriendLayout.setOnClickListener {
            val intent = Intent(mContext, SupportActivity::class.java)
            intent.putExtra("key", getString(R.string.refer_a_friend))
            startActivity(intent)
        }

        binding.myPlaylistLayout.setOnClickListener {
            val intent = Intent(mContext, CommonActivity::class.java)
            intent.putExtra("key", Constants.USER_PLAYLIST)
            startActivity(intent)
        }

        binding.subscribeLayout.setOnClickListener {
            moveToSubscription(mContext)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSubscriptionChange(subscriptionChange: SubscriptionChange) {
        lifecycleScope.launchWhenResumed {
            mainViewModel.currentSubscription()
        }
    }

}