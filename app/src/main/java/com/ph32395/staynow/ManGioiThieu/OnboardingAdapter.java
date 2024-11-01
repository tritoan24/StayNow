package com.ph32395.staynow.ManGioiThieu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ph32395.staynow.R;
import com.airbnb.lottie.LottieAnimationView;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private final List<OnboardingScreen> onboardingScreens;

    public OnboardingAdapter(List<OnboardingScreen> onboardingScreens) {
        this.onboardingScreens = onboardingScreens;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.onboarding_screen, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        OnboardingScreen screen = onboardingScreens.get(position);
        holder.bind(screen);
    }

    @Override
    public int getItemCount() {
        return onboardingScreens.size();
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView description;
        private final LottieAnimationView lottieAnimationView;

        // ViewHolder khởi tạo các thành phần giao diện
        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.onboarding_tieu_de);
            description = itemView.findViewById(R.id.onboarding_mo_ta);
            lottieAnimationView = itemView.findViewById(R.id.onboarding_animation);
        }

        // Gán dữ liệu từ OnboardingScreen vào các thành phần giao diện
        public void bind(OnboardingScreen screen) {
            title.setText(screen.getTitle());
            description.setText(screen.getDescription());

            // Load animation từ URL Lottie và bắt đầu phát
            lottieAnimationView.setAnimationFromUrl(screen.getAnimationUrl());
            lottieAnimationView.playAnimation();
        }
    }
}
