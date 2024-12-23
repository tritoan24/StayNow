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

    public NguoiDungModel(String maNguoiDung, String hoTen, String sdt, String email, String anh_daidien, Integer so_luotdatlich, String loai_taikhoan, String trang_thaitaikhoan, boolean daXacThuc, Long ngay_taotaikhoan, Long ngay_capnhat) {
        this.maNguoiDung = maNguoiDung;
        this.hoTen = hoTen;
        this.sdt = sdt;
        this.email = email;
        this.anhDaiDien = anh_daidien;
        this.soLuotDatLich = so_luotdatlich;
        this.loaiTaiKhoan = loai_taikhoan;
        this.trangThaiTaiKhoan = trang_thaitaikhoan;
        this.daXacThuc= daXacThuc;
        this.ngayTaoTaiKhoan = ngay_taotaikhoan;
        this.ngayCapNhat = ngay_capnhat;
    }

    public String getLoaiTaiKhoan() {
        return loaiTaiKhoan;
    }

}
