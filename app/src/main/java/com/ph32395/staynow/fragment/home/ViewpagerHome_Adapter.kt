package com.ph32395.staynow.fragment.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ph32395.staynow.Model.LoaiPhongTro

class ViewPagerHomeAdapter(fragment: Fragment, private val categories: List<LoaiPhongTro>) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
        val category = categories[position].id
        return HomeTabFragment.newInstance(category)
    }
}

