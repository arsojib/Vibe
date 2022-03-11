package com.beat.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.beat.R
import com.beat.base.BaseFragment
import com.beat.databinding.NotificationFragmentLayoutBinding
import com.beat.view.adapter.NotificationAdapter

class NotificationFragment : BaseFragment() {

    private lateinit var binding: NotificationFragmentLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.notification_fragment_layout, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialComponent()
    }

    private fun initialComponent() {
        binding.header.title.text = getString(R.string.notifications)

        binding.recyclerView.adapter = NotificationAdapter(mContext, ArrayList())

        binding.header.back.setOnClickListener {
            onNavBackClick()
        }
    }

}