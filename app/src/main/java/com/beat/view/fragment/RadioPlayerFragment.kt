package com.beat.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.beat.R
import com.beat.databinding.RadioPlayerLayoutBinding
import com.beat.util.listener.ClickListener
import com.beat.view.adapter.HomeRadioAdapter
import com.beat.view.customView.ItemDecorator
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RadioPlayerFragment : BottomSheetDialogFragment(), ClickListener {

    private lateinit var binding: RadioPlayerLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.radio_player_layout, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialComponent()
    }

    private fun initialComponent() {
        binding.recyclerView.addItemDecoration(ItemDecorator(-55))
        binding.recyclerView.adapter = context?.let { HomeRadioAdapter(it, this) }
    }

    override fun onClick(position: Int) {

    }

}