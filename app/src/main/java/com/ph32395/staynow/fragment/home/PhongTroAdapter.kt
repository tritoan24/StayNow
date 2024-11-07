package com.ph32395.staynow.fragment.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ph32395.staynow.Model.PhongTro
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.ItemRoomBinding

class PhongTroAdapter(private var roomList: List<PhongTro>) : RecyclerView.Adapter<PhongTroAdapter.RoomViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding=ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = roomList[position]
        holder.bind(room)
    }

    override fun getItemCount(): Int = roomList.size

    inner class RoomViewHolder(itemView: ItemRoomBinding) : RecyclerView.ViewHolder(itemView.root) {
        private val roomImage: ImageView = itemView.imgPhongTro
        private val roomName: TextView = itemView.tvTenPhongTro
        private val roomAddress: TextView = itemView.tvDiaChi
        private val roomPrice: TextView = itemView.tvGiaThue
        private val roomArea: TextView = itemView.tvDienTich
        private val roomViews: TextView = itemView.tvSoLuotXem

        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(room: PhongTro) {
            // Cập nhật ảnh phòng trọ
            Glide.with(itemView.context)
                .load(room.imageRoom)
                .into(roomImage)

            // Cập nhật tên phòng trọ
            roomName.text = room.tenPhongTro

            // Cập nhật địa chỉ phòng trọ
            roomAddress.text = room.diaChi

            // Cập nhật giá thuê
            roomPrice.text = "Từ ${room.giaThue?.let { String.format("%,.0f", it) }} VND"

            // Cập nhật diện tích
            roomArea.text = " ${room.dienTich?.let { String.format("%.1f", it) }} m²"

            // Cập nhật số lượt xem
            roomViews.text = "${room.soLuotXem}"
        }
    }
}
