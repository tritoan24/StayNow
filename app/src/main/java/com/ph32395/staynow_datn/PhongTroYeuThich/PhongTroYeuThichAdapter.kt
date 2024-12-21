package com.ph32395.staynow_datn.PhongTroYeuThich

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.ph32395.staynow_datn.Activity.RoomDetailActivity
import com.ph32395.staynow_datn.Model.PhongTroModel
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.fragment.home.PrettyTimeHelper
import com.ph32395.staynow_datn.hieunt.widget.tap
import java.util.Date

class PhongTroYeuThichAdapter(
    private val favoriteList: MutableList<Pair<String, PhongTroModel>>,
    private val onUnfavoriteClick: (PhongTroModel) -> Unit,
) : RecyclerView.Adapter<PhongTroYeuThichAdapter.FavoriteViewHolder>() {

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val roomName = itemView.findViewById<TextView>(R.id.txtTenPhongTroYeuThich)
        private val roomPrice = itemView.findViewById<TextView>(R.id.tvGiaThueYeuThich)
        private val roomAddress = itemView.findViewById<TextView>(R.id.txtDiaChiYeuThich)
        private val favoriteTime = itemView.findViewById<TextView>(R.id.txtThoiGianYeuThich)
        private val roomImage = itemView.findViewById<ImageView>(R.id.imagePhongTroYeuThich)
        private val unfavoriteIcon = itemView.findViewById<ImageView>(R.id.iconFavorite)
        private var maPhongTro = ""

        fun bind(room: PhongTroModel, roomId: String) {
            roomName.text = room.Ten_phongtro
            roomPrice.text = "${String.format("%,.0f", room.Gia_phong)} VND"
            roomAddress.text = room.Dia_chi

            Log.d("PhongTroYeuThichAdapter", "Ten phong tro: ${room.Ten_phongtro}")
            Log.d("PhongTroYeuThichAdapter", "Gia phong tro: ${room.Gia_phong}")
            Log.d("PhongTroYeuThichAdapter", "Dia chi: ${room.Dia_chi}")

            // Hiển thị ảnh
            if (room.imageUrls.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(room.imageUrls[0])
                    .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(16)))
                    .into(roomImage)
            }

            // Hiển thị thời gian yêu thích
            room.Thoigian_yeuthich?.let {
                val prettyTime = PrettyTimeHelper.createCustomPrettyTime()
                favoriteTime.text = prettyTime.format(Date(it))
            }
            maPhongTro = room.Ma_phongtro
            Log.d("PhongTroYeuThichAdapter", "Ma phong tro: $maPhongTro")

            //            Xu ly su kien khi click item sang man chi tiet
            itemView.tap {
                val context = itemView.context
                val intent = Intent(context, RoomDetailActivity::class.java)
                intent.putExtra("maPhongTro", roomId)
                context.startActivity(intent)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_phong_tro_yeu_thich, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val (roomId, roomModel) = favoriteList[position]
        holder.bind(roomModel, roomId)
    }

    override fun getItemCount() = favoriteList.size
}
