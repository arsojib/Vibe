package com.beat.view.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.beat.R
import com.beat.databinding.AudioSettingMenuLayoutBinding
import com.beat.util.Constants
import com.beat.util.listener.AudioSettingMenuListener

class AudioSettingMenuDialog constructor(
    private val context: Context,
    private val quality: String,
    private val audioSettingMenuListener: AudioSettingMenuListener
) {

    private lateinit var dialog: AlertDialog
    private lateinit var binding: AudioSettingMenuLayoutBinding

    init {
        initialDialog()
    }

    private fun initialDialog() {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.audio_setting_menu_layout,
            null,
            false
        )

        val builder: AlertDialog.Builder =
            AlertDialog.Builder(context)
        builder.setView(binding.root)
        dialog = builder.create()
        dialog.setCancelable(true)

        when (quality) {
            Constants.LOW -> {
                binding.rbDataSaving.isChecked = true
            }
            Constants.NORMAL -> {
                binding.rbStandardQuality.isChecked = true
            }
            Constants.HIGH -> {
                binding.rbHighDefinition.isChecked = true
            }
        }

        binding.rbDataSaving.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                audioSettingMenuListener.onQualityChange(Constants.LOW)
                dialog.dismiss()
            }
        }

        binding.rbStandardQuality.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                audioSettingMenuListener.onQualityChange(Constants.NORMAL)
                dialog.dismiss()
            }
        }

        binding.rbHighDefinition.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                audioSettingMenuListener.onQualityChange(Constants.HIGH)
                dialog.dismiss()
            }
        }

        binding.dataSavingLayout.setOnClickListener {
            audioSettingMenuListener.onQualityChange(Constants.LOW)
            dialog.dismiss()
        }

        binding.standardQualityLayout.setOnClickListener {
            audioSettingMenuListener.onQualityChange(Constants.NORMAL)
            dialog.dismiss()
        }

        binding.highDefinitionLayout.setOnClickListener {
            audioSettingMenuListener.onQualityChange(Constants.HIGH)
            dialog.dismiss()
        }

        dialog.show()
    }

}