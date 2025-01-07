package com.ph32395.staynow_datn.QuanLyNhaTro

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.QuanLyPhongTro.QuanLyPhongTroActivity
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.databinding.DialogMsgNhaTroNhaBinding
import com.ph32395.staynow_datn.databinding.ItemNhaTroNhaBinding
import com.techiness.progressdialoglibrary.ProgressDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NhaTroAdapter(
    private val listNhaTro: List<NhaTroModel>
) : RecyclerView.Adapter<NhaTroAdapter.NhaTroAdapterViewHolder>() {

    private lateinit var binding: ItemNhaTroNhaBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val nhaTroRef = firestore.collection("NhaTro")
    private val phongTroRef = firestore.collection("PhongTro")
    private val TAG = "ZZNhaTroAdapterZZ"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NhaTroAdapterViewHolder {

        binding = ItemNhaTroNhaBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return NhaTroAdapterViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listNhaTro.size
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: NhaTroAdapterViewHolder, position: Int) {
        val item = listNhaTro[position]

        holder.bin(item, binding)
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Chuyển đến màn hình QuanLyPhongTroActivity khi nhấn vào một tòa nhà
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, QuanLyPhongTroActivity::class.java).apply {
                putExtra("maNhaTro", item.maNhaTro)
            }
            holder.itemView.context.startActivity(intent)
        }

        holder.btnEdit.setOnClickListener {
            val bottomSheetCreateAndUpdateNhaTro = BottomSheetCreateAndUpdateNhaTro(item)
            val context = holder.itemView.context
            if (context is FragmentActivity) {
                bottomSheetCreateAndUpdateNhaTro.show(
                    context.supportFragmentManager,
                    bottomSheetCreateAndUpdateNhaTro.tag
                )
            }
        }
        holder.itemView.setOnLongClickListener {
            val dialog = AlertDialog.Builder(holder.itemView.context)
            dialog.setTitle("Thông báo !!!")
                .setMessage("Bạn có chắc chắn xóa không ???")
                .setCancelable(true)
                .setPositiveButton("Yes") { v, i ->
                    deleteNhaTro(userId, holder.itemView.context, item)
                }
                .setNegativeButton("No") { v, i -> }
                .show()
            true
        }

        holder.btnOnAndOf.setOnClickListener {
            val progressDialog = ProgressDialog(holder.itemView.context)
            with(progressDialog) {
                theme = ProgressDialog.THEME_DARK
            }
            if (item.trangThai) {
                Log.e(TAG, "onBindViewHolder: nut ngung hoat dong toa nha")

                val dialog = Dialog(holder.itemView.context)
                val binding2 =
                    DialogMsgNhaTroNhaBinding.inflate(LayoutInflater.from(holder.itemView.context))
                dialog.setContentView(binding2.root)

                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                // Lấy `LayoutParams` và đặt `margin`
                val layoutParams = dialog.window?.attributes
                layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT // Đặt chiều rộng
                dialog.window?.attributes = layoutParams

                // Thêm margin vào nội dung chính của `Dialog`
                val dialogLayoutParams = binding2.root.layoutParams as ViewGroup.MarginLayoutParams
                dialogLayoutParams.setMargins(
                    32,
                    0,
                    32,
                    0
                ) // Điều chỉnh margin (trái, trên, phải, dưới)
                binding2.root.layoutParams = dialogLayoutParams

                binding2.cbRead.setOnCheckedChangeListener { buttonView, isChecked ->
                    Log.e(TAG, "onBindViewHolder: isChecked $isChecked")
                    // Vô hiệu hóa hoặc kích hoạt nút btnConfirm
                    binding2.btnConfirm.isEnabled = isChecked
                    // Thay đổi màu nền dựa trên trạng thái
                    if (!isChecked) {
                        binding2.btnConfirm.setBackgroundColor(
                            ContextCompat.getColor(
                                holder.itemView.context,
                                R.color.gray
                            )
                        )
                    } else {
                        binding2.btnConfirm.setBackgroundColor(
                            ContextCompat.getColor(
                                holder.itemView.context,
                                R.color.my_light_primary
                            )
                        )

                    }
                }


                binding2.btnConfirm.setOnClickListener {
                    if (userId != null) {
                        changeRoomOf(userId, item, dialog, progressDialog)
                        Log.d(TAG, "onBindViewHolder: 1")
                    }
                }

                binding2.btnNotConfirm.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            } else {
                Log.e(TAG, "onBindViewHolder: nut hoat dong toa nha lai")
                val dialog = Dialog(holder.itemView.context)
                val binding2 =
                    DialogMsgNhaTroNhaBinding.inflate(LayoutInflater.from(holder.itemView.context))
                dialog.setContentView(binding2.root)

                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                // Lấy `LayoutParams` và đặt `margin`
                val layoutParams = dialog.window?.attributes
                layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT // Đặt chiều rộng
                dialog.window?.attributes = layoutParams

                // Thêm margin vào nội dung chính của `Dialog`
                val dialogLayoutParams = binding2.root.layoutParams as ViewGroup.MarginLayoutParams
                dialogLayoutParams.setMargins(
                    32,
                    0,
                    32,
                    0
                ) // Điều chỉnh margin (trái, trên, phải, dưới)
                binding2.root.layoutParams = dialogLayoutParams

                binding2.cbRead.setOnCheckedChangeListener { buttonView, isChecked ->
                    Log.e(TAG, "onBindViewHolder: isChecked $isChecked")
                    // Vô hiệu hóa hoặc kích hoạt nút btnConfirm
                    binding2.btnConfirm.isEnabled = isChecked
                    // Thay đổi màu nền dựa trên trạng thái
                    if (!isChecked) {
                        binding2.btnConfirm.setBackgroundColor(
                            ContextCompat.getColor(
                                holder.itemView.context,
                                R.color.gray
                            )
                        )
                    } else {
                        binding2.btnConfirm.setBackgroundColor(
                            ContextCompat.getColor(
                                holder.itemView.context,
                                R.color.my_light_primary
                            )
                        )

                    }
                }

                binding2.btnConfirm.setOnClickListener {
                    if (userId != null) {
                        changeRoomOn(userId, item, dialog, progressDialog)
                    }
                }

                binding2.btnNotConfirm.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }
        }

    }

    private fun changeRoomOn(
        userId: String,
        item: NhaTroModel,
        dialog: Dialog,
        progressDialog: ProgressDialog
    ) {
        progressDialog.show()
        nhaTroRef.document(userId)
            .collection("DanhSachNhaTro")
            .document(item.maNhaTro) // ID của tài liệu nhà trọ cần cập nhật
            .update("trangThai", true) // Cập nhật trường 'trangThai' thành true
            .addOnSuccessListener {
                Log.d(TAG, "Successfully updated trạng thái")
                dialog.dismiss()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating trạng thái", exception)
                progressDialog.dismiss()
            }
            .addOnCompleteListener {
                progressDialog.dismiss()
            }
        phongTroRef.whereEqualTo("maNguoiDung", userId)
            .whereEqualTo("maNhaTro", item.maNhaTro)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val docId = document.id // Lấy document ID
                    phongTroRef.document(docId)
                        .update(
                            "trangThaiLuu", true,
                            "trangThaiDuyet", ""
                        )
                        .addOnSuccessListener {
                            Log.d(TAG, "Cập nhật thành công: $docId")
                            dialog.dismiss()
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "Lỗi khi cập nhật: ${it.message}")
                        }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "onBindViewHolder: ${it.message.toString()}")
            }
            .addOnCompleteListener {
                progressDialog.dismiss()

            }

    }

    private fun changeRoomOf(
        userId: String,
        item: NhaTroModel,
        dialog: Dialog,
        progressDialog: ProgressDialog
    ) {
        progressDialog.show()
        nhaTroRef.document(userId)
            .collection("DanhSachNhaTro")
            .document(item.maNhaTro) // ID của tài liệu nhà trọ cần cập nhật
            .update("trangThai", false) // Cập nhật trường 'trangThai' thành true
            .addOnSuccessListener {
                Log.d(TAG, "Successfully updated trạng thái")
                dialog.dismiss()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating trạng thái", exception)
                progressDialog.dismiss()
            }
            .addOnCompleteListener {
                progressDialog.dismiss()

            }
        phongTroRef.whereEqualTo("maNguoiDung", userId)
            .whereEqualTo("maNhaTro", item.maNhaTro)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val docId = document.id // Lấy document ID
                    phongTroRef.document(docId)
                        .update(
                            "trangThaiLuu", true,
                            "trangThaiDuyet", ""
                        )
                        .addOnSuccessListener {
                            Log.d(TAG, "Cập nhật thành công: $docId")
                            dialog.dismiss()
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "Lỗi khi cập nhật: ${it.message}")
                        }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "onBindViewHolder: ${it.message.toString()}")
                progressDialog.dismiss()
            }
            .addOnCompleteListener {
                progressDialog.dismiss()
            }
    }

    class NhaTroAdapterViewHolder(itemView: ItemNhaTroNhaBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val btnEdit = itemView.btnEdit
        val btnOnAndOf = itemView.btnNgungHoatDongAndHoatDong

        @SuppressLint("SetTextI18n")
        fun bin(item: NhaTroModel, itemView: ItemNhaTroNhaBinding) {
            itemView.tvTenNhaTro.text = item.tenNhaTro
            itemView.tvDiaChi.text = item.diaChiChiTiet
            itemView.tvTenLoaiNhaTro.text = item.tenLoaiNhaTro
            itemView.tvTrangThai.text = if (item.trangThai) "Hoạt động" else "Ngừng hoạt động"

            if (item.trangThai) itemView.tvTrangThai.setTextColor(Color.GREEN)
            else itemView.tvTrangThai.setTextColor(Color.RED)

            itemView.tvNgayTao.text = convertTimestampToDate(item.ngayTao)

            if (item.trangThai) itemView.icon.setImageResource(R.drawable.icon_ngung_hoat_dong)
            else itemView.icon.setImageResource(R.drawable.icon_hoat_dong_lai)
        }

        fun convertTimestampToDate(timestamp: Long): String {
            // Chọn định dạng ngày bạn muốn
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            // Chuyển đổi timestamp thành đối tượng Date
            val date = Date(timestamp)
            // Định dạng đối tượng Date thành chuỗi
            return dateFormat.format(date)
        }

    }

    private fun deleteNhaTro(userId: String?, context: Context, item: NhaTroModel) {
        if (userId != null) {
            nhaTroRef.document(userId).collection("DanhSachNhaTro").document(item.maNhaTro)
                .delete().addOnSuccessListener {
                    Toast.makeText(context, "Delete thành công", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Log.e(TAG, "DELETE thất bại ${it.message.toString()}")
                }.addOnCompleteListener {
                    Log.d(TAG, "DELETE nhà trọ hoàn thành")
                }
        }

    }

}