package com.ph32395.staynow.DiaChiGHN


import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class GHNRequest {

    companion object {
        const val SHOPID = "191530"
        const val TokenGHN = "d01c7b44-ec5d-11ee-8bfa-8a2dda8ec551"
    }

    private val ghnRequestInterface: GHNServices

    init {
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val request: Request = chain.request().newBuilder()
                    .addHeader("ShopId", SHOPID)
                    .addHeader("Token", TokenGHN)
                    .build()
                return chain.proceed(request)
            }
        })

        ghnRequestInterface = Retrofit.Builder()
            .baseUrl(GHNServices.GHN_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build()
            .create(GHNServices::class.java)
    }

    fun callAPI(): GHNServices {
        return ghnRequestInterface
    }
}