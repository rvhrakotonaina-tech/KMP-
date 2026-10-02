package com.example.moneytracker.data.ai

interface AIService {
    suspend fun getChatResponse(prompt: String): String?
}
