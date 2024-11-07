package com.ph32395.staynow.fragment.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _selectedTab = MutableLiveData<String>()
    val selectedTab: LiveData<String> get() = _selectedTab

    fun selectTab(loaiPhongTro: String) {
        _selectedTab.value = loaiPhongTro
    }
}
