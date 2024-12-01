package com.ph32395.staynow.Model;

public class PhanHoiModel {
    private String Ma_phanhoi;       // Mã phản hồi
    private String Ma_nguoidung;    // Mã người dùng gửi phản hồi
    private String Noi_dung;        // Nội dung phản hồi
    private String Img;             // Đường dẫn hoặc URL ảnh
    private String Thoi_giangui;    // Thời gian gửi phản hồi (dạng chuỗi)

    // Constructor không tham số (cần thiết cho Firebase)
    public PhanHoiModel() {
    }

    // Constructor đầy đủ tham số
    public PhanHoiModel(String ma_phanhoi, String ma_nguoidung, String noi_dung, String img, String thoi_giangui) {
        Ma_phanhoi = ma_phanhoi;
        Ma_nguoidung = ma_nguoidung;
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

    public String getMa_nguoidung() {
        return Ma_nguoidung;
    }

    public void setMa_nguoidung(String ma_nguoidung) {
        Ma_nguoidung = ma_nguoidung;
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
