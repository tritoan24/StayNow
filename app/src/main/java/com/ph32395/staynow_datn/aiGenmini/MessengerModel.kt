package com.ph32395.staynow_datn.aiGenmini


data class MessageModel(
    val message : String,
    val role : String,
)


data class RoomSearchResult(
    val roomIds: List<String>,
    val totalResults: Int,
    val alternativeRoomIds: List<String> = emptyList()
)