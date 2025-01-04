package com.ph32395.staynow_datn.QuanLyNhaTro

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ph32395.staynow_datn.QuanLyNhaTro.fragment.FragmentNhaTroDangHoatDong
import com.ph32395.staynow_datn.QuanLyNhaTro.fragment.FragmentNhaTroKhongHoatDong

class ViewPagerAdapterNhaTro(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentNhaTroDangHoatDong()
            1 -> FragmentNhaTroKhongHoatDong()
            else -> throw IllegalStateException("Invalid tab position")
        }
    }
}