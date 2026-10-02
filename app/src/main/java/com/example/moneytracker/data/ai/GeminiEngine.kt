package com.example.moneytracker.data.ai

import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiEngine(
    private val defaultApiKey: String
) {
    private fun getModel(customKey: String?): GenerativeModel {
        val key = if (!customKey.isNullOrBlank()) customKey else defaultApiKey
        return GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = key
        )
    }

    suspend fun generateContent(prompt: String, customKey: String? = null): String? = withContext(Dispatchers.IO) {
        val keyToUse = if (!customKey.isNullOrBlank()) customKey else defaultApiKey
        if (keyToUse.isBlank() || keyToUse == "YOUR_API_KEY_HERE") {
            return@withContext "API_KEY_MISSING"
        }
        try {
            val response = getModel(customKey).generateContent(prompt)
            response.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun startChat(history: List<ChatMessage>, customKey: String? = null): GeminiChatSession {
        val chat = getModel(customKey).startChat(
            history = history.map { 
                content(role = it.role) { text(it.text) }
            }
        )
        return GeminiChatSession(chat)
    }
}

data class ChatMessage(
    val text: String,
    val role: String // "user" or "model"
)

class GeminiChatSession(
    private val chat: Chat
) {
    suspend fun sendMessage(message: String): String? = withContext(Dispatchers.IO) {
        try {
            val response = chat.sendMessage(message)
            response.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
