package com.ph32395.staynow.quanlyhoadon

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ph32395.staynow.TaoHoaDon.InvoiceViewModel
import com.ph32395.staynow.TaoHopDong.InvoiceStatus
import com.ph32395.staynow.databinding.FragmentInvoiceBinding

@Suppress("DEPRECATION")
class InvoiceFragment : Fragment() {

    private lateinit var binding: FragmentInvoiceBinding
    private var contractId: String? = null
    private var status: InvoiceStatus? = null
    private var isLandlord: Boolean = false
    private lateinit var invoiceAdapter: BillAdapter
    private val invoiceViewModel: InvoiceViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInvoiceBinding.inflate(inflater, container, false)

        // Lấy contractId và status từ Bundle
        contractId = arguments?.getString("CONTRACT_ID")
        status = arguments?.getSerializable("STATUS") as? InvoiceStatus
        isLandlord = arguments?.getBoolean("isLandlord") == false
        return binding.root
    }

    companion object {
        fun newInstance(
            contractId: String,
            status: InvoiceStatus,
            isLandlord: Boolean
        ): InvoiceFragment {
            val fragment = InvoiceFragment()
            val bundle = Bundle()
            bundle.putString("CONTRACT_ID", contractId)
            bundle.putBoolean("isLandlord", isLandlord)
            bundle.putSerializable("STATUS", status)
            Log.d(
                "InvoiceFragment",
                "Creating fragment with contractId: $contractId, status: $status"
            )
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo adapter với status
        if (status == null) {
            status = InvoiceStatus.PAID
        }
        invoiceAdapter = BillAdapter(status!!, isLandlord, null)
        // Thiết lập RecyclerView

        binding.recyclerViewInvoices.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = invoiceAdapter
        }

        // Quan sát LiveData từ ViewModel
        invoiceViewModel.invoices.observe(viewLifecycleOwner) { invoices ->
            // Cập nhật danh sách vào adapter
            invoiceAdapter.submitList(invoices)
        }

        // Gọi hàm fetch dữ liệu
        val contractId = arguments?.getString("CONTRACT_ID") ?: ""
        status?.let {
            invoiceViewModel.fetchInvoicesByContractIdAndStatus(contractId, it)
            Log.d("InvoiceFragment", "Fetching invoices with contractId: $contractId, status: $it")
        }


    }
}
