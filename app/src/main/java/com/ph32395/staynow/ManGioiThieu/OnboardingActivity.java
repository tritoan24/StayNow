package com.ph32395.staynow.ManGioiThieu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.ph32395.staynow.DangKiDangNhap.DangKy;
import com.ph32395.staynow.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout indicator;
    private Button btnBatDau;

    private static final String PREFS_NAME = "AppSettings";
    private static final String LANGUAGE_KEY = "Language";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lấy ngôn ngữ đã lưu và áp dụng
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedLanguage = prefs.getString(LANGUAGE_KEY, "en");
        setLocale(savedLanguage);

        setContentView(R.layout.activity_onboarding_screen);

        viewPager = findViewById(R.id.viewPager);
        indicator = findViewById(R.id.tabLayout);
        btnBatDau = findViewById(R.id.btn_bat_dau);

        // Dịch nội dung Onboarding dựa trên ngôn ngữ đã chọn
        List<OnboardingScreen> onboardingScreens = new ArrayList<>();
        if ("vi".equals(savedLanguage)) {
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
        } else {
            onboardingScreens.add(new OnboardingScreen(
                    "Welcome to the App!",
                    "Easily search and explore rooms across the country tailored to your needs.",
                    "https://lottie.host/9b0216fa-2f5c-4295-a33b-9c9730bfe2f6/n6DQ4Js9no.json"
            ));
            onboardingScreens.add(new OnboardingScreen(
                    "Key Features",
                    "Discover useful features for you.",
                    "https://lottie.host/f55ffbc4-92ef-47a1-9903-49d1de33b16d/BjjdVyS33M.json"
            ));
            onboardingScreens.add(new OnboardingScreen(
                    "Easy Payment and Management",
                    "Manage invoices, contracts, and payments with ease.",
                    "https://lottie.host/dd2bc13b-ab31-410a-bcd9-7013f442406e/E6r8bUkABj.json"
            ));
        }

        OnboardingAdapter adapter = new OnboardingAdapter(onboardingScreens);
        viewPager.setAdapter(adapter);

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
            // Lưu trạng thái hoàn thành Onboarding
            startActivity(new Intent(OnboardingActivity.this, DangKy.class));
            finish();
        });
    }

    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();

        config.setLocale(locale);
        resources.updateConfiguration(config, displayMetrics);
    }
}
