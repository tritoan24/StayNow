package com.ph32395.staynow.quanlyhoadon

import com.ph32395.staynow.TaoHopDong.ContractViewModel
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ph32395.staynow.TaoHoaDon.InvoiceViewModel
import com.ph32395.staynow.TaoHopDong.ContractStatus
import com.ph32395.staynow.TaoHopDong.InvoiceStatus
import com.ph32395.staynow.databinding.ActivityBillManagementBinding
import com.ph32395.staynow.fragment.contract_tenant.LoaiTaiKhoan
import kotlinx.coroutines.launch

class BillManagementActivity : AppCompatActivity(), BillAdapter.OnInvoiceStatusUpdateListener {

    private lateinit var binding: ActivityBillManagementBinding
    private lateinit var contractAdapter: ContractAdapterQLHD
    private lateinit var billAdapter: BillAdapter

    private lateinit var mAuth: FirebaseAuth

    // Sử dụng ViewModel
    private lateinit var contractViewModel: ContractViewModel
    private val invoiceViewModel: InvoiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBillManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contractViewModel = ViewModelProvider(this)[ContractViewModel::class.java]

        // Thiết lập RecyclerView cho hợp đồng
        contractAdapter = ContractAdapterQLHD()
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
            contractAdapter.submitList(contracts)
        }

        contractViewModel.expiredContracts.observe(this) { contracts ->
            contractAdapter.submitList(contracts)
        }
        contractViewModel.allContracts.observe(this) { contracts ->
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

        contractViewModel.userRoleLiveData.observe(this, Observer { role ->
            val isLandlord = checkUserRole(role)
            if (userId != null) {
                fetchBillsAndContractByUser(userId, isLandlord)
            }
        })

        if (userId != null) {
            contractViewModel.getUserRole(userId)
        }

        // Bắt sự kiện click trên toolbar
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    override fun onInvoiceStatusUpdate(invoiceId: String, newStatus: InvoiceStatus) {
        invoiceViewModel.updateInvoiceStatus(invoiceId, newStatus)
    }

    private fun fetchBillsAndContractByUser(userId: String, isLandlord: Boolean) {

        if (isLandlord) {
            lifecycleScope.launch {
                contractViewModel.fetchContractsByLandlordForBillManagement(
                    userId,
                    setOf(
                        ContractStatus.ACTIVE,
                        ContractStatus.TERMINATED,
                        ContractStatus.EXPIRED,
                    )
                )
                invoiceViewModel.fetchInvoicesForUser("idNguoigui", userId, InvoiceStatus.PENDING)

            }
        } else {
            lifecycleScope.launch {
                contractViewModel.fetchContractsByTenantForBillManagement(
                    userId,
                    setOf(
                        ContractStatus.ACTIVE,
                        ContractStatus.TERMINATED,
                        ContractStatus.EXPIRED,
                    )
                )
                invoiceViewModel.fetchInvoicesForUser("idNguoinhan", userId, InvoiceStatus.PENDING)

            }
        }
    }

    private fun checkUserRole(role: String): Boolean {
        return role == LoaiTaiKhoan.NguoiChoThue.toString()
    }
}
