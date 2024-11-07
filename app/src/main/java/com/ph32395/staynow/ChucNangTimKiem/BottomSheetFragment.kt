package com.ph32395.staynow.ChucNangTimKiem

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mohammedalaa.seekbar.DoubleValueSeekBarView
import com.mohammedalaa.seekbar.OnDoubleValueSeekBarChangeListener
import com.ph32395.staynow.databinding.FragmentPriceRangeBinding

class BottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentPriceRangeBinding  // Binding tương ứng với layout của BottomSheet
    val TAG: String = "BottomSheet"
    var priceMinNew: Int = 0
    var priceMaxNew: Int = 0


    // Interface để gửi dữ liệu về Activity
    interface PriceRangeListener {
        fun onPriceRangeSelected(minPrice: Int, maxPrice: Int)
    }

    private var listener: PriceRangeListener? = null

    // Gán giá trị của listener khi Fragment được đính kèm vào Activity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PriceRangeListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement PriceRangeListener")
        }
    }

    // Xóa listener khi Fragment tách ra khỏi Activity
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    // Phương thức để cập nhật giá trị min/max từ Activity
    @SuppressLint("SetTextI18n")
    fun updatePriceRange(min: Int, max: Int) {
        // Cập nhật UI hoặc các giá trị trong giao diện
        Log.d(TAG, "updatePriceRange: $min, $max")
        priceMaxNew = max
        priceMinNew = min
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout cho BottomSheet
        binding = FragmentPriceRangeBinding.inflate(inflater, container, false)
        var finalMinValue: Int = 500
        var finalMaxValue: Int = 20000

        val doubleValueSeekBarView = binding.doubleRangeSeekbar
        binding.tvMinPrice.text =
            if (priceMinNew == 0) "${doubleValueSeekBarView.currentMinValue}đ" else "${priceMinNew}đ"
        binding.tvMaxPrice.text =
            if (priceMaxNew == 0) "${doubleValueSeekBarView.currentMaxValue}đ" else "${priceMaxNew}đ"
        doubleValueSeekBarView.currentMinValue = if (priceMinNew == 0) 500 else priceMinNew
        doubleValueSeekBarView.currentMaxValue = if (priceMaxNew == 0) 20000 else priceMaxNew

        binding.btnLamMoi.setOnClickListener {
            doubleValueSeekBarView.currentMinValue = 500
            doubleValueSeekBarView.currentMaxValue = 20000
            binding.tvMinPrice.text = "${doubleValueSeekBarView.currentMinValue}đ"
            binding.tvMaxPrice.text = "${doubleValueSeekBarView.currentMaxValue}đ"
        }
        doubleValueSeekBarView.setOnRangeSeekBarViewChangeListener(object :
            OnDoubleValueSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onValueChanged(
                seekBar: DoubleValueSeekBarView?,
                min: Int,
                max: Int,
                fromUser: Boolean
            ) {
                Log.d(TAG, "onValueChanged:  min: $min -- max: $max")
                binding.tvMinPrice.text = "${min}đ"
                binding.tvMaxPrice.text = "${max}đ"
            }

            override fun onStartTrackingTouch(
                seekBar: DoubleValueSeekBarView?,
                min: Int,
                max: Int
            ) {
                Log.d(TAG, "onStartTrackingTouch: min: $min -- max: $max")
            }

            override fun onStopTrackingTouch(seekBar: DoubleValueSeekBarView?, min: Int, max: Int) {
                Log.d(TAG, "onStopTrackingTouch: min: $min -- max: $max")
                finalMaxValue = max
                finalMinValue = min
            }
        })

        binding.btnApDung.setOnClickListener {
            Log.d(TAG, "onCreateView: btnAppDung max: $finalMaxValue ---- min: $finalMinValue")
            // Gọi interface để gửi giá trị về Activity
            listener?.onPriceRangeSelected(finalMinValue, finalMaxValue)
            dismiss() // Đóng BottomSheet
        }

        return binding.root
    }
}
