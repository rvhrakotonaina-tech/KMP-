package com.example.moneytracker.data.ai

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import java.io.File
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

    

/**
 * LocalLlmEngine provides inference using MediaPipe LLM on-device.
 * The model file (.task) must be placed in `context.filesDir/models/`.
 * If the model file is missing, `generateContent` returns null and the UI
 * will display an appropriate message.
 */
class LocalLlmEngine(
    private val context: Context
) : AIService {
    private var llmInference: LlmInference? = null
    private val inferenceMutex = Mutex()
    private var initializedModelPath: String? = null

    override suspend fun getChatResponse(prompt: String): String? {
        val modelFile = ModelDownloadManager(context).modelFile
        return generateContent(prompt, modelFile)
    }

    private fun getInferenceInstance(modelFile: File): LlmInference? = synchronized(this) {
        if (!modelFile.exists()) {
            closeInternal()
            return null
        }
        val currentPath = modelFile.absolutePath
        if (initializedModelPath == currentPath && llmInference != null) {
            return llmInference
        }
        
        try {
            closeInternal()
            cleanStaleCache()

            // Ensure sufficient usable disk space for XNNPack weight cache allocation
            val freeBytes = context.cacheDir?.usableSpace ?: 0L
            if (freeBytes < 250 * 1024 * 1024L) {
                // Insufficient disk space for native weight cache allocation; return null safely
                return null
            }

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(currentPath)
                .setMaxTokens(220)
                .build()
            llmInference = LlmInference.createFromOptions(context, options)
            initializedModelPath = currentPath
        } catch (t: Throwable) {
            t.printStackTrace()
            llmInference = null
            initializedModelPath = null
        }
        return llmInference
    }

    private fun cleanStaleCache() {
        try {
            context.cacheDir?.listFiles()?.forEach { file ->
                if (file.isFile && (file.name.contains("xnn", ignoreCase = true) || file.name.contains("tmp", ignoreCase = true))) {
                    file.delete()
                }
            }
        } catch (_: Exception) {}
    }

    suspend fun generateContent(prompt: String, modelFile: File): String? = withContext(Dispatchers.Default) {
        // Ensure only one inference runs at a time (MediaPipe LLM inference is not thread‑safe)
        inferenceMutex.withLock {
            val inference = getInferenceInstance(modelFile) ?: return@withContext null
            try {
                val response = inference.generateResponse(prompt)
                if (!response.isNullOrBlank()) {
                    sanitizeResponse(response)
                } else {
                    null
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                null
            }
        }
    }

    companion object {
        private val mojibakePattern = Regex(
            """[\u00C2-\u00DF][\u0080-\u00BF]|[\u00E0-\u00EF][\u0080-\u00BF]{2}|[\u00F0-\u00F4][\u0080-\u00BF]{3}|Ã[\u0080-\u00BF]|Å[\u0080-\u009F\u00A0-\u00BF]|â[\u0080-\u009F\u00A0-\u00BF]{2}"""
        )

        fun restoreUtf8Encoding(text: String): String {
            // Fast check: return untouched if no characters form a UTF-8 mojibake byte sequence
            if (!mojibakePattern.containsMatchIn(text)) {
                return text
            }

            // 1. Try full string decode via ISO-8859-1 bytes -> UTF-8
            try {
                val bytes = text.toByteArray(java.nio.charset.StandardCharsets.ISO_8859_1)
                val decoder = java.nio.charset.StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                    .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
                val decoded = decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString()
                if (decoded.isNotEmpty()) return decoded
            } catch (_: Exception) {
                // 2. Fallback to Windows-1252 if full string fits in Windows-1252
                try {
                    val win1252 = java.nio.charset.Charset.forName("Windows-1252")
                    val bytes = text.toByteArray(win1252)
                    val decoder = java.nio.charset.StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                        .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
                    val decoded = decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString()
                    if (decoded.isNotEmpty()) return decoded
                } catch (_: Exception) {
                    // 3. For mixed content (e.g., emojis alongside mojibake), decode matching segments
                    return mojibakePattern.replace(text) { matchResult ->
                        decodeMojibakeChunk(matchResult.value)
                    }
                }
            }
            return text
        }

        private fun decodeMojibakeChunk(chunk: String): String {
            try {
                val bytes = chunk.toByteArray(java.nio.charset.StandardCharsets.ISO_8859_1)
                val decoder = java.nio.charset.StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                    .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
                return decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString()
            } catch (_: Exception) {}

            try {
                val win1252 = java.nio.charset.Charset.forName("Windows-1252")
                val bytes = chunk.toByteArray(win1252)
                val decoder = java.nio.charset.StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                    .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
                return decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString()
            } catch (_: Exception) {}

            return chunk
        }

        fun sanitizeResponse(text: String): String {
            // First, restore any UTF-8 characters that were decoded with improper single-byte charsets
            var cleaned = restoreUtf8Encoding(text)

            // 1. Remove Qwen ChatML headers and special tokens if emitted by the model
            cleaned = cleaned.replace(Regex("<\\|im_start\\|>\\s*(assistant|system|user)?\\s*\\n?"), "")
            cleaned = cleaned.replace(Regex("<\\|.*?\\|>"), "")

            // 2. Convert literal \r\n, \n, /r/n, and /n escape sequences into actual newlines safely without altering legitimate text
            cleaned = cleaned
                .replace("\\r\\n", "\n")
                .replace("\\n", "\n")
                .replace("/r/n", "\n")
                .replace(Regex("""(?<!https?:\S{0,200})(?<!\b[a-zA-Z0-9])/(?:n|r/n)(?=\s|[•\-\d\p{Lu}]|$)"""), "\n")
                .replace(Regex("""(?<=[.,:;!?•\-\s]|^)/(?:n|r/n)"""), "\n")

            // 3. Remove markdown formatting like bold (**), headings (##), etc.
            cleaned = cleaned.replace("**", "")
            cleaned = cleaned.replace(Regex("(?m)^#+\\s+"), "")
            cleaned = cleaned.replace(Regex("`{3,}.*?\\n"), "") // remove code blocks
            cleaned = cleaned.replace("`", "")

            // 4. Remove unnecessary leading/trailing whitespace
            return cleaned.trim()
        }
    }


    private fun closeInternal() {
        try {
            llmInference?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        llmInference = null
        initializedModelPath = null
    }

    fun close() = synchronized(this) {
        closeInternal()
    }
}
