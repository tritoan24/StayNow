package com.ph32395.staynow.ManGioiThieu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.ph32395.staynow.DangNhap;
import com.ph32395.staynow.R;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout indicator;
    private Button btnBatDau;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_screen);

        viewPager = findViewById(R.id.viewPager);
        indicator = findViewById(R.id.tabLayout);
        btnBatDau = findViewById(R.id.btn_bat_dau);

        List<OnboardingScreen> onboardingScreens = new ArrayList<>();
        onboardingScreens.add(new OnboardingScreen("Chao Mung Den Vo Ung Dung!", "Ung dung giup ban de dang tim kiem thong tin.", R.drawable.profile));
        onboardingScreens.add(new OnboardingScreen("Tinh Nang Noi Bat", "Kham pha cac tinh nang huu ich cho ban.", R.drawable.profile));
        onboardingScreens.add(new OnboardingScreen("Bat Dau Ngay!", "Chi can nhan vao nut ben duoi de bat dau.", R.drawable.profile));

        OnboardingAdapter adapter = new OnboardingAdapter(onboardingScreens);
        viewPager.setAdapter(adapter);

        // Áp dụng custom page transformer
        viewPager.setPageTransformer(new CustomPageTransformer());

        new TabLayoutMediator(indicator, viewPager, (tab, position) -> {}).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                btnBatDau.setVisibility(position == onboardingScreens.size() - 1 ? View.VISIBLE : View.GONE);
            }
        });

        btnBatDau.setOnClickListener(v -> {
            // Chuyển hướng đến hoạt động chính của ứng dụng
            startActivity(new Intent(OnboardingActivity.this, DangNhap.class));
            finish();
        });
    }
}
