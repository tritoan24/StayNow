package com.ph32395.staynow.quanlyhoadon

import ContractViewModel
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ph32395.staynow.TaoHoaDon.InvoiceMonthlyModel
import com.ph32395.staynow.TaoHoaDon.InvoiceViewModel
import com.ph32395.staynow.TaoHopDong.ContractStatus
import com.ph32395.staynow.TaoHopDong.InvoiceStatus
import com.ph32395.staynow.databinding.ActivityBillManagementBinding

class BillManagementActivity : AppCompatActivity(), BillAdapter.OnInvoiceStatusUpdateListener {

    private lateinit var binding: ActivityBillManagementBinding
    private lateinit var contractAdapter: ContractAdapter_QLHD
    private lateinit var billAdapter: BillAdapter

    private lateinit var mAuth: FirebaseAuth

    // Sử dụng ViewModel
    private val contractViewModel: ContractViewModel by viewModels()
    private val invoiceViewModel: InvoiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBillManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Thiết lập RecyclerView cho hợp đồng
        contractAdapter = ContractAdapter_QLHD()
        binding.rvContracts.apply {
            layoutManager = LinearLayoutManager(
                this@BillManagementActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = contractAdapter
        }

        // Quan sát LiveData từ ViewModel
        contractViewModel.activeContracts.observe(this) { contracts ->
            // Cập nhật adapter khi có dữ liệu mới
            contractAdapter.submitList(contracts)
        }


        // Quan sát LiveData từ ViewModel cho hóa đơn
        billAdapter = BillAdapter(InvoiceStatus.PENDING, this)
        binding.rvBills.apply {
            layoutManager = LinearLayoutManager(this@BillManagementActivity)
            adapter = billAdapter
        }

        invoiceViewModel.invoices.observe(this) { invoices ->
            if (invoices.isNullOrEmpty()) {
                Log.d("BillManagementActivity", "No invoices available")
            } else {
                Log.d("BillManagementActivity", "Invoices: ${invoices.size}")
            }
            billAdapter.submitList(invoices)
        }


        // Khởi tạo FirebaseAuth và lấy userId
        mAuth = FirebaseAuth.getInstance()
        val userId = mAuth.currentUser?.uid

        contractViewModel.fetchContractsByTenant(userId!!, ContractStatus.ACTIVE)

        //lấy tất cả hóa đơn chưa thanh toán của người dùng
        Log.d("BillManagementActivity", "Fetching invoices for user: $userId")
        invoiceViewModel.fetchInvoices(userId, InvoiceStatus.PENDING)

        // Bắt sự kiện click trên toolbar
        binding.ivBack.setOnClickListener {
            finish() // Kết thúc Activity khi bấm nút Back
        }
    }

    override fun onInvoiceStatusUpdate(invoiceId: String, newStatus: InvoiceStatus) {
        invoiceViewModel.updateInvoiceStatus(invoiceId, newStatus)
    }


}
