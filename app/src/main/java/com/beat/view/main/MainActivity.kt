package com.beat.view.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.beat.R
import com.beat.base.BasePlayerActivity
import com.beat.core.utils.VideoInitial
import com.beat.databinding.ActivityMainBinding
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.activity.SupportActivity
import com.beat.view.adapter.ViewPagerAdapter
import com.beat.view.audioPlayer.MusicPlayerConnectorViewModel
import com.beat.view.audioPlayer.MusicPlayerFragment
import com.beat.view.audioPlayer.RadioPlayerFragment
import com.example.android.uamp.media.listener.event.PlayerInitial
import com.example.android.uamp.media.listener.event.RadioInitial
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

class MainActivity : BasePlayerActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    SlidingUpPanelLayout.PanelSlideListener {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private lateinit var mainViewModel: MainViewModel
    private lateinit var musicPlayerConnectorViewModel: MusicPlayerConnectorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainViewModel = ViewModelProvider(this, providerFactory).get(MainViewModel::class.java)
        musicPlayerConnectorViewModel =
            ViewModelProvider(this, providerFactory).get(MusicPlayerConnectorViewModel::class.java)
        mainViewModel.videoTabFullScreen.value = false
        initialComponent()
    }

    private fun initialComponent() {
        binding.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)
        binding.viewPager.swipeLocked = true
        setupViewPager(binding.viewPager)

        mainViewModel.onVideoTabFullScreen().observe(this, Observer<Boolean> {
            it?.let { videoTabFullScreen ->
                if (videoTabFullScreen) {
                    binding.topLayout.visibility = View.GONE
//                    binding.miniPlayer.visibility = View.GONE
                } else {
                    binding.topLayout.visibility = View.VISIBLE
//                    binding.miniPlayer.visibility = View.VISIBLE
                }
            }
        })

        musicPlayerConnectorViewModel.getDragView().observe(this, Observer<View> {
            binding.slidingLayout.setDragView(it)
        })

        musicPlayerConnectorViewModel.getExpandViewClick().observe(this, Observer<View> {
            binding.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        })

        binding.slidingLayout.addPanelSlideListener(this)

        initialCurrentPlayer()
        handleDeepLink()

        binding.notification.setOnClickListener {
            val intent = Intent(this, SupportActivity::class.java)
            intent.putExtra("key", getString(R.string.notifications))
            startActivity(intent)
        }

    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(HomeFragment())
        adapter.addFragment(VideoFragment())
        adapter.addFragment(RadioFragment())
        adapter.addFragment(SearchFragment())
        adapter.addFragment(ProfileFragment())
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 5
    }

    private fun initialCurrentPlayer() {
        initialPlayer(getPlayerInitialState())
        initialRadio(getRadioInitialState())
    }

    private fun handleDeepLink() {
        if (intent != null) {
            val appLinkAction: String? = intent.action
            val appLinkData: Uri? = intent.data
            mainViewModel.navigateToDeepLink(this, appLinkAction, appLinkData)
        }
    }

    private fun initialPlayer(initial: Boolean) {
        if (initial) {
            setPlayerInitialState(initial)
            setRadioInitialState(!initial)
            binding.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            supportFragmentManager.beginTransaction()
                .replace(R.id.playerContainer, MusicPlayerFragment(), MusicPlayerFragment().tag)
                .commit()
        }
    }

    private fun initialRadio(initial: Boolean) {
        if (initial) {
            setRadioInitialState(initial)
            setPlayerInitialState(!initial)
            binding.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            supportFragmentManager.beginTransaction()
                .replace(R.id.playerContainer, RadioPlayerFragment(), RadioPlayerFragment().tag)
                .commit()
        }
    }

    private fun removeAudioPlayer() {
        setRadioInitialState(false)
        setPlayerInitialState(false)
        mainViewModel.removeAudioPlayer()
        binding.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
        val currentFragment = supportFragmentManager.findFragmentById(R.id.playerContainer)
        if (currentFragment != null) supportFragmentManager.beginTransaction()
            .remove(currentFragment).commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_home -> {
                binding.viewPager.currentItem = 0
                mainViewModel.videoTabChange.value = true
            }
            R.id.navigation_video -> {
                binding.viewPager.currentItem = 1
                mainViewModel.getVideoPageContent()
                mainViewModel.videoTabChange.value = false
            }
            R.id.navigation_radio -> {
                binding.viewPager.currentItem = 2
                mainViewModel.radioListRequest()
                mainViewModel.videoTabChange.value = true
            }
            R.id.navigation_search -> {
                binding.viewPager.currentItem = 3
                mainViewModel.videoTabChange.value = true
            }
            R.id.navigation_profile -> {
                binding.viewPager.currentItem = 4
                mainViewModel.videoTabChange.value = true
            }
        }
        return true
    }

    override fun onPause() {
        super.onPause()
        mainViewModel.videoTabChange.value = true
    }

    override fun onBackPressed() {
        if (mainViewModel.videoTabFullScreen.value!!) {
            mainViewModel.videoTabFullScreen.value = false
        } else {
            super.onBackPressed()
        }
    }

    override fun onPanelSlide(panel: View?, slideOffset: Float) {
        musicPlayerConnectorViewModel.slideOffset.value = slideOffset
    }

    override fun onPanelStateChanged(
        panel: View?,
        previousState: SlidingUpPanelLayout.PanelState?,
        newState: SlidingUpPanelLayout.PanelState?
    ) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPlayerInitial(playerInitial: PlayerInitial) {
        lifecycleScope.launchWhenResumed {
            initialPlayer(playerInitial.initial)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRadioInitial(radioInitial: RadioInitial) {
        lifecycleScope.launchWhenResumed {
            initialRadio(radioInitial.initial)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVideoInitial(videoInitial: VideoInitial) {
        lifecycleScope.launchWhenResumed {
            removeAudioPlayer()
        }
    }

}
