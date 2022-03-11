package com.beat.view.content.userPlaylist

import android.app.ProgressDialog
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
import com.beat.core.data.model.Resource
import com.beat.core.data.model.UserPlaylistResponse
import com.beat.data.model.Alert
import com.beat.data.model.Playlist
import com.beat.databinding.AddToPlaylistFragmentLayoutBinding
import com.beat.util.Logger
import com.beat.util.listener.PaginationListener
import com.beat.util.listener.PlaylistClickListener
import com.beat.util.listener.PlaylistCreateListener
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.adapter.AddToPlaylistAdapter
import com.beat.view.dialog.CreatePlaylistDialog
import javax.inject.Inject

class AddToPlaylistFragment : BaseDaggerFragment(), PlaylistCreateListener, PlaylistClickListener,
    PaginationListener {

    private lateinit var binding: AddToPlaylistFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private lateinit var userPlaylistViewModel: UserPlaylistViewModel
    private lateinit var addToPlaylistAdapter: AddToPlaylistAdapter

    private lateinit var progressDialog: ProgressDialog

    private lateinit var trackId: String
    private var isPaginationDone = false
    private var itemNumber = 0
    private var perPageItem = 11

    companion object {
        fun newInstance(trackId: String?): AddToPlaylistFragment {
            val fragment = AddToPlaylistFragment()
            val bundle = Bundle()
            bundle.putString("trackId", trackId)
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
                R.layout.add_to_playlist_fragment_layout,
                container,
                false
            )
        userPlaylistViewModel =
            ViewModelProvider(this, providerFactory).get(UserPlaylistViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trackId = arguments?.getString("trackId", "")!!

        initialComponent()
    }

    private fun initialComponent() {
        progressDialog = ProgressDialog(mContext)
        binding.header.title.text = getString(R.string.add_to_playlist)
        addToPlaylistAdapter = AddToPlaylistAdapter(mContext, this, this)
        binding.recyclerView.adapter = addToPlaylistAdapter
        userPlaylistViewModel.getUserPlaylistResponse()
            .observe(viewLifecycleOwner, Observer<Resource<UserPlaylistResponse>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            binding.progressBar.visibility = View.GONE
                            binding.alertLayout.layout.visibility = View.GONE
                            binding.createPlaylist.visibility = View.VISIBLE
                            val list: ArrayList<Playlist> =
                                userPlaylistViewModel.getPlaylistsFromResponse(
                                    resource.data!!
                                )
                            addToPlaylistAdapter.addAll(
                                list
                            )
                            if (list.size < perPageItem) {
                                addToPlaylistAdapter.stopPagination(true)
                                isPaginationDone = false
                                itemNumber += list.size
                            } else {
                                addToPlaylistAdapter.stopPagination(false)
                                isPaginationDone = true
                                itemNumber += perPageItem
                            }
                        }
                        Resource.Status.ERROR -> {
                            isPaginationDone = true
                            binding.progressBar.visibility = View.GONE
                            binding.alertLayout.layout.visibility = View.VISIBLE
                            binding.createPlaylist.visibility = View.VISIBLE
                            binding.alertLayout.data =
                                Alert(
                                    getString(R.string.no_playlist),
                                    getString(R.string.no_favorite_demo_text),
                                    resources.getDrawable(R.drawable.ic_ph_playlist)
                                )
                        }
                        Resource.Status.LOADING -> {
                             binding.createPlaylist.visibility = View.GONE
                            binding.alertLayout.layout.visibility = View.GONE
                        }
                    }
                }
            })

        userPlaylistViewModel.getModifiedPlaylistDetailsWithTrackResponse()
            .observe(viewLifecycleOwner, Observer<Resource<String>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            if (progressDialog.isShowing) progressDialog.dismiss()
                            showToast(
                                getString(R.string.added_to_playlist),
                                Toast.LENGTH_LONG
                            )
                            onNavBackClick()
                        }
                        Resource.Status.ERROR -> {
                            if (progressDialog.isShowing) progressDialog.dismiss()
                            showToast(
                                getString(R.string.something_went_wrong_please_try_again),
                                Toast.LENGTH_LONG
                            )
                        }
                        Resource.Status.LOADING -> {
                            progressDialog.setCancelable(false)
                            progressDialog.setMessage("Adding...")
                            progressDialog.show()
                        }
                    }
                }
            })

        userPlaylistViewModel.getUserPlaylists()

        binding.createPlaylist.setOnClickListener {
            CreatePlaylistDialog(this, userPlaylistViewModel, this)
        }

        binding.header.back.setOnClickListener {
            onNavBackClick()
        }
    }

    override fun onPlaylistCreated(playlist: Playlist) {
        addToPlaylistAdapter.addToTop(playlist)
    }

    override fun onPlaylistClick(playlist: Playlist, position: Int) {
        userPlaylistViewModel.getAllTrackOfModifiedPlaylist(
            playlist.playlistId,
            trackId
        )
    }

    override fun onPagination() {
        if (!isPaginationDone) return
        isPaginationDone = false
        itemNumber += 1
        val endItem = itemNumber + perPageItem
        userPlaylistViewModel.getUserPlaylistsPagination("playlists=$itemNumber-$endItem")
        Logger.d("Pagination", "playlists=$itemNumber-$endItem")
    }

}