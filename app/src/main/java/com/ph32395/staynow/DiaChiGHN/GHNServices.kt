package com.ph32395.staynow.DiaChiGHN


import com.ph32395.staynow.DiaChiGHN.Model.District
import com.ph32395.staynow.DiaChiGHN.Model.DistrictRequest
import com.ph32395.staynow.DiaChiGHN.Model.Province
import com.ph32395.staynow.DiaChiGHN.Model.ResponseGHN
import com.ph32395.staynow.DiaChiGHN.Model.Ward
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GHNServices {

    companion object {
        const val GHN_URL = "https://dev-online-gateway.ghn.vn/"
    }

    @GET("/shiip/public-api/master-data/province")
    fun getListProvince(): Call<ResponseGHN<ArrayList<Province>>>

    @POST("/shiip/public-api/master-data/district")
    fun getListDistrict(@Body districtRequest: DistrictRequest): Call<ResponseGHN<ArrayList<District>>>

    @GET("/shiip/public-api/master-data/ward")
    fun getListWard(@Query("district_id") district_id: Int): Call<ResponseGHN<ArrayList<Ward>>>
}