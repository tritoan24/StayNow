package com.ph32395.staynow.PhongTroYeuThich

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow.Model.PhongTroModel
import com.ph32395.staynow.R

class PhongTroYeuThichActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PhongTroYeuThichAdapter
    private lateinit var btnBack: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var txtPhongYeuThich: TextView
    private val favoriteList = mutableListOf<Pair<String, PhongTroModel>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phong_tro_yeu_thich)
        progressBar = findViewById(R.id.progressBarPhongTroYeuThich)
        txtPhongYeuThich = findViewById(R.id.txtPhongYeuThich)

        btnBack = findViewById(R.id.iconBackPhongTroYeuThich)
        btnBack.setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerViewPhongTroYeuThich)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = PhongTroYeuThichAdapter(favoriteList) { room ->
            removeFromFavorites(room)
        }
        recyclerView.adapter = adapter

        fetchFavorites()
    }

//    Lay danh sach phong troeu thích
    private fun fetchFavorites() {
        progressBar.visibility = View.VISIBLE //Hien thi ProgressBar
        txtPhongYeuThich.visibility = View.GONE //An thong bao khong co phong
        recyclerView.visibility = View.GONE
//  su dung addSnapshotListener de lang nghe thay doi
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("PhongTroYeuThich")
            .whereEqualTo("Id_nguoidung", userId)
            .addSnapshotListener { snapshot, error ->
                progressBar.visibility = View.GONE //An ProgressBar khi tai du lieu thanh cong
                if (error != null) {
                    Toast.makeText(this, "Lỗi lắng nghe dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // Nếu không có dữ liệu yêu thích
                favoriteList.clear()
                if (snapshot != null && !snapshot.isEmpty) {
                    favoriteList.clear()
                    for (doc in snapshot.documents) {
                        val roomId = doc.getString("Id_phongtro") ?: continue
                        val favoriteTime = doc.getLong("Thoigian_yeuthich") ?: 0L
                        fetchRoomDetail(roomId, favoriteTime)
                    }
                    recyclerView.visibility = View.VISIBLE //Hien thi danh sach phong
                } else {
                    txtPhongYeuThich.visibility = View.VISIBLE //Hien thi thong bao
//                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun fetchRoomDetail(maPhongTro: String, favoriteTime: Long) {

        FirebaseFirestore.getInstance().collection("PhongTro")
            .document(maPhongTro)
            .get()
            .addOnSuccessListener { doc ->
                val room = doc.toObject(PhongTroModel::class.java) ?: return@addOnSuccessListener
                room.Thoigian_yeuthich = favoriteTime
                val existingRoomIndex = favoriteList.indexOfFirst { it.first == maPhongTro }

                if (existingRoomIndex >= 0) {
                    // Cập nhật nếu đã tồn tại
                    favoriteList[existingRoomIndex] = Pair(maPhongTro, room)
                } else {
                    // Thêm mới nếu chưa tồn tại
                    favoriteList.add(Pair(maPhongTro, room))
                }
                adapter.notifyDataSetChanged()
                recyclerView.visibility = View.VISIBLE
                txtPhongYeuThich.visibility = View.GONE
            }
            .addOnFailureListener {
                if (favoriteList.isEmpty()) {
                    txtPhongYeuThich.visibility = View.VISIBLE
                }
            }
    }

//    Xoa phong tro yeu thích
    private fun removeFromFavorites(room: PhongTroModel) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val pairToRemove = favoriteList.find { it.second == room } ?: return
        val roomId = pairToRemove.first
        FirebaseFirestore.getInstance().collection("PhongTroYeuThich")
            .document("$userId-$roomId")
            .delete()
            .addOnSuccessListener {
                favoriteList.remove(pairToRemove)
                adapter.notifyDataSetChanged()
            }
    }
}
