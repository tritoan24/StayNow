package com.ph32395.staynow_datn.QuyenLoiKyHopDong

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ph32395.staynow_datn.R

class QuyenLoinguoiThueFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quyen_loinguoi_thue, container, false)
    }
}