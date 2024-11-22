package com.ph32395.staynow.BaoMat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;

import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ph32395.staynow.Model.PhongTroModel;
import com.ph32395.staynow.R;
import com.ph32395.staynow.fragment.home.HomeViewModel;
import com.ph32395.staynow.fragment.home.PhongTroAdapter;

import java.util.ArrayList;
import java.util.List;

import kotlin.Pair;

public class ThongTinNguoiDung extends AppCompatActivity {

    private ImageView imgInfor;
    private TextView nameInfor;
    private TextView phoneInfor;
    private TextView emailInfor;
    private ViewPager2 rc_Item;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private HomeViewModel homeViewModel; // Khai báo ViewModel


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_tin_nguoi_dung);

        // Ánh xạ các View từ XML
        imgInfor = findViewById(R.id.infor_avatar);
        nameInfor = findViewById(R.id.infor_name);
        phoneInfor = findViewById(R.id.infor_phone);
        emailInfor = findViewById(R.id.infor_email);
        rc_Item = findViewById(R.id.viewInfor);
        mDatabase = FirebaseDatabase.getInstance().getReference();


        String userId = getIntent().getStringExtra("idUser");
        // Kiểm tra nếu người dùng đã đăng nhập
        if (userId != null) {
            mDatabase.child("NguoiDung").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Lấy dữ liệu người dùng
                        String name = snapshot.child("ho_ten").getValue(String.class);
                        String phone = snapshot.child("sdt").getValue(String.class);
                        String img = snapshot.child("anh_daidien").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);

                        // Che số điện thoại và email
                        String maskedPhone = maskPhone(phone);
                        String maskedEmail = maskEmail(email);

                        // Hiển thị thông tin người dùng (kiểm tra null trước)
                        nameInfor.setText(name != null ? name : "Chưa cập nhật");
                        phoneInfor.setText(maskedPhone != null ? maskedPhone : "Chưa cập nhật");
                        emailInfor.setText(maskedEmail != null ? maskedEmail : "Chưa cập nhật");

                        // Gắn ảnh đại diện nếu có
                        if (img != null && !img.isEmpty()) {
                            Glide.with(ThongTinNguoiDung.this)
                                    .load(img)
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_user) // Ảnh mặc định
                                    .into(imgInfor);
                        } else {
                            imgInfor.setImageResource(R.drawable.ic_user); // Ảnh mặc định
                        }
                    } else {
                        Log.e("ThongTinNguoiDung", "Người dùng không tồn tại trong cơ sở dữ liệu.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ThongTinNguoiDung", "Lỗi khi lấy dữ liệu người dùng: " + error.getMessage());
                }
            });
        } else {
            Log.e("ThongTinNguoiDung", "Người dùng chưa đăng nhập.");
        }
    }



    // Hàm che số điện thoại
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 10) {
            return phone; // Trả về nguyên nếu không đủ độ dài
        }
        return phone.substring(0, 3) + "*****" + phone.substring(phone.length() - 2);
    }

    // Hàm che email
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email; // Trả về nguyên nếu không hợp lệ
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 3) {
            return "****" + email.substring(atIndex); // Nếu email ngắn, che toàn bộ phần trước @
        }
        return email.substring(0, 3) + "*****" + email.substring(atIndex);
    }

}
