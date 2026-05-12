package com.example.flowerai.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST

interface ApiService {

    @POST("chat")
    suspend fun sendMessage(
        @Body request: ChatRequest
    ): ChatResponse

    @POST("register")
    suspend fun register(
        @Body request: AuthRequest
    ): AuthResponse

    @POST("login")
    suspend fun login(
        @Body request: AuthRequest
    ): AuthResponse

    @GET("history/{sessionId}")
    suspend fun getHistory(
        @Path("sessionId") sessionId: String
    ): HistoryResponse

    @GET("cart/{sessionId}")
    suspend fun getCart(
        @Path("sessionId") sessionId: String
    ): CartResponse

    @GET("users/{userId}/chats")
    suspend fun getChats(
        @Path("userId") userId: Int
    ): ChatListResponse

    @POST("users/{userId}/chats")
    suspend fun createChat(
        @Path("userId") userId: Int,
        @Body request: CreateChatRequest = CreateChatRequest()
    ): ChatSummaryResponse

    @DELETE("users/{userId}/chats/{sessionId}")
    suspend fun deleteChat(
        @Path("userId") userId: Int,
        @Path("sessionId") sessionId: String
    ): Map<String, String>

}
