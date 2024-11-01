package com.ph32395.staynow.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.ph32395.staynow.DangNhap;
import com.ph32395.staynow.ManGioiThieu.OnboardingActivity;
import com.ph32395.staynow.R;

public class SplashActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String FIRST_TIME_KEY = "first_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean(FIRST_TIME_KEY, true);

        // Hiển thị splash screen trong 3 giây, sau đó kiểm tra trạng thái mở ứng dụng lần đầu
        new Handler().postDelayed(() -> {
            checkFirstTime(isFirstTime);

            if (isFirstTime) {
                // Cập nhật lại giá trị để lần sau không hiển thị màn hình giới thiệu nữa
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(FIRST_TIME_KEY, false);
                editor.apply();
            }

            // Đóng SplashActivity để không quay lại được màn hình này
            finish();
        }, 3000);
    }

    private void checkFirstTime(boolean isFirstTime) {
        if (isFirstTime) {
            // Lần đầu mở ứng dụng, chuyển đến màn hình giới thiệu
            startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
        } else {
            // Không phải lần đầu, chuyển trực tiếp đến màn hình đăng nhập
            startActivity(new Intent(SplashActivity.this, DangNhap.class));
        }
    }
}
