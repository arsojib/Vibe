package com.beat.view.audioPlayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.beat.R
import com.beat.base.BaseDaggerFragment
import com.beat.core.data.model.PlaylistDetailsWithTrackResponse
import com.beat.core.data.model.RadioListResponse
import com.beat.core.data.model.Resource
import com.beat.data.bindingAdapter.getDuration
import com.beat.data.model.Track
import com.beat.databinding.RadioPlayerFragmentBinding
import com.beat.util.listener.ClickListener
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.adapter.RadioPlayerAdapter
import com.beat.view.customView.ItemDecorator
import javax.inject.Inject

class RadioPlayerFragment : BaseDaggerFragment(), ClickListener {

    private lateinit var binding: RadioPlayerFragmentBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory
    private lateinit var musicPlayerConnectorViewModel: MusicPlayerConnectorViewModel
    private lateinit var radioPlayerViewModel: RadioPlayerViewModel
    private lateinit var radioPlayerAdapter: RadioPlayerAdapter

    private var radioImage: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.radio_player_fragment, container, false)
        musicPlayerConnectorViewModel =
            ViewModelProvider(this, providerFactory).get(MusicPlayerConnectorViewModel::class.java)
        radioPlayerViewModel =
            ViewModelProvider(this, providerFactory).get(RadioPlayerViewModel::class.java)
        binding.miniPlayer.lifecycleOwner = viewLifecycleOwner
        binding.mainRadioPlayer.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialComponent()
    }

    private fun initialComponent() {
        binding.mainRadioPlayer.recyclerView.addItemDecoration(ItemDecorator(-30))
        radioPlayerAdapter = RadioPlayerAdapter(mContext, this)
        binding.mainRadioPlayer.recyclerView.adapter = radioPlayerAdapter
        binding.miniPlayer.progress = radioPlayerViewModel.getMediaPosition()
        binding.mainRadioPlayer.progress = radioPlayerViewModel.getMediaPosition()
        musicPlayerConnectorViewModel.dragView.value = binding.mainRadioPlayer.topLayout

        binding.miniPlayer.progress = radioPlayerViewModel.getMediaPosition()
        binding.mainRadioPlayer.progress = radioPlayerViewModel.getMediaPosition()
        radioPlayerViewModel.fetchCurrentPlayingItem()

        radioPlayerViewModel.getRadioListResponse()
            .observe(viewLifecycleOwner, Observer<Resource<RadioListResponse>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            binding.mainRadioPlayer.progressBar.visibility = View.INVISIBLE
                            radioPlayerAdapter.addAll(
                                radioPlayerViewModel.getRadioListFromResponse(
                                    binding.mainRadioPlayer.data?.releaseId ?: "",
                                    it.data
                                )
                            )
                        }
                        Resource.Status.ERROR -> {
                            showToast(
                                resource.message!!,
                                Toast.LENGTH_LONG
                            )
                        }
                        Resource.Status.LOADING -> {

                        }
                    }
                }
            })

        radioPlayerViewModel.getPlaylistDetailsWithTrackResponse()
            .observe(viewLifecycleOwner, Observer<Resource<PlaylistDetailsWithTrackResponse>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            val list = radioPlayerViewModel.getTrackListFromPlaylistDetailsResponse(
                                resource.data!!
                            )
                            if (list.size != 0) {
                                resource.data?.playlist?.id?.let { it1 ->
                                    radioPlayerViewModel.playRadio(
                                        list, list[0],
                                        it1,
                                        resource.data!!.playlist.title,
                                        radioImage
                                    )
                                }
                            }
                        }
                        Resource.Status.ERROR -> {

                        }
                        Resource.Status.LOADING -> {

                        }
                    }
                }
            })

        radioPlayerViewModel.getNowPlayingMediaItem().observe(viewLifecycleOwner, Observer<Track> {
            it?.let { item ->
                binding.mainRadioPlayer.data = item
                binding.miniPlayer.data = item
            }
        })

        radioPlayerViewModel.getMediaButtonRes().observe(viewLifecycleOwner, Observer<Int> {
            it?.let { res ->
                binding.mainRadioPlayer.play.setImageResource(res)
                binding.miniPlayer.play.setImageResource(res)
            }
        })

        radioPlayerViewModel.getMediaPosition().observe(viewLifecycleOwner, Observer<Int> {
            binding.mainRadioPlayer.duration.text = getString(
                R.string.current_duration_format,
                getDuration(it / 1000), getDuration(binding.mainRadioPlayer.data?.duration ?: 0)
            )
        })

        musicPlayerConnectorViewModel.getSlideOffset().observe(viewLifecycleOwner, Observer<Float> {
            binding.miniPlayer.layout.alpha = 1f - it
            binding.mainRadioPlayer.layout.alpha = it

            if (it == 1f) {
                binding.miniPlayer.layout.visibility = View.GONE
                binding.mainRadioPlayer.topLayout.visibility = View.VISIBLE
            } else {
                binding.miniPlayer.layout.visibility = View.VISIBLE
                binding.mainRadioPlayer.topLayout.visibility = View.INVISIBLE
            }
        })

        binding.miniPlayer.layout.setOnClickListener {
            musicPlayerConnectorViewModel.expandViewClick.value = it
        }

        binding.miniPlayer.play.setOnClickListener {
            binding.mainRadioPlayer.data?.trackId?.let { it1 -> radioPlayerViewModel.onPlay(it1) }
        }

        binding.mainRadioPlayer.play.setOnClickListener {
            binding.mainRadioPlayer.data?.trackId?.let { it1 -> radioPlayerViewModel.onPlay(it1) }
        }

        radioPlayerViewModel.radioListRequest()

    }

    override fun onClick(position: Int) {
        val radio = radioPlayerAdapter.getRadioByPosition(position)
        radioImage = radio!!.bannerImage
        radioPlayerViewModel.getAllTrackOfPlaylist(
            radio.playlistId
        )
    }

}