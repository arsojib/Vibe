package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.databinding.SearchTrackItemBinding

class RecentSearchAdapter(context: Context, arrayList: ArrayList<String>) :
    RecyclerView.Adapter<RecentSearchAdapter.ViewHolder>() {

    private val mContext = context
    private val list = arrayList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val searchTrackItemBinding: SearchTrackItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mContext),
            R.layout.search_track_item,
            parent,
            false
        )
        return ViewHolder(searchTrackItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 5
    }

    class ViewHolder(searchTrackItemBinding: SearchTrackItemBinding) :
        RecyclerView.ViewHolder(searchTrackItemBinding.root) {
        val binding = searchTrackItemBinding
    }

}