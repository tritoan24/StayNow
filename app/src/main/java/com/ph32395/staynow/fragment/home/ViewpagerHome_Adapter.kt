package com.ph32395.staynow.fragment.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerHomeAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 4
    override fun createFragment(position: Int): Fragment {
        return HomeTabFragment.newInstance(position)
    }
}
