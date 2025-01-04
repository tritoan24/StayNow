package com.ph32395.staynow_datn.QuanLyNhaTro.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ph32395.staynow_datn.databinding.FragmentNhaTroKhongHoatDongBinding

class FragmentNhaTroKhongHoatDong : Fragment() {

    private lateinit var binding: FragmentNhaTroKhongHoatDongBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentNhaTroKhongHoatDongBinding.inflate(
            inflater,
            container?.parent as ViewGroup?, false
        )




        return binding.root
    }
}