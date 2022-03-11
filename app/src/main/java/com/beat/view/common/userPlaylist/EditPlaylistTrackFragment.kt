package com.beat.view.common.userPlaylist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.beat.R
import com.beat.base.BaseDaggerFragment
import com.beat.core.data.model.PlaylistDetailsWithTrackResponse
import com.beat.core.data.model.Resource
import com.beat.data.model.Alert
import com.beat.databinding.EditPlaylistFragmentLayoutBinding
import com.beat.util.listener.PlaylistTrackEditListener
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.adapter.EditPlaylistTrackAdapter
import javax.inject.Inject

class EditPlaylistTrackFragment : BaseDaggerFragment(), PlaylistTrackEditListener {

    private lateinit var binding: EditPlaylistFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private lateinit var userPlaylistViewModel: UserPlaylistViewModel
    private lateinit var editPlaylistTrackAdapter: EditPlaylistTrackAdapter

    private lateinit var playlistId: String
    private lateinit var playlistTitle: String

    companion object {
        fun newInstance(
            playlistId: String?,
            playlistTitle: String?
        ): EditPlaylistTrackFragment {
            val fragment =
                EditPlaylistTrackFragment()
            val bundle = Bundle()
            bundle.putString("playlistId", playlistId)
            bundle.putString("playlistTitle", playlistTitle)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.edit_playlist_fragment_layout,
                container,
                false
            )
        userPlaylistViewModel =
            ViewModelProvider(this, providerFactory).get(UserPlaylistViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playlistId = arguments?.getString("playlistId", "")!!
        playlistTitle = arguments?.getString("playlistTitle", "")!!

        initialComponent()
    }

    private fun initialComponent() {
        binding.header.title.text = playlistTitle
        editPlaylistTrackAdapter = EditPlaylistTrackAdapter(mContext, playlistTitle, this)
        binding.recyclerView.adapter = editPlaylistTrackAdapter

        userPlaylistViewModel.getPlaylistDetailsWithTrackResponse()
            .observe(viewLifecycleOwner, Observer<Resource<PlaylistDetailsWithTrackResponse>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            binding.progressBar.visibility = View.GONE
                            binding.alertLayout.layout.visibility = View.GONE
                            editPlaylistTrackAdapter.addAll(
                                userPlaylistViewModel.getTrackListFromPlaylistDetailsResponse(
                                    resource.data!!
                                )
                            )
                        }
                        Resource.Status.ERROR -> {
                            binding.progressBar.visibility = View.GONE
                            binding.alertLayout.layout.visibility = View.VISIBLE
                            binding.alertLayout.data =
                                Alert(
                                    getString(R.string.no_playlist),
                                    getString(R.string.no_favorite_demo_text),
                                    resources.getDrawable(R.drawable.ic_ph_playlist, null)
                                )
                        }
                        Resource.Status.LOADING -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.alertLayout.layout.visibility = View.GONE
                        }
                    }
                }
            })

        userPlaylistViewModel.getAllTrackOfPlaylist(playlistId)

        binding.header.back.setOnClickListener {
            onNavBackClick()
        }
    }

    override fun onPlaylistTrackRemove() {
        userPlaylistViewModel.addTrackToPlaylist(
            playlistId,
            playlistTitle,
            editPlaylistTrackAdapter.getTrackIds()
        )
    }

}