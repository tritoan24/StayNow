package com.ph32395.staynow.LoaiPhong

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ph32395.staynow.Interface.AdapterTaoPhongTroEnteredListenner
import com.ph32395.staynow.databinding.ItemLoaiphongBinding

class LoaiPhongAdapter(
    private val context: Context,
    private val loaiphongList: List<LoaiPhong>,
    private val listener: AdapterTaoPhongTroEnteredListenner
) : RecyclerView.Adapter<LoaiPhongAdapter.LoaiPhongViewHolder>() {

    // Biến lưu trữ vị trí loại phòng được chọn
    private var selectedPosition: Int = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoaiPhongViewHolder {
        val binding = ItemLoaiphongBinding.inflate(LayoutInflater.from(context), parent, false)
        return LoaiPhongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LoaiPhongViewHolder, position: Int) {
        val loaiphong = loaiphongList[position]
        holder.bind(loaiphong, position == selectedPosition)
    }

    override fun getItemCount(): Int = loaiphongList.size

    inner class LoaiPhongViewHolder(private val binding: ItemLoaiphongBinding) : RecyclerView.ViewHolder(binding.root) {

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
                    listener.onLoaiPhongSelected(loaiphongList[adapterPosition], true)
                }
            }
        }

        fun bind(loaiphong: LoaiPhong, isSelected: Boolean) {
            binding.tenLoaiPhong.text = loaiphong.Ten_loaiphong
            binding.root.isSelected = isSelected
        }
    }
}
