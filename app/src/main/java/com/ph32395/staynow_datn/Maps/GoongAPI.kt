package com.ph32395.staynow_datn.Maps

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GoongAPI {

    @GET("Place/AutoComplete")
    fun getPlace(
        @Query("api_key") apiKey: String,
        @Query("input") query: String
    ): Call<SuggestionResponse>

    @GET("Geocode")
    fun getGeocode(
        @Query("api_key") apiKey: String,
        @Query("address") address: String
    ):Call<GeocodeResponse>
    @GET("Geocode")
    fun getGeocodeLang(
        @Query("api_key") apiKey: String,
        @Query("latlng") latlng: String
    ):Call<GeocodeResponse>




}