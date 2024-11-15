package com.ph32395.staynow.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ph32395.staynow.Model.TienNghiModel
import com.ph32395.staynow.R

class TienNghiAdapter (
    private val tienNghiList: List<TienNghiModel>
) : RecyclerView.Adapter<TienNghiAdapter.TienNghiViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TienNghiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tien_nghi, parent, false)
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

        fun bind(tienNghi: TienNghiModel) {
            Glide.with(itemView.context).load(tienNghi.Icon_tiennghi).into(iconTienNghi)
            tenTienNghi.text = tienNghi.Ten_tiennghi
        }
    }
}