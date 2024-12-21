package com.ph32395.staynow_datn.ManGioiThieu;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class CustomPageTransformer implements ViewPager2.PageTransformer {
    @Override
    public void transformPage(@NonNull View page, float position) {
        // Chuyển động mờ dần và thu nhỏ
        if (position < -1) { // Trang nằm ngoài bên trái
            page.setAlpha(0);
            page.setScaleY(0.8f); // Giảm kích thước để rõ hơn
        } else if (position <= 0) { // Trang đang ở giữa
            page.setAlpha(1 + position); // Hiệu ứng mờ dần
            page.setScaleY(1 + position * 0.2f); // Hiệu ứng thu nhỏ tăng cường
        } else if (position <= 1) { // Trang nằm bên phải
            page.setAlpha(1 - position); // Hiệu ứng mờ dần
            page.setScaleY(1 - position * 0.2f); // Hiệu ứng thu nhỏ tăng cường
        } else { // Trang nằm ngoài bên phải
            page.setAlpha(0);
            page.setScaleY(0.8f); // Giảm kích thước để rõ hơn
        }
    }
}