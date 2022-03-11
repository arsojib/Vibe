package com.beat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beat.R
import com.beat.data.model.SubscriptionPlan
import com.beat.databinding.SubscriptionPlanItemBinding
import com.beat.util.listener.SubscriptionClickListener

class SubscriptionPlanAdapter(
    private val context: Context,
    private val subscriptionClickListener: SubscriptionClickListener
) : RecyclerView.Adapter<SubscriptionPlanAdapter.ViewHolder>() {

    private val list = ArrayList<SubscriptionPlan>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val subscriptionPlanItemBinding: SubscriptionPlanItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.subscription_plan_item, parent, false
        )
        return ViewHolder(subscriptionPlanItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.data = list[position]

        holder.binding.root.setOnClickListener {
            subscriptionClickListener.onSubscriptionPlanClick(list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(subscriptionPlanItemBinding: SubscriptionPlanItemBinding) :
        RecyclerView.ViewHolder(subscriptionPlanItemBinding.root) {
        val binding = subscriptionPlanItemBinding
    }

    fun addAll(list: List<SubscriptionPlan>) {
        this.list.clear()
        notifyDataSetChanged()
        for (subscriptionPlan in list) {
            add(subscriptionPlan)
        }
    }

    private fun add(subscriptionPlan: SubscriptionPlan) {
        list.add(subscriptionPlan)
        notifyItemInserted(list.size - 1)
    }

}