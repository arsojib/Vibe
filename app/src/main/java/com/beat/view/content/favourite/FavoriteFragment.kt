package com.beat.view.content.favourite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.beat.R
import com.beat.base.BaseDaggerFragment
import com.beat.databinding.FavoriteFragmentLayoutBinding
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.adapter.ViewPagerWithTitleAdapter
import javax.inject.Inject

class FavoriteFragment : BaseDaggerFragment() {

    private lateinit var binding: FavoriteFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.favorite_fragment_layout,
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
        binding.header.title.text = getString(R.string.favorites)
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        setupViewPager(binding.viewPager)

        binding.header.back.setOnClickListener {
            onNavBackClick()
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerWithTitleAdapter(childFragmentManager)
        adapter.addFrag(FavoriteAudioFragment(), getString(R.string.audio))
        adapter.addFrag(FavoriteVideoFragment(), getString(R.string.video))
        adapter.addFrag(FavoriteArtistFragment(), getString(R.string.artist))
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 3
    }

}