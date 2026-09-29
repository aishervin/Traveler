package com.example.api

import android.content.Context
import com.google.gson.JsonElement

object AuthService {
    suspend fun login(
        context: Context,
        nationalCode: String,
        password: String,
        capToken: String
    ): Map<String, Any> {
        val normalizedCode = nationalCode.trim()
        require(normalizedCode.isNotBlank() && password.isNotBlank()) {
            "کد ملی و رمز عبور را وارد کنید."
        }

        val response = BaarbargApi.post(
            context = context,
            path = "/Account/UserLoginV2",
            body = LoginRequest(normalizedCode, password, capToken),
            authenticated = false
        )
        val result = response.get("obj")
        val token = result?.let { value ->
            when {
                value.isJsonPrimitive && value.asJsonPrimitive.isString -> value.asString
                value.isJsonObject -> firstString(value, "token", "accessToken", "access_token")
                else -> null
            }
        } ?: firstString(response, "token", "accessToken", "access_token")

        require(!token.isNullOrBlank()) { "پاسخ ورود از سرویس فاقد توکن معتبر بود." }
        GeminiKeyStore.saveAccessToken(context, token)
        GeminiKeyStore.saveNationalCode(context, normalizedCode)
        return mapOf("mode" to "live", "nationalCode" to normalizedCode)
    }

    private fun firstString(element: JsonElement, vararg keys: String): String? {
        if (!element.isJsonObject) return null
        return keys.firstNotNullOfOrNull { key ->
            element.asJsonObject.get(key)
                ?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }
                ?.asString
                ?.takeIf(String::isNotBlank)
        }
    }
}
