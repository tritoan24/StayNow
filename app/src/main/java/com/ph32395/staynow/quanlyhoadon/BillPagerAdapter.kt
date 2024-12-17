package com.ph32395.staynow.quanlyhoadon

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ph32395.staynow.TaoHopDong.InvoiceStatus


class BillPagerAdapter(fragmentActivity: FragmentActivity, private val contractId: String,private val isLandlord:Boolean) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2 // 2 tab

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> InvoiceFragment.newInstance(contractId, InvoiceStatus.PAID,isLandlord)
            1 -> InvoiceFragment.newInstance(contractId, InvoiceStatus.CANCELLED,isLandlord)
            else -> throw IllegalStateException("Invalid position")
        }
    }
}
