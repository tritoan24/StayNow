package com.ph32395.staynow.QuanLyNguoiThue

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow.databinding.ItemThongTinThanhVienOTroBinding
import com.tommasoberlose.progressdialog.ProgressDialogFragment

class AdapterThanhVien(
    private val listTv: List<ThanhVien>,
    private val idHopDong: String
) : RecyclerView.Adapter<AdapterThanhVien.AdapterThanhVienViewHolder>() {

    private lateinit var binding: ItemThongTinThanhVienOTroBinding
    private val TAG = "zzzzAdapterThanhVienzzzz"
    val dbQuanLyNguoiThue = FirebaseFirestore.getInstance().collection("QuanLyNguoiThue")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterThanhVienViewHolder {

        binding = ItemThongTinThanhVienOTroBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return AdapterThanhVienViewHolder(binding)

    }

    override fun getItemCount(): Int {
        return listTv.size
    }

    override fun onBindViewHolder(holder: AdapterThanhVienViewHolder, position: Int) {
        val dataTv = listTv[position]
        holder.bin(dataTv)

        holder.itemView.setOnClickListener {
            Log.d(TAG, "onBindViewHolder: item")
            val bottomSheetFragment = BottomSheetCreateAndUpdateThanhVien(idHopDong, dataTv)
            val context = holder.itemView.context
            if (context is FragmentActivity) {
                bottomSheetFragment.show(context.supportFragmentManager, bottomSheetFragment.tag)
            }
        }
        holder.itemView.setOnLongClickListener {
            Log.e(TAG, "onBindViewHolder: $dataTv")
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Thông báo")
                .setCancelable(true)
                .setMessage("Bạn có chắc chắn xóa không")
                .setPositiveButton("Yes") { d, w ->
                    Log.d(TAG, "onBindViewHolder: yes d $d")
                    Log.d(TAG, "onBindViewHolder: yes w $w")
                    deleteThanhVien(dataTv.maTv,idHopDong,holder.itemView.context)
                    ProgressDialogFragment.showProgressBar(holder.itemView.context as Activity)

                }
                .setNegativeButton("No") { d, w ->
                    Log.d(TAG, "onBindViewHolder: no d $d")
                    Log.d(TAG, "onBindViewHolder: no d $w")
                }
                .show()
            true
        }

    }

    class AdapterThanhVienViewHolder(itemView: ItemThongTinThanhVienOTroBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val name = itemView.tvTenThanhVien
        val sdt = itemView.tvSdt
        val ngayVao = itemView.tvNgayVao
        val image = itemView.ivAvatarUser
        fun bin(thanhVien: ThanhVien) {
            name.text = thanhVien.name
            sdt.text = thanhVien.sdt
            ngayVao.text = thanhVien.ngayVao
            Glide.with(itemView.context)
                .load(thanhVien.image)
                .circleCrop()
                .into(image)
        }
    }

    private fun deleteThanhVien(thanhVienId: String, idHopDong: String, context: Context) {
        dbQuanLyNguoiThue.document(idHopDong)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nguoiThue = document.toObject(NguoiThueModel::class.java)
                    if (nguoiThue != null) {
                        // Lấy danh sách thành viên
                        val updatedList = nguoiThue.thanhVienList.toMutableList()

                        // Tìm và xóa thành viên theo ID
                        val thanhVienToDelete = updatedList.find { it.maTv == thanhVienId }
                        if (thanhVienToDelete != null) {
                            updatedList.remove(thanhVienToDelete)

                            // Cập nhật danh sách trong Firestore
                            dbQuanLyNguoiThue.document(idHopDong)
                                .update("thanhVienList", updatedList)
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Xóa thành viên thành công")
                                    Toast.makeText(context, "Xóa thành viên thành công", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Lỗi khi xóa thành viên: ${e.message}")
                                }
                                .addOnCompleteListener {
                                    ProgressDialogFragment.hideProgressBar(context as Activity)

                                }
                        } else {
                            Log.e("Firestore", "Không tìm thấy thành viên cần xóa")
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Lỗi khi lấy document: ${e.message}")
            }
    }


}