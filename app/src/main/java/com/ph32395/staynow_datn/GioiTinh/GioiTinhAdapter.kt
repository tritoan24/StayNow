package com.ph32395.staynow_datn.NoiThat

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ph32395.staynow_datn.GioiTinh.GioiTinh
import com.ph32395.staynow_datn.Interface.AdapterTaoPhongTroEnteredListenner
import com.ph32395.staynow_datn.databinding.ItemGioitinhBinding

class GioiTinhAdapter(
    private val context: Context,
    public var gioitinhList: List<GioiTinh>,
    private val listener: AdapterTaoPhongTroEnteredListenner,
    private var selectedPosition: Int = RecyclerView.NO_POSITION
) : RecyclerView.Adapter<GioiTinhAdapter.GioiTinhViewHolder>() {
    private val selected = mutableSetOf<GioiTinh>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GioiTinhViewHolder {
        val binding = ItemGioitinhBinding.inflate(LayoutInflater.from(context), parent, false)
        return GioiTinhViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GioiTinhViewHolder, position: Int) {
        val gioitinh = gioitinhList[position]
        holder.bind(gioitinh, position == selectedPosition)
    }

    override fun getItemCount(): Int = gioitinhList.size

    fun selectById(maGioiTinh: String) {
        val selectedPosition = gioitinhList.indexOfFirst { it.maGioiTinh == maGioiTinh }
        gioitinhList.forEach {
        }

        if (selectedPosition != -1) {
            setSelectedPosition(selectedPosition)
        } else {
            Log.e("GioiTinhAdapter", "Không tìm thấy giới tính với ID: $maGioiTinh")
        }
    }


    // Thay đổi trạng thái selected từ bên ngoài
    fun setSelectedPosition(position: Int) {
        if (position != selectedPosition) {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    inner class GioiTinhViewHolder(private val binding: ItemGioitinhBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (selectedPosition != adapterPosition) {
                    val previousPosition = selectedPosition
                    selectedPosition = adapterPosition
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(selectedPosition)
                    listener.onGioiTinhSelected(gioitinhList[adapterPosition], true)
                }
            }
        }

        fun bind(gioitinh: GioiTinh, isSelected: Boolean) {
            binding.tenGioiTinh.text = gioitinh.tenGioiTinh
            Glide.with(context)
                .load(gioitinh.imgUrlGioiTinh)
                .into(binding.iconGioiTinh)
            binding.root.isSelected = isSelected
        }
    }
//    // Phương thức để đánh dấu các nội thất đã chọn trước đó
//    fun setSelected(selectedList: List<GioiTinh>) {
//        selected.clear()
//        selected.addAll(selectedList)
//        notifyDataSetChanged()
//    }
}
