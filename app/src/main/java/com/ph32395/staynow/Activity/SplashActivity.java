package com.ph32395.staynow.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.ph32395.staynow.DangKiDangNhap.DangNhap;
import com.ph32395.staynow.MainActivity;
import com.ph32395.staynow.ManGioiThieu.OnboardingActivity;
import com.ph32395.staynow.R;

public class SplashActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String FIRST_TIME_KEY = "first_time";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        // Lấy tham chiếu đến LottieAnimationView
        LottieAnimationView loadingSplash = findViewById(R.id.loadingSplash);

        // Chạy animation
        loadingSplash.playAnimation();
//



        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean(FIRST_TIME_KEY, true);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        new Handler().postDelayed(() -> {
            if (isFirstTime) {
                // Nếu là lần đầu, chuyển đến màn hình giới thiệu
                startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
            } else if (!isLoggedIn) {
                // Nếu chưa đăng nhập, chuyển đến màn hình đăng ký
                startActivity(new Intent(SplashActivity.this, DangNhap.class));
            } else {
                // Nếu đã đăng nhập, chuyển đến màn hình chính
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }

            finish(); // Đóng SplashActivity để không quay lại được màn hình này
        }, 3000);
    }
}