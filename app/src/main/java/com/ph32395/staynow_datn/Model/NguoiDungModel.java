package com.ph32395.staynow_datn.Model;

public class NguoiDungModel {
    private String Ma_nguoidung;
    private String Ho_ten;
    private String Sdt;
    private String Email;
    private String Anh_daidien;
    private String GioiTinh;
    private Integer So_luotdatlich;
    private String Loai_taikhoan;
    private String Trang_thaitaikhoan;
    private boolean DaXacThuc;
    private Long Ngay_taotaikhoan;
    private Long Ngay_capnhat;

    public NguoiDungModel() {
    }

    public NguoiDungModel(String ma_nguoidung, String ho_ten, String sdt, String email, String anh_daidien, Integer so_luotdatlich, String loai_taikhoan, String trang_thaitaikhoan, boolean daXacThuc, Long ngay_taotaikhoan, Long ngay_capnhat) {
        Ma_nguoidung = ma_nguoidung;
        Ho_ten = ho_ten;
        Sdt = sdt;
        Email = email;
        Anh_daidien = anh_daidien;
        So_luotdatlich = so_luotdatlich;
        Loai_taikhoan = loai_taikhoan;
        Trang_thaitaikhoan = trang_thaitaikhoan;
        DaXacThuc = daXacThuc;
        Ngay_taotaikhoan = ngay_taotaikhoan;
        Ngay_capnhat = ngay_capnhat;
    }

    public String getGioiTinh() {
        return GioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        GioiTinh = gioiTinh;
    }

    public String getMa_nguoidung() {
        return Ma_nguoidung;
    }

    public void setMa_nguoidung(String ma_nguoidung) {
        Ma_nguoidung = ma_nguoidung;
    }

    public String getHo_ten() {
        return Ho_ten;
    }

    public void setHo_ten(String ho_ten) {
        Ho_ten = ho_ten;
    }

    public String getSdt() {
        return Sdt;
    }

    public void setSdt(String sdt) {
        Sdt = sdt;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getAnh_daidien() {
        return Anh_daidien;
    }

    public void setAnh_daidien(String anh_daidien) {
        Anh_daidien = anh_daidien;
    }

    public Integer getSo_luotdatlich() {
        return So_luotdatlich;
    }

    public void setSo_luotdatlich(Integer so_luotdatlich) {
        So_luotdatlich = so_luotdatlich;
    }

    public String getLoai_taikhoan() {
        return Loai_taikhoan;
    }

    public void setLoai_taikhoan(String loai_taikhoan) {
        Loai_taikhoan = loai_taikhoan;
    }

    public String getTrang_thaitaikhoan() {
        return Trang_thaitaikhoan;
    }

    public void setTrang_thaitaikhoan(String trang_thaitaikhoan) {
        Trang_thaitaikhoan = trang_thaitaikhoan;
    }

    public boolean isDaXacThuc() {
        return DaXacThuc;
    }

    public void setDaXacThuc(boolean daXacThuc) {
        DaXacThuc = daXacThuc;
    }

    public Long getNgay_taotaikhoan() {
        return Ngay_taotaikhoan;
    }

    public void setNgay_taotaikhoan(Long ngay_taotaikhoan) {
        Ngay_taotaikhoan = ngay_taotaikhoan;
    }

    public Long getNgay_capnhat() {
        return Ngay_capnhat;
    }

    public void setNgay_capnhat(Long ngay_capnhat) {
        Ngay_capnhat = ngay_capnhat;
    }
}
