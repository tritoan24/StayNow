package com.ph32395.staynow_datn.quanlyhoadon

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ph32395.staynow_datn.TaoHoaDon.InvoiceViewModel
import com.ph32395.staynow_datn.TaoHopDong.ChiTietHopDong
import com.ph32395.staynow_datn.TaoHopDong.ContractViewModel
import com.ph32395.staynow_datn.TaoHopDong.HopDong
import com.ph32395.staynow_datn.TaoHopDong.InvoiceStatus
import com.ph32395.staynow_datn.databinding.FragmentInvoiceBinding
import com.ph32395.staynow_datn.hieunt.widget.tap
import java.text.NumberFormat
import java.util.Locale

@Suppress("DEPRECATION")
class InvoiceFragment : Fragment() {

    private lateinit var binding: FragmentInvoiceBinding
    private var contractId: String? = null
    private var status: InvoiceStatus? = null
    private var isLandlord: Boolean = false
    private lateinit var invoiceAdapter: BillAdapter
    private val invoiceViewModel: InvoiceViewModel by viewModels()
    private lateinit var contractViewModel: ContractViewModel

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
        contractViewModel = ViewModelProvider(this)[ContractViewModel::class.java]

        // Gọi hàm fetch dữ liệu
        val contractId = arguments?.getString("CONTRACT_ID") ?: ""

        // Quan sát LiveData contract
        contractViewModel.contract.observe(viewLifecycleOwner, Observer { contract ->
            if (contract != null) {
                binding.cardViewBill.visibility = View.VISIBLE
                updateUI(contract)
            } else {
                binding.cardViewBill.visibility = View.GONE
            }
        })

        // Gọi fetchContract để lấy hợp đồng
        contractViewModel.fetchContract(contractId, status!!)

        status?.let {
            invoiceViewModel.fetchInvoicesByContractIdAndStatus(contractId, it)
            Log.d("InvoiceFragment", "Fetching invoices with contractId: $contractId, status: $it")
        }

        //navigate đối với hóa đơn hợp đồng
        binding.cardViewBill.tap {
            val intent = Intent(context, ChiTietHopDong::class.java)
            intent.putExtra("CONTRACT_ID", contractId)
            intent.putExtra("detail", "detail")
            startActivity(intent)
        }

    }

    @SuppressLint("SetTextI18n")
    fun updateUI(contract: HopDong) {
        binding.tvBillId.text = "ID: ${contract.hoaDonHopDong.idHoaDon}"
        binding.tvCustomerName.text = contract.nguoiThue.hoTen
        binding.tvBillType.text = "Kiểu hóa đơn: ${contract.hoaDonHopDong.kieuHoadon}"
        binding.tvTotalAmount.text = "Tổng tiền: " + formatCurrency(contract.hoaDonHopDong.tongTien)
        binding.tvDate.text = "Ngày: " + contract.hoaDonHopDong.ngayLap
        binding.tvStatus.text = "Trạng thái: ${contract.hoaDonHopDong.trangThai}"
    }

    // Định dạng tiền tệ
    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(amount)
    }

}
