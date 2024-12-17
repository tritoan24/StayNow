package com.ph32395.staynow_datn.TaoHopDong.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.TaoHopDong.UtilityFeeDetail
import java.text.NumberFormat
import java.util.Locale
class FixedFeeAdapter(private val data: List<UtilityFeeDetail>) :
    RecyclerView.Adapter<FixedFeeAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSTT: TextView = itemView.findViewById(R.id.tvSTT)
        val tvTenDichVu: TextView = itemView.findViewById(R.id.tvTenDichVu)
        val tvDonGia: TextView = itemView.findViewById(R.id.tvDonGia)
        val tvThanhTien: TextView = itemView.findViewById(R.id.tvThanhTien)
        val tvSoLuong: TextView = itemView.findViewById(R.id.tvSoLuong)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fixed_fee, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.tvSTT.text = (position + 1).toString()
        holder.tvTenDichVu.text = item.tenDichVu
        holder.tvDonGia.text = formatCurrency(item.giaTien)
        holder.tvThanhTien.text =formatCurrency(item.thanhTien)
        holder.tvSoLuong.text = item.soLuong.toString()
    }

    override fun getItemCount() = data.size

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(amount)
    }
}