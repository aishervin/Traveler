package com.example.api

import android.content.Context
import android.content.SharedPreferences

object GeminiKeyStore {
    private const val PREF_NAME = "baarbarg_secure_prefs"
    private const val KEY_GEMINI = "gemini.apiKey"
    private const val KEY_ACCESS_TOKEN = "auth.accessToken"
    private const val KEY_POLICY = "automation.policy.enabled"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getGeminiApiKey(context: Context): String? {
        return prefs(context).getString(KEY_GEMINI, null)
    }

    fun hasGeminiApiKey(context: Context): Boolean {
        return !getGeminiApiKey(context).isNullOrBlank()
    }

    fun saveGeminiApiKey(context: Context, key: String) {
        val trimmed = key.trim()
        if (trimmed.length < 20) throw IllegalArgumentException("کلید Gemini معتبر به نظر نمی‌رسد.")
        prefs(context).edit().putString(KEY_GEMINI, trimmed).apply()
    }

    fun removeGeminiApiKey(context: Context) {
        prefs(context).edit().remove(KEY_GEMINI).apply()
    }

    fun getAccessToken(context: Context): String? {
        return prefs(context).getString(KEY_ACCESS_TOKEN, null)
    }

    fun saveAccessToken(context: Context, token: String) {
        prefs(context).edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun clearSession(context: Context) {
        prefs(context).edit().remove(KEY_ACCESS_TOKEN).apply()
    }

    fun isAutomationEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_POLICY, false)
    }

    fun setAutomationEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_POLICY, enabled).apply()
    }
}
