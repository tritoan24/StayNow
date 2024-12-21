package com.ph32395.staynow_datn.TaoHoaDon


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ph32395.staynow_datn.TaoHopDong.UtilityFeeDetail
import com.ph32395.staynow_datn.databinding.ItemVariableFeeInvoiceBinding
import java.text.NumberFormat
import java.util.Locale

class InvoiceVariableFeesAdapter(
    private val fees: List<UtilityFeeDetail>
) : RecyclerView.Adapter<InvoiceVariableFeesAdapter.ViewHolder>() {
    private var electricityQuantity: Double = 0.0
    private var waterQuantity: Double = 0.0

    // Một interface để trả thành tiền về activity
    interface OnCalculationCompleteListener {
        fun onCalculationComplete(calculatedFees: List<Double>)
    }

    var calculationListener: OnCalculationCompleteListener? = null

    fun updateQuantities(electricityQuantity: Double, waterQuantity: Double) {
        this.electricityQuantity = electricityQuantity
        this.waterQuantity = waterQuantity
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemVariableFeeInvoiceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVariableFeeInvoiceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fee = fees[position]
        with(holder.binding) {
            tvSTT.text = (position + 1).toString()
            tvTenDichVu.text = fee.tenDichVu
            tvDonGia.text = formatCurrency(fee.giaTien)
            tvDonVi.text = "/${fee.donVi}"

            // Tính toán thành tiền
            var thanhTien = 0.0
            when (fee.tenDichVu.lowercase()) {
                "điện" -> {
                    thanhTien = fee.giaTien * electricityQuantity
                }
                "nước" -> {
                    thanhTien = fee.giaTien * waterQuantity
                }
                else -> {
                    // Các phí khác
                    thanhTien = fee.giaTien
                }
            }

            tvThanhTien.text = formatCurrency(thanhTien)
        }

        // Gọi listener để trả về danh sách thành tiền
        val calculatedFees = fees.map { fee ->
            when (fee.tenDichVu.lowercase()) {
                "điện" -> fee.giaTien * electricityQuantity
                "nước" -> fee.giaTien * waterQuantity
                else -> fee.giaTien
            }
        }
        calculationListener?.onCalculationComplete(calculatedFees)
    }

    override fun getItemCount() = fees.size

    private fun formatCurrency(amount: Number): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(amount)
    }
}