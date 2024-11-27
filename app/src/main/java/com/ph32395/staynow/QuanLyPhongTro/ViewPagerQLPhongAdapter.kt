package com.ph32395.staynow.QuanLyPhongTro

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ph32395.staynow.QuanLyPhongTro.fragment.PhongChoDuyetFragment
import com.ph32395.staynow.QuanLyPhongTro.fragment.PhongDaBiHuyFragment
import com.ph32395.staynow.QuanLyPhongTro.fragment.PhongDaChoThueFragment
import com.ph32395.staynow.QuanLyPhongTro.fragment.PhongDaDangFragment
import com.ph32395.staynow.QuanLyPhongTro.fragment.PhongDangLuuFragment

class ViewPagerQLPhongAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 5 //So tab

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> PhongDaDangFragment()
            1 -> PhongDangLuuFragment()
            2 -> PhongChoDuyetFragment()
            3 -> PhongDaBiHuyFragment()
            4 -> PhongDaChoThueFragment()
            else -> throw IllegalStateException("Invalid tab position")
        }
    }
}