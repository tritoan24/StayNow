package com.ph32395.staynow_datn.ChucNangNhanTinCC
data class Chat(
    val chatId: String? = null,
    val lastMessage: String? = null,
    val lastMessageTime: Long? = null,
    val unreadCount: Int = 0,
    val otherUserId: String? = null
)

data class Messenger(
    val senderId: String? = null,
    val message: String? = null,
    val timestamp: Long? = null
)

data class ChatDetails(
    val messages: Map<String, Messenger>? = null,
    val participants: Map<String, Boolean>? = null
)
