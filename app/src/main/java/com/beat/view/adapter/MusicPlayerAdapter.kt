package com.beat.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.beat.R
import com.beat.data.model.Track
import com.beat.databinding.PlayingTrackItemBinding
import com.beat.view.audioPlayer.MusicPlayerFragment
import com.beat.view.audioPlayer.MusicPlayerViewModel
import com.beat.view.customView.CustomCircularSeekBar
import java.util.*

class MusicPlayerAdapter(
    private val fragment: MusicPlayerFragment,
    private val musicPlayerViewModel: MusicPlayerViewModel
) :
    PagerAdapter() {

    private val list = ArrayList<Track>()

    private var currentPage = 0
    private var previousState = -1
    private var userScrollChange = false

    private val defaultProgress = MutableLiveData<Int>().apply {
        value = 0
    }

    private var inflater: LayoutInflater = LayoutInflater.from(fragment.context)

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding: PlayingTrackItemBinding =
            DataBindingUtil.inflate(inflater, R.layout.playing_track_item, container, false)
        binding.lifecycleOwner = fragment
        binding.imageUrl = list[position].trackImage

        if (position == currentPage) {
            binding.progress = musicPlayerViewModel.getMediaPosition()
        } else {
            binding.progress = defaultProgress
        }
        binding.duration = list[position].duration * 1000

        binding.mediaProgress.setOnSeekArcChangeListener(object :
            CustomCircularSeekBar.OnSeekArcChangeListener {
            override fun onProgressChanged(
                seekArc: CustomCircularSeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    musicPlayerViewModel.onSeek(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekArc: CustomCircularSeekBar?) {

            }

            override fun onStopTrackingTouch(seekArc: CustomCircularSeekBar?) {

            }
        })

        container.addView(binding.root, 0)
        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    fun addAll(list: List<Track>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun getItemByIndex(position: Int): Track {
        return list[position]
    }

    fun getPositionById(mediaId: String, currentPosition: Int): Int {
        if (list.size > currentPosition && list[currentPosition].trackId == mediaId) {
            return currentPosition
        } else {
            list.forEachIndexed { index, track ->
                if (track.trackId == mediaId) {
                    return index
                }
            }
        }
        return -1
    }

    fun setFavorite(id: String, favorite: Boolean) {
        list.forEachIndexed { index, track ->
            if (track.trackId == id) {
                list[index].favorite = favorite
            }
        }
    }

    fun setPageChangeListener(): SimpleOnPageChangeListener = simpleOnPageChangeListener

    private val simpleOnPageChangeListener: SimpleOnPageChangeListener =
        object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                currentPage = position
                notifyDataSetChanged()
                if (userScrollChange) musicPlayerViewModel.onPlay(list[position].trackId)
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (previousState == ViewPager.SCROLL_STATE_DRAGGING
                    && state == ViewPager.SCROLL_STATE_SETTLING
                )
                    userScrollChange = true
                else if (previousState == ViewPager.SCROLL_STATE_SETTLING
                    && state == ViewPager.SCROLL_STATE_IDLE
                )
                    userScrollChange = false

                previousState = state
            }
        }

}