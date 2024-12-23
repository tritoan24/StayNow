package com.ph32395.staynow_datn.Model;

public class PhanHoiModel {
    private String maPhanHoi;       // Mã phản hồi
    private String maNguoiDung;    // Mã người dùng gửi phản hồi
    private String noiDung;        // Nội dung phản hồi
    private String imgPhanHoi;             // Đường dẫn hoặc URL ảnh
    private String thoiGianGui;    // Thời gian gửi phản hồi (dạng chuỗi)

    // Constructor không tham số (cần thiết cho Firebase)
    public PhanHoiModel() {
    }

    // Constructor đầy đủ tham số

    public PhanHoiModel(String maPhanHoi, String maNguoiDung, String noiDung, String imgPhanHoi, String thoiGianGui) {
        this.maPhanHoi = maPhanHoi;
        this.maNguoiDung = maNguoiDung;
        this.noiDung = noiDung;
        this.imgPhanHoi = imgPhanHoi;
        this.thoiGianGui = thoiGianGui;
    }

    public String getMaPhanHoi() {
        return maPhanHoi;
    }

    public void setMaPhanHoi(String maPhanHoi) {
        this.maPhanHoi = maPhanHoi;
    }

    public String getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(String maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getImgPhanHoi() {
        return imgPhanHoi;
    }

    public void setImgPhanHoi(String imgPhanHoi) {
        this.imgPhanHoi = imgPhanHoi;
    }

    public String getThoiGianGui() {
        return thoiGianGui;
    }

    public void setThoiGianGui(String thoiGianGui) {
        this.thoiGianGui = thoiGianGui;
    }
}
