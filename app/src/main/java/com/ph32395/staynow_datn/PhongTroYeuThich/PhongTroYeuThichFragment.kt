package com.ph32395.staynow_datn.PhongTroYeuThich

import com.ph32395.staynow_datn.Model.PhongTroModel
import com.ph32395.staynow_datn.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PhongTroYeuThichFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PhongTroYeuThichAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var txtPhongYeuThich: TextView
    private val favoriteList = mutableListOf<Pair<String, PhongTroModel>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_phong_tro_yeu_thich, container, false)

        progressBar = view.findViewById(R.id.progressBarPhongTroYeuThich)
        txtPhongYeuThich = view.findViewById(R.id.txtPhongYeuThich)
        recyclerView = view.findViewById(R.id.recyclerViewPhongTroYeuThich)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = PhongTroYeuThichAdapter(favoriteList) { room ->
            removeFromFavorites(room)
        }
        recyclerView.adapter = adapter

        fetchFavorites()
    }

    // Lấy danh sách phòng trọ yêu thích
    private fun fetchFavorites() {
        progressBar.visibility = View.VISIBLE // Hiển thị ProgressBar
        txtPhongYeuThich.visibility = View.GONE // Ẩn thông báo không có phòng
        recyclerView.visibility = View.GONE

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("PhongTroYeuThich")
            .whereEqualTo("idNguoiDung", userId)
            .addSnapshotListener { snapshot, error ->
                progressBar.visibility = View.GONE // Ẩn ProgressBar khi tải dữ liệu thành công
                if (error != null) {
                    Toast.makeText(requireContext(), "Lỗi lắng nghe dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // Nếu không có dữ liệu yêu thích
                favoriteList.clear()
                if (snapshot != null && !snapshot.isEmpty) {
                    favoriteList.clear()
                    for (doc in snapshot.documents) {
                        val roomId = doc.getString("idPhongTro") ?: continue
                        val favoriteTime = doc.getLong("thoiGianYeuThich") ?: 0L
                        fetchRoomDetail(roomId, favoriteTime)
                    }
                    recyclerView.visibility = View.VISIBLE // Hiển thị danh sách phòng
                } else {
                    txtPhongYeuThich.visibility = View.VISIBLE // Hiển thị thông báo
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


    // Xóa phòng trọ yêu thích
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
