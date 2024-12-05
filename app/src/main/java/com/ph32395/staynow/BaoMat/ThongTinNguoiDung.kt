package com.ph32395.staynow.BaoMat

import android.content.Intent
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ph32395.staynow.ChucNangNhanTinCC.TextingMessengeActivity
import com.ph32395.staynow.R
import com.ph32395.staynow.fragment.home.HomeViewModel
import com.ph32395.staynow.fragment.home.PhongTroAdapter
import gun0912.tedimagepicker.util.ToastUtil.context

class ThongTinNguoiDung : AppCompatActivity() {

    private lateinit var imgInfor: ImageView
    private lateinit var nameInfor: TextView
    private lateinit var phoneInfor: TextView
    private lateinit var emailInfor: TextView
    private lateinit var rcListRoom: RecyclerView
    private lateinit var btnBackCT: ImageView
    private lateinit var btnNhanTin: LinearLayout
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var homViewModel: HomeViewModel
    private lateinit var roomAdapter: PhongTroAdapter
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thong_tin_nguoi_dung)

        // Ánh xạ các View từ XML
        imgInfor = findViewById(R.id.infor_avatar);
        nameInfor = findViewById(R.id.infor_name);
        phoneInfor = findViewById(R.id.infor_phone);
        emailInfor = findViewById(R.id.infor_email);
        rcListRoom = findViewById(R.id.rc_listRoom)
        btnNhanTin = findViewById(R.id.btnNhanTin)
        btnBackCT = findViewById(R.id.btnBackCT)
        mDatabase = FirebaseDatabase.getInstance().getReference();


        btnBackCT.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        homViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val userId = getIntent().getStringExtra("idUser");
        if(userId != null) {
            mDatabase.child("NguoiDung").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                  if(snapshot.exists()) {
                      val name = snapshot.child("ho_ten").value.toString().trim();
                      val phone = snapshot.child("sdt").value.toString().trim();
                      val img = snapshot.child("anh_daidien").value.toString().trim();
                      val email = snapshot.child("email").value.toString().trim();

                      val maskedPhone = maskPhone(phone)
                      val maskedEmail = maskEmail(email)

                      nameInfor.text = name ?: "Chưa cập nhật"
                      phoneInfor.text = maskedPhone ?: "Chưa cập nhật"
                      emailInfor.text = maskedEmail ?: "Chưa cập nhật"


                      rcListRoom.layoutManager = GridLayoutManager(context, 2)
                      roomAdapter = PhongTroAdapter(mutableListOf(), homViewModel)
                      rcListRoom.adapter = roomAdapter

                      homViewModel.loadRoomByStatus(userId)
//                      Lang nghe thay doi
                      homViewModel.phongDaDang.observe(this@ThongTinNguoiDung) {rooms ->
                          roomAdapter.updateRoomList(rooms)
                          Log.d("ThongTinNguoiDung", rooms.toString())
                      }

                      // Gắn ảnh đại diện nếu có
                      if (img != null && !img.isEmpty()) {
                          Glide.with(this@ThongTinNguoiDung)
                              .load(img)
                              .circleCrop()
                              .placeholder(R.drawable.ic_user)
                              .into(imgInfor)
                      }else {
                          imgInfor.setImageResource(R.drawable.ic_user)
                      }
                  }else {
                      Log.e("ThongTinNguoiDung", "Người dùng không tồn tại trong cơ sở dữ liệu.");

                  }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("ThongTinNguoiDung", "Lỗi khi lấy dữ liệu người dùng: ${error.message}")
                }

            })
        }else {
            Log.e("ThongTinNguoiDung","Người dùng đăng nhập ")
        }

        //Chuyen den man nhan tin
        btnNhanTin.setOnClickListener {
            val intent = Intent(this,TextingMessengeActivity::class.java)
            intent.putExtra("userId",userId)
            startActivity(intent)
        }



    }

    fun maskPhone(phone: String?): String? {
        if(phone == null || phone.length < 10) {
            return phone
        }
        return phone.substring(0,3) + "*****" + phone.substring(phone.length - 2);
    }

    fun maskEmail(email: String?): String? {
        if(email == null || !email.contains("@")) {
            return email
        }
        val atIndex = email.indexOf("@")
        if(atIndex <= 3) {
            return "****" + email.substring(atIndex)
        }
        return email.substring(0,3) + "*****" + email.substring(atIndex)
    }
}