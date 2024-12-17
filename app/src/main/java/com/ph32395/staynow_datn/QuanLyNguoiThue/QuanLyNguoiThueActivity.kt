package com.ph32395.staynow_datn.QuanLyNguoiThue

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.databinding.ActivityQuanLyNguoiThueBinding

class QuanLyNguoiThueActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuanLyNguoiThueBinding
    private var data = FirebaseFirestore.getInstance().collection("HopDong")
    private var TAG = "zzzzzQuanLyNguoiThueActivityzzzzzz"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuanLyNguoiThueBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        fetchListHopDongActive(userId)


        //Back
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun fetchListHopDongActive(userId: String?) {

        data.whereEqualTo("chuNha.maNguoiDung", userId)
            .whereEqualTo("trangThai", "ACTIVE")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }
                val list = mutableListOf<NguoiThueModel>()

                for (docChange in value!!.documentChanges) {
                    when (docChange.type) {
                        DocumentChange.Type.ADDED -> {
                            Log.d(TAG, "New contract: data ${docChange.document.data}")
                            Log.d(
                                TAG,
                                "New contract: data ${docChange.document.data.getValue("thongtinphong")}"
                            )
                            Log.d(
                                TAG,
                                "fetchListHopDongActive: idHopDong ${
                                    docChange.document.data.getValue("maHopDong")
                                }"
                            )

                            //nguoiThue
                            val nguoiThue =
                                docChange.document.data.getValue("nguoiThue") as Map<String, Any>
                            val maNguoiThue = nguoiThue.getValue("maNguoiDung").toString()

                            //Thongtin phong
                            val thongTinPhong =
                                docChange.document.data.getValue("thongtinphong") as Map<String, Any>
                            val tenPhong = thongTinPhong.getValue("tenPhong").toString()
                            Log.e(TAG, "fetchListHopDongActive:tenPhong $tenPhong")

                            //So nguoi o
                            val soNguoiO = docChange.document.data.getValue("soNguoiO").toString()

                            //ngayBatDau
                            val ngayBatDau =
                                docChange.document.data.getValue("ngayBatDau").toString()

                            val idHopDong = docChange.document.data.getValue("maHopDong").toString()
                            Log.d(TAG, "fetchListHopDongActive:nguoiThue $nguoiThue")
                            Log.d(TAG, "fetchListHopDongActive:maNguoiThue $maNguoiThue")
                            val nguoiThueModel = NguoiThueModel(
                                idHopDong,
                                maNguoiThue,
                                tenPhong,
                                ngayBatDau,
                                soNguoiO.toInt()
                            )
                            list.add(nguoiThueModel)
                            Log.d(TAG, "fetchListHopDongActive: $list")
                            val adapterQuanLyThanhVienTro = AdapterQuanLyThanhVienTro(list)
                            binding.rvQuanLyNguoiThue.layoutManager =
                                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                            binding.rvQuanLyNguoiThue.adapter = adapterQuanLyThanhVienTro

                        }

                        DocumentChange.Type.MODIFIED -> {
                            Log.d(TAG, "Updated contract: ${docChange.document.data}")
                        }

                        DocumentChange.Type.REMOVED -> {
                            Log.d(TAG, "Removed contract: ${docChange.document.data}")
                        }
                    }
                }

            }


    }


}