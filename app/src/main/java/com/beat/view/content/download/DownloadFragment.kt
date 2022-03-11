package com.beat.view.content.download

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.beat.R
import com.beat.base.BaseDaggerFragment
import com.beat.databinding.DownloadFragmentLayoutBinding
import com.beat.view.adapter.ViewPagerWithTitleAdapter

class DownloadFragment : BaseDaggerFragment() {

    private lateinit var binding: DownloadFragmentLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.download_fragment_layout,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialComponent()
    }

    private fun initialComponent() {
        binding.header.title.text = getString(R.string.downloads)
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        setupViewPager(binding.viewPager)

        binding.header.back.setOnClickListener {
            onNavBackClick()
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerWithTitleAdapter(childFragmentManager)
        adapter.addFrag(DownloadedTracksFragment(), getString(R.string.tracks))
        adapter.addFrag(DownloadedAlbumFragment(), getString(R.string.albums))
        adapter.addFrag(DownloadedPlaylistFragment(), getString(R.string.playlists))
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 3
    }

}