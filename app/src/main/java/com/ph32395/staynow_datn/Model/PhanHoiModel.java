package com.ph32395.staynow_datn.Model;

public class PhanHoiModel {
    private String Ma_phanhoi;       // Mã phản hồi
    private String maNguoiDung;    // Mã người dùng gửi phản hồi
    private String Noi_dung;        // Nội dung phản hồi
    private String Img;             // Đường dẫn hoặc URL ảnh
    private String Thoi_giangui;    // Thời gian gửi phản hồi (dạng chuỗi)

    // Constructor không tham số (cần thiết cho Firebase)
    public PhanHoiModel() {
    }

    // Constructor đầy đủ tham số
    public PhanHoiModel(String ma_phanhoi, String maNguoiDung, String noi_dung, String img, String thoi_giangui) {
        Ma_phanhoi = ma_phanhoi;
        maNguoiDung = maNguoiDung;
        Noi_dung = noi_dung;
        Img = img;
        Thoi_giangui = thoi_giangui;
    }

    // Getter và Setter cho từng thuộc tính
    public String getMa_phanhoi() {
        return Ma_phanhoi;
    }

    public void setMa_phanhoi(String ma_phanhoi) {
        Ma_phanhoi = ma_phanhoi;
    }

    public String getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(String maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getNoi_dung() {
        return Noi_dung;
    }

    public void setNoi_dung(String noi_dung) {
        Noi_dung = noi_dung;
    }

    public String getImg() {
        return Img;
    }

    public void setImg(String img) {
        Img = img;
    }

    public String getThoi_giangui() {
        return Thoi_giangui;
    }

    public void setThoi_giangui(String thoi_giangui) {
        Thoi_giangui = thoi_giangui;
    }
}
