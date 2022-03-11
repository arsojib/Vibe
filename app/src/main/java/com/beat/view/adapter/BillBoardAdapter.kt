package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import com.beat.R
import com.beat.data.model.TopBanner
import com.beat.databinding.BillboardItemBinding
import com.beat.util.Constants
import com.beat.util.moveToPlaylistFragment
import com.beat.util.moveToReleaseFragment

class BillBoardAdapter(private val context: Context, private val list: ArrayList<TopBanner>) :
    PagerAdapter() {

    private var inflater: LayoutInflater = LayoutInflater.from(context)

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding: BillboardItemBinding =
            DataBindingUtil.inflate(inflater, R.layout.billboard_item, container, false)

        binding.data = list[position]

        binding.root.setOnClickListener {
            if (list[position].type == Constants.ALBUM) {
                moveToReleaseFragment(
                    context,
                    list[position].releaseId
                )
            } else {
                moveToPlaylistFragment(
                    context,
                    list[position].playlistId,
                    addToPlaylist = false,
                    offline = false
                )
            }
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