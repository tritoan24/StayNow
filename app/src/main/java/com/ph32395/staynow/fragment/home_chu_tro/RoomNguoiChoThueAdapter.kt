package com.ph32395.staynow.fragment.home_chu_tro

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow.Activity.RoomDetailActivity

import com.ph32395.staynow.Model.PhongTroModel
import com.ph32395.staynow.R
import com.ph32395.staynow.fragment.home.HomeViewModel
import com.ph32395.staynow.fragment.home.PrettyTimeHelper
import java.util.Date

class RoomNguoiChoThueAdapter(
    private val viewModel: HomeViewModel
) : ListAdapter<Pair<String, PhongTroModel>,
        RoomNguoiChoThueAdapter.RoomViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room_doi_tac, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = getItem(position)
        holder.bind(room)

//    Lang nghe su thay doi dien tich tu LiveData
        viewModel.roomList.observe(holder.itemView.context as LifecycleOwner) { rooms ->
            val updateRoom = rooms.find { it.first == room.first }
            updateRoom?.let {
                holder.updateArea(it.second.Dien_tich)
            }
        }
    }

    fun getFormattedTimeCustom(thoiGianTaoPhong: Long?): String {
        if (thoiGianTaoPhong == null || thoiGianTaoPhong == 0L) return "Không có thời gian"
        val prettyTime = PrettyTimeHelper.createCustomPrettyTime()
        val date = Date(thoiGianTaoPhong)
        return prettyTime.format(date)
    }

    inner class RoomViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val areaTextView: TextView = view.findViewById(R.id.txtDienTich)
        private val txtThoiGianTao: TextView = view.findViewById(R.id.txtThoiGianTao)
        private var loaiTaiKhoan: String = ""
        private val firestore = FirebaseFirestore.getInstance()

        fun bind(room: Pair<String, PhongTroModel>) {
            val roomModel = room.second
            val roomId = room.first //lay id phong tro tu Pair

            // Cập nhật ảnh phòng trọ (kiểm tra danh sách imageUrls trước khi truy cập)
            val imageView = view.findViewById<ImageView>(R.id.imagePhongTro)
            if (roomModel.imageUrls.isNullOrEmpty()) {
                // Hiển thị ảnh mặc định nếu imageUrls rỗng
                imageView.setImageResource(R.drawable.image_otp) // Thay bằng ID ảnh mặc định của bạn
            } else {
                // Hiển thị ảnh đầu tiên trong danh sách
                Glide.with(itemView.context)
                    .load(roomModel.imageUrls[0])
                    .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(16)))
                    .into(imageView)
            }
            view.findViewById<TextView>(R.id.txtTenPhongTro).text = roomModel.Ten_phongtro
            view.findViewById<TextView>(R.id.tvGiaThue).text = "${roomModel.Gia_phong.let { String.format("%,.0f", it) }} VND"
            view.findViewById<TextView>(R.id.txtDiaChiHome).text = roomModel.Dia_chi
            view.findViewById<TextView>(R.id.txtSoLuotXem).text = roomModel.So_luotxemphong.toString()

//            cap nhat thoi gian
            val formattedTime = getFormattedTimeCustom(roomModel.ThoiGian_taophong)
            txtThoiGianTao.text = formattedTime
            // Cập nhật diện tích (m²)
            areaTextView.text = "${roomModel.Dien_tich} m²"

            val userId = FirebaseAuth.getInstance().currentUser?.uid?: ""
            if (userId.equals(roomModel.Ma_nguoidung)) {
                loaiTaiKhoan = "ManCT"
            } else {
                loaiTaiKhoan = "ManND"
            }
//            Su kien chuyen sang man chi tiet
            itemView.setOnClickListener {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                saveRoomToHistory(userId, roomId)

                val context = itemView.context
                val intent = Intent(context, RoomDetailActivity::class.java)
                intent.putExtra("maPhongTro", roomId)
                intent.putExtra("ManHome", loaiTaiKhoan)
                context.startActivity(intent)

//                goi view model de tang so luot xem
                viewModel.incrementRoomViewCount(roomId)
            }
        }

        //        Lưu thong tin phong tro da xem
        fun saveRoomToHistory(userId: String, roomId: String) {
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

        //        Cap nhat dien tich
        fun updateArea(area: Long?) {
            areaTextView.text = "${area ?: 0.0f} m²"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Pair<String, PhongTroModel>>() {
        override fun areItemsTheSame(
            oldItem: Pair<String, PhongTroModel>,
            newItem: Pair<String, PhongTroModel>
        ) = oldItem.first == newItem.first

        override fun areContentsTheSame(
            oldItem: Pair<String, PhongTroModel>,
            newItem: Pair<String, PhongTroModel>
        ) = oldItem == newItem
    }
}
