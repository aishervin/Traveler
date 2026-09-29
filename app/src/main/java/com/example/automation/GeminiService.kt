package com.example.automation

import android.content.Context
import com.example.api.GeminiKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String,
    val role: String, // 'user', 'assistant', 'activity'
    val text: String
)

object GeminiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun runTurn(
        context: Context,
        messages: List<ChatMessage>,
        prompt: String,
        onNavigate: ((String) -> Unit)? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = GeminiKeyStore.getGeminiApiKey(context)
        val automationEnabled = GeminiKeyStore.isAutomationEnabled(context)

        if (apiKey.isNullOrBlank()) {
            throw IllegalStateException("لطفاً کلید API جمینای (Gemini) را در تنظیمات وارد کنید.")
        }
        if (!automationEnabled) {
            throw IllegalStateException("برای اجرای فرمان‌ها و اتوماسیون، اجازه اتوماسیون را در تنظیمات فعال کنید.")
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

        val contentsArray = JSONArray()
        for (msg in messages) {
            if (msg.role == "assistant" || msg.role == "user") {
                val contentObj = JSONObject()
                contentObj.put("role", if (msg.role == "user") "user" else "model")
                val partsArray = JSONArray()
                val partObj = JSONObject()
                partObj.put("text", msg.text)
                partsArray.put(partObj)
                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
            }
        }
        val currentContent = JSONObject()
        currentContent.put("role", "user")
        val currentParts = JSONArray()
        val currentPart = JSONObject()
        currentPart.put("text", prompt)
        currentParts.put(currentPart)
        currentContent.put("parts", currentParts)
        contentsArray.put(currentContent)

        val requestBodyJson = JSONObject()
        requestBodyJson.put("contents", contentsArray)

        val toolsArray = JSONArray()
        val functionDeclarationsArray = JSONArray()
        for (toolDef in ToolRegistry.getToolDefinitions()) {
            val funcObj = JSONObject()
            funcObj.put("name", toolDef["name"])
            funcObj.put("description", toolDef["description"])
            val params = toolDef["parameters"] as? Map<*, *>
            if (params != null) {
                @Suppress("UNCHECKED_CAST")
                funcObj.put("parameters", JSONObject(params as Map<String, Any>))
            }
            functionDeclarationsArray.put(funcObj)
        }
        val toolObj = JSONObject()
        toolObj.put("functionDeclarations", functionDeclarationsArray)
        toolsArray.put(toolObj)
        requestBodyJson.put("tools", toolsArray)

        val body = requestBodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseString = response.body?.string() ?: throw Exception("پاسخی از سرور جمینای دریافت نشد.")

        if (!response.isSuccessful) {
            throw Exception("خطای جمینای (${response.code}): $responseString")
        }

        val jsonRoot = JSONObject(responseString)
        val candidates = jsonRoot.optJSONArray("candidates")
        if (candidates == null || candidates.length() == 0) {
            return@withContext "پاسخی از مدل دریافت نشد."
        }

        val candidate = candidates.getJSONObject(0)
        val content = candidate.optJSONObject("content")
        val parts = content?.optJSONArray("parts")

        if (parts != null && parts.length() > 0) {
            val firstPart = parts.getJSONObject(0)
            if (firstPart.has("functionCall")) {
                val fc = firstPart.getJSONObject("functionCall")
                val fnName = fc.getString("name")
                val fnArgsObj = fc.optJSONObject("args") ?: JSONObject()
                val argsMap = mutableMapOf<String, Any>()
                val keys = fnArgsObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    argsMap[k] = fnArgsObj.get(k)
                }

                val toolResult = ToolRegistry.executeTool(context, fnName, argsMap, onNavigate)
                return@withContext "دستور '$fnName' با موفقیت اجرا شد.\nنتیجه: ${com.google.gson.Gson().toJson(toolResult)}"
            } else if (firstPart.has("text")) {
                return@withContext firstPart.getString("text")
            }
        }

        return@withContext "پاسخ نامشخص از مدل."
    }
}
