package com.ph32395.staynow.TienNghi

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ph32395.staynow.Interface.AdapterTaoPhongTroEnteredListenner
import com.ph32395.staynow.databinding.ItemNoithatBinding
import com.ph32395.staynow.databinding.ItemTiennghiBinding

class TienNghiAdapter(
    private val context: Context,
    private val listTiennghi: List<TienNghi>,
    private val listener: AdapterTaoPhongTroEnteredListenner
) : RecyclerView.Adapter<TienNghiAdapter.TienNghiViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TienNghiViewHolder {
        val binding = ItemTiennghiBinding.inflate(LayoutInflater.from(context), parent, false)
        return TienNghiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TienNghiViewHolder, position: Int) {
        val noiThat = listTiennghi[position]
        holder.bind(noiThat)
    }

    override fun getItemCount(): Int = listTiennghi.size

    inner class TienNghiViewHolder(private val binding: ItemTiennghiBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                // Đổi trạng thái selected
                it.isSelected = !it.isSelected
                //nếu trạng thái đang là true thì mới gọi listener
                if (it.isSelected) {
                    // Gọi listener để truyền dữ liệu về Activity
                    listener.onTienNghiSelected(listTiennghi[adapterPosition], it.isSelected)
                }
            }
        }

        fun bind(tiennghi: TienNghi) {
            binding.tiennghiName.text = tiennghi.Ten_tiennghi
            Glide.with(context)
                .load(tiennghi.Icon_tiennghi)
                .into(binding.tiennghiImage)
            // Set trạng thái selected
            binding.root.isSelected = false
        }
    }
}
