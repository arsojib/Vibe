package com.beat.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.beat.R
import com.beat.base.BaseDaggerFragment
import com.beat.data.model.Alert
import com.beat.data.model.SearchPatch
import com.beat.data.model.Track
import com.beat.databinding.SearchFragmentLayoutBinding
import com.beat.util.*
import com.beat.util.listener.ContinuePlayingMenuListener
import com.beat.util.listener.ReleasePopUpMenuClickListener
import com.beat.util.listener.TrackClickListener
import com.beat.util.listener.TrackPopUpMenuClickListener
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.adapter.SearchAdapter
import com.beat.view.dialog.ContinuePlayingMenuDialog
import com.example.android.uamp.media.extensions.isPlaying
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchFragment : BaseDaggerFragment(), TrackPopUpMenuClickListener,
    ReleasePopUpMenuClickListener, TrackClickListener {

    private lateinit var binding: SearchFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private lateinit var mainViewModel: MainViewModel
    private lateinit var searchAdapter: SearchAdapter

    private var query = ""

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
            DataBindingUtil.inflate(inflater, R.layout.search_fragment_layout, container, false)
        mainViewModel = ViewModelProvider(this, providerFactory).get(MainViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialComponent()
    }

    private fun initialComponent() {
//        binding.recentSearchRecyclerView.adapter = RecentSearchAdapter(mContext, ArrayList())
        searchAdapter = SearchAdapter(mContext, this, this, this)
        binding.searchResultRecyclerView.adapter = searchAdapter
        binding.alertLayout.data =
            Alert(
                getString(R.string.type_to_search),
                getString(R.string.type_and_search_millions_of_songs_and_video_artists_and_more),
                resources.getDrawable(R.drawable.ic_ph_search, null)
            )

        mainViewModel.getSearchPatchList()
            .observe(viewLifecycleOwner, Observer<ArrayList<SearchPatch>> {
                it?.let { arrayList ->
                    binding.refreshLayout.isRefreshing = false
                    binding.progressBar.visibility = View.GONE
                    binding.alertLayout.root.visibility =
                        if (arrayList.size == 0) View.VISIBLE else View.GONE
                    searchAdapter.addAll(arrayList)
                }
            })

        mainViewModel.getNowPlayingMediaItem()
            .observe(viewLifecycleOwner, Observer<Track> {
                searchAdapter.setCurrentPlayingItem(it)
            })

        mainViewModel.fetchCurrentPlayingItem()

        binding.refreshLayout.setOnRefreshListener {
            mainViewModel.getSearchContent(query)
        }

        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?): Boolean {
                binding.alertLayout.root.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                query = q ?: ""
                searchAdapter.clearAll()
                binding.searchBar.clearFocus()
                mainViewModel.getSearchContent(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    override fun onFavorite(id: String, type: String, isFav: Boolean) {
        mainViewModel.hasPermission(mContext) {
            mainViewModel.patchFavorite(id, type, isFav)
            GlobalScope.launch {
                searchAdapter.setFavorite(id, type, isFav)
            }
        }
    }

    override fun onTrackAddToPlaylist(trackId: String) {
        mainViewModel.hasPermission(mContext) {
            moveToAddToPlaylistFragment(mContext, trackId)
        }
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

    override fun onTrackClick(track: Track, position: Int, isCurrentlyPlaying: Boolean) {
        mainViewModel.hasPermission(mContext) {
            if (!isRadioInitialed && mainViewModel.getPlaybackStateCompat().isPlaying && !isCurrentlyPlaying) {
                ContinuePlayingMenuDialog(mContext, track, object : ContinuePlayingMenuListener {
                    override fun onPlayNow(track: Track) {
                        mainViewModel.playTrackToNext(track, true)
                    }

                    override fun onPlayNext(track: Track) {
                        mainViewModel.playTrackToNext(track, false)
                    }

                    override fun addToQueue(track: Track) {
                        mainViewModel.addTrackToQueue(track)
                    }
                })
            } else {
                mainViewModel.playTrack(track)
            }
        }
    }

    override fun onTrackMenuClick(track: Track) {
        //No Action Needed
        //Already served by other listener
    }

}