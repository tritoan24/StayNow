package com.ph32395.staynow.fragment.contract_tenant

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ph32395.staynow.MainActivity
import com.ph32395.staynow.TaoHopDong.ContractStatus
import com.ph32395.staynow.TaoHopDong.ContractViewModel
import com.ph32395.staynow.databinding.FragmentContractBinding
import kotlinx.coroutines.launch

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
    ): View? {
        // Inflate layout và gán cho binding
        _binding = FragmentContractBinding.inflate(inflater, container, false)

        // Khởi tạo ViewModel
        contractViewModel = ViewModelProvider(this).get(ContractViewModel::class.java)

        // Đổi tên tiêu đề và thêm underline cho tiêu đề hợp đồng chờ xác nhận
        val pendingContractsTitle = binding.tvPendingContractsTitle
        pendingContractsTitle.text = SpannableString("Hợp đồng chờ xác nhận").apply {
            setSpan(UnderlineSpan(), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // Khởi tạo adapter và gán vào RecyclerView
        activeAdapter = ContractAdapter(contractViewModel, ContractStatus.ACTIVE)
        pendingAdapter = ContractAdapter(contractViewModel, ContractStatus.PENDING)
        expireAdapter = ContractAdapter(contractViewModel, ContractStatus.EXPIRED)
        terminatedAdapter = ContractAdapter(contractViewModel, ContractStatus.TERMINATED)

        binding.rvPendingContracts.layoutManager = LinearLayoutManager(context)
        binding.rvPendingContracts.adapter = pendingAdapter

        binding.rvActiveContracts.layoutManager = LinearLayoutManager(context)
        binding.rvActiveContracts.adapter = activeAdapter

        // Khởi tạo FirebaseAuth và lấy userId
        mAuth = FirebaseAuth.getInstance()
        val userId = mAuth.currentUser?.uid
        Log.d("ContractFragment", "User ID: $userId")

        // Thiết lập RecyclerView
        binding.rvActiveContracts.apply {
            adapter = activeAdapter
            layoutManager = LinearLayoutManager(context)
        }

        contractViewModel.activeContracts.observe(viewLifecycleOwner) { contracts ->
            activeAdapter.updateContractList(contracts)
        }

        contractViewModel.pendingContracts.observe(viewLifecycleOwner) { contracts ->
            pendingAdapter.updateContractList(contracts)
        }


        contractViewModel.expireContracts.observe(viewLifecycleOwner) { contracts ->
            expireAdapter.updateContractList(contracts)
        }

        contractViewModel.terminatedContracts.observe(viewLifecycleOwner) { contracts ->
            terminatedAdapter.updateContractList(contracts)
        }

        if (userId != null) {
            lifecycleScope.launch {
                contractViewModel.fetchContractsByTenant(userId, ContractStatus.ACTIVE)
                contractViewModel.fetchContractsByTenant(userId, ContractStatus.PENDING)
                contractViewModel.fetchContractsByTenant(userId, ContractStatus.EXPIRED)
                contractViewModel.fetchContractsByTenant(userId, ContractStatus.TERMINATED)
            }
        }

        // Trả về root view của binding
        return binding.root
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