package com.ph32395.staynow_datn.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.TienNghi.TienNghi

class TienNghiAdapter (
    private val tienNghiList: List<TienNghi>
) : RecyclerView.Adapter<TienNghiAdapter.TienNghiViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TienNghiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tien_nghi_chitiet, parent, false)
        return TienNghiViewHolder(view)
    }

    override fun onBindViewHolder(holder: TienNghiViewHolder, position: Int) {
        val tienNghi = tienNghiList[position]
        holder.bind(tienNghi)
    }

    override fun getItemCount(): Int = tienNghiList.size

    inner class TienNghiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconTienNghi: ImageView = itemView.findViewById(R.id.iconTienNghi)
        private val tenTienNghi: TextView = itemView.findViewById(R.id.txtTenTienNghi)

        fun bind(tienNghi: TienNghi) {
            Glide.with(itemView.context).load(tienNghi.iconTienNghi).into(iconTienNghi)
            tenTienNghi.text = tienNghi.tenTienNghi
        }
    }
}