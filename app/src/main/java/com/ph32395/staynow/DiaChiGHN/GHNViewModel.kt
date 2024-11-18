package com.ph32395.staynow.DiaChiGHN

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ph32395.staynow.DiaChiGHN.Model.District
import com.ph32395.staynow.DiaChiGHN.Model.Province
import com.ph32395.staynow.DiaChiGHN.Model.Ward

class GHNViewModel : ViewModel() {
    private val _provinceList = MutableLiveData<List<Province>>()
    val provinceList: LiveData<List<Province>> get() = _provinceList

    private val repository = GHNRepository()

    fun getProvinces(): MutableLiveData<List<Province>?> {
        return repository.fetchProvinces()
    }

    fun getDistricts(provinceId: Int): MutableLiveData<List<District>?> {
        return repository.fetchDistricts(provinceId)
    }

    fun getWards(districtId: Int): MutableLiveData<List<Ward>?> {
        return repository.fetchWards(districtId)
    }
}
