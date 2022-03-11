package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.databinding.NotificationItemBinding

class NotificationAdapter(context: Context, arrayList: ArrayList<String>) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private val mContext = context
    private val list = arrayList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val notificationItemBinding: NotificationItemBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.notification_item, parent, false)
        return ViewHolder(notificationItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 2
    }

    class ViewHolder(notificationItemBinding: NotificationItemBinding) : RecyclerView.ViewHolder(notificationItemBinding.root) {
        val binding = notificationItemBinding
    }

}