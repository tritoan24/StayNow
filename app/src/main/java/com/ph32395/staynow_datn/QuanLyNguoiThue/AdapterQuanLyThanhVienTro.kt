package com.ph32395.staynow_datn.QuanLyNguoiThue

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.databinding.ItemQuanLyNguoiTheTroNhaBinding

class AdapterQuanLyThanhVienTro(
    private val list: List<NguoiThueModel>
) : RecyclerView.Adapter<AdapterQuanLyThanhVienTro.AdapterQuanLyThanhVienTroViewHolder>() {

    private lateinit var binding: ItemQuanLyNguoiTheTroNhaBinding
    private val database = FirebaseDatabase.getInstance().getReference("NguoiDung")
    val dbQuanLyNguoiThue = FirebaseFirestore.getInstance().collection("QuanLyNguoiThue")
    private val TAG = "zzzAdapterQuanLyThanhVienTrozzz"
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdapterQuanLyThanhVienTroViewHolder {

        binding = ItemQuanLyNguoiTheTroNhaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return AdapterQuanLyThanhVienTroViewHolder(binding)

    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AdapterQuanLyThanhVienTroViewHolder, position: Int) {
        val item = list[position]

        holder.tvTenPhong.text = item.tenPhong
        holder.tvBaseThanhVien.text = "/${item.soNguoiGioHanO}"
        val listTv = mutableListOf<ThanhVien>()

        database.child(item.idDaiDienThue).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("hoTen").value.toString()
                val image = snapshot.child("anhDaiDien").value.toString()
                val email = snapshot.child("email").value.toString()
                val sdt = snapshot.child("sdt").value.toString()

                Log.d(TAG, "onDataChange:name $name ---> email $email --->sdt $sdt ")
                val thanhVien =
                    ThanhVien(item.idDaiDienThue, name, image, email, item.ngayBatDau, sdt)
                if (!listTv.contains(thanhVien)) { // Kiểm tra trùng lặp trước khi thêm
                    listTv.add(thanhVien)
                }
                val nguoiThue = NguoiThueModel(
                    idHopDong = item.idHopDong,
                    idDaiDienThue = item.idDaiDienThue,
                    tenPhong = item.tenPhong,
                    ngayBatDau = item.ngayBatDau,
                    soNguoiGioHanO = item.soNguoiGioHanO,
                    danhSachThanhVien = listTv
                )

                Log.e(TAG, "onDataChange:listTv $listTv")
                luuThongTinNguoiDaiDien(nguoiThue)

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled:error getUserDaiDien ${error.message}")
            }
        })

        fetchListTv(
            item.idHopDong,
            holder.itemView.context,
            holder.listThanhVien,
            holder.tvCountThanhVien,
            holder.tvBaseThanhVien,
            item.soNguoiGioHanO
        )


        holder.btnAddThanhVien.setOnClickListener {
            val bottomSheetFragment = BottomSheetCreateAndUpdateThanhVien(item.idHopDong,null)
            val context = holder.itemView.context
            if (context is FragmentActivity) {
                bottomSheetFragment.show(context.supportFragmentManager, bottomSheetFragment.tag)
            }


        }

    }

    private fun fetchListTv(
        idHopDong: String,
        context: Context,
        listThanhVien: RecyclerView,
        tvCountThanhVien: TextView,
        tvBaseThanhVien: TextView,
        soNguoiGioHanO: Int
    ) {

        dbQuanLyNguoiThue.document(idHopDong).addSnapshotListener { snapshot, error ->

            if (error != null) {
                Log.e(TAG, "Lỗi khi lắng nghe thay đổi: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val nguoiThue = snapshot.toObject(NguoiThueModel::class.java)
                if (nguoiThue != null) {
                    Log.d(TAG, "Dữ liệu người thuê đã thay đổi: ${nguoiThue.danhSachThanhVien}")
                    Log.d(TAG, "fetchListTv: ${nguoiThue.danhSachThanhVien.size}")
                    tvCountThanhVien.text = nguoiThue.danhSachThanhVien.size.toString()
                    if (nguoiThue.danhSachThanhVien.size > soNguoiGioHanO) {
                        tvBaseThanhVien.setTextColor(Color.RED)
                        tvCountThanhVien.setTextColor(Color.RED)
                    } else {
                        tvBaseThanhVien.setTextColor(Color.BLACK)
                        tvCountThanhVien.setTextColor(Color.BLACK)
                    }
                    val adapterThanhVien = AdapterThanhVien(nguoiThue.danhSachThanhVien,idHopDong)
                    listThanhVien.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    listThanhVien.adapter = adapterThanhVien
                }
            }


        }


    }

    private fun luuThongTinNguoiDaiDien(nguoiThue: NguoiThueModel) {
        val userRef = dbQuanLyNguoiThue.document(nguoiThue.idHopDong)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                return@addOnSuccessListener
            } else {
                // Dữ liệu chưa tồn tại, thêm mới
                userRef.set(nguoiThue)
                    .addOnSuccessListener {
                        Log.d(TAG, "Lưu thành công")
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "Lưu không thành công: ${it.message}")
                    }
            }
        }
    }

    private fun addThanhVienMoi(thanhVienMoi: ThanhVien, idHopDong: String) {
        dbQuanLyNguoiThue.document(idHopDong)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nguoiThue = document.toObject(NguoiThueModel::class.java)
                    if (nguoiThue != null) {
                        // Thêm thành viên mới vào danh sách
                        val updatedList = nguoiThue.danhSachThanhVien.toMutableList()
                        updatedList.add(thanhVienMoi)

                        // Cập nhật danh sách trong Firestore
                        dbQuanLyNguoiThue.document(nguoiThue.idHopDong)
                            .update("thanhVienList", updatedList)
                            .addOnSuccessListener {
                                Log.d("Firestore", "Thêm thành viên mới thành công")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Lỗi khi thêm thành viên mới: ${e.message}")
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Lỗi khi lấy document: ${e.message}")
            }
    }

    class AdapterQuanLyThanhVienTroViewHolder(itemView: ItemQuanLyNguoiTheTroNhaBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val btnAddThanhVien = itemView.btnAddThanhVien
        val tvTenPhong = itemView.tvTenPhongTro
        val listThanhVien = itemView.listViewNguoiThue
        val tvCountThanhVien = itemView.tvCountThanhVien
        val tvBaseThanhVien = itemView.tvBaseThanhVien


    }

}