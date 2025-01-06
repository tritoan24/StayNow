package com.ph32395.staynow_datn.Interface

import com.ph32395.staynow_datn.GioiTinh.GioiTinh
import com.ph32395.staynow_datn.LoaiPhong.LoaiPhong
import com.ph32395.staynow_datn.NoiThat.NoiThat
import com.ph32395.staynow_datn.QuanLyNhaTro.NhaTroModel
import com.ph32395.staynow_datn.TaoPhongTro.PhiDichVu
import com.ph32395.staynow_datn.ThongTin.ThongTin
import com.ph32395.staynow_datn.TienNghi.TienNghi

interface AdapterTaoPhongTroEnteredListenner {
    fun onNoiThatSelected(noiThat: NoiThat, isSelected: Boolean)
    fun onTienNghiSelected(tienNghi: TienNghi, isSelected: Boolean)
    fun onThongTinimfor(prices: List<Pair<ThongTin, Int>>)
    fun onLoaiPhongSelected(loaiPhong: LoaiPhong, isSelected: Boolean)
    fun onNhaTroSelected(nhaTro: NhaTroModel, isSelected: Boolean)
    fun onGioiTinhSelected(gioiTinh: GioiTinh, isSelected: Boolean)
    fun onAllPricesEntered(prices: List<PhiDichVu>)
}