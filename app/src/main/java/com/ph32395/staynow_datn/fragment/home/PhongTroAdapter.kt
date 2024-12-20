package com.ph32395.staynow_datn.fragment.home

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.Activity.RoomDetailActivity
import com.ph32395.staynow_datn.Model.PhongTroModel
import com.ph32395.staynow_datn.databinding.ItemRoomBinding
import com.ph32395.staynow_datn.hieunt.widget.tap
import java.util.Date

class PhongTroAdapter(
    private var roomList: MutableList<Pair<String, PhongTroModel>>,
    private val viewmodel: HomeViewModel
) : RecyclerView.Adapter<PhongTroAdapter.RoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoomViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val (room, roomId) = roomList[position]
        holder.bind(roomId, room)

    }

    override fun getItemCount(): Int = roomList.size

    //    Cap nhat danh sach phong tro
    @SuppressLint("NotifyDataSetChanged")
    fun updateRoomList(newRoomList: List<Pair<String, PhongTroModel>>) {
        roomList.clear()
        roomList.addAll(newRoomList)
        notifyItemRangeChanged(0, roomList.size)
    }

    //    Lay thoi gian tao phong
    fun getFormattedTimeCustom(thoiGianTaoPhong: Long?): String {
        if (thoiGianTaoPhong == null || thoiGianTaoPhong == 0L) return "Không có thời gian"
        val prettyTime = PrettyTimeHelper.createCustomPrettyTime()
        val date = Date(thoiGianTaoPhong)
        return prettyTime.format(date)
    }

    inner class RoomViewHolder(itemView: ItemRoomBinding) : RecyclerView.ViewHolder(itemView.root) {
        private val roomImage: ImageView = itemView.imgPhongTro
        private val roomName: TextView = itemView.tvTenPhongTro
        private val roomAddress: TextView = itemView.tvDiaChi
        private val roomPrice: TextView = itemView.tvGiaThue
        private val roomArea: TextView = itemView.tvDienTich
        private val roomViews: TextView = itemView.tvSoLuotXem
        private val roomTime: TextView = itemView.tvTgianTao
        private var loaiTaiKhoan: String = ""
        private val firestore = FirebaseFirestore.getInstance()

        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(room: PhongTroModel, roomId: String) {
            // Cập nhật ảnh phòng trọ
            Glide.with(itemView.context)
                .load(room.imageUrls[0])
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(16)))
                .into(roomImage)

            // Cập nhật tên phòng trọ
            roomName.text = room.Ten_phongtro

            // Cập nhật địa chỉ phòng trọ
            roomAddress.text = room.Dia_chi

            // Cập nhật giá thuê
            roomPrice.text = "${room.Gia_phong.let { String.format("%,.0f", it) }} VND"

//            Hien thi dien tich
            roomArea.text = "${room.Dien_tich} m²"


            // Cập nhật số lượt xem
            roomViews.text = "${room.So_luotxemphong}"

//            Hien thi thoi gian
            val formattedTime = getFormattedTimeCustom(room.ThoiGian_taophong)

            roomTime.text = formattedTime

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (userId.equals(room.Ma_nguoidung)) {
                loaiTaiKhoan = "ManCT"
            } else {
                loaiTaiKhoan = "ManND"
            }


//            Xu ly su kien khi click item sang man chi tiet
            itemView.tap {
                //Luu thong tin phong tro da xem
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@tap
                saveRoomToHistory(userId, roomId)

                val context = itemView.context
                val intent = Intent(context, RoomDetailActivity::class.java)
                intent.putExtra("maPhongTro", roomId)
                Log.d("PTAdapter", loaiTaiKhoan)
                intent.putExtra("ManHome", loaiTaiKhoan)
                context.startActivity(intent)
            }
        }

        //        Lưu thong tin phong tro da xem
        private fun saveRoomToHistory(userId: String, roomId: String) {
            val historyRef = firestore.collection("PhongTroDaXem")
                .whereEqualTo("Id_nguoidung", userId)
                .whereEqualTo("Id_phongtro", roomId)

            historyRef.get().addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Phòng trọ đã tồn tại -> Cập nhật thời gian xem
                    val documentId = documents.documents[0].id
                    firestore.collection("PhongTroDaXem").document(documentId)
                        .update("Thoi_gianxem", System.currentTimeMillis())
                } else {
                    // Phòng trọ chưa tồn tại -> Thêm mới
                    val newHistory = mapOf(
                        "Id_nguoidung" to userId,
                        "Id_phongtro" to roomId,
                        "Thoi_gianxem" to System.currentTimeMillis()
                    )
                    firestore.collection("PhongTroDaXem").add(newHistory)
                }
            }.addOnFailureListener {
                Log.e("Firestore", "Error saving room to history", it)
            }
        }
    }
}