package com.ph32395.staynow.TaoHoaDon

import ContractViewModel
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.ph32395.staynow.R
import com.ph32395.staynow.TaoHopDong.ContractStatus
import com.ph32395.staynow.databinding.ActivityCreateMonthlyInvoiceBinding


class ChoiceContract : AppCompatActivity() {
    private lateinit var binding: ActivityCreateMonthlyInvoiceBinding

    //khai báo view model
    private val viewModel: ContractViewModel by viewModels()

    //Khai báo adapter
    private val adapter = ContractAdapterInvoice(ContractStatus.ACTIVE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateMonthlyInvoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Xử lý UI edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Cấu hình RecyclerView
        setupRecyclerView()

        // Quan sát dữ liệu từ ViewModel
        observeViewModel()

        viewModel.fetchAllContractsByUser(Firebase.auth.currentUser?.uid ?: "")
    }
    private fun setupRecyclerView() {
        binding.recyclerViewContract.apply {
            layoutManager = LinearLayoutManager(this@ChoiceContract)
            adapter = this@ChoiceContract.adapter
        }
    }

    private fun observeViewModel() {
        // Quan sát danh sách hợp đồng từ ViewModel
        viewModel.allContracts.observe(this, Observer { contracts ->
            // Cập nhật dữ liệu cho Adapter
            adapter.updateContractList(contracts)
        })
    }
}