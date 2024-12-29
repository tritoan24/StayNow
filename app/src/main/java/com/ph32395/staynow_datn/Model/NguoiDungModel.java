package com.ph32395.staynow_datn.Model;

public class NguoiDungModel {
    private String maNguoiDung;
    private String hoTen;
    private String sdt;
    private String email;
    private String anhDaiDien;
    private String gioiTinh;
    private Integer soLuotDatLich;
    private String loaiTaiKhoan;
    private String trangThaiTaiKhoan;
    private boolean daXacThuc;
    private Long ngayTaoTaiKhoan;
    private Long ngayCapNhat;

    public NguoiDungModel(String maNguoiDung, String hoTen, String sdt, String email, String anhDaiDien, Integer soLuotDatLich, String trangThaiTaiKhoan,String loaiTaiKhoan, boolean daXacThuc, Long ngayTaoTaiKhoan, Long ngayCapNhat) {
        this.maNguoiDung = maNguoiDung;
        this.hoTen = hoTen;
        this.sdt = sdt;
        this.email = email;
        this.anhDaiDien = anhDaiDien;
        this.soLuotDatLich = soLuotDatLich;
        this.trangThaiTaiKhoan = trangThaiTaiKhoan;
        this.loaiTaiKhoan = loaiTaiKhoan;
        this.daXacThuc = daXacThuc;
        this.ngayTaoTaiKhoan = ngayTaoTaiKhoan;
        this.ngayCapNhat = ngayCapNhat;
    }

    public String getLoaiTaiKhoan() {
        return loaiTaiKhoan;
    }

    public NguoiDungModel() {
    }

    public String getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(String maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAnhDaiDien() {
        return anhDaiDien;
    }

    public void setAnhDaiDien(String anhDaiDien) {
        this.anhDaiDien = anhDaiDien;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public Integer getSoLuotDatLich() {
        return soLuotDatLich;
    }

    public void setSoLuotDatLich(Integer soLuotDatLich) {
        this.soLuotDatLich = soLuotDatLich;
    }

    public void setLoaiTaiKhoan(String loaiTaiKhoan) {
        this.loaiTaiKhoan = loaiTaiKhoan;
    }

    public String getTrangThaiTaiKhoan() {
        return trangThaiTaiKhoan;
    }

    public void setTrangThaiTaiKhoan(String trangThaiTaiKhoan) {
        this.trangThaiTaiKhoan = trangThaiTaiKhoan;
    }

    public boolean isDaXacThuc() {
        return daXacThuc;
    }

    public void setDaXacThuc(boolean daXacThuc) {
        this.daXacThuc = daXacThuc;
    }

    public Long getNgayTaoTaiKhoan() {
        return ngayTaoTaiKhoan;
    }

    public void setNgayTaoTaiKhoan(Long ngayTaoTaiKhoan) {
        this.ngayTaoTaiKhoan = ngayTaoTaiKhoan;
    }

    public Long getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(Long ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }
}
