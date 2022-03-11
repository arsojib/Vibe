package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.Radio
import com.beat.databinding.RadioItemBinding
import com.beat.util.listener.ClickListener

class HomeRadioAdapter(context: Context, clickListener: ClickListener) :
    RecyclerView.Adapter<HomeRadioAdapter.ViewHolder>() {

    private val mContext = context
    private val list = ArrayList<Radio>()
    private val mClickListener = clickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val radioItemBinding: RadioItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mContext),
            R.layout.radio_item,
            parent,
            false
        )
        return ViewHolder(radioItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.data = list[position]

        holder.binding.root.setOnClickListener {
            mClickListener.onClick(position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(radioItemBinding: RadioItemBinding) :
        RecyclerView.ViewHolder(radioItemBinding.root) {
        val binding = radioItemBinding
    }

    fun addAll(list: ArrayList<Radio>) {
        this.list.clear()
        notifyDataSetChanged()
        for (radio in list) {
            add(radio)
        }
    }

    private fun add(radio: Radio) {
        list.add(radio)
        notifyItemInserted(list.size - 1)
    }

    fun getRadioByPosition(position: Int): Radio? {
        return if (list.size > position) {
            list[position]
        } else {
            null
        }
    }

}