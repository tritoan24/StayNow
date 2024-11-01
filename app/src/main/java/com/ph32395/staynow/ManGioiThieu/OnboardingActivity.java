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
        onboardingScreens.add(new OnboardingScreen(
                "Chào Mừng Đến Với Ứng Dụng!",
                "Dễ dàng tìm kiếm và khám phá các phòng trọ phù hợp với nhu cầu của bạn trên khắp cả nước.",
                "https://lottie.host/9b0216fa-2f5c-4295-a33b-9c9730bfe2f6/n6DQ4Js9no.json"
        ));
        onboardingScreens.add(new OnboardingScreen(
                "Tính Năng Nổi Bật",
                "Khám phá các tính năng hữu ích cho bạn.",
                "https://lottie.host/f55ffbc4-92ef-47a1-9903-49d1de33b16d/BjjdVyS33M.json"
        ));
        onboardingScreens.add(new OnboardingScreen(
                "Thanh toán và quản lý dễ dàng",
                "Quản lý hóa đơn, hợp đồng, và các khoản thanh toán tiện lợi chỉ với vài thao tác.",
                "https://lottie.host/dd2bc13b-ab31-410a-bcd9-7013f442406e/E6r8bUkABj.json"
        ));


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
