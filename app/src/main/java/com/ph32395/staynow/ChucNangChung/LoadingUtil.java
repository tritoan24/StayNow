package com.ph32395.staynow.ChucNangChung;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.airbnb.lottie.LottieAnimationView;
import com.ph32395.staynow.R;

public class LoadingUtil {
    private LottieAnimationView loadingIndicator;
    private Activity activity;
    private View blockingView;

    public LoadingUtil(Activity activity) {
        this.activity = activity;
        loadingIndicator = activity.findViewById(R.id.loadingIndicator);
    }

    public void show() {
        if (loadingIndicator != null) {
            // Create a blocking view that covers the entire screen
            blockingView = new View(activity);
            blockingView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            blockingView.setClickable(true);

            // Add blocking view to the root layout
            ViewGroup rootView = activity.findViewById(android.R.id.content);
            rootView.addView(blockingView);

            // Show loading indicator
            loadingIndicator.setVisibility(View.VISIBLE);
            loadingIndicator.playAnimation();

            // Prevent screen interactions
            activity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            );
        }
    }

    public void hide() {
        if (loadingIndicator != null) {
            // Remove blocking view
            if (blockingView != null) {
                ViewGroup rootView = activity.findViewById(android.R.id.content);
                rootView.removeView(blockingView);
                blockingView = null;
            }

            // Hide loading indicator
            loadingIndicator.setVisibility(View.GONE);
            loadingIndicator.cancelAnimation();

            // Re-enable screen interactions
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
}