package com.ph32395.staynow.TaoHopDong.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ph32395.staynow.TaoHopDong.UtilityFeeDetail
import com.ph32395.staynow.databinding.ItemVariableFeeBinding
import java.text.NumberFormat
import java.util.Locale

class VariableFeeAdapter(private val fees: List<UtilityFeeDetail>) :
    RecyclerView.Adapter<VariableFeeAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemVariableFeeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVariableFeeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fee = fees[position]
        with(holder.binding) {
            tvSTT.text =  (position + 1).toString()
            tvTenDichVu.text = fee.tenDichVu
            tvDonGia.text = formatCurrency(fee.giaTien)
            tvDonVi.text = "/${fee.donVi}"
        }
    }

    override fun getItemCount() = fees.size

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(amount)
    }
}