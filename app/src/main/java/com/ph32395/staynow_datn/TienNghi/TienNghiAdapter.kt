package com.ph32395.staynow_datn.TienNghi

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ph32395.staynow_datn.Interface.AdapterTaoPhongTroEnteredListenner
import com.ph32395.staynow_datn.NoiThat.NoiThat
import com.ph32395.staynow_datn.databinding.ItemTiennghiBinding

class TienNghiAdapter(
    private val context: Context,
    private val listTiennghi: List<TienNghi>,
    private val listener: AdapterTaoPhongTroEnteredListenner,
    private val selectedItems: MutableSet<String> = mutableSetOf()
) : RecyclerView.Adapter<TienNghiAdapter.TienNghiViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TienNghiViewHolder {
        val binding = ItemTiennghiBinding.inflate(LayoutInflater.from(context), parent, false)
        return TienNghiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TienNghiViewHolder, position: Int) {
        val tienNghi = listTiennghi[position]
        val isItemSelected = selectedItems.contains(tienNghi.maTienNghi.toString())
        holder.bind(tienNghi, isItemSelected)
    }

    override fun getItemCount(): Int = listTiennghi.size

    inner class TienNghiViewHolder(private val binding: ItemTiennghiBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val tienNghi = listTiennghi[adapterPosition]
                val maTienNghi = tienNghi.maTienNghi.toString()

                // Toggle selection
                if (selectedItems.contains(maTienNghi)) {
                    selectedItems.remove(maTienNghi)
                    binding.root.isSelected = false
                    listener.onTienNghiSelected(tienNghi, false)
                } else {
                    selectedItems.add(maTienNghi)
                    binding.root.isSelected = true
                    listener.onTienNghiSelected(tienNghi, true)
                }
                notifyItemChanged(adapterPosition)
            }
        }

        fun bind(tienNghi: TienNghi, isSelected: Boolean) {
            binding.apply {
                tiennghiName.text = tienNghi.tenTienNghi
                Glide.with(context)
                    .load(tienNghi.iconTienNghi)
                    .into(tiennghiImage)

                // Set trạng thái selected dựa trên danh sách đã chọn
                root.isSelected = isSelected
            }
        }
    }

    fun updateSelectedItems(selected: List<com.ph32395.staynow_datn.SuaPhongTro.TienNghi>?) {
        selectedItems.clear()
        selected?.forEach { selectedItems.add(it.maTienNghi.toString()) }
        notifyDataSetChanged()
    }
    fun getSelectedTienNghi(): List<TienNghi> {
        return listTiennghi.filter { selectedItems.contains(it.maTienNghi.toString()) }
    }
}
