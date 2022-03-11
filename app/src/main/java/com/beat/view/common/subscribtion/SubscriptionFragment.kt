package com.beat.view.common.subscribtion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.beat.R
import com.beat.base.BaseDaggerFragment
import com.beat.core.data.model.Resource
import com.beat.data.model.SubscriptionPlan
import com.beat.databinding.SubscriptionFragmentLayoutBinding
import com.beat.util.event.SubscriptionChange
import com.beat.util.listener.CompleteCallBack
import com.beat.util.listener.SubscriptionClickListener
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.adapter.SubscriptionPlanAdapter
import com.beat.view.dialog.SubscriptionConfirmationDialog
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

class SubscriptionFragment : BaseDaggerFragment(), SubscriptionClickListener, CompleteCallBack {

    private lateinit var binding: SubscriptionFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private lateinit var subscriptionViewModel: SubscriptionViewModel
    private lateinit var subscriptionPlanAdapter: SubscriptionPlanAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.subscription_fragment_layout,
                container,
                false
            )
        subscriptionViewModel =
            ViewModelProvider(this, providerFactory).get(SubscriptionViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialComponent()
    }

    private fun initialComponent() {
        binding.header.title.text = getString(R.string.subscription)
        binding.recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        subscriptionPlanAdapter = SubscriptionPlanAdapter(mContext, this)
        binding.recyclerView.adapter = subscriptionPlanAdapter

        subscriptionViewModel.getSubscriptionPlans()
            .observe(viewLifecycleOwner, Observer<Resource<List<SubscriptionPlan>>> {
                it?.let { resource ->
                    when (it.status) {
                        Resource.Status.SUCCESS -> {
                            subscriptionPlanAdapter.addAll(resource.data!!)
                        }
                        Resource.Status.ERROR -> {

                        }
                        Resource.Status.LOADING -> {

                        }
                    }
                }
            })

        subscriptionViewModel.getSubscriptionProducts()

        binding.header.back.setOnClickListener {
            onNavBackClick()
        }
    }

    override fun onSubscriptionPlanClick(subscriptionPlan: SubscriptionPlan) {
        SubscriptionConfirmationDialog(
            mContext,
            subscriptionPlan,
            subscriptionViewModel,
            viewLifecycleOwner,
            this
        )
    }

    override fun onComplete() {
        EventBus.getDefault().post(SubscriptionChange())
        onNavBackClick()
    }

}