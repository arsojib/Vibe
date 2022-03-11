package com.beat.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.core.data.model.*
import com.beat.core.utils.CoreConstants
import com.beat.data.model.Patch
import com.beat.data.model.*
import com.beat.databinding.HomeItemWithHeaderBinding
import com.beat.databinding.HomeItemWithoutHeaderBinding
import com.beat.util.Constants
import com.beat.util.listener.PlaylistMenuClickListener
import com.beat.util.listener.ReleasePopUpMenuClickListener
import com.beat.view.customView.ZoomOutTransformation
import com.beat.view.main.MainViewModel
import kotlinx.coroutines.*

class HomeTabAdapter(
    private val context: Context,
    private val list: ArrayList<Patch>,
    private val mainViewModel: MainViewModel,
    private val releasePopUpMenuClickListener: ReleasePopUpMenuClickListener,
    private val playlistMenuClickListener: PlaylistMenuClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val WITH_TITLE = 1
    private val WITHOUT_TITLE = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == WITHOUT_TITLE) {
            val homeBinding: HomeItemWithoutHeaderBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.home_item_without_header,
                parent,
                false
            )
            return ViewHolderWithOutTitle(homeBinding)
        } else {
            val homeBinding: HomeItemWithHeaderBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.home_item_with_header,
                parent,
                false
            )
            return ViewHolder(homeBinding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.binding.title.text = list[position].title
            holder.binding.progressBar.visibility =
                if (list[position].isDataLoaded) View.GONE else View.VISIBLE
            when (list[position].patch) {
                Constants.MUSIC_PATCH -> {
                    holder.binding.recyclerView.adapter =
                        HomeMusicAdapter(
                            context, if (list[position].list == null) ArrayList() else
                                list[position].list as ArrayList<Track>
                        )
                    getFreeTrackList(position)
                }
                Constants.RELEASE_PATCH -> {
                    holder.binding.recyclerView.adapter =
                        HomeAlbumAdapter(
                            context,
                            if (list[position].list == null) ArrayList() else
                                list[position].list as ArrayList<Release>,
                            releasePopUpMenuClickListener
                        )
                    getReleaseList(position)
                }
                Constants.FEATURED_PLAYLIST_PATCH -> {
                    holder.binding.recyclerView.adapter =
                        HomePlaylistAdapter(
                            context,
                            if (list[position].list == null) ArrayList() else
                                list[position].list as ArrayList<Playlist>,
                            playlistMenuClickListener
                        )
                    getFeaturedPlaylists(position)
                }
                Constants.TRENDING_ARTIST_PATCH -> {
                    holder.binding.recyclerView.adapter =
                        HomeArtistAdapter(
                            context, if (list[position].list == null) ArrayList() else
                                list[position].list as ArrayList<Artist>
                        )
                    getTrendingArtistList(position)
                }
                Constants.FEATURED_PLAYLIST_GROUP_PATCH -> {
                    holder.binding.recyclerView.adapter =
                        HomeSpecialPlaylistAdapter(
                            context, if (list[position].list == null) ArrayList() else
                                list[position].list as ArrayList<FeaturedGroupPlaylist>
                        )
                    getFeaturedGroupPlayLists(position)
                }
                Constants.GENRES_PATCH -> {
                    holder.binding.recyclerView.adapter =
                        HomeGenresAdapter(
                            context, if (list[position].list == null) ArrayList() else
                                list[position].list as ArrayList<Genres>
                        )
                    getGenresList(position)
                }
                else -> {
                    holder.binding.recyclerView.adapter =
                        HomeMusicAdapter(context, ArrayList())
                }
            }
        } else if (holder is ViewHolderWithOutTitle) {
//            holder.binding.viewPager.clipToPadding = false
//            holder.binding.viewPager.setPadding(100, 0, 100, 0)
//            holder.binding.viewPager.pageMargin = 12
            holder.binding.tabDots.setupWithViewPager(holder.binding.viewPager, true);
            holder.binding.viewPager.setPageTransformer(true, ZoomOutTransformation())
            holder.binding.viewPager.adapter = BillBoardAdapter(
                context, if (list[position].list == null) ArrayList() else
                    list[position].list as ArrayList<TopBanner>
            )
            getTopBannerList(position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].patch == Constants.BILLBOARD_PATCH) {
            WITHOUT_TITLE
        } else {
            WITH_TITLE
        }
    }

    class ViewHolder(homeBinding: HomeItemWithHeaderBinding) :
        RecyclerView.ViewHolder(homeBinding.root) {
        val binding = homeBinding
    }

    inner class ViewHolderWithOutTitle(homeBinding: HomeItemWithoutHeaderBinding) :
        RecyclerView.ViewHolder(homeBinding.root) {
        val binding = homeBinding
    }

    private fun getTopBannerList(position: Int) {
        if (list[position].isDataLoaded) return
        list[position].isDataLoaded = true
        GlobalScope.launch {
            val resource: Resource<TopBannerResponse> =
                mainViewModel.topBannerRequest(Constants.TOP_BANNER_GROUP_ID)
            Log.d("testing", "" + list[position].isDataLoaded + " " + " " + list.size)
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    list[position].isDataLoaded = true
                    list[position].list = mainViewModel.getTopBannerFromResponse(resource.data!!)
                    withContext(Dispatchers.Main) {
                        Log.d("testing", "" + list[position].isDataLoaded + " " + " " + list.size)
                        notifyItemChanged(position)
                    }
                }
                Resource.Status.ERROR -> {
                    list[position].isDataLoaded = false
                }
                Resource.Status.LOADING -> {
                }
            }
        }
    }

    private fun getGenresList(position: Int) {
        if (list[position].isDataLoaded) return
        list[position].isDataLoaded = true
        GlobalScope.launch {
            val resource: Resource<GenresListResponse> =
                mainViewModel.genresListRequest(Constants.GENRES_GROUP_ID)
            Log.d("testing", "" + list[position].isDataLoaded + " " + " " + list.size)
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    list[position].isDataLoaded = true
                    list[position].list = mainViewModel.getGenresFromResponse(resource.data!!)
                    withContext(Dispatchers.Main) {
                        Log.d("testing", "" + list[position].isDataLoaded + " " + " " + list.size)
                        notifyItemChanged(position)
                    }
                }
                Resource.Status.ERROR -> {
                    list[position].isDataLoaded = false
                }
                Resource.Status.LOADING -> {
                }
            }
        }
    }

    private fun getTrendingArtistList(position: Int) {
        if (list[position].isDataLoaded) return
        list[position].isDataLoaded = true
        GlobalScope.launch {
            val resource: Resource<TrendingArtistResponse> =
                mainViewModel.trendingArtistListRequest(Constants.TRENDING_ARTIST_GROUP_ID)
            Log.d("testing", "" + list[position].isDataLoaded + " " + " " + list.size)
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    list[position].isDataLoaded = true
                    list[position].list =
                        mainViewModel.getTrendingArtistFromResponse(resource.data!!)
                    withContext(Dispatchers.Main) {
                        Log.d("testing", "" + list[position].isDataLoaded + " " + " " + list.size)
                        notifyItemChanged(position)
                    }
                }
                Resource.Status.ERROR -> {
                    list[position].isDataLoaded = false
                }
                Resource.Status.LOADING -> {
                }
            }
        }
    }

    private fun getFreeTrackList(position: Int) {
        if (list[position].isDataLoaded) return
        list[position].isDataLoaded = true
        GlobalScope.launch {
            val resource: Resource<ProgramResponse> =
                mainViewModel.programRequest(list[position].id!!)
            Log.d("testing", "" + list[position].isDataLoaded + " " + " " + list.size)
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    list[position].isDataLoaded = true
                    list[position].list =
                        mainViewModel.getTrackFromProgramResponse(resource.data!!)
                    withContext(Dispatchers.Main) {
                        Log.d("testing", "" + list[position].isDataLoaded + " " + " " + list.size)
                        notifyItemChanged(position)
                    }
                }
                Resource.Status.ERROR -> {
                    list[position].isDataLoaded = false
                }
                Resource.Status.LOADING -> {
                }
            }
        }
    }

    private fun getReleaseList(position: Int) {
        if (list[position].isDataLoaded) return
        list[position].isDataLoaded = true
        GlobalScope.launch {
            val resource: Resource<ReleaseGroupResponse> =
                mainViewModel.releaseGroupRequest(list[position].id!!)
            Log.d("testing", "" + list[position].isDataLoaded + " " + " " + list.size)
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    list[position].isDataLoaded = true
                    list[position].list =
                        mainViewModel.getReleaseGroupFromResponse(resource.data!!)
                    withContext(Dispatchers.Main) {
                        Log.d("testing", "" + list[position].isDataLoaded + " " + " " + list.size)
                        notifyItemChanged(position)
                    }
                }
                Resource.Status.ERROR -> {
                    list[position].isDataLoaded = false
                }
                Resource.Status.LOADING -> {
                }
            }
        }
    }

    private fun getFeaturedPlaylists(position: Int) {
        if (list[position].isDataLoaded) return
        list[position].isDataLoaded = true
        GlobalScope.launch {
            val resource: Resource<FeaturedPlaylistResponse> =
                mainViewModel.featuredPlayListRequest()
            Log.d("testing", "" + list[position].isDataLoaded + " " + " " + list.size)
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    list[position].isDataLoaded = true
                    list[position].list =
                        mainViewModel.getFeaturedPlaylistFromResponse(resource.data!!)
                    withContext(Dispatchers.Main) {
                        Log.d("testing", "" + list[position].isDataLoaded + " " + " " + list.size)
                        notifyItemChanged(position)
                    }
                }
                Resource.Status.ERROR -> {
                    list[position].isDataLoaded = false
                }
                Resource.Status.LOADING -> {
                }
            }
        }
    }

    private fun getFeaturedGroupPlayLists(position: Int) {
        if (list[position].isDataLoaded) return
        list[position].isDataLoaded = true
        GlobalScope.launch {
            val resource: Resource<FeaturedGroupPlaylistResponse> =
                mainViewModel.featuredGroupPlayListRequest()
            Log.d("testing", "" + list[position].isDataLoaded + " " + " " + list.size)
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    list[position].isDataLoaded = true
                    list[position].list =
                        mainViewModel.getFeaturedGroupPlaylistFromResponse(resource.data!!)
                    withContext(Dispatchers.Main) {
                        Log.d("testing", "" + list[position].isDataLoaded + " " + " " + list.size)
                        notifyItemChanged(position)
                    }
                }
                Resource.Status.ERROR -> {
                    list[position].isDataLoaded = false
                }
                Resource.Status.LOADING -> {
                }
            }
        }
    }

    fun reloadPlaylistPatch() {
        for (i in 0 until (list.size)) {
            if (list[i].patch == Constants.FEATURED_PLAYLIST_PATCH) {
                list[i].list = null
                list[i].isDataLoaded = false
                notifyItemChanged(i)
                break
            }
        }
    }

    fun setFavorite(id: String, type: String, favorite: Boolean) {
        when (type) {
            CoreConstants.FAVORITE_RELEASE -> {
                for (i in 0 until (list.size)) {
                    if (list[i].patch == Constants.RELEASE_PATCH) {
                        val releaseList =
                            if (list[i].list == null) break else list[i].list as ArrayList<Release>
                        releaseList.forEachIndexed { index, release ->
                            if (release.releaseId == id) {
                                releaseList[index].favorite = favorite
                            }
                        }
                    }
                }
            }
        }
    }

}