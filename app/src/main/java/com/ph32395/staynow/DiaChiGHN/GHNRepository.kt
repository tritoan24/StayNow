package com.ph32395.staynow.DiaChiGHN

import androidx.lifecycle.MutableLiveData
import com.ph32395.staynow.DiaChiGHN.Model.District
import com.ph32395.staynow.DiaChiGHN.Model.Province
import com.ph32395.staynow.DiaChiGHN.Model.Ward

class GHNRepository {

    private val dataFetcher = GHNDataFetcher()

    fun fetchProvinces(): MutableLiveData<List<Province>?> {
        val provinceLiveData = MutableLiveData<List<Province>?>()
        dataFetcher.fetchProvinces { provinces ->
            provinceLiveData.postValue(provinces)
        }
        return provinceLiveData
    }

    fun fetchDistricts(provinceId: Int): MutableLiveData<List<District>?> {
        val districtLiveData = MutableLiveData<List<District>?>()
        dataFetcher.fetchDistricts(provinceId) { districts ->
            districtLiveData.postValue(districts)
        }
        return districtLiveData
    }

    fun fetchWards(districtId: Int): MutableLiveData<List<Ward>?> {
        val wardLiveData = MutableLiveData<List<Ward>?>()
        dataFetcher.fetchWards(districtId) { wards ->
            wardLiveData.postValue(wards)
        }
        return wardLiveData
    }
}
