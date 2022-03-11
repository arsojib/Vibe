package com.beat.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.beat.R
import com.beat.base.BaseDaggerFragment
import com.beat.core.data.model.PlaylistDetailsWithTrackResponse
import com.beat.core.data.model.RadioListResponse
import com.beat.core.data.model.Resource
import com.beat.databinding.RadioFragmentLayoutBinding
import com.beat.util.listener.ClickListener
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.adapter.HomeRadioAdapter
import com.beat.view.customView.ItemDecorator
import com.beat.view.fragment.RadioPlayerFragment
import javax.inject.Inject

class RadioFragment : BaseDaggerFragment(), ClickListener {

    private lateinit var binding: RadioFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private lateinit var mainViewModel: MainViewModel
    private lateinit var homeRadioAdapter: HomeRadioAdapter
    private var radioImage: String = ""

    companion object {
        fun newInstance(): RadioFragment {
            return RadioFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.radio_fragment_layout, container, false)
        mainViewModel = ViewModelProvider(this, providerFactory).get(MainViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialComponent()
    }

    private fun initialComponent() {
        binding.recyclerView.addItemDecoration(ItemDecorator(-30))
        homeRadioAdapter = HomeRadioAdapter(mContext, this)
        binding.recyclerView.adapter = homeRadioAdapter

        mainViewModel.getRadioListResponse()
            .observe(viewLifecycleOwner, Observer<Resource<RadioListResponse>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            binding.progressBar.visibility = View.INVISIBLE
                            homeRadioAdapter.addAll(mainViewModel.getRadioListFromResponse(it.data))
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

        mainViewModel.getPlaylistDetailsWithTrackResponse()
            .observe(viewLifecycleOwner, Observer<Resource<PlaylistDetailsWithTrackResponse>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            val list = mainViewModel.getTrackListFromPlaylistDetailsResponse(
                                resource.data!!
                            )
                            if (list.size != 0) {
                                resource.data?.playlist?.id?.let { it1 ->
                                    mainViewModel.playRadio(
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

    }

    override fun onClick(position: Int) {
        val radio = homeRadioAdapter.getRadioByPosition(position)
        radioImage = radio!!.bannerImage
        mainViewModel.getAllTrackOfPlaylist(
            radio.playlistId
        )
    }

}