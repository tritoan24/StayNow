package com.ph32395.staynow.ManGioiThieu;


public class OnboardingScreen {
    private String tieuDe;
    private String moTa;
    private int hinh;

    public OnboardingScreen(String tieuDe, String moTa, int hinh) {
        this.tieuDe = tieuDe;
        this.moTa = moTa;
        this.hinh = hinh;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public String getMoTa() {
        return moTa;
    }

    public int getHinh() {
        return hinh;
    }
}
