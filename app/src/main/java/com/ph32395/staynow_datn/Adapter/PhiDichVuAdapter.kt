package com.ph32395.staynow_datn.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ph32395.staynow_datn.Model.PhiDichVuModel
import com.ph32395.staynow_datn.R

class PhiDichVuAdapter(
    private val phiDichVuList: List<PhiDichVuModel>
) : RecyclerView.Adapter<PhiDichVuAdapter.PhiViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PhiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_phi_dichvu, parent, false)
        return PhiViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhiViewHolder, position: Int) {
        val phiDichVu = phiDichVuList[position]
        holder.bind(phiDichVu)
    }

    override fun getItemCount(): Int = phiDichVuList.size

    inner class PhiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconDichVu: ImageView = itemView.findViewById(R.id.iconPhiDichVu)
        private val tenDichVu: TextView = itemView.findViewById(R.id.txtTenDichVu)
        private val soTien: TextView = itemView.findViewById(R.id.txtSoTien)
        private val donViPhiDichVu: TextView = itemView.findViewById(R.id.txtDonViDichVu)

        fun bind(phiDichVu: PhiDichVuModel) {
            Glide.with(itemView.context).load(phiDichVu.icon_dichvu).into(iconDichVu)
            tenDichVu.text = phiDichVu.ten_dichvu
            soTien.text = "${String.format("%,.0f", phiDichVu.so_tien)}đ"
            donViPhiDichVu.text = phiDichVu.don_vi
        }
    }
}