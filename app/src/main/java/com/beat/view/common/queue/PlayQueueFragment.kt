package com.beat.view.common.queue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.base.BaseDaggerFragment
import com.beat.data.model.Track
import com.beat.databinding.PlayQueueFragmentLayoutBinding
import com.beat.util.listener.ItemMoveCallbackListener
import com.beat.util.listener.OnStartDragListener
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.adapter.PlayQueueAdapter
import javax.inject.Inject

class PlayQueueFragment : BaseDaggerFragment(), OnStartDragListener {

    private lateinit var binding: PlayQueueFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private lateinit var playQueueViewModel: PlayQueueViewModel

    private lateinit var adapter: PlayQueueAdapter
    private lateinit var touchHelper: ItemTouchHelper

    private lateinit var track: Track

    companion object {
        fun newInstance(track: Track?): PlayQueueFragment {
            val fragment = PlayQueueFragment()
            val bundle = Bundle()
            bundle.putParcelable("track", track)
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
            DataBindingUtil.inflate(inflater, R.layout.play_queue_fragment_layout, container, false)
        playQueueViewModel =
            ViewModelProvider(this, providerFactory).get(PlayQueueViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        track = arguments?.getParcelable("track")!!

        initialComponent()
    }

    private fun initialComponent() {
        binding.data = track
        adapter = PlayQueueAdapter(mContext, this)
        val callback: ItemTouchHelper.Callback = ItemMoveCallbackListener(adapter)
        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(binding.songsRecyclerView)
        binding.songsRecyclerView.adapter = adapter

        playQueueViewModel.getMediaList().observe(viewLifecycleOwner, Observer<List<Track>> {
            it?.let { list ->
                adapter.addAll(list)
            }
        })

        playQueueViewModel.fetchMediaList()

        binding.done.setOnClickListener {
            playQueueViewModel.updatePlaylist(
                adapter.getAll(),
                adapter.getPlayableMedia(track.trackId)
            )
            onNavBackClick()
        }

        binding.back.setOnClickListener {
            onNavBackClick()
        }
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        touchHelper.startDrag(viewHolder)
    }

}