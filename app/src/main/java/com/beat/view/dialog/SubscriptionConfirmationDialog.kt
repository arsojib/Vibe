package com.beat.view.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.beat.R
import com.beat.core.data.model.Resource
import com.beat.data.model.SubscriptionPlan
import com.beat.databinding.SubscriptionConfirmationLayoutBinding
import com.beat.util.listener.CompleteCallBack
import com.beat.view.common.subscribtion.SubscriptionViewModel

class SubscriptionConfirmationDialog constructor(
    private val context: Context,
    private val subscriptionPlan: SubscriptionPlan,
    private val subscriptionViewModel: SubscriptionViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val completeCallBack: CompleteCallBack
) {

    private lateinit var dialog: AlertDialog
    private lateinit var binding: SubscriptionConfirmationLayoutBinding

    init {
        initialDialog()
    }

    private fun initialDialog() {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.subscription_confirmation_layout,
            null,
            false
        )

        val builder: AlertDialog.Builder =
            AlertDialog.Builder(context)
        builder.setView(binding.root)
        dialog = builder.create()
        dialog.setCancelable(false)

        if (subscriptionPlan.state == 3) {
            binding.details.text = context.getString(R.string.are_you_sure_want_to_unsubscribe)
        } else {
            val price = getPriceInDecimal(subscriptionPlan.recurringPrice)
            binding.details.text =
                context.getString(R.string.you_are_subscribing_to_vibe_for_BDT, price)
        }

        subscriptionViewModel.getResponse().observe(lifecycleOwner, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Resource.Status.SUCCESS -> {
                        binding.uiFreezer.visibility = View.INVISIBLE
                        binding.progressBar.visibility = View.INVISIBLE
                        dismiss()
                        completeCallBack.onComplete()
                    }
                    Resource.Status.ERROR -> {
                        binding.uiFreezer.visibility = View.INVISIBLE
                        binding.progressBar.visibility = View.INVISIBLE
                        Toast.makeText(
                            context,
                            context.getString(R.string.something_went_wrong_please_try_again),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Resource.Status.LOADING -> {
                        binding.uiFreezer.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }
            }
        })

        binding.confirm.setOnClickListener {
            if (subscriptionPlan.state == 3) {
                subscriptionViewModel.unsubscribe(subscriptionPlan.subscriptionId)
            } else {
                subscriptionViewModel.subscribe(subscriptionPlan.productId)
            }
        }

        binding.cancel.setOnClickListener {
            dismiss()
        }

        dialog.show()
    }

    private fun getPriceInDecimal(price: Int): String {
        return (price.toFloat() / 100).toString()
    }

    private fun dismiss() {
        subscriptionViewModel.getResponse().value = null
        subscriptionViewModel.getResponse().removeObservers(lifecycleOwner)
        dialog.dismiss()
    }

}