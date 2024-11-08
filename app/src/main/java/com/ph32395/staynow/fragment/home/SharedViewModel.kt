package com.ph32395.staynow.fragment.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ph32395.staynow.Model.LoaiPhongTro


class SharedViewModel : ViewModel() {
    private val _selectedLoaiPhongTro = MutableLiveData<String>()
    val selectedLoaiPhongTro: LiveData<String> get() = _selectedLoaiPhongTro

    fun selectLoaiPhongTro(loaiPhongTro: String) {
        _selectedLoaiPhongTro.value = loaiPhongTro
    }
}
