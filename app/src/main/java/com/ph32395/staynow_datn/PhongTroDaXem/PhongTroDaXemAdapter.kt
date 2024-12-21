package com.ph32395.staynow_datn.PhongTroDaXem

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.Activity.RoomDetailActivity
import com.ph32395.staynow_datn.Model.PhongTroModel
import com.ph32395.staynow_datn.QuanLyPhongTro.custom.CustomConfirmationDialog
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.fragment.home.PrettyTimeHelper
import java.util.Date

class PhongTroDaXemAdapter : ListAdapter<PhongTroModel,
        PhongTroDaXemAdapter.RoomViewHolder>(DIFF_CALLBACK) {

//    So sanh xem hai doi tuong c giong nhau khong
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PhongTroModel>() {
            override fun areItemsTheSame(oldItem: PhongTroModel, newItem: PhongTroModel) = oldItem.maNguoiDung == newItem.maNguoiDung
            override fun areContentsTheSame(oldItem: PhongTroModel, newItem: PhongTroModel) = oldItem == newItem
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_phong_tro_da_xem, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val roomName = itemView.findViewById<TextView>(R.id.txtTenPhongTroDaXem)
        private val roomAddress = itemView.findViewById<TextView>(R.id.txtDiaChiDaXem)
        private val roomPrice = itemView.findViewById<TextView>(R.id.tvGiaThueDaXem)
        private val roomImage = itemView.findViewById<ImageView>(R.id.imagePhongTroDaXem)
        private val txtThoiGianDaXem = itemView.findViewById<TextView>(R.id.txtThoiGianDaXem)
        private val btnDelete = itemView.findViewById<ImageView>(R.id.btnDeleteDaXem)

        fun bind(roomModel: PhongTroModel) {

            roomName.text = roomModel.Ten_phongtro
            roomAddress.text = roomModel.Dia_chi
            roomPrice.text = "${roomModel.Gia_phong.let { String.format("%,.0f", it) }} VND"


            if (roomModel.imageUrls.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(roomModel.imageUrls[0])
                    .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(16)))
                    .into(roomImage)
            }

            // Hiển thị thời gian đã xem
            roomModel.Thoi_gianxem?.let {
                val prettyTime = PrettyTimeHelper.createCustomPrettyTime()
                val date = Date(it)
                txtThoiGianDaXem.text = prettyTime.format(date)
            } ?: run {
                txtThoiGianDaXem.text = "Chưa xác định"
            }

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, RoomDetailActivity::class.java)
                intent.putExtra("maPhongTro", roomModel.Ma_phongtro)
                Log.d("PhongTroDaXemAdapter", "Ma phong tro: ${roomModel.Ma_phongtro}")
                itemView.context.startActivity(intent)
            }

//            Xu ly nut xoa
            btnDelete.setOnClickListener {
//                Hien thii hop thoai
                val dialog = CustomConfirmationDialog(
                    message = "Bạn có chắc chắn muốn xóa khỏi lịch sử xem phòng không?",
                    onConfirm = {
                        deleteRoom(roomModel)

                    },
                    onCancel = {

                    }
                )
                dialog.show((itemView.context as AppCompatActivity).supportFragmentManager, "CustomConfirmationDialog")
            }
        }

        private fun deleteRoom(roomModel: PhongTroModel) {
            val firestore = FirebaseFirestore.getInstance()

//            Lay id nguoi dung din tai
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            if (currentUserId == null) {
                return
            }

            // Xóa phòng trọ khỏi Firestore trong bảng "PhongTroDaXem" kiem tra id Phong va Id User
            firestore.collection("PhongTroDaXem")
                .whereEqualTo("Id_phongtro", roomModel.Ma_phongtro)
                .whereEqualTo("Id_nguoidung", currentUserId) //Kiem tra id nguoi dung
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        firestore.collection("PhongTroDaXem")
                            .document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                // Cập nhật UI sau khi xóa thành công
                                Log.d("PhongTroDaXemAdapter", "Đã xóa phòng trọ thành công.")
                            }
                            .addOnFailureListener { exception ->
                                Log.e("PhongTroDaXemAdapter", "Lỗi xóa phòng trọ", exception)
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("PhongTroDaXemAdapter", "Lỗi tìm phòng trọ để xóa", exception)
                }
        }
    }
}
