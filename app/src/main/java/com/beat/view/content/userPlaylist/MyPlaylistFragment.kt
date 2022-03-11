package com.beat.view.content.userPlaylist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.beat.R
import com.beat.base.BaseDaggerFragment
import com.beat.core.data.model.Resource
import com.beat.core.data.model.UserPlaylistResponse
import com.beat.data.model.Alert
import com.beat.data.model.Playlist
import com.beat.databinding.MyPlaylistFragmentLayoutBinding
import com.beat.util.Logger
import com.beat.util.listener.PaginationListener
import com.beat.util.listener.PlaylistCreateListener
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.adapter.MyPlaylistAdapter
import com.beat.view.dialog.CreatePlaylistDialog
import javax.inject.Inject

class MyPlaylistFragment : BaseDaggerFragment(), PlaylistCreateListener, PaginationListener {

    private lateinit var binding: MyPlaylistFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private lateinit var userPlaylistViewModel: UserPlaylistViewModel
    private lateinit var myPlaylistAdapter: MyPlaylistAdapter

    private var isPaginationDone = false
    private var itemNumber = 0
    private var perPageItem = 11

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.my_playlist_fragment_layout,
                container,
                false
            )
        userPlaylistViewModel =
            ViewModelProvider(this, providerFactory).get(UserPlaylistViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialComponent()
    }

    private fun initialComponent() {
        binding.header.title.text = getString(R.string.playlists)
        binding.recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        myPlaylistAdapter = MyPlaylistAdapter(mContext, this)
        binding.recyclerView.adapter = myPlaylistAdapter
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
                            myPlaylistAdapter.addAll(
                                list
                            )
                            if (list.size < perPageItem) {
                                myPlaylistAdapter.stopPagination(true)
                                isPaginationDone = false
                                itemNumber += list.size
                            } else {
                                myPlaylistAdapter.stopPagination(false)
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

        userPlaylistViewModel.getUserPlaylists()

        binding.createPlaylist.setOnClickListener {
            CreatePlaylistDialog(this, userPlaylistViewModel, this)
        }

        binding.header.back.setOnClickListener {
            onNavBackClick()
        }
    }

    override fun onPlaylistCreated(playlist: Playlist) {
        myPlaylistAdapter.addToTop(playlist)
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