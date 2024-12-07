package com.ph32395.staynow.fragment.contract_tenant

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ph32395.staynow.MainActivity
import com.ph32395.staynow.R
import com.ph32395.staynow.TaoHopDong.ContractStatus
import com.ph32395.staynow.TaoHopDong.ContractViewModel
import com.ph32395.staynow.databinding.FragmentContractBinding
import com.ph32395.staynow.hieunt.widget.tap
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

    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout và gán cho binding
        _binding = FragmentContractBinding.inflate(inflater, container, false)
        // Khởi tạo ViewModel
        contractViewModel = ViewModelProvider(this)[ContractViewModel::class.java]
        // Đổi tên tiêu đề và thêm underline cho tiêu đề hợp đồng chờ xác nhận

        val pendingContractsTitle = binding.tvContractsTitle
        pendingContractsTitle.text = SpannableString("Hợp đồng chờ xác nhận").apply {
            setSpan(UnderlineSpan(), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.ivBack.tap {
            requireActivity().onBackPressed()
        }
        // Khởi tạo adapter và gán vào RecyclerView
        setupAdapters()

        // Khởi tạo FirebaseAuth và lấy userId
        mAuth = FirebaseAuth.getInstance()
        val userId = mAuth.currentUser?.uid

        setupObservers()

        // Quan sát LiveData để nhận giá trị vai trò
        contractViewModel.userRoleLiveData.observe(viewLifecycleOwner, Observer { role ->
            val isLandlord = checkUserRole(role)
            if (userId != null) {
                fetchContractsByUser(userId, isLandlord)
            }
        })

        if (userId != null) {
            contractViewModel.getUserRole(userId)
        }
        setupRecyclerView(pendingAdapter, "Hợp đồng chờ xác nhận")

        binding.ivMenu.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), binding.ivMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_contracts, popupMenu.menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener { item ->
                val itemName = item.title.toString()

                when (item.itemId) {
                    R.id.menu_pending_contracts -> {
                        setupRecyclerView(pendingAdapter, itemName)
                        checkEmptyState(pendingAdapter)  // Kiểm tra trạng thái trống cho pendingAdapter
                        true
                    }

                    R.id.menu_active_contracts -> {
                        setupRecyclerView(activeAdapter, itemName)
                        checkEmptyState(activeAdapter)  // Kiểm tra trạng thái trống cho pendingAdapter
                        true
                    }

                    R.id.menu_expired_contracts -> {
                        setupRecyclerView(expireAdapter, itemName)
                        checkEmptyState(expireAdapter)  // Kiểm tra trạng thái trống cho pendingAdapter
                        true
                    }

                    R.id.menu_terminated_contracts -> {
                        setupRecyclerView(terminatedAdapter, itemName)
                        checkEmptyState(terminatedAdapter)  // Kiểm tra trạng thái trống cho pendingAdapter
                        true
                    }

                    else -> false
                }
            }
        }

        // Trả về root view của binding
        return binding.root
    }

    private fun setupRecyclerView(adapter: ContractAdapter, title: String) {
        binding.rvContracts.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(context)
        }
        binding.tvContractsTitle.text = title
    }

    private fun setupAdapters() {
        activeAdapter =
            ContractAdapter(contractViewModel, ContractStatus.ACTIVE) { contractId, newStatus ->
                contractViewModel.updateContractStatus(contractId, newStatus)
            }
        pendingAdapter =
            ContractAdapter(contractViewModel, ContractStatus.PENDING) { contractId, newStatus ->
                contractViewModel.updateContractStatus(contractId, newStatus)
            }
        expireAdapter =
            ContractAdapter(contractViewModel, ContractStatus.EXPIRED) { contractId, newStatus ->
                contractViewModel.updateContractStatus(contractId, newStatus)
            }
        terminatedAdapter =
            ContractAdapter(contractViewModel, ContractStatus.TERMINATED) { contractId, newStatus ->
                contractViewModel.updateContractStatus(contractId, newStatus)
            }

    }

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
    }

    private fun fetchContractsByUser(userId: String, isLandlord: Boolean) {

        if (isLandlord) {
            lifecycleScope.launch {
                contractViewModel.fetchContractsByLandlordForContractFragment(userId, setOf(ContractStatus.ACTIVE))
                contractViewModel.fetchContractsByLandlordForContractFragment(userId, setOf(ContractStatus.PENDING))
                contractViewModel.fetchContractsByLandlordForContractFragment(userId, setOf(ContractStatus.EXPIRED))
                contractViewModel.fetchContractsByLandlordForContractFragment(userId, setOf(ContractStatus.TERMINATED))

            }
        } else {
            lifecycleScope.launch {
                contractViewModel.fetchContractsByTenantForContractFragment(userId, setOf(ContractStatus.ACTIVE))
                contractViewModel.fetchContractsByTenantForContractFragment(userId, setOf(ContractStatus.PENDING))
                contractViewModel.fetchContractsByTenantForContractFragment(userId, setOf(ContractStatus.EXPIRED))
                contractViewModel.fetchContractsByTenantForContractFragment(userId, setOf(ContractStatus.TERMINATED))

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
        // Giải phóng binding để tránh rò rỉ bộ nhớ
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