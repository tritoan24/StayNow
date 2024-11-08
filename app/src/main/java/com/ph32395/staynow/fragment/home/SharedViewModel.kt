package com.ph32395.staynow.fragment.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class SharedViewModel : ViewModel() {
    private val _selectedLoaiPhongTro = MutableLiveData<String>()
    val selectedLoaiPhongTro: LiveData<String> get() = _selectedLoaiPhongTro

    fun selectLoaiPhongTro(loaiPhongTro: String) {
        _selectedLoaiPhongTro.value = loaiPhongTro
    }
}
