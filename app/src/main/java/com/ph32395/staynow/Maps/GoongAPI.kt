package com.ph32395.staynow.Maps

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




}