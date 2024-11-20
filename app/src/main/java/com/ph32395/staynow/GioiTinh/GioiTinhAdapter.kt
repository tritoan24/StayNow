package com.ph32395.staynow.NoiThat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ph32395.staynow.GioiTinh.GioiTinh
import com.ph32395.staynow.Interface.AdapterTaoPhongTroEnteredListenner
import com.ph32395.staynow.LoaiPhong.LoaiPhong
import com.ph32395.staynow.databinding.ItemGioitinhBinding
import com.ph32395.staynow.databinding.ItemNoithatBinding

class GioiTinhAdapter(
    private val context: Context,
    private val gioitinhList: List<GioiTinh>,
    private val listener: AdapterTaoPhongTroEnteredListenner,

    private var selectedPosition: Int = RecyclerView.NO_POSITION

) : RecyclerView.Adapter<GioiTinhAdapter.GioiTinhViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GioiTinhViewHolder {
        val binding = ItemGioitinhBinding.inflate(LayoutInflater.from(context), parent, false)
        return GioiTinhViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GioiTinhViewHolder, position: Int) {
        val gioitinh = gioitinhList[position]
        holder.bind(gioitinh, position == selectedPosition)
    }

    override fun getItemCount(): Int = gioitinhList.size

    inner class GioiTinhViewHolder(private val binding: ItemGioitinhBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {

                // Kiểm tra nếu item đã chọn khác với item hiện tại
                if (selectedPosition != adapterPosition) {
                    // Cập nhật vị trí được chọn và thông báo thay đổi
                    val previousPosition = selectedPosition
                    selectedPosition = adapterPosition

                    // Thông báo cập nhật giao diện cho item cũ và mới
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(selectedPosition)

                    // Gọi listener để truyền dữ liệu về Activity
                    listener.onGioiTinhSelected(gioitinhList[adapterPosition], true)
                }
            }
        }

        fun bind(gioitinh: GioiTinh) {
            binding.tenGioiTinh.text = gioitinh.Ten_gioitinh
            Glide.with(context)
                .load(gioitinh.ImgUrl_gioitinh)
                .into(binding.iconGioiTinh)

            // Set trạng thái selected
            binding.root.isSelected = false
}
        fun bind(gioitinh: GioiTinh, isSelected: Boolean) {
            binding.tenGioiTinh.text = gioitinh.Ten_gioitinh
            Glide.with(context)
                .load(gioitinh.ImgUrl_gioitinh)
                .into(binding.iconGioiTinh)
            binding.root.isSelected = isSelected
        }
    }
}
