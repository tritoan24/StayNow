package com.ph32395.staynow_datn.quanlyhoadon

import android.annotation.SuppressLint
import com.ph32395.staynow_datn.TaoHopDong.ContractViewModel
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ph32395.staynow_datn.TaoHoaDon.InvoiceViewModel
import com.ph32395.staynow_datn.TaoHopDong.ContractStatus
import com.ph32395.staynow_datn.TaoHopDong.InvoiceStatus
import com.ph32395.staynow_datn.databinding.ActivityBillManagementBinding
import com.ph32395.staynow_datn.fragment.contract_tenant.LoaiTaiKhoan
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class BillManagementActivity : AppCompatActivity(), BillAdapter.OnInvoiceStatusUpdateListener {

    private lateinit var binding: ActivityBillManagementBinding
    private lateinit var contractAdapter: ContractAdapterQLHD
    private lateinit var billAdapter: BillAdapter

    private lateinit var mAuth: FirebaseAuth

    // Sử dụng ViewModel
    private lateinit var contractViewModel: ContractViewModel
    private val invoiceViewModel: InvoiceViewModel by viewModels()

    @SuppressLint("SetTextI18n")
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

        // Khởi tạo FirebaseAuth và lấy userId
        mAuth = FirebaseAuth.getInstance()
        val userId = mAuth.currentUser?.uid

        contractViewModel.userRoleLiveData.observe(this, Observer { role ->
            val isLandlord = checkUserRole(role)
            if (userId != null) {
                fetchBillsAndContractByUser(userId, isLandlord)
                // Quan sát LiveData từ ViewModel cho hóa đơn
                Log.d("BillManagementActivityLOOp", "isLandlord: $isLandlord")
                setAdapter(isLandlord)
            }
        })


        contractViewModel.allContracts.observe(this) { contracts ->
            contractAdapter.submitList(contracts)
            val contractSize = contracts.size
            binding.tvContractsQuantity.text = "($contractSize)"
        }


        invoiceViewModel.invoices.observe(this) { invoices ->
            if (invoices.isNullOrEmpty()) {
                Log.d("BillManagementActivity", "No invoices available")
            } else {
                Log.d("BillManagementActivity", "Invoices: ${invoices.size}")
            }
            billAdapter.submitList(invoices)
            val invoiceSize = invoices.size
            binding.tvInvoicesQuantity.text = "($invoiceSize)"
        }



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

        lifecycleScope.launch {
            val contractJob = async {
                if (isLandlord) {
                    contractViewModel.fetchContractsByLandlordForBillManagement(
                        userId,
                        setOf(
                            ContractStatus.ACTIVE,
                            ContractStatus.TERMINATED,
                            ContractStatus.EXPIRED
                        )
                    )
                } else {
                    contractViewModel.fetchContractsByTenantForBillManagement(
                        userId,
                        setOf(
                            ContractStatus.ACTIVE,
                            ContractStatus.TERMINATED,
                            ContractStatus.EXPIRED
                        )
                    )
                }
            }

            val invoiceJob = async {
                if (isLandlord) {
                    invoiceViewModel.fetchInvoicesForUser(
                        "idNguoigui",
                        userId,
                        InvoiceStatus.PENDING
                    )
                } else {
                    invoiceViewModel.fetchInvoicesForUser(
                        "idNguoinhan",
                        userId,
                        InvoiceStatus.PENDING
                    )
                }
            }

            // Chờ cả 2 job hoàn thành
            contractJob.await()
            invoiceJob.await()
        }

    }

    private fun checkUserRole(role: String): Boolean {
        return role == LoaiTaiKhoan.NguoiChoThue.toString()
    }

    private fun setAdapter(isLandlord: Boolean) {
        if (!::billAdapter.isInitialized) {
            billAdapter = BillAdapter(InvoiceStatus.PENDING, isLandlord, this)
            binding.rvBills.apply {
                layoutManager = LinearLayoutManager(this@BillManagementActivity)
                adapter = billAdapter
            }
        } else {
            // Nếu adapter đã được khởi tạo, chỉ cần cập nhật lại isLandlord
            billAdapter.updateIsLandlord(isLandlord)
        }
    }

}
