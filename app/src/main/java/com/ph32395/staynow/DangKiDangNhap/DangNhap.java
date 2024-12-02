package com.ph32395.staynow.DangKiDangNhap;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ph32395.staynow.BaoMat.QuenMK;
import com.ph32395.staynow.MainActivity;
import com.ph32395.staynow.Model.NguoiDungModel;
import com.ph32395.staynow.R;
import com.ph32395.staynow.utils.Constants;

import java.util.Objects;

public class DangNhap extends AppCompatActivity {
    private Button btnDangNhap, btnDangNhapGoogle;
    private EditText edMail, edPass;
    private FirebaseAuth mAuth;
    private RegisterWithGoogle registerWithGoogle;
    private DatabaseReference mDatabase;
    private ImageView img_anhienpass;

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private static final int RC_SIGN_IN_REGISTER = 9001;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dang_nhap);
        ServerWakeUpService.INSTANCE.wakeUpServer();

        btnDangNhap = findViewById(R.id.loginButton);
        TextView txtdangky = findViewById(R.id.txtdangky);
        edMail = findViewById(R.id.username);
        edPass = findViewById(R.id.password);
        img_anhienpass = findViewById(R.id.img_anhienpass);
        btnDangNhapGoogle = findViewById(R.id.loginWithGGButton);
        TextView txtquenmk = findViewById(R.id.Txtquenmk);
        CheckBox cbremember = findViewById(R.id.Cbremember);

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // Khởi tạo RegisterWithGoogle để xử lý đăng nhập với Google
        registerWithGoogle = new RegisterWithGoogle(this);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        // Load thông tin đã lưu (nếu có)
        boolean isChecked = prefs.getBoolean("isChecked", false);
        if (isChecked) {
            edMail.setText(prefs.getString("email", ""));
            edPass.setText(prefs.getString("password", ""));
            cbremember.setChecked(true);
        }

        // Lưu thông tin khi checkbox thay đổi
        cbremember.setOnCheckedChangeListener((buttonView, isChecked1) -> {
            SharedPreferences.Editor editor = prefs.edit();
            if (isChecked1) {
                editor.putString("email", edMail.getText().toString());
                editor.putString("password", edPass.getText().toString());
                editor.putBoolean("isChecked", true);
            } else {
                editor.putString("email", "");
                editor.putString("password", "");
                editor.putBoolean("isChecked", false);
            }
            editor.apply();
        });

//        btnDangNhap sau khi merger
        btnDangNhap.setOnClickListener(v -> {
            String email = edMail.getText().toString().trim();
            String password = edPass.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edMail.setError("Email không hợp lệ");
            } else {
                //Dang nhap Firebase Auth truoc
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(DangNhap.this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                if (currentUser != null) {
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference userRef = database.getReference("NguoiDung");

//                                    Tim kiem nguoi dung qua UID trong Realtime Database
                                    userRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                String status = dataSnapshot.child("trang_thaitaikhoan").getValue(String.class);
                                                Boolean daXacThucValue = dataSnapshot.child("daXacThuc").getValue(Boolean.class);
                                                boolean daXacthuc = daXacThucValue != null && daXacThucValue;
                                                String loaiTaiKhoan = dataSnapshot.child("loai_taikhoan").getValue(String.class);

                                                if ("HoatDong".equals(status)) {
                                                    if (daXacthuc) {
                                                        if (!loaiTaiKhoan.equals("ChuaChon")) {
//                                                            Luu trang thai da dang nhap vao SharedPreferences
                                                            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                                            SharedPreferences.Editor editor = prefs.edit();
                                                            editor.putBoolean("is_logged_in", true);
                                                            editor.putString("check", loaiTaiKhoan);
                                                            editor.apply();

//                                                            Chuyen sang man Main
                                                            startActivity(new Intent(DangNhap.this, MainActivity.class));
                                                            finish();
                                                        } else {
//                                                            neu loai tai khoan chua chon thi den man loai tai khoan
                                                            startActivity(new Intent(DangNhap.this, ChonLoaiTK.class));
                                                        }
                                                    } else {
                                                        proceedToOtpActivity(currentUser);
                                                    }
                                                } else {
                                                    showFailureAnimation("Tài khoản của bạn đã bị khóa");
                                                }
                                            } else {
                                                showFailureAnimation("Không tìm thấy thông tin người dùng");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            showFailureAnimation("ỗi kết nối tới máy chủ");
                                        }
                                    });
                                } else {
                                    showFailureAnimation("Lỗi xác thực người dùng");
                                }
                            } else {
                                showFailureAnimation("Email hoặc mật khẩu không đúng");
                            }
                        });
            }
        });

        txtdangky.setOnClickListener(v -> {
            startActivity(new Intent(DangNhap.this, DangKy.class));
        });
        txtquenmk.setOnClickListener(v -> {
            startActivity(new Intent(DangNhap.this, QuenMK.class));
        });

        btnDangNhapGoogle.setOnClickListener(v -> {
            Intent signInIntent = registerWithGoogle.getGoogleSignInClient().getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN_REGISTER);
        });

        // Ẩn hiện password
        img_anhienpass.setOnClickListener(v -> {
            int cursorPosition = edPass.getSelectionStart();

            if (edPass.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
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
    private void saveUserInfo(String Ma_nguoidung, String Ho_ten, String Sdt, String Email, String Anh_daidien, Integer So_luotdatlich, String Loai_taikhoan, String Trang_thaitaikhoan, boolean isXacThuc, Long Ngay_taotaikhoan, Long Ngay_capnhat) {

        NguoiDungModel nguoiDung = new NguoiDungModel(Ma_nguoidung, Ho_ten, Sdt, Email, Anh_daidien, So_luotdatlich, Loai_taikhoan, Trang_thaitaikhoan, isXacThuc, Ngay_taotaikhoan, Ngay_capnhat);

        mDatabase.child("NguoiDung").child(Ma_nguoidung).setValue(nguoiDung)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Lưu thông tin thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Lưu thông tin thất bại", Toast.LENGTH_SHORT).show();
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
                                Intent intent = new Intent(DangNhap.this, OTPActivity.class);
                                intent.putExtra("uid", user.getUid());
                                intent.putExtra("email", user.getEmail());
                                startActivity(intent);
                            });
                        }

                        @Override
                        public void onFailure(@NonNull String errorMessage) {
                            runOnUiThread(() -> {
                                Toast.makeText(DangNhap.this, errorMessage, Toast.LENGTH_SHORT).show();
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

        if (requestCode == RC_SIGN_IN_REGISTER) {
            registerWithGoogle.handleSignInResult(requestCode, data, new RegisterWithGoogle.OnSignInResultListener() {
                @Override
                public void onSignInSuccess(FirebaseUser user) {
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
                                            String trangThaiTaiKhoan = snapshot.child("trang_thaitaikhoan").getValue(String.class);
                                            Boolean daXacThucValue = snapshot.child("daXacThuc").getValue(Boolean.class);
                                            // Đảm bảo `daXacThuc` không null, mặc định là false nếu không có giá trị
                                            boolean daXacThuc = daXacThucValue != null && daXacThucValue;
                                            String loaiTaiKhoan = snapshot.child("loai_taikhoan").getValue(String.class);
                                            // Kiểm tra nếu trạng thái tài khoản là "HoatDong"
                                            if ("HoatDong".equals(trangThaiTaiKhoan)) {

                                                if (daXacThuc) {
                                                    assert loaiTaiKhoan != null;
                                                    Intent intent;
                                                    if (!loaiTaiKhoan.equals("ChuaChon")) {
                                                        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = prefs.edit();
                                                        editor.putBoolean("is_logged_in", true);
                                                        editor.putString("check", loaiTaiKhoan);
                                                        editor.apply();
                                                        intent = new Intent(DangNhap.this, MainActivity.class);
                                                    } else {
                                                        intent = new Intent(DangNhap.this, ChonLoaiTK.class);
                                                    }
                                                    startActivity(intent);
                                                } else {
                                                    proceedToOtpActivity(user);
                                                }

                                            } else {
                                                Toast.makeText(DangNhap.this, "Tài khoản của bạn đã bị khóa", Toast.LENGTH_SHORT).show();
                                                // Đăng xuất tài khoản Google hiện tại
                                                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(DangNhap.this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());
                                                googleSignInClient.signOut()
                                                        .addOnCompleteListener(DangNhap.this, new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                // Sau khi đăng xuất thành công, yêu cầu người dùng đăng nhập lại
                                                                Toast.makeText(DangNhap.this, "Vui lòng chọn tài khoản khác", Toast.LENGTH_SHORT).show();
                                                                // Chuyển sang màn hình đăng nhập lại
                                                                Intent intent = new Intent(DangNhap.this, DangKy.class);
                                                                startActivity(intent);
                                                                finish();  // Đảm bảo người dùng không quay lại màn hình trước đó
                                                            }
                                                        });
                                            }

                                        }
                                    } else {
                                        // Tài khoản chưa tồn tại, tạo mới
                                        if (user.getPhoneNumber() == null) {
                                            saveUserInfo(user.getUid(), user.getDisplayName(), "ChuaCo", user.getEmail(), String.valueOf(user.getPhotoUrl()), 0, "ChuaChon", "HoatDong", false, System.currentTimeMillis(), System.currentTimeMillis());
                                            proceedToOtpActivity(user);
                                        } else {
                                            saveUserInfo(user.getUid(), user.getDisplayName(), user.getPhoneNumber(), user.getEmail(), String.valueOf(user.getPhotoUrl()), 0, "ChuaChon", "HoatDong", false, System.currentTimeMillis(), System.currentTimeMillis());
                                            Toast.makeText(DangNhap.this, "Đăng nhập với Google thành công", Toast.LENGTH_SHORT).show();
                                            proceedToOtpActivity(user);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    showFailureAnimation("Lỗi khi kiểm tra tài khoản");
                                }
                            });
                }

                @Override
                public void onSignInFailed(Exception e) {
                    showFailureAnimation("Đăng nhập thất bại");
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