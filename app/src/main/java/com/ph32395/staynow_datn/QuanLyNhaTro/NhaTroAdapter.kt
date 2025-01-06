package com.ph32395.staynow_datn.QuanLyNhaTro

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.QuanLyPhongTro.QuanLyPhongTroActivity
import com.ph32395.staynow_datn.databinding.ItemNhaTroNhaBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NhaTroAdapter(
    private val listNhaTro: List<NhaTroModel>
) : RecyclerView.Adapter<NhaTroAdapter.NhaTroAdapterViewHolder>() {

    private lateinit var binding: ItemNhaTroNhaBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val nhaTroRef = firestore.collection("NhaTro")
    private val TAG = "ZZNhaTroAdapterZZ"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NhaTroAdapterViewHolder {

        binding = ItemNhaTroNhaBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return NhaTroAdapterViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listNhaTro.size
    }

    override fun onBindViewHolder(holder: NhaTroAdapterViewHolder, position: Int) {
        val item = listNhaTro[position]

        holder.bin(item, binding)
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        holder.itemView.setOnClickListener {
            // Chuyển đến màn hình QuanLyPhongTroActivity khi nhấn vào một tòa nhà
            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, QuanLyPhongTroActivity::class.java).apply {
                    putExtra("maNhaTro", item.maNhaTro)
                }
                holder.itemView.context.startActivity(intent)
            }

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

    }

    class NhaTroAdapterViewHolder(itemView: ItemNhaTroNhaBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val btnEdit = itemView.btnEdit

        @SuppressLint("SetTextI18n")
        fun bin(item: NhaTroModel, itemView: ItemNhaTroNhaBinding) {
            itemView.tvTenNhaTro.text = item.tenNhaTro
            itemView.tvDiaChi.text = item.diaChiChiTiet
            itemView.tvTenLoaiNhaTro.text = item.tenLoaiNhaTro
            itemView.tvNgayTao.text = convertTimestampToDate(item.ngayTao)
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