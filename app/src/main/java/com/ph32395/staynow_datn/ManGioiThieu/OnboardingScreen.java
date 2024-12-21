package com.ph32395.staynow_datn.ManGioiThieu;


public class OnboardingScreen {
    private String title;
    private String description;
    private String animationUrl;

    public OnboardingScreen(String title, String description, String animationUrl) {
        this.title = title;
        this.description = description;
        this.animationUrl = animationUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAnimationUrl() {
        return animationUrl;
    }
}