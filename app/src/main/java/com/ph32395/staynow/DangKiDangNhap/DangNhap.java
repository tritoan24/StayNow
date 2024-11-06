package com.ph32395.staynow.DangKiDangNhap;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ph32395.staynow.MainActivity;
import com.ph32395.staynow.Model.NguoiDung;
import com.ph32395.staynow.R;

public class DangNhap extends AppCompatActivity {
    private Button btnDangNhap, btnDangNhapGoogle;
    private TextView txtdangky;
    private EditText edMail, edPass;
    private FirebaseAuth mAuth;
    private RegisterWithGoogle registerWithGoogle;
    private DatabaseReference mDatabase;
    private ImageView img_anhienpass;



    private static final int RC_SIGN_IN_REGISTER = 9001;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dang_nhap);

        btnDangNhap = findViewById(R.id.loginButton);
        txtdangky = findViewById(R.id.txtdangky);
        edMail = findViewById(R.id.username);
        edPass = findViewById(R.id.password);
        img_anhienpass = findViewById(R.id.img_anhienpass);
        btnDangNhapGoogle = findViewById(R.id.loginWithGGButton);

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // Khởi tạo RegisterWithGoogle để xử lý đăng nhập với Google
        registerWithGoogle = new RegisterWithGoogle(this);


        btnDangNhap.setOnClickListener(v -> {
            String email = edMail.getText().toString().trim();
            String password = edPass.getText().toString().trim();

            Log.d("DangNhap", "Email: " + email);
            Log.d("DangNhap", "Password: " + password);

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            } else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                edMail.setError("Email không hợp lệ");
            } else {
                // Đăng nhập bằng Firebase
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    String userId = user.getUid();
                                    mDatabase.child("NguoiDung").child(userId).get().addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful() && task1.getResult() != null) {
                                            NguoiDung nguoiDung = task1.getResult().getValue(NguoiDung.class);
                                            if (nguoiDung != null) {
                                                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                                SharedPreferences.Editor editor = prefs.edit();
                                                editor.putString("Name", nguoiDung.getHo_ten());
                                                editor.putString("Phone", nguoiDung.getSdt());
                                                editor.putString("Image", nguoiDung.getAnh_daidien());
                                                editor.putString("AccountType", nguoiDung.getLoai_taikhoan()); // Hoặc "Người thuê"
                                                //editor.putString("AccountType", "Chủ nhà"); // Hoặc "Chủ nhà"
                                                editor.putBoolean("is_logged_in", true);
                                                editor.apply();
                                                startActivity(new Intent(DangNhap.this, MainActivity.class));
                                                finish();
                                            } else {
                                                Log.d("DangNhap", "Thông tin người dùng không tồn tại.");
                                                Toast.makeText(DangNhap.this, "Lỗi khi lấy thông tin người dùng.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Log.d("DangNhap", "Lỗi khi lấy thông tin người dùng: " + task1.getException());
                                            Toast.makeText(DangNhap.this, "Đăng nhập thất bại!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } else {
                                showFailureAnimation(task.getException().getMessage());
                            }
                        });
            }
        });


        txtdangky.setOnClickListener(v -> {
            startActivity(new Intent(DangNhap.this, DangKy.class));
        });

        btnDangNhapGoogle.setOnClickListener(v -> {
            Intent signInIntent = registerWithGoogle.getGoogleSignInClient().getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN_REGISTER);
        });

        // Ẩn hiện password
        img_anhienpass.setOnClickListener(v -> {
            int cursorPosition = edPass.getSelectionStart();

            if(edPass.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD){
                edPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                img_anhienpass.setImageResource(R.drawable.visiblepass);
            } else {
                edPass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                img_anhienpass.setImageResource(R.drawable.chepass);
            }

            edPass.setSelection(cursorPosition);
        });



    }

    // Hàm lưu thông tin người dùng vào Realtime Database
    private void saveUserInfo(String Ma_nguoidung, String Ho_ten, String Sdt, String Email, String Anh_daidien,Integer So_luotdatlich, String Loai_taikhoan, String Trang_thaitaikhoan, Long Ngay_taotaikhoan, Long Ngay_capnhat) {

        NguoiDung nguoiDung = new NguoiDung(Ma_nguoidung, Ho_ten, Sdt, Email, Anh_daidien, So_luotdatlich, Loai_taikhoan, Trang_thaitaikhoan, Ngay_taotaikhoan, Ngay_capnhat);
        mDatabase.child("NguoiDung").child(Ma_nguoidung).setValue(nguoiDung)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Lưu thông tin thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Lưu thông tin thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN_REGISTER) {
            registerWithGoogle.handleSignInResult(requestCode, data, new RegisterWithGoogle.OnSignInResultListener() {
                @Override
                public void onSignInSuccess(FirebaseUser user) {
                    //neu so dien thoai chua co thi hien thi dialog de nhap so dien thoai
                    String phoneNumber = user.getPhoneNumber() == null ? "ChuaCo" : user.getPhoneNumber();

                    // Log thông tin người dùng
                    Log.d("DangNhap", "User ID: " + user.getUid());
                    Log.d("DangNhap", "Display Name: " + user.getDisplayName());
                    Log.d("DangNhap", "Email: " + user.getEmail());
                    Log.d("DangNhap", "Phone Number: " + phoneNumber);
                    Log.d("DangNhap", "Photo URL: " + user.getPhotoUrl());

                    saveUserInfo(user.getUid(), user.getDisplayName(), phoneNumber, user.getEmail(), String.valueOf(user.getPhotoUrl()), 0, "NguoiThue", "HoatDong", System.currentTimeMillis(), System.currentTimeMillis());
                    Toast.makeText(DangNhap.this, "Đăng nhập với Google thành công", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DangNhap.this, MainActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onSignInFailed(Exception e) {
                    Toast.makeText(DangNhap.this, "Đăng nhập với Google thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Phương thức để hiển thị animation thất bại
    private void showFailureAnimation(String errorMessage) {
        // Tạo một dialog mới
        Dialog dialog = new Dialog(this, R.style.TransparentDialog);
        dialog.setContentView(R.layout.hieuungthongbaoloi);
        dialog.setCancelable(true);

        TextView txtThongBao = dialog.findViewById(R.id.text_message);
        txtThongBao.setText("Đăng nhập thất bại!");

        LottieAnimationView animationView = dialog.findViewById(R.id.animation_view);
        animationView.setAnimation("thongbaoloi_animation.json");
        animationView.setSpeed(2.0f);
        animationView.playAnimation();

        dialog.show();

        new Handler().postDelayed(() -> {
            dialog.dismiss();
        }, 1500);
    }
}