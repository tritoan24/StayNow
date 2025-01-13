package com.ph32395.staynow_datn.aiGenmini

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.ph32395.staynow_datn.Model.PhongTroModel
import com.ph32395.staynow_datn.databinding.ItemRoomBinding

class RecommendedRoomsAdapter(
    private val roomList: MutableList<Pair<String, PhongTroModel>> = mutableListOf(),
    private val onRoomClick: (String, PhongTroModel) -> Unit
) : RecyclerView.Adapter<RecommendedRoomsAdapter.RoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemRoomBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val (roomId, room) = roomList[position]
        holder.bind(roomId, room)
    }

    override fun getItemCount(): Int = roomList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateRoomList(newRoomList: List<Pair<String, PhongTroModel>>) {
        roomList.clear()
        roomList.addAll(newRoomList)
        notifyDataSetChanged()
    }

    inner class RoomViewHolder(private val binding: ItemRoomBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(roomId: String, room: PhongTroModel) {
            binding.apply {
                // Tải ảnh phòng
                Glide.with(itemView.context)
                    .load(room.imageUrls.firstOrNull())
                    .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(8)))
                    .into(imgPhongTro)

                // Hiển thị thông tin phòng
                tvTenPhongTro.text = room.tenPhongTro
                tvGiaThue.text = "${String.format("%,.0f", room.giaPhong)} VND"
                tvDiaChi.text = "${room.dienTich} m²"
                tvDiaChi.text = room.diaChi

                // Xử lý sự kiện click
                root.setOnClickListener {
                    onRoomClick(roomId, room)
                }
            }
        }
    }
}