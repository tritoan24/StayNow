package com.ph32395.staynow_datn.Interface

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIService {
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}

data class ChatCompletionRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>,
    val temperature: Double = 0.7
)

data class Message(
    val role: String,
    val content: String
)

data class ChatCompletionResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message,
    val finishReason: String
)