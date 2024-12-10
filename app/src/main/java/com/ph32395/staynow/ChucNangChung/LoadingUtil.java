package com.ph32395.staynow.ChucNangChung;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import com.airbnb.lottie.LottieAnimationView;
import com.ph32395.staynow.R;
public class LoadingUtil {

    private Activity activity;
    private LottieAnimationView loadingIndicator;
    public View blockingView;

    public LoadingUtil(Activity activity) {
        this.activity = activity;
        this.loadingIndicator = activity.findViewById(R.id.loadingIndicator);
    }

    // Hiển thị loading và lớp phủ
    public void show() {
        if (loadingIndicator != null) {
            // Tạo lớp phủ che toàn màn hình
            blockingView = new View(activity);
            blockingView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            blockingView.setClickable(true);

            // Thêm lớp phủ vào root layout
            ViewGroup rootView = activity.findViewById(android.R.id.content);
            rootView.addView(blockingView);

            // Hiển thị loading indicator
            loadingIndicator.setVisibility(View.VISIBLE);
            loadingIndicator.playAnimation();

            // Ngăn người dùng tương tác với màn hình
            activity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            );
        }
    }

    // Ẩn loading và loại bỏ lớp phủ
    public void hide() {
        if (loadingIndicator != null) {
            // Remove blocking view
            if (blockingView != null) {
                ViewGroup rootView = activity.findViewById(android.R.id.content);
                if (rootView != null) {
                    rootView.removeView(blockingView);
                    blockingView = null;
                }
            }

            // Hide loading indicator
            loadingIndicator.setVisibility(View.GONE);
            loadingIndicator.cancelAnimation();

            // Clear touch blocking flags
            Window window = activity.getWindow();
            if (window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }
    }
}
