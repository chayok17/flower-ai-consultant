package com.example.flowerai.model

data class Message(
    val text: String? = null,
    val bouquet: Bouquet? = null,
    val isUser: Boolean
)

data class Bouquet(
    val name: String,
    val flowers: String,
    val price: String
)