package com.ph32395.staynow.ChucNangTimKiem

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.ph32395.staynow.databinding.FragmentPriceRangeBinding
import java.text.NumberFormat
import java.util.Locale

class BottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentPriceRangeBinding  // Binding tương ứng với layout của BottomSheet
    val TAG: String = "BottomSheet"
    var priceMinNew: Int = 500000
    var priceMaxNew: Int = 20000000


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

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout cho BottomSheet
//        var finalMinValue: Int = 500000
//        var finalMaxValue: Int = 20000000

        binding = FragmentPriceRangeBinding.inflate(inflater, container, false)
        val rangeSeekBarView = binding.rangeSeekBar
        rangeSeekBarView.setRange(500000f, 20000000f, 1000000f)
        rangeSeekBarView.setProgress(500000f, 20000000f)
        binding.tvMinPrice.text =
            if (priceMinNew == 0) "${formatToVietnameseCurrency(rangeSeekBarView.minProgress.toDouble())} VND"
            else "${formatToVietnameseCurrency(priceMinNew.toDouble())} VND"
        binding.tvMaxPrice.text =
            if (priceMaxNew == 0) "${formatToVietnameseCurrency(rangeSeekBarView.maxProgress.toDouble())} VND"
            else "${formatToVietnameseCurrency(priceMaxNew.toDouble())} VND"
        if (priceMinNew > 0 || priceMaxNew > 0) {
            rangeSeekBarView.setProgress(priceMinNew.toFloat(), priceMaxNew.toFloat())
        } else {
            Log.d(TAG, "onCreateView: set progress f")
        }


        binding.rangeSeekBar.setOnRangeChangedListener(object : OnRangeChangedListener {
            @SuppressLint("DefaultLocale")
            override fun onRangeChanged(
                view: RangeSeekBar?,
                leftValue: Float,
                rightValue: Float,
                isFromUser: Boolean
            ) {
                Log.d(TAG, "onRangeChanged: view $view")
                Log.d(TAG, "onRangeChanged: leftValue $leftValue")
                Log.d(TAG, "onRangeChanged: rightValue $rightValue")
                Log.d(TAG, "onRangeChanged: isFromUser $isFromUser")
                val leftRounded = Math.round(leftValue / 100000) * 100000
                val rightRounded = Math.round(rightValue / 100000) * 100000

                binding.tvMinPrice.text =
                    "${formatToVietnameseCurrency(leftRounded.toDouble())} VND"
                binding.tvMaxPrice.text =
                    "${formatToVietnameseCurrency(rightRounded.toDouble())} VND"
                priceMinNew = leftRounded
                priceMaxNew = rightRounded

            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                Log.d(TAG, "onStartTrackingTouch: view ${view?.maxProgress}")
                Log.d(TAG, "onStartTrackingTouch: isLeft $isLeft")
            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                Log.d(TAG, "onStopTrackingTouch: viewMin ${view?.minProgress}")
                Log.d(TAG, "onStopTrackingTouch: viewMax ${view?.maxProgress}")
                Log.d(TAG, "onStopTrackingTouch: isLeft $isLeft")
            }
        })

        binding.btnApDung.setOnClickListener {
            Log.d(TAG, "onCreateView: btnAppDung max: $priceMaxNew ---- min: $priceMinNew")
            // Gọi interface để gửi giá trị về Activity
            listener?.onPriceRangeSelected(
                if (priceMinNew == 0) 500000 else priceMinNew,
                if (priceMaxNew == 0) 20000000 else priceMaxNew
            )
            dismiss() // Đóng BottomSheet
        }
        binding.btnLamMoi.setOnClickListener {
            binding.tvMinPrice.text =
                "${formatToVietnameseCurrency(rangeSeekBarView.minProgress.toDouble())} VND"
            binding.tvMaxPrice.text =
                "${formatToVietnameseCurrency(rangeSeekBarView.maxProgress.toDouble())} VND"
            rangeSeekBarView.setProgress(500000f, 20000000f)
        }

        return binding.root
    }

    fun formatToVietnameseCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        val formattedValue = format.format(amount)

        // Loại bỏ ký hiệu "₫" và dấu phân cách hàng nghìn
        return formattedValue.replace("₫", "").replace(",", "").trim()
    }

}
