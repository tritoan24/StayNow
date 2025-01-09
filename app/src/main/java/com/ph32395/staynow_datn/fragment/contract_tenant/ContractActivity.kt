package com.ph32395.staynow_datn.fragment.contract_tenant

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.TaoHopDong.ContractStatus
import com.ph32395.staynow_datn.TaoHopDong.ContractViewModel
import com.ph32395.staynow_datn.databinding.ActivityContractBinding
import com.ph32395.staynow_datn.hieunt.widget.tap
import kotlinx.coroutines.launch

class ContractActivity : AppCompatActivity() {

    // Đối tượng Binding
    private lateinit var binding: ActivityContractBinding

    private val contractViewModel: ContractViewModel by viewModels()

    private lateinit var activeAdapter: ContractAdapter
    private lateinit var pendingAdapter: ContractAdapter
    private lateinit var expiredAdapter: ContractAdapter
    private lateinit var cancelledAdapter: ContractAdapter
    private lateinit var terminatedAdapter: ContractAdapter
    private lateinit var terminatedProcessingAdapter: ContractAdapter
    private lateinit var processingAdapter: ContractAdapter

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContractBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo FirebaseAuth và lấy userId
        mAuth = FirebaseAuth.getInstance()
        val userId = mAuth.currentUser?.uid

        setupObservers()

        // Quan sát LiveData để nhận giá trị vai trò
        contractViewModel.userRoleLiveData.observe(this) { role ->
            val isLandlord = checkUserRole(role)
            if (userId != null) {
                fetchContractsByUser(userId, isLandlord)
                setupAdapters(isLandlord)
                setupRecyclerView(pendingAdapter, "Hợp đồng đang chờ xác nhận")
            }
        }

        if (userId != null) {
            contractViewModel.getUserRole(userId)
        }

        binding.ivBack.tap {
            onBackPressed()
        }

        binding.ivMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, binding.ivMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_contracts, popupMenu.menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener { item ->
                val itemName = item.title.toString()

                when (item.itemId) {
                    R.id.menu_pending_contracts -> {
                        setupRecyclerView(pendingAdapter, itemName)
                        checkEmptyState(pendingAdapter)
                        true
                    }

                    R.id.menu_processing_contracts -> {
                        setupRecyclerView(processingAdapter, itemName)
                        checkEmptyState(processingAdapter)
                        true
                    }

                    R.id.menu_active_contracts -> {
                        setupRecyclerView(activeAdapter, itemName)
                        checkEmptyState(activeAdapter)
                        true
                    }

                    R.id.menu_expired_contracts -> {
                        setupRecyclerView(expiredAdapter, itemName)
                        checkEmptyState(expiredAdapter)
                        true
                    }

                    R.id.menu_cancelled_contracts -> {
                        setupRecyclerView(cancelledAdapter, itemName)
                        checkEmptyState(cancelledAdapter)
                        true
                    }

                    R.id.menu_terminatedProcessing_contracts -> {
                        setupRecyclerView(terminatedProcessingAdapter, itemName)
                        checkEmptyState(terminatedProcessingAdapter)
                        true
                    }
                    R.id.menu_terminated_contracts -> {
                        setupRecyclerView(terminatedAdapter, itemName)
                        checkEmptyState(terminatedAdapter)
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun setupRecyclerView(adapter: ContractAdapter, title: String) {
        binding.rvContracts.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(this@ContractActivity)
        }
        binding.tvContractsTitle.text = title
    }

    private fun setupAdapters(isLandlord: Boolean) {
        activeAdapter =
            ContractAdapter(
                contractViewModel,
                ContractStatus.ACTIVE,
                isLandlord,
                { contractId, newStatus ->
                    contractViewModel.updateContractStatus(contractId, newStatus)
                },
                { contract, reason, status ->
                    contractViewModel.updateContractTerminationRequest(
                        contract.maHopDong,
                        reason,
                        status
                    )
                }
            )
        pendingAdapter =
            ContractAdapter(
                contractViewModel,
                ContractStatus.PENDING,
                isLandlord,
                { contractId, newStatus ->
                    contractViewModel.updateContractStatus(contractId, newStatus)
                },
                { contract, reason, status ->
                    contractViewModel.updateContractTerminationRequest(
                        contract.maHopDong,
                        reason,
                        status
                    )
                }
            )
        expiredAdapter =
            ContractAdapter(
                contractViewModel,
                ContractStatus.EXPIRED,
                isLandlord,
                { contractId, newStatus ->
                    contractViewModel.updateContractStatus(contractId, newStatus)
                }, { contract, reason, status ->
                    contractViewModel.updateContractTerminationRequest(
                        contract.maHopDong,
                        reason,
                        status
                    )
                }
            )
        cancelledAdapter =
            ContractAdapter(
                contractViewModel,
                ContractStatus.CANCELLED,
                isLandlord,
                { contractId, newStatus ->
                    contractViewModel.updateContractStatus(contractId, newStatus)
                },
                { contract, reason, status ->
                    contractViewModel.updateContractTerminationRequest(
                        contract.maHopDong,
                        reason,
                        status
                    )
                })
        terminatedAdapter =
            ContractAdapter(
                contractViewModel,
                ContractStatus.TERMINATED,
                isLandlord,
                { contractId, newStatus ->
                    contractViewModel.updateContractStatus(contractId, newStatus)
                }, { contract, reason, status ->
                    contractViewModel.updateContractTerminationRequest(
                        contract.maHopDong,
                        reason,
                        status
                    )
                }
            )
        terminatedProcessingAdapter =
            ContractAdapter(
                contractViewModel,
                ContractStatus.TERMINATED_PROCESSING,
                isLandlord,
                { contractId, newStatus ->
                    contractViewModel.updateContractStatus(contractId, newStatus)
                }, { contract, reason, status ->
                    contractViewModel.updateContractTerminationRequest(
                        contract.maHopDong,
                        reason,
                        status
                    )
                }
            )

        processingAdapter =
            ContractAdapter(
                contractViewModel,
                ContractStatus.PROCESSING,
                isLandlord,
                { contractId, newStatus ->
                    contractViewModel.updateContractStatus(contractId, newStatus)
                }, { contract, reason, status ->
                    contractViewModel.updateContractTerminationRequest(
                        contract.maHopDong,
                        reason,
                        status
                    )
                }
            )

    }

    @SuppressLint("SetTextI18n")
    private fun setupObservers() {
        contractViewModel.activeContracts.observe(this) { contracts ->
            activeAdapter.updateContractList(contracts)
        }
        contractViewModel.pendingContracts.observe(this) { contracts ->
            pendingAdapter.updateContractList(contracts)
        }
        contractViewModel.expiredContracts.observe(this) { contracts ->
            expiredAdapter.updateContractList(contracts)
        }
        contractViewModel.cancelledContracts.observe(this) { contracts ->
            cancelledAdapter.updateContractList(contracts)
        }
        contractViewModel.terminatedContracts.observe(this) { contracts ->
            terminatedAdapter.updateContractList(contracts)
        }
        contractViewModel.terminatedProcessingContracts.observe(this) { contracts ->
            terminatedProcessingAdapter.updateContractList(contracts)
        }
        contractViewModel.processingContracts.observe(this) { contracts ->
            processingAdapter.updateContractList(contracts)
        }
    }

    private fun fetchContractsByUser(userId: String, isLandlord: Boolean) {
        if (isLandlord) {
            lifecycleScope.launch {
                contractViewModel.fetchContractsByLandlordForContractFragment(
                    userId,
                    setOf(ContractStatus.ACTIVE)
                )
                contractViewModel.fetchContractsByLandlordForContractFragment(
                    userId,
                    setOf(ContractStatus.PENDING)
                )
                contractViewModel.fetchContractsByLandlordForContractFragment(
                    userId,
                    setOf(ContractStatus.EXPIRED)
                )
                contractViewModel.fetchContractsByLandlordForContractFragment(
                    userId,
                    setOf(ContractStatus.CANCELLED)
                )
                contractViewModel.fetchContractsByLandlordForContractFragment(
                    userId,
                    setOf(ContractStatus.TERMINATED)
                )
                contractViewModel.fetchContractsByLandlordForContractFragment(
                    userId,
                    setOf(ContractStatus.TERMINATED_PROCESSING)
                )
                contractViewModel.fetchContractsByLandlordForContractFragment(
                    userId,
                    setOf(ContractStatus.PROCESSING)
                )

            }
        } else {
            lifecycleScope.launch {
                contractViewModel.fetchContractsByTenantForContractFragment(
                    userId,
                    setOf(ContractStatus.ACTIVE)
                )
                contractViewModel.fetchContractsByTenantForContractFragment(
                    userId,
                    setOf(ContractStatus.PENDING)
                )
                contractViewModel.fetchContractsByTenantForContractFragment(
                    userId,
                    setOf(ContractStatus.EXPIRED)
                )
                contractViewModel.fetchContractsByTenantForContractFragment(
                    userId,
                    setOf(ContractStatus.CANCELLED)
                )
                contractViewModel.fetchContractsByTenantForContractFragment(
                    userId,
                    setOf(ContractStatus.TERMINATED)
                )
                contractViewModel.fetchContractsByTenantForContractFragment(
                    userId,
                    setOf(ContractStatus.TERMINATED_PROCESSING)
                )
                contractViewModel.fetchContractsByTenantForContractFragment(
                    userId,
                    setOf(ContractStatus.PROCESSING)
                )
            }
        }
    }

    private fun checkUserRole(role: String): Boolean {
        return role == LoaiTaiKhoan.NguoiChoThue.toString()
    }

    // Hàm kiểm tra trạng thái trống và hiển thị cảnh báo
    private fun checkEmptyState(adapter: ContractAdapter) {
        val isEmpty = adapter.itemCount == 0

        if (isEmpty) {
            binding.icEmptyList.visibility = View.VISIBLE
        } else {
            binding.icEmptyList.visibility = View.GONE
        }
    }
}

enum class LoaiTaiKhoan {
    NguoiChoThue,
    NguoiThue,
    TatCa
}
