package com.ph32395.staynow.DangKiDangNhap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ph32395.staynow.MainActivity;
import com.ph32395.staynow.Model.NguoiDungModel;
import com.ph32395.staynow.R;


import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;


public class DangKy extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText tenEditText, sdtEditText, emailEditText, passwordEditText, rppass;
    private Button registerButton, registerButtonWithGoogle;
    private RegisterWithGoogle registerWithGoogle;
    private ImageView img_avatar;
    private TextView txtdangnhap;


    // Khai báo Firebase Storage
    private StorageReference mStorageRef;

    private static final int RC_SIGN_IN_REGISTER = 9001; // Request code for Google Sign-In
    private Uri avatarUri; // Biến lưu trữ đường dẫn hình ảnh đại diện

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_ky);

        // Khởi tạo Firebase Auth và Realtime Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Khởi tạo Firebase Storage
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Liên kết các thành phần giao diện
        tenEditText = findViewById(R.id.ten);
        sdtEditText = findViewById(R.id.sdt);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        rppass = findViewById(R.id.rp_password);
        registerButton = findViewById(R.id.btn_register);
        registerButtonWithGoogle = findViewById(R.id.btn_register_google);
        txtdangnhap = findViewById(R.id.txtdangnhap);
        img_avatar = findViewById(R.id.img_avatar);


        //Thông tin người dùng
        String Ngay_taotaikhoan = String.valueOf(System.currentTimeMillis());
        String Ngay_capnhat = String.valueOf(System.currentTimeMillis());
        Integer So_luotdatlich = 0;
        String Loai_taikhoan = "ChuaChon";
        String Trang_thaitaikhoan = "HoatDong";


        // Khởi tạo RegisterWithGoogle để xử lý đăng nhập với Google
        registerWithGoogle = new RegisterWithGoogle(this);

//        // Sự kiện chọn ảnh đại diện
        img_avatar.setOnClickListener(view ->
                ImagePicker.with(this)
                        .crop()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .start()
        );
//


        //do anh ra Fa


        // sự kiện khi ấn vào nút đăng nhập
        txtdangnhap.setOnClickListener(view -> {
            Intent intent = new Intent(DangKy.this, DangNhap.class);
            startActivity(intent);
        });

        // Đăng ký sự kiện cho nút "Đăng ký"
        registerButton.setOnClickListener(view -> {
            String ten = tenEditText.getText().toString().trim();
            String sdt = sdtEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String rppassword = rppass.getText().toString().trim();

            boolean isValid = true;

            if (ten.isEmpty()) {
                tenEditText.setError("Vui lòng nhập Họ Tên");
                isValid = false;
            }

            if (sdt.isEmpty()) {
                sdtEditText.setError("Vui lòng nhập số điện thoại");
                isValid = false;
            }

            if (email.isEmpty()) {
                emailEditText.setError("Vui lòng nhập email");
                isValid = false;
            }

            if (password.isEmpty()) {
                passwordEditText.setError("Vui lòng nhập mật khẩu");
                isValid = false;
            }
            if (rppassword.isEmpty()) {
                rppass.setError("Vui lòng lại nhập mật khẩu");
                isValid = false;
            }


            if (isValid) {
                if(sdt.length() < 10) {
                    sdtEditText.setError("Số điện thoại phải có ít nhất 10 số");
                }else if(!sdt.startsWith("0") || sdt.length() > 11){
                    sdtEditText.setError("Số điện thoại không hợp lệ");
                }else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    emailEditText.setError("Email không hợp lệ");
                }else if(password.length() < 6) {
                    passwordEditText.setError("Mật khẩu phải có ít nhất 6 ký tự");
                }
                else if(!password.matches("^(?=.*[A-Z])(?=.*[0-9]).{6,}$")){
                    passwordEditText.setError("Mật khẩu phải có ít nhất 1 chữ hoa và 1 chữ số");
                }
                else if(!password.equals(rppassword)){
                    rppass.setError("Mật khẩu không trùng khớp");
                }else {
                    signUpWithEmailPassword(ten, sdt, email, password, avatarUri.toString(), So_luotdatlich, Loai_taikhoan, Trang_thaitaikhoan, Long.parseLong(Ngay_taotaikhoan), Long.parseLong(Ngay_capnhat));
                }

            }

        });

        // Đăng ký sự kiện cho nút "Đăng ký bằng Google"
        registerButtonWithGoogle.setOnClickListener(view -> {
            Intent signInIntent = registerWithGoogle.getGoogleSignInClient().getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN_REGISTER);
        });
    }

    private void signUpWithEmailPassword( String Ho_ten, String Sdt, String Email,String password, String Anh_daidien,Integer So_luotdatlich, String Loai_taikhoan, String Trang_thaitaikhoan, Long Ngay_taotaikhoan, Long Ngay_capnhat) {
        mAuth.createUserWithEmailAndPassword(Email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(DangKy.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                        // Tải ảnh lên Firebase Storage sau khi đăng ký thành công
                        if (avatarUri != null) {
                            com.ph32395.staynow.Utils.ImageUploader imageUploader = new com.ph32395.staynow.Utils.ImageUploader();
                            imageUploader.uploadImage(avatarUri, user.getUid(), new com.ph32395.staynow.Utils.ImageUploader.UploadCallback() {
                                @Override
                                public void onSuccess(String imageUrl) {
                                    // Lưu thông tin người dùng với URL ảnh
                                    saveUserInfo(user.getUid(), Ho_ten, Sdt, Email, imageUrl, So_luotdatlich, Loai_taikhoan, Trang_thaitaikhoan, Ngay_taotaikhoan, Ngay_capnhat);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(DangKy.this, "Tải ảnh lên thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            saveUserInfo(user.getUid(), Ho_ten, Sdt, Email, "https://static.vecteezy.com/system/resources/previews/000/422/862/original/avatar-icon-vector-illustration.jpg", So_luotdatlich, Loai_taikhoan, Trang_thaitaikhoan, Ngay_taotaikhoan, Ngay_capnhat);

                        }

                        Intent intent = new Intent(DangKy.this, ChonLoaiTK.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(DangKy.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    // Hàm lưu thông tin người dùng vào Realtime Database
    private void saveUserInfo(String Ma_nguoidung, String Ho_ten, String Sdt, String Email, String Anh_daidien,Integer So_luotdatlich, String Loai_taikhoan, String Trang_thaitaikhoan, Long Ngay_taotaikhoan, Long Ngay_capnhat) {

       NguoiDungModel nguoiDung = new NguoiDungModel(Ma_nguoidung, Ho_ten, Sdt, Email, Anh_daidien, So_luotdatlich, Loai_taikhoan, Trang_thaitaikhoan, Ngay_taotaikhoan, Ngay_capnhat);

        mDatabase.child("NguoiDung").child(Ma_nguoidung).setValue(nguoiDung)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(DangKy.this, "Lưu thông tin thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DangKy.this, "Lưu thông tin thất bại", Toast.LENGTH_SHORT).show();
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

                    if(user.getPhoneNumber() == null){
                        saveUserInfo(user.getUid(), user.getDisplayName(),"ChuaCo", user.getEmail(), String.valueOf(user.getPhotoUrl()), 0, "ChuaChon", "HoatDong", System.currentTimeMillis(), System.currentTimeMillis());
                        Intent intent = new Intent(DangKy.this, MainActivity.class);
                        startActivity(intent);
                    }else {
                        saveUserInfo(user.getUid(), user.getDisplayName(), user.getPhoneNumber(), user.getEmail(), String.valueOf(user.getPhotoUrl()), 0, "ChuaChon", "HoatDong", System.currentTimeMillis(), System.currentTimeMillis());
                        Toast.makeText(DangKy.this, "Đăng nhập với Google thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(DangKy.this, MainActivity.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void onSignInFailed(Exception e) {
                    Toast.makeText(DangKy.this, "Đăng nhập với Google thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Kiểm tra xem có phải là kết quả chọn ảnh không
        if (resultCode == Activity.RESULT_OK && data != null) {
            avatarUri = data.getData(); // Lưu đường dẫn hình ảnh
            Glide.with(this)
                    .load(avatarUri)
                    .circleCrop()
                    .into(img_avatar);
            img_avatar.setImageURI(avatarUri); // Hiển thị hình ảnh lên ImageView
        }
    }


}
