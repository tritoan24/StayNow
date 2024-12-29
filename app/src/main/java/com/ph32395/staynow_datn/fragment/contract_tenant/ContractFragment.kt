package com.ph32395.staynow_datn.fragment.contract_tenant

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ph32395.staynow_datn.MainActivity
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.TaoHopDong.ContractStatus
import com.ph32395.staynow_datn.TaoHopDong.ContractViewModel
import com.ph32395.staynow_datn.databinding.FragmentContractBinding
import com.ph32395.staynow_datn.hieunt.widget.tap
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class ContractFragment : Fragment() {

    // Đối tượng Binding
    private var _binding: FragmentContractBinding? = null
    private val binding get() = _binding!!

    private lateinit var contractViewModel: ContractViewModel

    private lateinit var activeAdapter: ContractAdapter
    private lateinit var pendingAdapter: ContractAdapter
    private lateinit var expireAdapter: ContractAdapter
    private lateinit var terminatedAdapter: ContractAdapter
    private lateinit var processingAdapter: ContractAdapter

    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContractBinding.inflate(inflater, container, false)
        // Khởi tạo ViewModel
        contractViewModel = ViewModelProvider(this)[ContractViewModel::class.java]

        binding.ivBack.tap {
            requireActivity().onBackPressed()
        }

        // Khởi tạo FirebaseAuth và lấy userId
        mAuth = FirebaseAuth.getInstance()
        val userId = mAuth.currentUser?.uid

        setupObservers()

        // Quan sát LiveData để nhận giá trị vai trò
        contractViewModel.userRoleLiveData.observe(viewLifecycleOwner) { role ->
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

        binding.ivMenu.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), binding.ivMenu)
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
                        setupRecyclerView(expireAdapter, itemName)
                        checkEmptyState(expireAdapter)
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

        return binding.root
    }

    private fun setupRecyclerView(adapter: ContractAdapter, title: String) {
        binding.rvContracts.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(context)
        }
        binding.tvContractsTitle.text = title
    }

    private fun setupAdapters(isLandlord: Boolean) {
        activeAdapter =
            ContractAdapter(
                contractViewModel,
                ContractStatus.ACTIVE,
                isLandlord
            ) { contractId, newStatus ->
                contractViewModel.updateContractStatus(contractId, newStatus)
            }
        pendingAdapter =
            ContractAdapter(
                contractViewModel,
                ContractStatus.PENDING,
                isLandlord
            ) { contractId, newStatus ->
                contractViewModel.updateContractStatus(contractId, newStatus)
            }
        expireAdapter =
            ContractAdapter(
                contractViewModel,
                ContractStatus.EXPIRED,
                isLandlord
            ) { contractId, newStatus ->
                contractViewModel.updateContractStatus(contractId, newStatus)
            }
        terminatedAdapter =
            ContractAdapter(
                contractViewModel,
                ContractStatus.TERMINATED,
                isLandlord
            ) { contractId, newStatus ->
                contractViewModel.updateContractStatus(contractId, newStatus)
            }
        processingAdapter =
            ContractAdapter(
                contractViewModel,
                ContractStatus.PROCESSING,
                isLandlord
            ) { contractId, newStatus ->
                contractViewModel.updateContractStatus(contractId, newStatus)
            }

    }

    @SuppressLint("SetTextI18n")
    private fun setupObservers() {
        contractViewModel.activeContracts.observe(viewLifecycleOwner) { contracts ->
            activeAdapter.updateContractList(contracts)

        }
        contractViewModel.pendingContracts.observe(viewLifecycleOwner) { contracts ->
            pendingAdapter.updateContractList(contracts)

        }
        contractViewModel.expiredContracts.observe(viewLifecycleOwner) { contracts ->
            expireAdapter.updateContractList(contracts)
        }
        contractViewModel.terminatedContracts.observe(viewLifecycleOwner) { contracts ->
            terminatedAdapter.updateContractList(contracts)
        }
        contractViewModel.processingContracts.observe(viewLifecycleOwner) { contracts ->
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
                    setOf(ContractStatus.TERMINATED)
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
                    setOf(ContractStatus.TERMINATED)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        if (activity is MainActivity) {
            (activity as MainActivity).setBottomNavigationVisibility(true)
        }
    }

}

enum class LoaiTaiKhoan {
    NguoiChoThue,
    NguoiThue
}