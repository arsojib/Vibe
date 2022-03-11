package com.beat.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.beat.R
import com.beat.base.BaseFragment
import com.beat.data.model.Track
import com.beat.databinding.SongDetailsFragmentLayoutBinding
import com.beat.util.listener.TrackClickListener
import com.beat.view.adapter.MusicAdapter2

class SongDetailsFragment : BaseFragment(), TrackClickListener {

    private lateinit var binding: SongDetailsFragmentLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.song_details_fragment_layout,
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
        binding.header.title.text = getString(R.string.song)
        binding.songsRecyclerView.adapter = MusicAdapter2(mContext, this)
//        binding.similarSongRecyclerView.adapter = HomeAlbumAdapter(mContext, ArrayList())

        binding.header.back.setOnClickListener {
            onNavBackClick()
        }
    }

    override fun onTrackClick(track: Track, position: Int, isCurrentlyPlaying: Boolean) {

    }

    override fun onTrackMenuClick(track: Track) {

    }

}