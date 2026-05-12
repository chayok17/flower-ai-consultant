package com.example.flowerai.network

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val text: String,
    @SerializedName("session_id")
    val sessionId: String = "android"
)

data class ChatResponse(
    val reply: String,
    val bouquets: List<BouquetResponse> = emptyList(),
    @SerializedName("cart_added")
    val cartAdded: BouquetResponse? = null
)

data class BouquetResponse(
    val id: Int,
    val name: String,
    val price: Int,
    val currency: String,
    @SerializedName("image_resource")
    val imageResource: String,
    val colors: List<String> = emptyList(),
    val note: String = ""
)

data class AuthRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    @SerializedName("user_id")
    val userId: Int,
    val username: String,
    @SerializedName("session_id")
    val sessionId: String
)

data class HistoryResponse(
    @SerializedName("session_id")
    val sessionId: String,
    val messages: List<HistoryMessage> = emptyList()
)

data class HistoryMessage(
    val role: String,
    val text: String
)

data class CartResponse(
    val count: Int,
    val items: List<CartItemResponse> = emptyList()
)

data class CartItemResponse(
    @SerializedName("bouquet_id")
    val bouquetId: Int,
    val quantity: Int,
    val name: String,
    val price: Int,
    val currency: String,
    @SerializedName("image_resource")
    val imageResource: String
)

data class ChatListResponse(
    val count: Int,
    val items: List<ChatSummaryResponse> = emptyList()
)

data class ChatSummaryResponse(
    @SerializedName("session_id")
    val sessionId: String,
    val title: String,
    @SerializedName("message_count")
    val messageCount: Int = 0
)

data class CreateChatRequest(
    val title: String? = null
)
