package com.ph32395.staynow.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ph32395.staynow.Model.ChiTietThongTinModel
import com.ph32395.staynow.R
import java.text.NumberFormat
import java.util.Locale

class ChiTietThongTinAdapter(
    private val chiTietList: List<ChiTietThongTinModel>
) : RecyclerView.Adapter<ChiTietThongTinAdapter.ChiTietViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChiTietViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chitiet_thongtin, parent, false)
        return ChiTietViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChiTietViewHolder, position: Int) {
        val chiTiet = chiTietList[position]
        holder.bind(chiTiet)
    }

    override fun getItemCount(): Int = chiTietList.size

    inner class ChiTietViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconThongTin: ImageView = itemView.findViewById(R.id.iconChiTietThongTin)
        private val tenThongTin: TextView = itemView.findViewById(R.id.txtTenThongTin)
        private val soLuongDonVi: TextView = itemView.findViewById(R.id.txtSoLuongDonVi)
        private val donVi: TextView = itemView.findViewById(R.id.txtDonVi)

        fun bind(chiTiet: ChiTietThongTinModel) {
            Glide.with(itemView.context).load(chiTiet.icon_thongtin).into(iconThongTin)
            tenThongTin.text = chiTiet.ten_thongtin
            val soLuong= chiTiet.so_luong_donvi.toString()
            if (soLuong.length >= 5) {
//                Format thanh VND
                val formattedAmount = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).apply {
                    maximumFractionDigits = 0 // Khong hien thi so thap pha
                }.format(soLuong.toLong())
                soLuongDonVi.text = formattedAmount
            } else {
                soLuongDonVi.text = soLuong
            }
            donVi.text = chiTiet.don_vi
        }
    }
}