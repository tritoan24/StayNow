package com.ph32395.staynow.BaoMat;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.ph32395.staynow.R;

public class QuenMK extends AppCompatActivity {

    private EditText edtEmail;
    private Button giveOTP;
    private TextView texxtGmail;
    private FirebaseAuth auth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quen_mk); // Đảm bảo gọi setContentView trước khi ánh xạ các view

        // Ánh xạ các view
        edtEmail = findViewById(R.id.etEmail); // Ánh xạ EditText cho email
        giveOTP = findViewById(R.id.giveOTP); // Ánh xạ Button gửi OTP
        texxtGmail = findViewById(R.id.tvGmaillHint); // Ánh xạ TextView hiển thị email
        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Lấy email của người dùng từ Firebase Auth và hiển thị vào TextView
        String currentUserEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "Email chưa có";

        // Kiểm tra và ẩn phần giữa email
        if (currentUserEmail != null && currentUserEmail.contains("@")) {
            String[] emailParts = currentUserEmail.split("@");
            String localPart = emailParts[0]; // Phần trước @
            String domainPart = emailParts[1]; // Phần sau @

            if (localPart.length() > 3) {
                // Lấy 3 ký tự đầu và ẩn phần còn lại
                localPart = localPart.substring(0, 5) + "***";
            }
            currentUserEmail = localPart + "@" + domainPart;
        }

        texxtGmail.setText(currentUserEmail); // Gắn email vào TextView

        // Sự kiện gửi OTP
        giveOTP.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim(); // Lấy email từ EditText

            // Kiểm tra email hợp lệ
            if (TextUtils.isEmpty(email)) {
                edtEmail.setError("Vui lòng nhập email!"); // Hiển thị lỗi nếu không có email
                return;
            }

            // Gửi email đặt lại mật khẩu
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Mã OTP đã được gửi đến email!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, Xacminh_OTP.class)); // Chuyển sang màn hình xác minh OTP
                        } else {
                            Toast.makeText(this, "Gửi OTP thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show(); // Thông báo lỗi nếu không thành công
                        }
                    });
        });
    }
}
