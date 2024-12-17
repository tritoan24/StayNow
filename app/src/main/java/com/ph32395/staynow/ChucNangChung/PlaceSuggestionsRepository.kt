package com.ph32395.staynow.ChucNangChung

import android.content.Context
import android.util.Log
import android.view.WindowInsetsAnimation
import com.ph32395.staynow.Maps.RetrofitInstance
import com.ph32395.staynow.Maps.SuggestionResponse
import com.ph32395.staynow.R
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback


// PlaceSuggestionsRepository.kt
class PlaceSuggestionsRepository(
    private val context: Context,
    private val onSuggestionsReady: (List<String>) -> Unit,
    private val onError: ((String) -> Unit)? = null
) {
    private val apiKeys = mutableListOf<String>().apply {
        // Thêm tất cả API keys
        add(context.getString(R.string.api_key_1))
        add(context.getString(R.string.api_key_2))
        add(context.getString(R.string.api_key_3))
        add(context.getString(R.string.api_key_4))
        add(context.getString(R.string.api_key_5))
        add(context.getString(R.string.api_key_6))
        add(context.getString(R.string.api_key_7))
        add(context.getString(R.string.api_key_8))
        add(context.getString(R.string.api_key_9))
        add(context.getString(R.string.api_key_10))
        add(context.getString(R.string.api_key_11))
        add(context.getString(R.string.api_key_12))

        // ... thêm các API keys còn lại
    }

    private var currentKeyIndex = 0
    private var usageMap = mutableMapOf<String, Int>()

    init {
        apiKeys.forEach { usageMap[it] = 0 }
    }

    fun fetchSuggestions(query: String) {
        // Bỏ qua các query quá ngắn
        if (query.length < 2) return

        val currentApiKey = getCurrentApiKey()

        RetrofitInstance.api.getPlace(currentApiKey, query)
            .enqueue(object : Callback<SuggestionResponse> {
                override fun onResponse(
                    call: Call<SuggestionResponse>,
                    response: Response<SuggestionResponse>
                ) {
                    // Kiểm tra giới hạn request
                    val remainingRequests =
                        response.headers()["X-RateLimit-Remaining"]?.toIntOrNull()

                    if (remainingRequests != null) {
                        usageMap[currentApiKey] = 998 - remainingRequests
                        Log.d("RateLimit", "Key $currentApiKey còn $remainingRequests requests")
                    }

                    // Nếu vượt quá giới hạn, chuyển sang key mới
                    if (remainingRequests != null && remainingRequests <= 0) {
                        rotateApiKey()
                        fetchSuggestions(query)
                        return
                    }

                    if (response.isSuccessful && response.body()?.status == "OK") {
                        val suggestions = response.body()?.predictions ?: emptyList()
                        val list = suggestions.map { it.description }

                        // Gọi callback trả về danh sách gợi ý
                        onSuggestionsReady(list)
                    } else {
                        onError?.invoke("Không thể lấy gợi ý")
                        Log.e("Retrofit", "Response error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<SuggestionResponse>, t: Throwable) {
                    onError?.invoke(t.message ?: "Lỗi kết nối")
                    Log.e("Retrofit", "API call failed: ${t.message}")
                }
            })
    }

    private fun getCurrentApiKey(): String {
        return apiKeys[currentKeyIndex]
    }

    private fun rotateApiKey() {
        currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size
        Log.d("API_KEY_SWITCH", "Chuyển sang API Key: ${apiKeys[currentKeyIndex]}")
    }
}
