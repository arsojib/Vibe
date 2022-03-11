package com.beat.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import java.util.*

class ViewPagerAdapter(private val manager: FragmentManager) :
    FragmentPagerAdapter(manager) {
    private val mFragmentList: MutableList<Fragment> = ArrayList()
    override fun getItem(position: Int): Fragment {
        return mFragmentList[position]
    }

    override fun getCount(): Int {
        return mFragmentList.size
    }

    fun clearFragment() {
        for (frag in mFragmentList) manager.beginTransaction().remove(
            frag
        ).commit()
        manager.executePendingTransactions()
        mFragmentList.clear() //working
    }

    fun addFragment(fragment: Fragment) {
        mFragmentList.add(fragment)
        notifyDataSetChanged()
    }

    override fun getItemPosition(`object`: Any): Int {
// POSITION_NONE makes it possible to reload the PagerAdapter
        return POSITION_NONE
    }
}