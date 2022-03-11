package com.beat.view.content.favourite

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
import com.beat.core.data.model.FavoriteArtistResponse
import com.beat.core.data.model.Resource
import com.beat.data.model.Alert
import com.beat.databinding.FavoriteArtistFragmentLayoutBinding
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.adapter.FavoriteArtistAdapter
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class FavoriteArtistFragment : BaseDaggerFragment() {

    private lateinit var binding: FavoriteArtistFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private lateinit var favoriteViewModel: FavoriteViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.favorite_artist_fragment_layout,
            container,
            false
        )
        favoriteViewModel =
            ViewModelProvider(this, providerFactory).get(FavoriteViewModel::class.java)
        Arrays.binarySearch()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialComponent()
    }

    private fun initialComponent() {
        binding.recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerView.adapter = FavoriteArtistAdapter(mContext, ArrayList())

        favoriteViewModel.getFavoriteArtistResponse()
            .observe(viewLifecycleOwner, Observer<Resource<FavoriteArtistResponse>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            binding.progressBar.visibility = View.GONE
                            binding.alertLayout.layout.visibility = View.GONE
                            binding.recyclerView.adapter = FavoriteArtistAdapter(
                                mContext,
                                favoriteViewModel.getArtistsFromResponse(resource.data!!)
                            )
                        }
                        Resource.Status.ERROR -> {
                            binding.progressBar.visibility = View.GONE
                            binding.alertLayout.layout.visibility = View.VISIBLE
                            binding.alertLayout.data =
                                Alert(
                                    getString(R.string.no_favorite_artists),
                                    getString(R.string.no_favorite_demo_text),
                                    resources.getDrawable(R.drawable.ic_ph_artist)
                                )
                        }
                        Resource.Status.LOADING -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.alertLayout.layout.visibility = View.GONE
                        }
                    }
                }
            })

        favoriteViewModel.getFavoriteArtists()
    }

}