package com.beat.view.content

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.beat.R
import com.beat.base.BasePlayerActivity
import com.beat.databinding.ActivityContentBinding
import com.beat.util.Constants
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.audioPlayer.MusicPlayerConnectorViewModel
import com.beat.view.audioPlayer.MusicPlayerFragment
import com.beat.view.audioPlayer.RadioPlayerFragment
import com.beat.view.content.details.AlbumDetailsFragment
import com.beat.view.content.details.ArtistDetailsFragment
import com.beat.view.content.details.PlaylistDetailsFragment
import com.beat.view.content.download.DownloadFragment
import com.beat.view.content.favourite.FavoriteFragment
import com.example.android.uamp.media.listener.event.PlayerInitial
import com.example.android.uamp.media.listener.event.RadioInitial
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

class ContentActivity : BasePlayerActivity(), SlidingUpPanelLayout.PanelSlideListener {

    private lateinit var binding: ActivityContentBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory
    private lateinit var musicPlayerConnectorViewModel: MusicPlayerConnectorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_content)
        musicPlayerConnectorViewModel =
            ViewModelProvider(this, providerFactory).get(MusicPlayerConnectorViewModel::class.java)
        initialComponent()
    }

    private fun initialComponent() {
        binding.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
        when {
            intent.getStringExtra("key")?.equals(Constants.ALBUM_DETAILS)!! -> {
                val releaseId = intent.getStringExtra("releaseId")
                moveToFragment(
                    AlbumDetailsFragment.newInstance(releaseId),
                    AlbumDetailsFragment().toString()
                )
            }
            intent.getStringExtra("key")?.equals(Constants.PLAYLIST_DETAILS)!! -> {
                val playlistId = intent.getStringExtra("playlistId")
                val addToPlaylist = intent.getBooleanExtra("addToPlaylist", false)
                val offline = intent.getBooleanExtra("offline", false)
                moveToFragment(
                    PlaylistDetailsFragment.newInstance(playlistId, addToPlaylist, offline),
                    PlaylistDetailsFragment().toString()
                )
            }
            intent.getStringExtra("key")?.equals(Constants.ARTIST_DETAILS)!! -> {
                val artistId = intent.getStringExtra("artistId")
                moveToFragment(
                    ArtistDetailsFragment.newInstance(artistId),
                    ArtistDetailsFragment().toString()
                )
            }
            intent.getStringExtra("key")?.equals(Constants.FAVORITES)!! -> {
                moveToFragment(FavoriteFragment(), FavoriteFragment().toString())
            }
            intent.getStringExtra("key")?.equals(Constants.DOWNLOADS)!! -> {
                moveToFragment(DownloadFragment(), DownloadFragment().toString())
            }
        }

        musicPlayerConnectorViewModel.getDragView().observe(this, Observer<View> {
            binding.slidingLayout.setDragView(it)
        })

        musicPlayerConnectorViewModel.getExpandViewClick().observe(this, Observer<View> {
            binding.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        })

        binding.slidingLayout.addPanelSlideListener(this)

        initialCurrentPlayer()
    }

    private fun initialCurrentPlayer() {
        initialPlayer(getPlayerInitialState())
        initialRadio(getRadioInitialState())
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

    override fun onPanelSlide(panel: View?, slideOffset: Float) {
        musicPlayerConnectorViewModel.slideOffset.value = slideOffset
    }

    override fun onPanelStateChanged(
        panel: View?,
        previousState: SlidingUpPanelLayout.PanelState?,
        newState: SlidingUpPanelLayout.PanelState?
    ) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onPlayerInitial(playerInitial: PlayerInitial) {
        initialPlayer(playerInitial.initial)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRadioInitial(radioInitial: RadioInitial) {
        initialRadio(radioInitial.initial)
    }

}