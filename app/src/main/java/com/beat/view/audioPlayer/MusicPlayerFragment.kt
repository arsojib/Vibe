package com.beat.view.audioPlayer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.beat.R
import com.beat.base.BaseDaggerFragment
import com.beat.core.utils.CoreConstants
import com.beat.data.bindingAdapter.getDuration
import com.beat.data.model.Track
import com.beat.databinding.MusicPlayerFragmentBinding
import com.beat.util.*
import com.beat.util.listener.AudioSettingMenuListener
import com.beat.util.listener.TrackPopUpMenuClickListener
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.adapter.MusicPlayerAdapter
import com.beat.view.common.CommonActivity
import com.beat.view.content.ContentActivity
import com.beat.view.dialog.AudioSettingMenuDialog
import com.beat.view.dialog.TrackMenuDialog
import com.example.android.uamp.media.extensions.currentPlayBackPosition
import javax.inject.Inject

class MusicPlayerFragment : BaseDaggerFragment(), AudioSettingMenuListener {

    private lateinit var binding: MusicPlayerFragmentBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private lateinit var musicPlayerViewModel: MusicPlayerViewModel
    private lateinit var musicPlayerConnectorViewModel: MusicPlayerConnectorViewModel
    private lateinit var musicPlayerAdapter: MusicPlayerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.music_player_fragment, container, false)
        musicPlayerViewModel =
            ViewModelProvider(this, providerFactory).get(MusicPlayerViewModel::class.java)
        musicPlayerConnectorViewModel =
            ViewModelProvider(this, providerFactory).get(MusicPlayerConnectorViewModel::class.java)
        binding.miniPlayer.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialComponent()
    }

    private fun initialComponent() {
        musicPlayerAdapter = MusicPlayerAdapter(this, musicPlayerViewModel)
        binding.mainMusicPlayer.viewPager.clipToPadding = false
        binding.mainMusicPlayer.viewPager.setPadding(100, 0, 100, 0)
        binding.mainMusicPlayer.viewPager.pageMargin = 40
        binding.mainMusicPlayer.viewPager.adapter = musicPlayerAdapter
        binding.mainMusicPlayer.viewPager.addOnPageChangeListener(musicPlayerAdapter.setPageChangeListener())
        musicPlayerConnectorViewModel.dragView.value = binding.mainMusicPlayer.topLayout

        binding.miniPlayer.progress = musicPlayerViewModel.getMediaPosition()

        musicPlayerViewModel.getMediaItem().observe(viewLifecycleOwner, Observer<List<Track>> {
            it?.let { list ->
                musicPlayerAdapter.addAll(list)
                musicPlayerViewModel.fetchCurrentPlayingItem()
            }
        })

        musicPlayerViewModel.getNowPlayingMediaItem().observe(viewLifecycleOwner, Observer<Track> {
            it?.let { item ->
                binding.mainMusicPlayer.data?.trackId?.let { preTrackId ->
                    musicPlayerViewModel.setDownloadObserver(
                        -1,
                        preTrackId,
                        item.trackId,
                        viewLifecycleOwner
                    )
                }
                binding.mainMusicPlayer.data = item
                binding.miniPlayer.data = item
                val currentPosition =
                    musicPlayerViewModel.getPlaybackState().currentPlayBackPosition.toInt()
                binding.mainMusicPlayer.viewPager.currentItem =
                    musicPlayerAdapter.getPositionById(item.trackId, currentPosition)
            }
        })

        musicPlayerViewModel.getMediaPosition().observe(viewLifecycleOwner, Observer<Int> {
            binding.mainMusicPlayer.duration.text = getString(
                R.string.current_duration_format,
                getDuration(it / 1000), getDuration(binding.mainMusicPlayer.data?.duration ?: 0)
            )
        })

        musicPlayerViewModel.getMediaButtonRes().observe(viewLifecycleOwner, Observer<Int> {
            it?.let { res ->
                binding.mainMusicPlayer.play.setImageResource(res)
                binding.miniPlayer.play.setImageResource(res)
            }
        })

        musicPlayerViewModel.getRepeatModeRes().observe(viewLifecycleOwner, Observer<Int> {
            it?.let { res ->
                binding.mainMusicPlayer.repeat.setImageResource(res)
            }
        })

        musicPlayerViewModel.getsShuffleModeTint().observe(viewLifecycleOwner, Observer<Int> {
            it?.let { res ->
                binding.mainMusicPlayer.shuffle.setColorFilter(
                    ContextCompat.getColor(
                        mContext,
                        res
                    )
                )
            }
        })

        musicPlayerViewModel.getRepeatModeTint().observe(viewLifecycleOwner, Observer<Int> {
            it?.let { res ->
                binding.mainMusicPlayer.repeat.setColorFilter(
                    ContextCompat.getColor(
                        mContext,
                        res
                    )
                )
            }
        })

        musicPlayerViewModel.getDownloadProgress().observe(viewLifecycleOwner, Observer<Int> {
            updateDownloadingProgress(it)
        })

        musicPlayerViewModel.getNewDownloadAddObserver(Observer {
            it?.let {
                binding.mainMusicPlayer.data?.trackId.let { currentTrackId ->
                    if (it == currentTrackId) musicPlayerViewModel.setDownloadObserver(
                        0,
                        "",
                        it,
                        viewLifecycleOwner
                    )
                }
            }
        }, viewLifecycleOwner)

        musicPlayerConnectorViewModel.getSlideOffset().observe(viewLifecycleOwner, Observer<Float> {
            binding.miniPlayer.layout.alpha = 1f - it
            binding.mainMusicPlayer.layout.alpha = it

            if (it == 1f) {
                binding.miniPlayer.layout.visibility = View.GONE
                binding.mainMusicPlayer.topLayout.visibility = View.VISIBLE
            } else {
                binding.miniPlayer.layout.visibility = View.VISIBLE
                binding.mainMusicPlayer.topLayout.visibility = View.INVISIBLE
            }
        })

        binding.miniPlayer.songProgress.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicPlayerViewModel.onSeek(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        musicPlayerViewModel.fetchMediaList()

        binding.miniPlayer.layout.setOnClickListener {
            musicPlayerConnectorViewModel.expandViewClick.value = it
        }

        binding.mainMusicPlayer.menu.setOnClickListener {
            onTrackMenuClick(musicPlayerAdapter.getItemByIndex(binding.mainMusicPlayer.viewPager.currentItem))
        }

        binding.mainMusicPlayer.shuffle.setOnClickListener {
            musicPlayerViewModel.onShuffle()
        }

        binding.mainMusicPlayer.previous.setOnClickListener {
            musicPlayerViewModel.onPrevious()
        }

        binding.miniPlayer.previous.setOnClickListener {
            musicPlayerViewModel.onPrevious()
        }

        binding.mainMusicPlayer.play.setOnClickListener {
            musicPlayerViewModel.onPlay(musicPlayerAdapter.getItemByIndex(binding.mainMusicPlayer.viewPager.currentItem).trackId)
        }

        binding.miniPlayer.play.setOnClickListener {
            musicPlayerViewModel.onPlay(musicPlayerAdapter.getItemByIndex(binding.mainMusicPlayer.viewPager.currentItem).trackId)
        }

        binding.mainMusicPlayer.next.setOnClickListener {
            musicPlayerViewModel.onNext()
        }

        binding.miniPlayer.next.setOnClickListener {
            musicPlayerViewModel.onNext()
        }

        binding.mainMusicPlayer.repeat.setOnClickListener {
            musicPlayerViewModel.onRepeat()
        }

        binding.mainMusicPlayer.audioSetting.setOnClickListener {
            AudioSettingMenuDialog(mContext, musicPlayerViewModel.getAudioQuality(), this)
        }

        binding.mainMusicPlayer.favorite.setOnClickListener {
            val track =
                musicPlayerAdapter.getItemByIndex(binding.mainMusicPlayer.viewPager.currentItem)
            track.favorite = !track.favorite
            musicPlayerViewModel.patchFavorite(
                track.trackId,
                CoreConstants.FAVORITE_TRACK,
                track.favorite,
                track
            )
            musicPlayerAdapter.setFavorite(track.trackId, track.favorite)
            binding.mainMusicPlayer.data = track
            binding.mainMusicPlayer.invalidateAll()
        }

        binding.mainMusicPlayer.shareOnSocial.setOnClickListener {
            val track =
                musicPlayerAdapter.getItemByIndex(binding.mainMusicPlayer.viewPager.currentItem)
            shareOnSocial(mContext, track.url)
        }

        binding.mainMusicPlayer.download.setOnClickListener {
            val track =
                musicPlayerAdapter.getItemByIndex(binding.mainMusicPlayer.viewPager.currentItem)
            musicPlayerViewModel.downloadTrack(track)
            updateDownloadingProgress(0)
        }

        binding.mainMusicPlayer.playQueue.setOnClickListener {
            val track =
                musicPlayerAdapter.getItemByIndex(binding.mainMusicPlayer.viewPager.currentItem)
            val intent = Intent(mContext, CommonActivity::class.java)
            intent.putExtra("key", Constants.PLAY_QUEUE)
            intent.putExtra("track", track)
            startActivity(intent)
        }

    }

    private fun updateDownloadingProgress(it: Int) {
        binding.mainMusicPlayer.progressBar.progress = it
        binding.mainMusicPlayer.download.visibility =
            if (it < 0) View.VISIBLE else View.INVISIBLE
        binding.mainMusicPlayer.progressBar.visibility =
            if (it in 0..99) View.VISIBLE else View.INVISIBLE
        binding.mainMusicPlayer.downloaded.visibility =
            if (it < 100) View.INVISIBLE else View.VISIBLE
    }

    private fun onTrackMenuClick(track: Track) {
        TrackMenuDialog(mContext, track, object : TrackPopUpMenuClickListener {

            override fun onFavorite(id: String, type: String, isFav: Boolean) {
                track.favorite = isFav
                musicPlayerViewModel.patchFavorite(
                    id,
                    type,
                    isFav,
                    track
                )
                musicPlayerAdapter.setFavorite(id, isFav)
                binding.mainMusicPlayer.data = track
                binding.mainMusicPlayer.invalidateAll()
            }

            override fun onTrackAddToPlaylist(trackId: String) {
                moveToAddToPlaylistFragment(mContext, trackId)
            }

            override fun onGoToArtist(artistId: String, artistName: String) {
                moveToArtistFragment(mContext, artistId)
            }

            override fun onGotoAlbum(releaseId: String, artistId: String, artistName: String) {
                moveToReleaseFragment(mContext, releaseId)
            }

            override fun onRemoveFromDownload(trackId: String) {

            }

            override fun onShareOnSocialMedia(url: String) {
                shareOnSocial(mContext, url)
            }

        }, false)
    }

    override fun onQualityChange(quality: String) {
        musicPlayerViewModel.setAudioQuality(quality)
    }

}