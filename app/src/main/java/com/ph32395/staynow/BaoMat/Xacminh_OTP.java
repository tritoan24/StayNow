package com.ph32395.staynow.BaoMat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.ph32395.staynow.R;

public class Xacminh_OTP extends AppCompatActivity {

    private EditText edtOTP1;
    private EditText edtOTP2;
    private EditText edtOTP3;
    private EditText edtOTP4;
    private EditText edtOTP5;
    private Button btnXacNhan;
    private Button btnResendOTP;
    private TextView txtTimeDown;

    private FirebaseAuth auth;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xacminh_otp);

        // Ánh xạ các view
        edtOTP1 = findViewById(R.id.otp_digit_1);
        edtOTP2 = findViewById(R.id.otp_digit_2);
        edtOTP3 = findViewById(R.id.otp_digit_3);
        edtOTP4 = findViewById(R.id.otp_digit_4);
        edtOTP5 = findViewById(R.id.otp_digit_5);
        btnXacNhan = findViewById(R.id.btnXacNhan);
        btnResendOTP = findViewById(R.id.btnResendOTP);
        txtTimeDown = findViewById(R.id.timeDown);

        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Bắt đầu đếm ngược thời gian
        startCountdownTimer();

        // Xác nhận OTP
        btnXacNhan.setOnClickListener(v -> {
            String otp = edtOTP1.getText().toString().trim() +
                    edtOTP2.getText().toString().trim() +
                    edtOTP3.getText().toString().trim() +
                    edtOTP4.getText().toString().trim() +
                    edtOTP5.getText().toString().trim();

            // Kiểm tra OTP hợp lệ
            if (TextUtils.isEmpty(otp) || otp.length() < 5) {
                Toast.makeText(this, "OTP không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Xử lý xác nhận OTP (có thể thêm logic kiểm tra OTP từ server tại đây)
            Toast.makeText(this, "OTP hợp lệ: " + otp, Toast.LENGTH_SHORT).show();
        });

        // Gửi lại mã OTP
        btnResendOTP.setOnClickListener(v -> {
            auth.getCurrentUser().sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Đã gửi lại mã OTP!", Toast.LENGTH_SHORT).show();
                            startCountdownTimer();
                        } else {
                            Toast.makeText(this, "Gửi lại mã thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // Hàm đếm ngược thời gian
    private void startCountdownTimer() {
        if (timer != null) {
            timer.cancel();
        }

        timer = new CountDownTimer(300000, 1000) { // 5 phút
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                txtTimeDown.setText(String.format("Thời gian: %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                txtTimeDown.setText("Hết giờ!");
            }
        };

        timer.start();
    }
}
