package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import com.beat.R
import com.beat.data.model.Release
import com.beat.databinding.VideoBannerItemBinding
import com.beat.util.listener.VideoClickListener

class VideoBannerAdapter(
    private val context: Context,
    private val list: ArrayList<Release>,
    private val videoClickListener: VideoClickListener
) : PagerAdapter() {

    private var inflater: LayoutInflater = LayoutInflater.from(context)

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding: VideoBannerItemBinding =
            DataBindingUtil.inflate(inflater, R.layout.video_banner_item, container, false)

        binding.data = list[position]

        binding.root.setOnClickListener {
            videoClickListener.onVideoClick(
                list[position], position
            )
        }

        container.addView(binding.root, 0)
        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

}