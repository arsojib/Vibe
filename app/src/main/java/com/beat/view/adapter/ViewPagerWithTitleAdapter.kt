package com.beat.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import java.util.*

class ViewPagerWithTitleAdapter(private val manager: FragmentManager) :
    FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val mFragmentList: MutableList<Fragment> = ArrayList()
    private val mFragmentTitleList: MutableList<String> = ArrayList()

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
        mFragmentTitleList.clear() //working
    }

    fun addFrag(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
        notifyDataSetChanged()
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mFragmentTitleList[position]
    }

    override fun getItemPosition(`object`: Any): Int {
// POSITION_NONE makes it possible to reload the PagerAdapter
        return PagerAdapter.POSITION_NONE
    }
}
