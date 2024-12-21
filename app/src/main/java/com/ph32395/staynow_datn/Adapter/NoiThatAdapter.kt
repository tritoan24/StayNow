package com.ph32395.staynow_datn.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ph32395.staynow_datn.Model.NoiThatModel
import com.ph32395.staynow_datn.R

class NoiThatAdapter(
    private val noiThatlist: List<NoiThatModel>
) : RecyclerView.Adapter<NoiThatAdapter.NoiThatViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NoiThatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_noi_that_chitiet, parent, false)
        return  NoiThatViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoiThatViewHolder, position: Int) {
        val noiThat = noiThatlist[position]
        holder.bind(noiThat)
    }

    override fun getItemCount(): Int = noiThatlist.size

    inner class NoiThatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconNoiThat: ImageView = itemView.findViewById(R.id.iconNoiThat)
        private val tenNoiThat: TextView = itemView.findViewById(R.id.txtTenNoiThat)

        fun bind(noiThat: NoiThatModel) {
            Glide.with(itemView.context).load(noiThat.Icon_noithat).into(iconNoiThat)
            tenNoiThat.text = noiThat.Ten_noithat
        }
    }
}