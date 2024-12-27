package com.ph32395.staynow_datn.DangKiDangNhap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ph32395.staynow_datn.BaoMat.CapNhatThongTin;
import com.ph32395.staynow_datn.ChucNangChung.LoadingUtil;
import com.ph32395.staynow_datn.MainActivity;
import com.ph32395.staynow_datn.Model.NguoiDungModel;
import com.ph32395.staynow_datn.R;
import com.ph32395.staynow_datn.utils.Constants;

import java.util.Objects;

public class DangKy extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText tenEditText, sdtEditText, emailEditText, passwordEditText, rppass;
    private RegisterWithGoogle registerWithGoogle;
    private ImageView img_avatar;

    private static final int RC_SIGN_IN_REGISTER = 9001; // Request code for Google Sign-In
    private Uri avatarUri;

    private LoadingUtil loadingUtil;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_ky);




        loadingUtil = new LoadingUtil(this);

        // Khởi tạo Firebase Auth và Realtime Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Liên kết các thành phần giao diện
        tenEditText = findViewById(R.id.ten);
        sdtEditText = findViewById(R.id.sdt);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        rppass = findViewById(R.id.rp_password);
        Button registerButton = findViewById(R.id.btn_register);
        Button registerButtonWithGoogle = findViewById(R.id.btn_register_google);
        TextView txtdangnhap = findViewById(R.id.txtdangnhap);
        img_avatar = findViewById(R.id.img_avatar);


        //Thông tin người dùng
        String Ngay_taotaikhoan = String.valueOf(System.currentTimeMillis());
        String Ngay_capnhat = String.valueOf(System.currentTimeMillis());
        Integer So_luotdatlich = 0;
        String loaiTaiKhoan = "ChuaChon";
        String Trang_thaitaikhoan = "HoatDong";
        boolean daXacThuc = false;


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



        // sự kiện khi ấn vào nút đăng nhập
        txtdangnhap.setOnClickListener(view -> {
            Intent intent = new Intent(DangKy.this, DangNhap.class);
            startActivity(intent);
        });

        // Đăng ký sự kiện cho nút "Đăng ký"
        registerButton.setOnClickListener(view -> {
            loadingUtil.show();
            String ten = tenEditText.getText().toString().trim();
            String sdt = sdtEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String rppassword = rppass.getText().toString().trim();

            boolean isValid = true;

            if (ten.isEmpty()) {
                tenEditText.setError("Vui lòng nhập Họ Tên");
                isValid = false;
                loadingUtil.hide();
            }

            if (sdt.isEmpty()) {
                sdtEditText.setError("Vui lòng nhập số điện thoại");
                isValid = false;
                loadingUtil.hide();
            }

            if (email.isEmpty()) {
                emailEditText.setError("Vui lòng nhập email");
                isValid = false;
                loadingUtil.hide();
            }

            if (password.isEmpty()) {
                passwordEditText.setError("Vui lòng nhập mật khẩu");
                isValid = false;
                loadingUtil.hide();
            }
            if (rppassword.isEmpty()) {
                rppass.setError("Vui lòng lại nhập mật khẩu");
                isValid = false;
                loadingUtil.hide();
            }

            if (isValid) {
                if (sdt.length() < 10) {
                    sdtEditText.setError("Số điện thoại phải có ít nhất 10 số");
                    loadingUtil.hide();

                } else if (!sdt.startsWith("0") || sdt.length() > 11) {
                    sdtEditText.setError("Số điện thoại không hợp lệ");
                    loadingUtil.hide();
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEditText.setError("Email không hợp lệ");
                    loadingUtil.hide();
                } else if (password.length() < 6) {
                    passwordEditText.setError("Mật khẩu phải có ít nhất 6 ký tự");
                    loadingUtil.hide();
                } else if (!password.matches("^(?=.*[A-Z])(?=.*[0-9]).{6,}$")) {
                    passwordEditText.setError("Mật khẩu phải có ít nhất 1 chữ hoa và 1 chữ số");
                    loadingUtil.hide();
                } else if (!password.equals(rppassword)) {
                    rppass.setError("Mật khẩu không trùng khớp");
                } else if (avatarUri == null) {
                    Toast.makeText(DangKy.this, "Vui lòng chọn ảnh đại diện", Toast.LENGTH_SHORT).show();
                    loadingUtil.hide();
                } else {
                    signUpWithEmailPassword(ten, sdt, email, password, avatarUri.toString(), So_luotdatlich, loaiTaiKhoan, Trang_thaitaikhoan, loaiTaiKhoan , daXacThuc, Long.parseLong(Ngay_taotaikhoan), Long.parseLong(Ngay_capnhat));
                    loadingUtil.hide();
                }
            }
        });

        // Đăng ký sự kiện cho nút "Đăng ký bằng Google"
        registerButtonWithGoogle.setOnClickListener(view -> {
            //đánh thức server mỗi lần run
            Intent signInIntent = registerWithGoogle.getGoogleSignInClient().getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN_REGISTER);
        });
    }

    private void signUpWithEmailPassword(String Ho_ten, String Sdt, String Email, String password, String Anh_daidien, Integer So_luotdatlich, String Loai_taikhoan, String Trang_thaitaikhoan, String loaiTaiKhoan, boolean daXacThuc, Long Ngay_taotaikhoan, Long Ngay_capnhat) {
        mAuth.createUserWithEmailAndPassword(Email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        com.ph32395.staynow_datn.Utils.ImageUploader imageUploader = new com.ph32395.staynow_datn.Utils.ImageUploader();
                        assert user != null;
                        imageUploader.uploadImage(avatarUri, user.getUid(), new com.ph32395.staynow_datn.Utils.ImageUploader.UploadCallback() {
                            @Override
                            public void onSuccess(String imageUrl) {
                                // Lưu thông tin người dùng với URL ảnh
                                saveUserInfo(user.getUid(), Ho_ten, Sdt, Email, imageUrl, 0, Trang_thaitaikhoan, loaiTaiKhoan, daXacThuc, Ngay_taotaikhoan, Ngay_capnhat);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.d("OTP", "Lỗi tải ảnh: " + e.getMessage());
                            }
                        });
                        //lấy token request lên server
                        proceedToOtpActivity(user);

                    } else {
                        loadingUtil.hide();
                        // Xử lý khi đăng ký thất bại
                        String errorMessage;
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            loadingUtil.hide();
                            errorMessage = "Email này đã được sử dụng. Vui lòng thử email khác!";
                        } else {
                            loadingUtil.hide();
                            // Các lỗi khác
                            errorMessage = "Đăng ký thất bại: " + Objects.requireNonNull(task.getException()).getMessage();
                        }
                        Toast.makeText(DangKy.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Hàm lưu thông tin người dùng vào Realtime Database
    private void saveUserInfo(String maNguoiDung, String Ho_ten, String Sdt, String Email, String anhDaiDien, Integer soLuotDatLich, String trangThaiTaiKhoan, String loaiTaiKhoan, boolean daXacThuc, Long Ngay_taotaikhoan, Long Ngay_capnhat) {

        NguoiDungModel nguoiDung = new NguoiDungModel(maNguoiDung, Ho_ten, Sdt, Email, anhDaiDien ,soLuotDatLich, trangThaiTaiKhoan, loaiTaiKhoan, daXacThuc, Ngay_taotaikhoan, Ngay_capnhat);

        mDatabase.child("NguoiDung").child(maNguoiDung).setValue(nguoiDung)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("OTP", "Lưu thông tin thành công");
                    } else {
                        Log.d("OTP", "Lưu thông tin thất bại:");
                    }
                });
    }


    // Hàm xử lý chuyển sang OTP Activity
    private void proceedToOtpActivity(FirebaseUser user) {
        Toast.makeText(this, "Đang xác minh tài khoản, đợi chút nha!", Toast.LENGTH_SHORT).show();
        user.getIdToken(true).addOnCompleteListener(tokenTask -> {
            if (tokenTask.isSuccessful()) {
                String token = tokenTask.getResult().getToken();
                if (token != null) {
                    String url = Constants.URL_SERVER_QUYET + "/" + Constants.ENDPOINT_VERIFY_TOKEN;
                    TokenService.INSTANCE.sendTokenToServer(token, url, new TokenService.TokenCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {

                                Intent intent = new Intent(DangKy.this, OTPActivity.class);
                                intent.putExtra("uid", user.getUid());
                                intent.putExtra("email", user.getEmail());
                                intent.putExtra("pass", passwordEditText.getText().toString().trim());
                                startActivity(intent);

                            });
                        }

                        @Override
                        public void onFailure(@NonNull String errorMessage) {
                            runOnUiThread(() -> {
                                Toast.makeText(DangKy.this, errorMessage, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }

            } else {
                Log.d("OTP", "Lỗi lấy token: " + Objects.requireNonNull(tokenTask.getException()).getMessage());
            }
        });
    }
    //    xu ly dang nhap bang Google
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadingUtil = new LoadingUtil(this);

        if (requestCode == RC_SIGN_IN_REGISTER) {
            loadingUtil.show();
            registerWithGoogle.handleSignInResult(requestCode, data, new RegisterWithGoogle.OnSignInResultListener() {
                @Override
                public void onSignInSuccess(FirebaseUser user) {
                    loadingUtil.show();

                    String email = user.getEmail();

                    // Kiểm tra email đã tồn tại trong Firebase Realtime Database
                    FirebaseDatabase.getInstance().getReference("NguoiDung")
                            .orderByChild("email")
                            .equalTo(email)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        // Tài khoản đã tồn tại, kiểm tra trạng thái tài khoản
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            String trangThaiTaiKhoan = snapshot.child(" ").getValue(String.class);
                                            Boolean daXacThucValue = snapshot.child("daXacThuc").getValue(Boolean.class);
                                            // Đảm bảo `daXacThuc` không null, mặc định là false nếu không có giá trị
                                            boolean daXacThuc = daXacThucValue != null && daXacThucValue;
                                            String loaiTaiKhoan = snapshot.child("loaiTaiKhoan").getValue(String.class);
                                            // Kiểm tra nếu trạng thái tài khoản là "HoatDong"
                                            if ("HoatDong".equals(trangThaiTaiKhoan)) {
                                                if (daXacThuc) {
                                                    assert loaiTaiKhoan != null;
                                                    Intent intent;
                                                    if (!loaiTaiKhoan.equals("ChuaChon")) {
                                                        loadingUtil.hide();
                                                        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = prefs.edit();
                                                        editor.putBoolean("is_logged_in", true);
                                                        editor.putString("check", loaiTaiKhoan);
                                                        editor.apply();
                                                        intent = new Intent(DangKy.this, MainActivity.class);
                                                    } else {
                                                        loadingUtil.hide();
                                                        intent = new Intent(DangKy.this, ChonLoaiTK.class);
                                                    }
                                                    startActivity(intent);
                                                } else {
                                                    proceedToOtpActivity(user);
                                                }

                                            } else {
                                                loadingUtil.hide();
                                                Toast.makeText(DangKy.this, "Tài khoản của bạn đã bị khóa", Toast.LENGTH_SHORT).show();
                                                // Đăng xuất tài khoản Google hiện tại
                                                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(DangKy.this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());
                                                googleSignInClient.signOut()
                                                        .addOnCompleteListener(DangKy.this, new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                // Sau khi đăng xuất thành công, yêu cầu người dùng đăng nhập lại
                                                                Toast.makeText(DangKy.this, "Vui lòng chọn tài khoản khác", Toast.LENGTH_SHORT).show();
                                                                // Chuyển sang màn hình đăng nhập lại
                                                                Intent intent = new Intent(DangKy.this, DangKy.class);
                                                                startActivity(intent);
                                                                finish();  // Đảm bảo người dùng không quay lại màn hình trước đó
                                                            }
                                                        });
                                            }

                                        }
                                    } else {
                                        // Tài khoản chưa tồn tại, tạo mới
                                        if (user.getPhoneNumber() == null) {
                                            saveUserInfo(user.getUid(), user.getDisplayName(), "ChuaCo", user.getEmail(), String.valueOf(user.getPhotoUrl()), 0, "HoatDong", "ChuaChon", false, System.currentTimeMillis(), System.currentTimeMillis());
                                            proceedToOtpActivity(user);
                                        } else {
                                            saveUserInfo(user.getUid(), user.getDisplayName(), user.getPhoneNumber(), user.getEmail(), String.valueOf(user.getPhotoUrl()), 0, "HoatDong", "ChuaChon",false, System.currentTimeMillis(), System.currentTimeMillis());
                                            Toast.makeText(DangKy.this, "Đăng nhập với Google thành công", Toast.LENGTH_SHORT).show();
                                            proceedToOtpActivity(user);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    showFailureAnimation("Lỗi khi kiểm tra tài khoản");
                                    // Loại bỏ lớp phủ nếu nó tồn tại
                                    if (loadingUtil.blockingView != null) {
                                        ViewGroup rootView = findViewById(android.R.id.content);
                                        rootView.removeView(loadingUtil.blockingView);
                                        loadingUtil.blockingView = null;  // Đảm bảo không tham chiếu lại lớp phủ
                                    }
                                }
                            });
                }

                @Override
                public void onSignInFailed(Exception e) {
                    showFailureAnimation("Hủy chọn tài khoản");
                    loadingUtil.hide();
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
            img_avatar.setImageURI(avatarUri);
        }
    }

    // Phương thức để hiển thị animation thất bại
    private void showFailureAnimation(String errorMessage) {
        // Tạo một dialog mới
        Dialog dialog = new Dialog(this, R.style.TransparentDialog);
        dialog.setContentView(R.layout.hieuungthongbaoloi);
        dialog.setCancelable(true);

        TextView txtThongBao = dialog.findViewById(R.id.text_message);
        txtThongBao.setText(errorMessage);


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