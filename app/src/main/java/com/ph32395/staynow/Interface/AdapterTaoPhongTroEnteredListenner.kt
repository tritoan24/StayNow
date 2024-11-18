package com.ph32395.staynow.Interface

import com.ph32395.staynow.DichVu.DichVu
import com.ph32395.staynow.GioiTinh.GioiTinh
import com.ph32395.staynow.LoaiPhong.LoaiPhong
import com.ph32395.staynow.NoiThat.NoiThat
import com.ph32395.staynow.ThongTin.ThongTin
import com.ph32395.staynow.TienNghi.TienNghi

interface AdapterTaoPhongTroEnteredListenner {
    fun onNoiThatSelected(noiThat: NoiThat, isSelected: Boolean)
    fun onTienNghiSelected(tienNghi: TienNghi, isSelected: Boolean)
    fun onThongTinimfor(prices: List<Pair<ThongTin, Int>>)
    fun onLoaiPhongSelected(loaiPhong: LoaiPhong, isSelected: Boolean)
    fun onGioiTinhSelected(gioiTinh: GioiTinh, isSelected: Boolean)
    fun onAllPricesEntered(prices: List<Pair<DichVu, Int>>)
}