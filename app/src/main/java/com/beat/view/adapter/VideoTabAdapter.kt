package com.beat.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.core.data.model.ReleaseGroupResponse
import com.beat.core.data.model.Resource
import com.beat.data.model.Patch
import com.beat.data.model.Release
import com.beat.databinding.HomeItemWithHeaderBinding
import com.beat.util.Constants
import com.beat.util.listener.VideoClickListener
import com.beat.view.main.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoTabAdapter(
    private val context: Context,
    private val list: ArrayList<Patch>,
    private val mainViewModel: MainViewModel,
    private val videoClickListener: VideoClickListener
) :
    RecyclerView.Adapter<VideoTabAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val homeBinding: HomeItemWithHeaderBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.home_item_with_header,
            parent,
            false
        )
        return ViewHolder(homeBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.title.text = list[position].title
        holder.binding.progressBar.visibility =
            if (list[position].isDataLoaded) View.GONE else View.VISIBLE
        when (list[position].patch) {
            Constants.VIDEO_PATCH -> {
                holder.binding.recyclerView.adapter =
                    HomeVideoAdapter(
                        context, if (list[position].list == null) ArrayList() else
                            list[position].list as ArrayList<Release>,
                        videoClickListener
                    )
                getReleaseList(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(homeBinding: HomeItemWithHeaderBinding) :
        RecyclerView.ViewHolder(homeBinding.root) {
        val binding = homeBinding
    }

    private fun getReleaseList(position: Int) {
        if (list[position].isDataLoaded) return
        list[position].isDataLoaded = true
        GlobalScope.launch {
            val resource: Resource<ReleaseGroupResponse> =
                mainViewModel.releaseGroupRequest(list[position].id!!)
            Log.d("testing", "" + list[position].isDataLoaded + " " + " " + list.size)
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    list[position].isDataLoaded = true
                    list[position].list =
                        mainViewModel.getReleaseGroupFromResponse(resource.data!!)
                }
                Resource.Status.ERROR -> {
                    list[position].isDataLoaded = false
                }
                Resource.Status.LOADING -> {
                }
            }
            withContext(Dispatchers.Main) {
                Log.d("testing", "" + list[position].isDataLoaded + " " + " " + list.size)
                notifyItemChanged(position)
            }
        }
    }

}