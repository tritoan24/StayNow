package com.ph32395.staynow.fragment.contract_tenant

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ph32395.staynow.MainActivity
import com.ph32395.staynow.TaoHopDong.ContractViewModel
import com.ph32395.staynow.databinding.FragmentContractBinding

class ContractFragment : Fragment() {

    // Đối tượng Binding
    private var _binding: FragmentContractBinding? = null
    private val binding get() = _binding!!

    private lateinit var contractViewModel: ContractViewModel
    private lateinit var contractAdapter: ContractAdapter // Adapter cho RecyclerView

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
        contractAdapter = ContractAdapter(viewmodel = contractViewModel)
        binding.rvPendingContracts.layoutManager = LinearLayoutManager(context)
        binding.rvPendingContracts.adapter = contractAdapter

        // Khởi tạo FirebaseAuth và lấy userId
        mAuth = FirebaseAuth.getInstance()
        val userId = mAuth.currentUser?.uid
        Log.d("ContractFragment", "User ID: $userId")

        // Khởi tạo Adapter
        contractAdapter = ContractAdapter(viewmodel = contractViewModel)

        // Thiết lập RecyclerView
        binding.rvActiveContracts.apply {
            adapter = contractAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Quan sát danh sách hợp đồng
        contractViewModel.contracts.observe(viewLifecycleOwner) { contracts ->
            // Cập nhật adapter khi có dữ liệu
            contractAdapter.updateContractList(contracts)
        }

        // Gọi hàm lấy dữ liệu
        contractViewModel.fetchAllContracts()
        // Trả về root view của binding
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Giải phóng binding để tránh rò rỉ bộ nhớ
        _binding = null

        // Nếu activity là MainActivity, có thể gọi phương thức này để điều chỉnh hiển thị bottom navigation
        if (activity is MainActivity) {
            (activity as MainActivity).setBottomNavigationVisibility(true)
        }
    }
}