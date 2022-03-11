package com.beat.view.content.download

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
import com.beat.data.model.Release
import com.beat.databinding.AlbumFragmentLayoutBinding
import com.beat.util.listener.ReleasePopUpMenuClickListener
import com.beat.util.shareOnSocial
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.adapter.DownloadedAlbumAdapter
import com.beat.view.content.details.ArtistDetailsFragment
import javax.inject.Inject

class DownloadedAlbumFragment : BaseDaggerFragment(), ReleasePopUpMenuClickListener {

    private lateinit var binding: AlbumFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory
    private lateinit var downloadViewModel: DownloadViewModel
    private lateinit var downloadedAlbumAdapter: DownloadedAlbumAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.album_fragment_layout, container, false)
        downloadViewModel =
            ViewModelProvider(this, providerFactory).get(DownloadViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialComponent()
    }

    private fun initialComponent() {
        downloadedAlbumAdapter = DownloadedAlbumAdapter(mContext, this)
        binding.recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerView.adapter = downloadedAlbumAdapter

        downloadViewModel.getReleaseList()
            .observe(viewLifecycleOwner, Observer<Resource<List<Release>>> {
                it?.let {
                    when (it.status) {
                        Resource.Status.SUCCESS -> {
                            binding.progressBar.visibility = View.GONE
                            binding.alertLayout.layout.visibility = View.GONE
                            downloadedAlbumAdapter.addAll(it.data!!)
                        }
                        Resource.Status.ERROR -> {
                            binding.progressBar.visibility = View.GONE
                            binding.alertLayout.layout.visibility = View.VISIBLE
                        }
                        Resource.Status.LOADING -> {

                        }
                    }
                }
            })

        downloadViewModel.getDataUpdatedObserver(Observer {
            downloadViewModel.fetchReleaseList()
        }, viewLifecycleOwner)

        downloadViewModel.fetchReleaseList()
    }

    private fun toArtist(artistId: String, artistName: String) {
        moveToFragmentWithBackStack(
            ArtistDetailsFragment.newInstance(
                artistId
            ),
            ArtistDetailsFragment::class.java.name,
            ArtistDetailsFragment::class.java.name
        )
    }

    private fun setFavorite(id: String, type: String, isFav: Boolean) {
        downloadViewModel.hasPermission(mContext) {
            downloadViewModel.patchFavorite(
                id,
                type,
                isFav
            )
            downloadedAlbumAdapter.setFavorite(id, isFav)
            binding.invalidateAll()
        }
    }

    override fun onFavorite(id: String, type: String, isFav: Boolean) {
        setFavorite(id, type, isFav)
    }

    override fun onGoToArtist(artistId: String, artistName: String) {
        toArtist(artistId, artistName)
    }

    override fun onShareOnSocialMedia(url: String) {
        shareOnSocial(mContext, url)
    }

}