package com.example.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.api.GeminiKeyStore

@Composable
fun LoginScreen(context: android.content.Context, onSignedIn: (Map<String, Any>) -> Unit, onOpen: (String) -> Unit) {
    var nationalCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    fun submit() {
        error = ""
        loading = true
        try {
            if (nationalCode.isBlank() || password.isBlank()) {
                throw IllegalArgumentException("کد ملی و رمز عبور را وارد کنید.")
            }
            GeminiKeyStore.saveAccessToken(context, "token_${System.currentTimeMillis()}")
            onSignedIn(mapOf("mode" to "live", "nationalCode" to nationalCode))
        } catch (e: Exception) {
            error = e.message ?: "ورود انجام نشد."
        } finally {
            loading = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "صدور بارنامه شهری",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ورود به سامانه باربر",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = nationalCode,
                        onValueChange = { nationalCode = it },
                        label = { Text("کد ملی / شناسه ملی") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("رمز عبور") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (error.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = ::submit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !loading
                    ) {
                        Text(if (loading) "در حال ورود..." else "ورود")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { onSignedIn(mapOf("mode" to "demo")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("ورود به نسخه آزمایشی (Demo)")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onOpen("signup") }, modifier = Modifier.weight(1f)) {
                            Text("ثبت نام")
                        }
                        OutlinedButton(onClick = { onOpen("forgotPassword") }, modifier = Modifier.weight(1f)) {
                            Text("فراموشی رمز")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "نسخه مستقل آزمایشی · سامانه مدیریت حمل",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
        }
    }
}
