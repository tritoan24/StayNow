package com.ph32395.staynow.quanlyhoadon

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.ph32395.staynow.databinding.ActivityHistoryBillContactBinding
import com.ph32395.staynow.hieunt.widget.tap


class HistoryBillContactActivity<InvoiceViewModel> : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBillContactBinding
    private lateinit var billPagerAdapter: BillPagerAdapter
    private var contractId: String? = null
    private val invoiceViewModel: InvoiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sử dụng ViewBinding để truy cập view
        binding = ActivityHistoryBillContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy contractId từ intent
        contractId = intent.getStringExtra("CONTRACT_ID")

        // Đảm bảo contractId không null
        contractId?.let {
            billPagerAdapter = BillPagerAdapter(this, it)
            binding.viewPager.adapter = billPagerAdapter

            // Liên kết TabLayout với ViewPager
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                when (position) {
                    0 -> tab.text = "Đã thanh toán"
                    1 -> tab.text = "Đã hủy"
                }
            }.attach()
        }

        binding.ivBack.tap {
            onBackPressedDispatcher.onBackPressed()
            finish()
        }

    }
}
