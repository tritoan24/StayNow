package com.ph32395.staynow_datn.QuanLyNhaTro.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ph32395.staynow_datn.databinding.FragmentNhaTroDangHoatDongBinding

class FragmentNhaTroDangHoatDong : Fragment() {

    private lateinit var binding: FragmentNhaTroDangHoatDongBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentNhaTroDangHoatDongBinding.inflate(
            inflater,
            container?.parent as ViewGroup?, false
        )




        return binding.root
    }
}