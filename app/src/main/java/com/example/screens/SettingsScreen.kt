package com.example.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.api.GeminiKeyStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    context: android.content.Context,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var apiBaseUrl by remember { mutableStateOf(GeminiKeyStore.getApiBaseUrl(context)) }
    var servicePassword by remember { mutableStateOf(GeminiKeyStore.getServicePassword(context) ?: "") }
    var securityKey by remember { mutableStateOf(GeminiKeyStore.getSecurityKey(context) ?: "") }
    var apiKey by remember { mutableStateOf(GeminiKeyStore.getGeminiApiKey(context) ?: "") }
    var automationEnabled by remember { mutableStateOf(GeminiKeyStore.isAutomationEnabled(context)) }
    var message by remember { mutableStateOf("") }

    fun save() {
        try {
            GeminiKeyStore.saveApiBaseUrl(context, apiBaseUrl)
            GeminiKeyStore.saveServiceCredentials(context, servicePassword, securityKey)
            if (apiKey.isNotBlank()) {
                GeminiKeyStore.saveGeminiApiKey(context, apiKey)
            } else {
                GeminiKeyStore.removeGeminiApiKey(context)
            }
            GeminiKeyStore.setAutomationEnabled(context, automationEnabled)
            message = "تنظیمات با موفقیت ذخیره شد."
        } catch (e: Exception) {
            message = e.message ?: "خطا در ذخیره تنظیمات."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات و حساب") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("‹ بازگشت") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("اتصال به سامانه بارنامه", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apiBaseUrl,
                        onValueChange = { apiBaseUrl = it },
                        label = { Text("نشانی HTTPS سرویس") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = servicePassword,
                        onValueChange = { servicePassword = it },
                        label = { Text("ServicePassword (اختیاری)") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = securityKey,
                        onValueChange = { securityKey = it },
                        label = { Text("SecurityKey (اختیاری)") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        "کلیدهای سرویس فقط روی این دستگاه و به‌صورت رمزگذاری‌شده نگه‌داری می‌شوند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("کلید API گوگل جمینای (Gemini)", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("کلید API") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("اجازه کلی اتوماسیون و دستیار", style = MaterialTheme.typography.titleSmall)
                        Text("اجازه دسترسی به اسناد و موقعیت مکانی برای انجام خودکار وظایف.", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = automationEnabled,
                        onCheckedChange = { automationEnabled = it }
                    )
                }
            }

            if (message.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text(text = message, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = ::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("ذخیره تنظیمات")
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("خروج از حساب کاربری")
            }
        }
    }
}
