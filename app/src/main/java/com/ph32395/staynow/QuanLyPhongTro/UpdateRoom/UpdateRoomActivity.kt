package com.ph32395.staynow.QuanLyPhongTro.UpdateRoom

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ph32395.staynow.R

class UpdateRoomActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_room)

//        Nhan du lieu tu Intent
        val updateRoomModel = intent.getParcelableExtra<UpdateRoomModel>("updateRoomModel")

//        Log du lieu ra
        updateRoomModel?.let { room ->
            Log.d("UpdateRoom", "Ten phong tro: ${room.Ten_phongtro}")
            Log.d("UpdateRoom", "Dia chi: ${room.Dia_chi}")
            Log.d("UpdateRoom", "Loai phong: ${room.Loai_phong}")
            Log.d("UpdateRoom", "Gioi tinh: ${room.Gioi_tinh}")
            Log.d("UpdateRoom", "Gia phong: ${String.format("%,.0f", room.Gia_phong)} VND")
            Log.d("UpdateRoom", "Chi tiet them: ${room.Chi_tietthem}")
//            Chi tiet thong tin
            if (room.Chi_tietthongtin.isNullOrEmpty()) {
                Log.d("UpdateRoom", "Danh sách ChiTietThongTin đang null hoặc rỗng")
            } else {
                room.Chi_tietthongtin.forEach {
                    Log.d("UpdateRoom", "Chi tiet thong tin: "+ it.toString())
                }
            }

//            Dich vu
            if (room.Dich_vu.isNullOrEmpty()) {
                Log.d("UpdateRoom", "Danh sách DichVu đang null hoặc rỗng")
            } else {
                room.Dich_vu.forEach {
                    Log.d("UpdateRoom", "Danh sach Dich vu: "+ it.toString())
                }
            }

            //            Noi that
            if (room.Noi_that.isNullOrEmpty()) {
                Log.d("UpdateRoom", "Danh sách NoiThat đang null hoặc rỗng")
            } else {
                room.Noi_that.forEach {
                    Log.d("UpdateRoom", "Danh sach Noi that: "+ it.toString())
                }
            }

            //            Tien nghi
            if (room.Tien_nghi.isNullOrEmpty()) {
                Log.d("UpdateRoom", "Danh sách TienNghi đang null hoặc rỗng")
            } else {
                room.Tien_nghi.forEach {
                    Log.d("UpdateRoom", "Danh sach Tien nghi: "+ it.toString())
                }
            }

            //            anh
            if (room.Url_image.isNullOrEmpty()) {
                Log.d("UpdateRoom", "Danh sách Url_image đang null hoặc rỗng")
            } else {
                room.Url_image.forEach {
                    Log.d("UpdateRoom", "Danh sach anh: "+ it.toString())
                }
            }
        }

    }
}