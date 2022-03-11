package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.Track
import com.beat.databinding.QueueTrackItemBinding
import com.beat.util.listener.ItemMoveCallbackListener
import com.beat.util.listener.OnStartDragListener
import java.util.*
import kotlin.collections.ArrayList

class PlayQueueAdapter(
    private val context: Context,
    private val startDragListener: OnStartDragListener
) :
    RecyclerView.Adapter<PlayQueueAdapter.ItemViewHolder>(),
    ItemMoveCallbackListener.Listener {

    private val list = ArrayList<Track>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val queueTrackItemBinding: QueueTrackItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.queue_track_item,
            parent,
            false
        )
        return ItemViewHolder(queueTrackItemBinding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.binding.data = list[position]

        holder.binding.remove.setOnClickListener {
            if (list.size > 1) {
                list.removeAt(position)
                notifyItemRemoved(position)
                notifyDataSetChanged()
            }
        }

        holder.binding.dragger.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                this.startDragListener.onStartDrag(holder)
            }
            return@setOnTouchListener true
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ItemViewHolder(queueTrackItemBinding: QueueTrackItemBinding) :
        RecyclerView.ViewHolder(queueTrackItemBinding.root) {
        val binding = queueTrackItemBinding
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(list, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(list, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(itemViewHolder: ItemViewHolder) {
    }

    override fun onRowClear(itemViewHolder: ItemViewHolder) {
    }

    fun addAll(list: List<Track>) {
        this.list.clear()
        notifyDataSetChanged()
        for (track in list) {
            add(track)
        }
    }

    private fun add(track: Track) {
        list.add(track)
        notifyItemInserted(list.size - 1)
    }

    fun getAll() = list

    fun getPlayableMedia(currentMediaId: String): String {
        list.forEach {
            if (it.trackId == currentMediaId) return currentMediaId
        }
        return list[0].trackId
    }

}