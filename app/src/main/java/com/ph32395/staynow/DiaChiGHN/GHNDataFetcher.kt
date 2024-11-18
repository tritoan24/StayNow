package com.ph32395.staynow.DiaChiGHN

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import com.ph32395.staynow.DiaChiGHN.Model.District
import com.ph32395.staynow.DiaChiGHN.Model.DistrictRequest
import com.ph32395.staynow.DiaChiGHN.Model.Province
import com.ph32395.staynow.DiaChiGHN.Model.ResponseGHN
import com.ph32395.staynow.DiaChiGHN.Model.Ward

class GHNDataFetcher {

    private val ghnService = GHNRequest().callAPI()

    fun fetchProvinces(onResult: (List<Province>?) -> Unit) {
        ghnService.getListProvince().enqueue(object : Callback<ResponseGHN<ArrayList<Province>>> {
            override fun onResponse(
                call: Call<ResponseGHN<ArrayList<Province>>>,
                response: Response<ResponseGHN<ArrayList<Province>>>
            ) {
                if (response.isSuccessful) {
                    onResult(response.body()?.data)
                } else {
                    Log.e("GHNDataFetcher", "Error fetching provinces: ${response.message()}")
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<ResponseGHN<ArrayList<Province>>>, t: Throwable) {
                Log.e("GHNDataFetcher", "Error fetching provinces", t)
                onResult(null)
            }
        })
    }

    // Các hàm fetchDistricts và fetchWards tương tự, dùng để lấy dữ liệu District và Ward từ API
    // Để đơn giản, chúng ta chỉ cần lấy dữ liệu Province từ API
    // Trong GHNDataFetcher
    fun fetchDistricts(provinceId: Int, callback: (List<District>?) -> Unit) {
        ghnService.getListDistrict(DistrictRequest(provinceId)).enqueue(object : Callback<ResponseGHN<ArrayList<District>>> {
            override fun onResponse(
                call: Call<ResponseGHN<ArrayList<District>>>,
                response: Response<ResponseGHN<ArrayList<District>>>
            ) {
                if (response.isSuccessful) {
                    callback(response.body()?.data)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<ResponseGHN<ArrayList<District>>>, t: Throwable) {
                callback(null)
            }
        })
    }
    // Trong GHNDataFetcher
    fun fetchWards(districtId: Int, callback: (List<Ward>?) -> Unit) {
        ghnService.getListWard(districtId).enqueue(object : Callback<ResponseGHN<ArrayList<Ward>>> {
            override fun onResponse(
                call: Call<ResponseGHN<ArrayList<Ward>>>,
                response: Response<ResponseGHN<ArrayList<Ward>>>
            ) {
                if (response.isSuccessful) {
                    callback(response.body()?.data)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<ResponseGHN<ArrayList<Ward>>>, t: Throwable) {
                callback(null)
            }
        })
    }



}
