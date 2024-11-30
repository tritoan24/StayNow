package com.ph32395.staynow.payment

import com.ph32395.staynow.utils.Constants
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiClient {
    @POST("/api/create-order")
    fun createOrder(@Body request: OrderRequest): Call<OrderResponse>

    companion object {
        fun create(): ApiClient {
            return Retrofit.Builder()
                .baseUrl(Constants.URL_PAYMENT)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiClient::class.java)
        }
    }

}